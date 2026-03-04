package com.github.pyctam.touchtypingpractice.services

import com.github.pyctam.touchtypingpractice.config.Finger
import com.github.pyctam.touchtypingpractice.config.PracticeMode
import com.github.pyctam.touchtypingpractice.config.PracticeMode.BOTH_HANDS
import com.github.pyctam.touchtypingpractice.config.Settings
import com.github.pyctam.touchtypingpractice.config.Settings.Companion.DEFAULT_TEXT_FONT_SIZE
import com.github.pyctam.touchtypingpractice.config.Settings.Companion.PROPERTY_KEY_LIMIT_PER_FINGER
import com.github.pyctam.touchtypingpractice.config.Settings.Companion.PROPERTY_PRACTICE_MODE
import com.github.pyctam.touchtypingpractice.config.Settings.Companion.PROPERTY_SELECTED_FINGERS
import com.github.pyctam.touchtypingpractice.config.Settings.Companion.PROPERTY_TEXT_FONT_SIZE
import com.github.pyctam.touchtypingpractice.config.Settings.Companion.PROPERTY_USE_ALL_FINGERS
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

    // Add space only once (it will be added with a reduced frequency in random generation)
    chars.append(" ")

    logger.info("buildAvailableCharacterSet: chars='$chars'")

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

    if (settings.useAllFingers) {
      // Use all available fingers for the selected practice mode
      when (settings.practiceMode) {
        PracticeMode.LEFT_HAND -> {
          LEFT_HAND_KEYS.forEach { (_, keyList) ->
            val limitedKeys = keyList.take(settings.keyLimitPerFinger)
            keys.append(limitedKeys.joinToString(""))
          }
        }
        PracticeMode.RIGHT_HAND -> {
          RIGHT_HAND_KEYS.forEach { (_, keyList) ->
            val limitedKeys = keyList.take(settings.keyLimitPerFinger)
            keys.append(limitedKeys.joinToString(""))
          }
        }
        PracticeMode.BOTH_HANDS -> {
          LEFT_HAND_KEYS.forEach { (_, keyList) ->
            val limitedKeys = keyList.take(settings.keyLimitPerFinger)
            keys.append(limitedKeys.joinToString(""))
          }
          RIGHT_HAND_KEYS.forEach { (_, keyList) ->
            val limitedKeys = keyList.take(settings.keyLimitPerFinger)
            keys.append(limitedKeys.joinToString(""))
          }
        }
      }
    } else {
      // Use only selected fingers
      Finger.entries.forEach { finger ->
        if (Finger.isFingerSelected(settings.selectedFingers, finger)) {
          // For BOTH_HANDS mode, check both hand maps
          when (settings.practiceMode) {
            PracticeMode.LEFT_HAND -> {
              val keyList = LEFT_HAND_KEYS[finger] ?: emptyList()
              val limitedKeys = keyList.take(settings.keyLimitPerFinger)
              keys.append(limitedKeys.joinToString(""))
            }
            PracticeMode.RIGHT_HAND -> {
              val keyList = RIGHT_HAND_KEYS[finger] ?: emptyList()
              val limitedKeys = keyList.take(settings.keyLimitPerFinger)
              keys.append(limitedKeys.joinToString(""))
            }
            PracticeMode.BOTH_HANDS -> {
              // Include keys from both hands for this finger
              val leftKeyList = LEFT_HAND_KEYS[finger] ?: emptyList()
              val rightKeyList = RIGHT_HAND_KEYS[finger] ?: emptyList()
              val leftLimited = leftKeyList.take(settings.keyLimitPerFinger)
              val rightLimited = rightKeyList.take(settings.keyLimitPerFinger)
              keys.append(leftLimited.joinToString(""))
              keys.append(rightLimited.joinToString(""))
            }
          }
        }
      }
    }

    logger.info(
      "getAvailableKeysForFingers: practiceMode=${settings.practiceMode}, " +
        "useAllFingers=${settings.useAllFingers}, keyLimitPerFinger=${settings.keyLimitPerFinger}, " +
        "availableKeys='$keys'"
    )

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
   * Rules:
   * - First character is never a space
   * - Consecutive spaces are limited to 1 maximum (no repeated spaces)
   * - Space frequency is approximately 10% of the text
   *
   * @param chars Available characters for text generation
   * @return A randomly generated text string of 100 characters
   */
  private fun generateRandomText(chars: String): String {
    val random = Random(System.currentTimeMillis())
    val text = StringBuilder()
    val nonSpaceChars = chars.replace(" ", "")
    var lastWasSpace = false

    // Ensure first character is not a space
    if (nonSpaceChars.isEmpty()) {
      logger.warn("No non-space characters available. Using space only.")
      return " ".repeat(100)
    }

    repeat(100) { index ->
      val charToAdd =
        if (index == 0) {
          // First character must not be space
          nonSpaceChars[random.nextInt(nonSpaceChars.length)]
        } else if (lastWasSpace) {
          // If last character was space, never add another space
          nonSpaceChars[random.nextInt(nonSpaceChars.length)]
        } else {
          // 90% chance of non-space character, 10% chance of space
          val rand = random.nextDouble()
          if (rand < 0.1 && chars.contains(' ')) {
            lastWasSpace = true
            ' '
          } else {
            nonSpaceChars[random.nextInt(nonSpaceChars.length)]
          }
        }

      if (charToAdd != ' ') {
        lastWasSpace = false
      }

      text.append(charToAdd)
    }

    logger.info("generateRandomText: generated='${text.take(50)}...' (length=${text.length})")

    return text.toString()
  }

  /**
   * Loads current practice settings from IntelliJ properties.
   *
   * @return The current Settings configuration
   */
  private fun loadSettings(): Settings {
    val properties = PropertiesComponent.getInstance()

    val textFontSize = properties.getInt(PROPERTY_TEXT_FONT_SIZE, DEFAULT_TEXT_FONT_SIZE)
    val practiceModeName = properties.getValue(PROPERTY_PRACTICE_MODE, BOTH_HANDS.name)
    val practiceMode = PracticeMode.valueOf(practiceModeName)
    val keyLimitPerFinger = properties.getInt(PROPERTY_KEY_LIMIT_PER_FINGER, 1)
    val allFingers = properties.getBoolean(PROPERTY_USE_ALL_FINGERS, true)
    val selectedFingers = properties.getInt(PROPERTY_SELECTED_FINGERS, 0)

    return Settings(textFontSize, practiceMode, keyLimitPerFinger, allFingers, selectedFingers)
  }
}
