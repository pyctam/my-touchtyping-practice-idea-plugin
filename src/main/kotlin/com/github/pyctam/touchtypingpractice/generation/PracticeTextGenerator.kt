package com.github.pyctam.touchtypingpractice.generation

import com.github.pyctam.touchtypingpractice.config.Finger
import kotlin.random.Random

/**
 * Pure, IDE-independent practice-text generator.
 *
 * Given a [PracticeTextConfig] it builds the set of eligible characters from the [KeyboardLayout]
 * and produces a random practice string. All randomness is injected via [random] so the output is
 * deterministic under a seeded [Random], which makes the generation rules unit-testable.
 *
 * Generation rules:
 * - The target length is a random value in `[MIN_TEXT_LENGTH, MAX_TEXT_LENGTH]`; the final text is
 *   at most that long (a trailing space, if any, is trimmed).
 * - The text is built word by word: each word is a random run of letters with a length in `[1,
 *   MAX_WORD_LENGTH]`, where [MAX_WORD_LENGTH] approximates the average English word length.
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
    val letters = availableKeys(config).filter { it in LOWERCASE_LETTERS }
    return letters + " "
  }

  /**
   * Generates a random string from [chars] following the generation rules documented on the class.
   *
   * The text is built word by word: a random target length is drawn first, then words of length
   * `[1, MAX_WORD_LENGTH]` are appended (clamped to the remaining space) with a single space
   * between them, until the target length is reached.
   *
   * @param chars the eligible characters; may include a space.
   * @return a random practice string, or a single fallback character if only spaces are available.
   */
  fun generateRandomText(chars: String): String {
    val nonSpaceChars = chars.replace(" ", "")
    if (nonSpaceChars.isEmpty()) {
      return FALLBACK_CHARACTER
    }
    val targetLength = random.nextInt(MIN_TEXT_LENGTH, MAX_TEXT_LENGTH + 1)
    val sb = StringBuilder(targetLength)
    while (sb.length < targetLength) {
      val remaining = targetLength - sb.length
      val wordLength = random.nextInt(1, minOf(MAX_WORD_LENGTH, remaining) + 1)
      repeat(wordLength) { sb.append(nonSpaceChars[random.nextInt(nonSpaceChars.length)]) }
      if (sb.length < targetLength) {
        sb.append(' ')
      }
    }
    return sb.toString().trimEnd()
  }

  /** Concatenates the eligible keys for every selected finger, in [Finger] declaration order. */
  private fun availableKeys(config: PracticeTextConfig): String {
    val sb = StringBuilder()
    for (finger in Finger.entries) {
      if (Finger.isFingerSelected(config.selectedFingers, finger)) {
        sb.append(
          KeyboardLayout.keysFor(config.practiceMode, finger, config.keyLimitPerFinger)
            .joinToString("")
        )
      }
    }
    return sb.toString()
  }

  private companion object {
    const val MIN_TEXT_LENGTH = 1
    const val MAX_TEXT_LENGTH = 64
    const val MAX_WORD_LENGTH = 7
    const val LOWERCASE_LETTERS = "abcdefghijklmnopqrstuvwxyz"
    const val FALLBACK_CHARACTER = "a"
  }
}
