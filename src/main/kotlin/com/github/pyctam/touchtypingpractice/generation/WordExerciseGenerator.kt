package com.github.pyctam.touchtypingpractice.generation

import com.github.pyctam.touchtypingpractice.config.GenerationMode
import kotlin.random.Random

/**
 * A ranked candidate for a word-based exercise.
 *
 * @property adapted the string to display and type (the source letters filtered to the enabled
 *   keys, in their original order).
 * @property source the original dictionary word this candidate was derived from, retained for
 *   debugging, analytics, and optional learner feedback.
 * @property sourceIndex the position of [source] in the word list (a frequency proxy; lower is more
 *   common).
 * @property retainedCount the number of characters kept from [source].
 * @property originalLength the length of [source].
 * @property exact true when no character was removed ([adapted] equals [source]).
 */
data class WordCandidate(
  val adapted: String,
  val source: String,
  val sourceIndex: Int,
  val retainedCount: Int,
  val originalLength: Int,
  val exact: Boolean,
) {
  /** The fraction of [source] retained, in `[0, 1]`. */
  val retention: Double
    get() = retainedCount.toDouble() / originalLength
}

/**
 * Pure, IDE-independent generator for word-based practice exercises.
 *
 * Adapts the bundled [WordList] to the keys enabled by a [PracticeTextConfig] and produces a
 * practice text of real (or letter-filtered) English words. All randomness is injected via
 * [random], so the output is deterministic under a seeded [Random].
 *
 * Two word modes are supported (see [GenerationMode]):
 * - [GenerationMode.EXACT_WORDS]: only dictionary words whose every character is enabled.
 * - [GenerationMode.ADAPTED_WORDS]: dictionary words with unavailable letters omitted, ranked so
 *   that high-retention, common, recognizable fragments are preferred.
 *
 * Candidate pools are precomputed per (mode, enabled-key mask) and cached, so the dictionary is
 * scanned only once per distinct key configuration rather than on every generation request.
 */
class WordExerciseGenerator(
  private val wordList: WordList,
  private val random: Random = Random.Default,
) {

  /**
   * Generates a practice text of words for [config].
   *
   * @return a space-separated practice text using only characters enabled under [config], or a
   *   single fallback character if no letters are enabled or no candidates exist.
   */
  fun generate(config: PracticeTextConfig): String {
    val enabled = enabledLetters(config)
    if (enabled.isEmpty()) return FALLBACK_CHARACTER
    val pool = rankedPool(config, enabled)
    if (pool.isEmpty()) return FALLBACK_CHARACTER
    val wordCount =
      random.nextInt(
        PracticeTextGenerator.MIN_WORDS_COUNT,
        PracticeTextGenerator.MAX_WORDS_COUNT + 1
      )
    val words = ArrayList<String>(wordCount)
    repeat(wordCount) { words.add(pick(pool).adapted) }
    return words.joinToString(" ")
  }

  /**
   * Returns the ranked, de-duplicated candidate pool for [config]'s generation mode and the given
   * [enabled] letters. The pool is cached per (mode, enabled-key mask).
   *
   * @param config supplies the generation mode.
   * @param enabled the enabled lowercase letters; defaults to those derived from [config].
   */
  fun rankedPool(
    config: PracticeTextConfig,
    enabled: Set<Char> = enabledLetters(config)
  ): List<WordCandidate> {
    val key = PoolKey(config.generationMode, lettersMask(enabled))
    poolCache[key]?.let {
      return it
    }
    val pool = buildPool(config.generationMode, enabled)
    if (poolCache.size >= MAX_CACHED_POOLS) poolCache.remove(poolCache.keys.first())
    poolCache[key] = pool
    return pool
  }

  /**
   * Builds and ranks the candidate pool for [mode] and [enabled], de-duplicating by adapted text.
   */
  private fun buildPool(mode: GenerationMode, enabled: Set<Char>): List<WordCandidate> {
    val mask = lettersMask(enabled)
    val candidates = ArrayList<WordCandidate>()
    for (entry in wordList.words) {
      val isExact = entry.letterMask and mask.inv() == 0
      if (mode == GenerationMode.EXACT_WORDS && !isExact) continue
      val adapted = if (isExact) entry.word else adaptWord(entry.word, enabled)
      val retained = adapted.length
      if (retained == 0) continue
      candidates.add(
        WordCandidate(adapted, entry.word, entry.index, retained, entry.word.length, isExact)
      )
    }
    candidates.sortWith(CANDIDATE_ORDER)
    // De-duplicate by adapted string, keeping the best (first) candidate per adapted string.
    val seen = HashSet<String>()
    return candidates.filter { seen.add(it.adapted) }
  }

  /**
   * Picks a candidate from [pool], preferring high-ranked (high-quality) candidates and avoiding
   * recently shown adapted strings when possible.
   */
  private fun pick(pool: List<WordCandidate>): WordCandidate {
    val window = pool.size.coerceAtMost(SELECTION_WINDOW)
    val top = pool.subList(0, window)
    val recent = recentAdapted.toSet()
    val fresh = top.filter { it.adapted !in recent }
    val source = fresh.ifEmpty { top }
    val chosen = source[random.nextInt(source.size)]
    recentAdapted.remove(chosen.adapted)
    recentAdapted.addLast(chosen.adapted)
    while (recentAdapted.size > MAX_RECENT) recentAdapted.removeFirst()
    return chosen
  }

  private val poolCache = LinkedHashMap<PoolKey, List<WordCandidate>>()
  private val recentAdapted = ArrayDeque<String>()

  private data class PoolKey(val mode: GenerationMode, val mask: Int)

  companion object {
    /** How many top-ranked candidates a single word is drawn from. */
    const val SELECTION_WINDOW = 25

    /** How many recently shown adapted strings are avoided on the next pick. */
    const val MAX_RECENT = 20

    /** Maximum number of cached candidate pools (bounds memory). */
    const val MAX_CACHED_POOLS = 16

    /** Returned when no letters are enabled or no candidates exist. */
    const val FALLBACK_CHARACTER = "a"

    private const val VOWELS = "aeiou"

    /**
     * Deterministic candidate ordering: more retained letters first, then higher retention, then a
     * more common source (lower index), then a vowel bonus (pronounceability proxy), then
     * lexicographic for stability.
     */
    val CANDIDATE_ORDER: Comparator<WordCandidate> =
      compareByDescending<WordCandidate> { it.retainedCount }
        .thenByDescending { it.retention }
        .thenBy { it.sourceIndex }
        .thenByDescending { it.adapted.any { c -> c in VOWELS } }
        .thenBy { it.adapted }

    /** The lowercase letters enabled by [config], derived from the keyboard layout. */
    fun enabledLetters(config: PracticeTextConfig): Set<Char> =
      enabledKeysFor(config).filter { it in 'a'..'z' }.toSet()

    /** Computes the 26-bit mask for a set of lowercase letters. */
    fun lettersMask(letters: Set<Char>): Int {
      var mask = 0
      for (c in letters) if (c in 'a'..'z') mask = mask or (1 shl (c - 'a'))
      return mask
    }
  }
}
