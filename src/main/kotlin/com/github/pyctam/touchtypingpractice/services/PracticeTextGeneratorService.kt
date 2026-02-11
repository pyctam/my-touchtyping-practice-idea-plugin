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
    // Left hand keys - organized by finger, with keys in order of progression (for key limits)
    // Home row position is first, then extensions based on key limit
    private val LEFT_HAND_KEYS =
      mapOf(
        Finger.THUMB to listOf("space"),
        Finger.INDEX to listOf("f", "v", "g", "t"), // f (home), v, g, t
        Finger.MIDDLE to listOf("d", "x", "c"), // d (home), x, c
        Finger.RING to listOf("s", "z"), // s (home), z
        Finger.LITTLE to listOf("a", "q", "w") // a (home), q, w
      )

    // Right hand keys - organized by finger, with keys in order of progression (for key limits)
    private val RIGHT_HAND_KEYS =
      mapOf(
        Finger.THUMB to listOf("space"),
        Finger.INDEX to listOf("j", "m", "n", "h"), // j (home), m, n, h
        Finger.MIDDLE to listOf("k", "comma", "i"), // k (home), comma, i
        Finger.RING to listOf("l", "period"), // l (home), period
        Finger.LITTLE to
          listOf(";", "'", "p", "bracketleft", "bracketright") // ; (home), ', p, [, ]
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
    return generateConfigurationAwareText(settings)
  }

  /**
   * Generates practice text that respects the configuration settings for hand, fingers, and key
   * limit per finger.
   *
   * @param settings The current practice settings
   * @return A randomly generated practice text using only allowed keys
   */
  private fun generateConfigurationAwareText(settings: Settings): String {
    val availableChars = buildAvailableCharacterSet(settings)
    if (availableChars.isEmpty()) {
      logger.warn("No characters available for practice. Using default sample text.")
      return SAMPLE_TEXTS.random()
    }

    logger.info(
      "Practice text generated with settings: practiceMode=${settings.practiceMode}, " +
        "keyLimitPerFinger=${settings.keyLimitPerFinger}, useAllFingers=${settings.useAllFingers}, " +
        "selectedFingers=${settings.selectedFingers}, availableChars='${availableChars.take(20)}...'"
    )

    return generateRandomText(availableChars)
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
      fingerKeyMap.forEach { (_, keyList) ->
        // Apply key limit per finger
        val limitedKeys = keyList.take(settings.keyLimitPerFinger)
        keys.append(limitedKeys.joinToString(""))
      }
    } else {
      // Use only selected fingers
      Finger.entries.forEach { finger ->
        if (Finger.isFingerSelected(settings.selectedFingers, finger)) {
          val keyList = fingerKeyMap[finger] ?: emptyList()
          // Apply key limit per finger
          val limitedKeys = keyList.take(settings.keyLimitPerFinger)
          keys.append(limitedKeys.joinToString(""))
        }
      }
    }

    return keys.toString()
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
