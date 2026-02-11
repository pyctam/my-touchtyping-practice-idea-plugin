package com.github.pyctam.touchtypingpractice.services

import com.github.pyctam.touchtypingpractice.config.Finger
import com.github.pyctam.touchtypingpractice.config.PracticeMode
import com.github.pyctam.touchtypingpractice.config.Settings
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import kotlin.random.Random

/**
 * Service for generating practice text based on plugin configuration. Supports different character
 * types: lowercase letters, numbers, punctuation, and spaces. Adapts text generation based on
 * practice mode, finger selection, and key limit per finger.
 */
@Service
class PracticeTextGeneratorService {
  private val logger: Logger = Logger.getInstance(PracticeTextGeneratorService::class.java)

  // Keyboard layout for different hands and fingers
  private companion object {
    // Left hand keys (organized by finger)
    private val LEFT_HAND_KEYS =
      mapOf(
        Finger.THUMB to "space",
        Finger.INDEX to "fvghy", // f, v, g, h, y from common touch typing positions
        Finger.MIDDLE to "djuke",
        Finger.RING to "skio",
        Finger.LITTLE to "la;p"
      )

    // Right hand keys (organized by finger)
    private val RIGHT_HAND_KEYS =
      mapOf(
        Finger.THUMB to "space",
        Finger.INDEX to "jmn", // j, m, n from common touch typing positions
        Finger.MIDDLE to "u,ki",
        Finger.RING to "opl",
        Finger.LITTLE to ";'[]"
      )

    // Sample texts for touch typing practice (organized by difficulty)
    private val SAMPLE_TEXTS =
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
        "accuracy comes before speed in touch typing"
      )

    // Punctuation and special characters for practice
    private const val NUMBERS = "0123456789"
    private const val PUNCTUATION = ".,;:!?'\"-"
    private const val LOWERCASE_LETTERS = "abcdefghijklmnopqrstuvwxyz"
  }

  /**
   * Generates practice text based on current configuration settings.
   *
   * @return A practice text string optimized for the current configuration
   */
  fun generatePracticeText(): String {
    val settings = loadSettings()
    return when {
      shouldGenerateCustomText() -> generateCustomText(settings)
      else -> generateSampleText()
    }
  }

  /**
   * Generates a custom text based on character type settings and configuration.
   *
   * @param settings The current practice settings
   * @return A randomly generated practice text
   */
  private fun generateCustomText(settings: Settings): String {
    val availableChars = buildAvailableCharacterSet(settings)
    if (availableChars.isEmpty()) {
      logger.warn("No characters available for practice. Using default sample text.")
      return SAMPLE_TEXTS.random()
    }

    return generateRandomText(availableChars)
  }

  /**
   * Generates a sample text from the predefined collection. Optionally augments it with numbers or
   * punctuation based on settings.
   *
   * @return A sample practice text
   */
  private fun generateSampleText(): String {
    var text = SAMPLE_TEXTS.random()

    // Optionally add numbers and punctuation to sample text
    if (shouldIncludeNumbers() || shouldIncludePunctuation()) {
      text = augmentTextWithCharacters(text)
    }

    return text
  }

  /**
   * Builds the set of available characters based on configuration settings. Takes into account
   * practice mode, finger selection, and character type preferences.
   *
   * @param settings The current practice settings
   * @return A string containing all available characters for practice
   */
  private fun buildAvailableCharacterSet(settings: Settings): String {
    val chars = StringBuilder()

    // Determine available keys based on practice mode and finger selection
    val availableKeys = getAvailableKeysForFingers(settings)

    // Add lowercase letters
    chars.append(availableKeys.filter { it in LOWERCASE_LETTERS })

    // Add numbers if configured
    if (shouldIncludeNumbers()) {
      chars.append(NUMBERS)
    }

    // Add punctuation if configured
    if (shouldIncludePunctuation()) {
      chars.append(PUNCTUATION)
    }

    // Add space
    chars.append(" ")

    return chars.toString()
  }

  /**
   * Gets available keys based on practice mode and selected fingers.
   *
   * @param settings The current practice settings
   * @return A string of available keys for practice
   */
  private fun getAvailableKeysForFingers(settings: Settings): String {
    val keys = StringBuilder()

    val fingerKeyMap =
      when (settings.practiceMode) {
        PracticeMode.LEFT_HAND -> LEFT_HAND_KEYS
        PracticeMode.RIGHT_HAND -> RIGHT_HAND_KEYS
        PracticeMode.BOTH_HANDS -> LEFT_HAND_KEYS + RIGHT_HAND_KEYS
      }

    if (settings.useAllFingers) {
      // Use all available fingers for the selected practice mode
      fingerKeyMap.values.forEach { keys.append(it) }
    } else {
      // Use only selected fingers
      Finger.entries.forEach { finger ->
        if (Finger.isFingerSelected(settings.selectedFingers, finger)) {
          keys.append(fingerKeyMap[finger] ?: "")
        }
      }
    }

    return keys.toString()
  }

  /**
   * Determines if custom text should be generated. Currently returns false to use sample texts by
   * default. Can be extended to support configuration-based custom text generation.
   */
  private fun shouldGenerateCustomText(): Boolean {
    // This can be extended in the future to support a configuration option
    // for enabling/disabling custom text generation
    return false
  }

  /**
   * Determines if numbers should be included in practice text. Can be extended to support a
   * configuration option.
   */
  private fun shouldIncludeNumbers(): Boolean {
    // This can be extended to read from PropertiesComponent or Settings
    return false
  }

  /**
   * Determines if punctuation should be included in practice text. Can be extended to support a
   * configuration option.
   */
  private fun shouldIncludePunctuation(): Boolean {
    // This can be extended to read from PropertiesComponent or Settings
    return false
  }

  /**
   * Generates random text from the provided character set.
   *
   * @param chars Available characters for text generation
   * @return A randomly generated text string of 100 characters
   */
  private fun generateRandomText(chars: String): String {
    val random = Random(System.currentTimeMillis())
    val text = StringBuilder()

    repeat(100) { text.append(chars[random.nextInt(chars.length)]) }

    return text.toString()
  }

  /**
   * Augments a base text with numbers and punctuation.
   *
   * @param baseText The original text to augment
   * @return The augmented text
   */
  private fun augmentTextWithCharacters(baseText: String): String {
    val result = StringBuilder(baseText)
    val random = Random(System.currentTimeMillis())

    // Add punctuation and numbers at random positions
    if (shouldIncludePunctuation()) {
      val punctIndex = random.nextInt(maxOf(1, result.length - 5))
      result.insert(punctIndex, ". ")
    }

    if (shouldIncludeNumbers()) {
      val numIndex = random.nextInt(maxOf(1, result.length - 5))
      result.insert(numIndex, "${random.nextInt(10)} ")
    }

    return result.toString()
  }

  /**
   * Loads current practice settings from IntelliJ properties.
   *
   * @return The current Settings configuration
   */
  private fun loadSettings(): Settings {
    val properties = PropertiesComponent.getInstance()

    val practiceModeName =
      properties.getValue(Settings.PROPERTY_PRACTICE_MODE, PracticeMode.BOTH_HANDS.name)
    val practiceMode = PracticeMode.valueOf(practiceModeName)
    val keyLimitPerFinger = properties.getInt(Settings.PROPERTY_KEY_LIMIT_PER_FINGER, 1)
    val allFingers = properties.getBoolean(Settings.PROPERTY_USE_ALL_FINGERS, true)
    val selectedFingers = properties.getInt(Settings.PROPERTY_SELECTED_FINGERS, 0)

    return Settings(practiceMode, keyLimitPerFinger, allFingers, selectedFingers)
  }
}
