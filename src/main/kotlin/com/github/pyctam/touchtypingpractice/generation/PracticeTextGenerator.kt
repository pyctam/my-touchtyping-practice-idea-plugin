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
 * Generation rules (preserved from the original implementation):
 * - Length is a random value in `[MIN_TEXT_LENGTH, MAX_TEXT_LENGTH]`.
 * - The first character is never a space.
 * - Two spaces are never consecutive.
 * - A trailing space is trimmed.
 * - Spaces occur with roughly [SPACE_PROBABILITY] frequency.
 */
class PracticeTextGenerator(private val random: Random = Random.Default) {

  /**
   * Generates a practice string for [config].
   *
   * @return a random practice string using only the characters eligible under [config], or a
   *   curated fallback sentence when no eligible characters exist.
   */
  fun generate(config: PracticeTextConfig): String {
    val availableChars = buildAvailableCharacters(config)
    if (availableChars.isEmpty()) {
      return FALLBACK_TEXTS.random(random)
    }
    return generateRandomText(availableChars)
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
   * @param chars the eligible characters; may include a space.
   * @return a random practice string, or a single fallback character if only spaces are available.
   */
  fun generateRandomText(chars: String): String {
    val nonSpaceChars = chars.replace(" ", "")
    if (nonSpaceChars.isEmpty()) {
      return FALLBACK_CHARACTER
    }
    val textLength = random.nextInt(MIN_TEXT_LENGTH, MAX_TEXT_LENGTH + 1)
    val sb = StringBuilder(textLength)
    var lastWasSpace = false
    repeat(textLength) { index ->
      val charToAdd =
        when {
          index == 0 -> nonSpaceChars[random.nextInt(nonSpaceChars.length)]
          lastWasSpace -> nonSpaceChars[random.nextInt(nonSpaceChars.length)]
          random.nextDouble() < SPACE_PROBABILITY && chars.contains(' ') -> {
            lastWasSpace = true
            ' '
          }
          else -> nonSpaceChars[random.nextInt(nonSpaceChars.length)]
        }
      if (charToAdd != ' ') {
        lastWasSpace = false
      }
      sb.append(charToAdd)
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
    const val MAX_TEXT_LENGTH = 127
    const val SPACE_PROBABILITY = 0.1
    const val LOWERCASE_LETTERS = "abcdefghijklmnopqrstuvwxyz"
    const val FALLBACK_CHARACTER = "a"

    /** Curated pangrams used only when the configured character set is empty. */
    val FALLBACK_TEXTS =
      listOf(
        "the quick brown fox jumps over the lazy dog",
        "pack my box with five dozen liquor jugs",
        "how vexingly quick daft zebras jump",
        "the five boxing wizards jump quickly",
        "sphinx of black quartz judge my vow",
        "waltz bad nymph for quick jigs",
        "all we know about grammar is that it's not that simple",
        "a journey of a thousand miles begins with a single step",
        "the early bird catches the worm",
        "practice makes perfect",
        "fingers dancing on keyboard in perfect rhythm",
        "typing faster with proper finger placement",
        "accuracy comes before speed in touch typing",
      )
  }
}
