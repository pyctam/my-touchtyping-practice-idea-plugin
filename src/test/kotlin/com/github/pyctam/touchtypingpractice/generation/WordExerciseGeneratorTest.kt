package com.github.pyctam.touchtypingpractice.generation

import com.github.pyctam.touchtypingpractice.config.Finger
import com.github.pyctam.touchtypingpractice.config.GenerationMode
import com.github.pyctam.touchtypingpractice.config.PracticeMode
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the pure [WordExerciseGenerator].
 *
 * A seeded [Random] is injected so the output is deterministic. The bundled word list is used for
 * the realistic tests; small custom lists (via [WordList.fromLines]) are used where exact pool
 * contents must be asserted. These tests run without an IDE.
 */
class WordExerciseGeneratorTest {

  private val wordList = WordList.load()

  private fun config(
    mode: PracticeMode = PracticeMode.BOTH_HANDS,
    fingers: Int = Finger.ALL_MASK,
    keyLimit: Int = 1,
    generationMode: GenerationMode = GenerationMode.ADAPTED_WORDS,
  ) = PracticeTextConfig(mode, fingers, keyLimit, generationMode)

  /** Both hands, index fingers, key limit 2 -> enabled letters f, g, j, h. */
  private val indexFingersConfig =
    config(
      mode = PracticeMode.BOTH_HANDS,
      fingers = Finger.encodeSelectedFingers(0, Finger.INDEX),
      keyLimit = 2,
    )

  // ---------------------------------------------------------------------------
  // Adaptation behavior
  // ---------------------------------------------------------------------------

  @Test
  fun adaptedPoolContainsFlashAdaptedToFlshWhenAIsUnavailable() {
    // Enabled letters f, l, s, h (no 'a'): "flash" (f,l,a,s,h) must adapt to "flsh" (4 letters).
    val enabled = setOf('f', 'l', 's', 'h')
    val pool = WordExerciseGenerator(wordList, Random(1)).rankedPool(config(), enabled)
    val flash = pool.first { it.source == "flash" }
    assertEquals("flsh", flash.adapted)
    assertEquals(4, flash.retainedCount)
    assertFalse(flash.exact)
  }

  @Test
  fun adaptedPoolContainsDocumentedExamples() {
    val generator = WordExerciseGenerator(wordList, Random(1))
    // PLANT -> PLNT if A is unavailable.
    val plant =
      generator.rankedPool(config(), setOf('p', 'l', 'n', 't')).first { it.source == "plant" }
    assertEquals("plnt", plant.adapted)
    // STRONG -> STRNG if O is unavailable.
    val strong =
      generator.rankedPool(config(), setOf('s', 't', 'r', 'n', 'g')).first { it.source == "strong" }
    assertEquals("strng", strong.adapted)
    // TRAIN -> TRIN if A is unavailable.
    val train =
      generator.rankedPool(config(), setOf('t', 'r', 'i', 'n')).first { it.source == "train" }
    assertEquals("trin", train.adapted)
    // GARDEN -> GRDEN if A is unavailable.
    val garden =
      generator.rankedPool(config(), setOf('g', 'r', 'd', 'e', 'n')).first { it.source == "garden" }
    assertEquals("grden", garden.adapted)
  }

  @Test
  fun adaptedResultKeepsReferenceToOriginalWord() {
    val generator = WordExerciseGenerator(wordList, Random(1))
    val pool = generator.rankedPool(indexFingersConfig)
    for (candidate in pool) {
      assertTrue(
        "Adapted '${candidate.adapted}' must be a subsequence of its source '${candidate.source}'",
        isSubsequence(candidate.adapted, candidate.source)
      )
      assertTrue(candidate.source.length >= candidate.adapted.length)
    }
  }

  @Test
  fun emptyAdaptedResultsAreRejected() {
    // "was" and "more" share no letters with f, g, j, h, so they must not appear.
    val generator = WordExerciseGenerator(wordList, Random(1))
    val pool = generator.rankedPool(indexFingersConfig)
    assertFalse(pool.any { it.source == "was" })
    assertFalse(pool.any { it.source == "more" })
    assertTrue(pool.all { it.adapted.isNotEmpty() })
  }

  @Test
  fun duplicateAdaptedOutputsAreDeduplicatedKeepingBestSource() {
    // "the" (index 0) and "they" (index 1) both adapt to "h" with enabled f, g, j, h.
    // The more frequent source ("the") must win the de-duplication.
    val list = WordList.fromLines(listOf("the", "they"))
    val generator = WordExerciseGenerator(list, Random(1))
    val pool = generator.rankedPool(config(), setOf('f', 'g', 'j', 'h'))
    val hCandidates = pool.filter { it.adapted == "h" }
    assertEquals("Expected exactly one candidate per adapted string", 1, hCandidates.size)
    assertEquals("the", hCandidates.single().source)
  }

  @Test
  fun duplicateAdaptedOutputsFromDifferentSourcesKeepMostFrequent() {
    // "off" (index 0) and "staff" (index 1) both adapt to "ff"; "off" is more frequent.
    val list = WordList.fromLines(listOf("off", "staff"))
    val generator = WordExerciseGenerator(list, Random(1))
    val pool = generator.rankedPool(config(), setOf('f', 'g', 'j', 'h'))
    val ffCandidates = pool.filter { it.adapted == "ff" }
    assertEquals(1, ffCandidates.size)
    assertEquals("off", ffCandidates.single().source)
  }

  // ---------------------------------------------------------------------------
  // Exact-word mode
  // ---------------------------------------------------------------------------

  @Test
  fun exactModeContainsOnlyFullyEnabledWords() {
    val generator = WordExerciseGenerator(wordList, Random(1))
    val pool = generator.rankedPool(config(generationMode = GenerationMode.EXACT_WORDS))
    val enabled = WordExerciseGenerator.enabledLetters(config())
    for (candidate in pool) {
      assertTrue(candidate.exact)
      assertEquals(candidate.source, candidate.adapted)
      assertTrue(
        "Exact candidate '${candidate.adapted}' must use only enabled letters",
        candidate.adapted.all { it in enabled }
      )
    }
  }

  @Test
  fun exactModeWithIndexFingersContainsOnlyFghjWords() {
    val generator = WordExerciseGenerator(wordList, Random(1))
    val pool =
      generator.rankedPool(
        config(generationMode = GenerationMode.EXACT_WORDS),
        setOf('f', 'g', 'j', 'h')
      )
    for (candidate in pool) {
      assertTrue(candidate.exact)
      assertTrue(candidate.adapted.all { it in setOf('f', 'g', 'j', 'h') })
    }
    // The single-letter words f, g, h, j are exact words in the list.
    assertTrue(pool.any { it.adapted == "f" })
    assertTrue(pool.any { it.adapted == "g" })
    assertTrue(pool.any { it.adapted == "h" })
    assertTrue(pool.any { it.adapted == "j" })
  }

  @Test
  fun exactModePrefersCommonWords() {
    val generator = WordExerciseGenerator(wordList, Random(1))
    // All fingers, high key limit -> all letters enabled, so "the" is an exact candidate.
    val pool =
      generator.rankedPool(config(keyLimit = 6, generationMode = GenerationMode.EXACT_WORDS))
    // "the" (index 0) is the most common word and must rank first.
    assertEquals("the", pool.first().source)
  }

  // ---------------------------------------------------------------------------
  // Ranking
  // ---------------------------------------------------------------------------

  @Test
  fun rankingPrefersHigherRetainedCountThenRetentionThenFrequency() {
    // Custom list so the pool contents are fully controlled.
    val list =
      WordList.fromLines(
        listOf(
          "zzzz", // 4 retained (enabled z)
          "ab", // 1 retained (a)
          "ba", // 1 retained (b)
          "a", // 1 retained (a)
        )
      )
    val generator = WordExerciseGenerator(list, Random(1))
    val pool = generator.rankedPool(config(), setOf('a', 'b', 'z'))
    // "zzzz" ranks first (4 retained). The three 1-letter results tie on retainedCount and
    // retention (1.0), so source frequency decides: "a" (index 0), "ab" (index 1), "ba" (index 2).
    assertEquals(listOf("zzzz", "a", "ab", "ba"), pool.map { it.adapted })
  }

  @Test
  fun rankingBreaksTiesBySourceFrequency() {
    // All candidates retain exactly one letter (retention 1.0), so only source frequency
    // (list order) can distinguish them.
    val list = WordList.fromLines(listOf("ba", "ab"))
    val generator = WordExerciseGenerator(list, Random(1))
    val pool = generator.rankedPool(config(), setOf('a', 'b'))
    assertEquals(listOf("ba", "ab"), pool.map { it.adapted })
  }

  @Test
  fun rankingIsDeterministic() {
    val a = WordExerciseGenerator(wordList, Random(1)).rankedPool(indexFingersConfig)
    val b = WordExerciseGenerator(wordList, Random(2)).rankedPool(indexFingersConfig)
    assertEquals(a, b)
  }

  @Test
  fun adaptedPoolRanksHighRetentionWordsFirst() {
    val generator = WordExerciseGenerator(wordList, Random(1))
    val pool = generator.rankedPool(indexFingersConfig)
    // The top candidate must retain at least as many letters as any later candidate.
    for (i in 1 until pool.size) {
      assertTrue(
        "Ranking violated at position $i",
        pool[i - 1].retainedCount >= pool[i].retainedCount
      )
    }
  }

  // ---------------------------------------------------------------------------
  // Generation
  // ---------------------------------------------------------------------------

  @Test
  fun generatedTextUsesOnlyEnabledCharacters() {
    val generator = WordExerciseGenerator(wordList, Random(42))
    val enabled = WordExerciseGenerator.enabledLetters(indexFingersConfig)
    repeat(20) {
      val text = generator.generate(indexFingersConfig)
      for (c in text) {
        assertTrue(
          "Character '$c' is not enabled (enabled: $enabled) in text: '$text'",
          c == ' ' || c in enabled
        )
      }
    }
  }

  @Test
  fun generatedTextHasWordCountWithinRange() {
    val generator = WordExerciseGenerator(wordList, Random(42))
    repeat(20) {
      val text = generator.generate(indexFingersConfig)
      val wordCount = text.split(" ").size
      assertTrue(
        "Word count $wordCount out of range in '$text'",
        wordCount in PracticeTextGenerator.MIN_WORDS_COUNT..PracticeTextGenerator.MAX_WORDS_COUNT
      )
    }
  }

  @Test
  fun generatedTextNeverStartsOrEndsWithSpaceAndHasNoDoubleSpaces() {
    val generator = WordExerciseGenerator(wordList, Random(42))
    repeat(20) {
      val text = generator.generate(indexFingersConfig)
      assertTrue(text.first() != ' ')
      assertTrue(text.last() != ' ')
      assertFalse("  " in text)
    }
  }

  @Test
  fun generatedTextIsDeterministicForTheSameSeed() {
    val a = WordExerciseGenerator(wordList, Random(7)).generate(indexFingersConfig)
    val b = WordExerciseGenerator(wordList, Random(7)).generate(indexFingersConfig)
    assertEquals(a, b)
  }

  @Test
  fun generatedTextAvoidsImmediateRepetition() {
    val generator = WordExerciseGenerator(wordList, Random(42))
    val text = generator.generate(indexFingersConfig)
    val words = text.split(" ")
    for (i in 1 until words.size) {
      assertFalse("Consecutive identical word '${words[i]}' in '$text'", words[i] == words[i - 1])
    }
  }

  @Test
  fun noEnabledLettersReturnsFallbackCharacter() {
    val generator = WordExerciseGenerator(wordList, Random(1))
    // No fingers selected -> no letters -> fallback.
    assertEquals(WordExerciseGenerator.FALLBACK_CHARACTER, generator.generate(config(fingers = 0)))
  }

  @Test
  fun emptyWordListReturnsFallbackCharacter() {
    val generator = WordExerciseGenerator(WordList.fromLines(emptyList()), Random(1))
    assertEquals(WordExerciseGenerator.FALLBACK_CHARACTER, generator.generate(indexFingersConfig))
  }

  @Test
  fun exactModeWithNoMatchingWordsReturnsFallbackCharacter() {
    // Enabled letters f, g, j, h have exact words (f, g, h, j), so use a list without them.
    val list = WordList.fromLines(listOf("was", "more"))
    val generator = WordExerciseGenerator(list, Random(1))
    assertEquals(
      WordExerciseGenerator.FALLBACK_CHARACTER,
      generator.generate(config(generationMode = GenerationMode.EXACT_WORDS)),
    )
  }

  @Test
  fun singleFingerLessonGeneratesOnlyThatFingersLetters() {
    val generator = WordExerciseGenerator(wordList, Random(42))
    val leftIndexOnly =
      config(
        mode = PracticeMode.LEFT_HAND,
        fingers = Finger.encodeSelectedFingers(0, Finger.INDEX),
        keyLimit = 1,
      )
    repeat(10) {
      val text = generator.generate(leftIndexOnly)
      for (c in text) {
        assertTrue("Character '$c' not in {f, } in '$text'", c == ' ' || c == 'f')
      }
    }
  }

  @Test
  fun multipleFingerLessonGeneratesOnlyEnabledLetters() {
    val generator = WordExerciseGenerator(wordList, Random(42))
    val config =
      config(
        mode = PracticeMode.BOTH_HANDS,
        fingers = Finger.encodeSelectedFingers(0, Finger.INDEX, Finger.MIDDLE),
        keyLimit = 1,
      )
    val enabled = WordExerciseGenerator.enabledLetters(config)
    repeat(10) {
      val text = generator.generate(config)
      for (c in text) {
        assertTrue("Character '$c' not in $enabled in '$text'", c == ' ' || c in enabled)
      }
    }
  }

  @Test
  fun allLearnedKeysLessonGeneratesRealWords() {
    // All fingers, high key limit -> all letters enabled -> adapted mode yields exact words.
    val generator = WordExerciseGenerator(wordList, Random(42))
    val config = config(keyLimit = 6)
    val text = generator.generate(config)
    for (word in text.split(" ")) {
      assertTrue(
        "Expected a real dictionary word, got '$word'",
        wordList.words.any { it.word == word }
      )
    }
  }

  // ---------------------------------------------------------------------------
  // Caching and performance
  // ---------------------------------------------------------------------------

  @Test
  fun candidatePoolIsCachedPerKeyConfiguration() {
    val generator = WordExerciseGenerator(wordList, Random(1))
    val first = generator.rankedPool(indexFingersConfig)
    val second = generator.rankedPool(indexFingersConfig)
    assertTrue("Expected the same cached pool instance", first === second)
  }

  @Test
  fun differentKeyConfigurationsGetDifferentPools() {
    val generator = WordExerciseGenerator(wordList, Random(1))
    val left =
      generator.rankedPool(
        config(
          mode = PracticeMode.LEFT_HAND,
          fingers = Finger.encodeSelectedFingers(0, Finger.INDEX),
          keyLimit = 2
        )
      )
    val right =
      generator.rankedPool(
        config(
          mode = PracticeMode.RIGHT_HAND,
          fingers = Finger.encodeSelectedFingers(0, Finger.INDEX),
          keyLimit = 2
        )
      )
    assertFalse("Left and right pools must differ", left === right)
  }

  @Test
  fun poolBuildAndGenerationAreFastWithBundledWordList() {
    val generator = WordExerciseGenerator(wordList, Random(1))
    val start = System.nanoTime()
    val pool = generator.rankedPool(indexFingersConfig)
    val poolBuildMs = (System.nanoTime() - start) / 1_000_000
    assertTrue("Pool build took ${poolBuildMs}ms", poolBuildMs < 2_000)
    assertTrue("Expected a non-empty pool", pool.isNotEmpty())

    val start2 = System.nanoTime()
    repeat(100) { generator.generate(indexFingersConfig) }
    val generateMs = (System.nanoTime() - start2) / 1_000_000
    assertTrue("100 generations took ${generateMs}ms", generateMs < 2_000)
  }

  private fun isSubsequence(sub: String, source: String): Boolean {
    var i = 0
    for (c in source) {
      if (i < sub.length && c == sub[i]) i++
    }
    return i == sub.length
  }
}
