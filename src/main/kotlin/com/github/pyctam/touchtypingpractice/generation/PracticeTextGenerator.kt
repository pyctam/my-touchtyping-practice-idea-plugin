package com.github.pyctam.touchtypingpractice.generation

import kotlin.random.Random

/**
 * Pure, IDE-independent practice-text generator (random-letter mode).
 *
 * Given a [PracticeTextConfig] it builds the set of eligible characters from the [KeyboardLayout]
 * and produces a random practice string. All randomness is injected via [random] so the output is
 * deterministic under a seeded [Random], which makes the generation rules unit-testable.
 *
 * Word-based generation (exact and adapted English words) lives in [WordExerciseGenerator]; the
 * [com.github.pyctam.touchtypingpractice.services.PracticeTextGeneratorService] dispatches between
 * the two based on [com.github.pyctam.touchtypingpractice.config.GenerationMode].
 *
 * Generation rules:
 * - The word count is a random value in `[MIN_WORDS_COUNT, MAX_WORDS_COUNT]` (5–20), approximating
 *   the length of a modern English sentence.
 * - Each word is a random run of letters with a length in `[1, MAX_WORD_LENGTH]`, where
 *   [MAX_WORD_LENGTH] approximates the average English word length.
 * - Words are separated by a single space; the first character is never a space.
 */
class PracticeTextGenerator(private val random: Random = Random.Default) {

  /**
   * Generates a practice string for [config].
   *
   * @return a random practice string using only the characters eligible under [config].
   */
  fun generate(config: PracticeTextConfig): String {
    return generateRandomText(buildAvailableCharacters(config))
  }

  /**
   * Builds the character set eligible for practice under [config].
   *
   * Only lowercase letters are drawn from the keyboard layout (numbers, punctuation and modifier
   * keys are excluded), and a single space is always appended.
   *
   * @return a string of the eligible characters; always contains at least a space.
   */
  fun buildAvailableCharacters(config: PracticeTextConfig): String {
    val letters = enabledKeysFor(config).filter { it in LOWERCASE_LETTERS }
    return letters + " "
  }

  /**
   * Generates a random string from [chars] following the generation rules documented on the class.
   *
   * The word count is drawn from `[MIN_WORDS_COUNT, MAX_WORDS_COUNT]`, then each word is a random
   * run of letters with a length in `[1, MAX_WORD_LENGTH]`, and the words are joined with a single
   * space.
   *
   * @param chars the eligible characters; may include a space.
   * @return a random practice string, or a single fallback character if only spaces are available.
   */
  fun generateRandomText(chars: String): String {
    val nonSpaceChars = chars.replace(" ", "")
    if (nonSpaceChars.isEmpty()) {
      return FALLBACK_CHARACTER
    }
    val wordCount = random.nextInt(MIN_WORDS_COUNT, MAX_WORDS_COUNT + 1)
    val words =
      List(wordCount) {
        val length = random.nextInt(1, MAX_WORD_LENGTH + 1)
        buildString {
          repeat(length) { append(nonSpaceChars[random.nextInt(nonSpaceChars.length)]) }
        }
      }
    return words.joinToString(" ")
  }

  companion object {
    /**
     * The maximum length of the word/term in the generated sample text. This is used to approximate
     * the average English word length.
     *
     * Could be offered as a configurable parameter from the UI.
     */
    const val MAX_WORD_LENGTH = 7

    /**
     * The minimum count of the words/terms in the generated sample text.
     *
     * Could be offered as a configurable parameter from the UI, similar to [MAX_WORD_LENGTH].
     */
    const val MIN_WORDS_COUNT = 5

    /**
     * The maximum count of the words/terms in the generated sample text. This approximates the
     * length of a modern English sentence (15–20 words).
     *
     * Could be offered as a configurable parameter from the UI, similar to [MIN_WORDS_COUNT].
     */
    const val MAX_WORDS_COUNT = 20

    const val LOWERCASE_LETTERS = "abcdefghijklmnopqrstuvwxyz"
    const val FALLBACK_CHARACTER = "a"
  }
}
