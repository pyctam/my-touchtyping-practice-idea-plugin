package com.github.pyctam.touchtypingpractice.generation

import com.github.pyctam.touchtypingpractice.config.Finger
import com.github.pyctam.touchtypingpractice.config.PracticeMode
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the pure [PracticeTextGenerator].
 *
 * A seeded [Random] is injected so the output is deterministic and the generation rules can be
 * asserted precisely. These tests run without an IDE because the generator has no IntelliJ
 * dependency.
 */
class PracticeTextGeneratorTest {

  private val generator = PracticeTextGenerator(Random(42))

  private fun config(
    mode: PracticeMode = PracticeMode.BOTH_HANDS,
    fingers: Int = Finger.ALL_MASK,
    keyLimit: Int = 1,
  ) = PracticeTextConfig(mode, fingers, keyLimit)

  @Test
  fun buildAvailableCharactersContainsOnlyLowercaseLettersAndSpace() {
    val chars = generator.buildAvailableCharacters(config())
    assertTrue("Expected a space to be present", chars.contains(' '))
    for (c in chars) {
      assertTrue("Expected only lowercase letters or space, found '$c'", c == ' ' || c in 'a'..'z')
    }
  }

  @Test
  fun buildAvailableCharactersRespectsKeyLimit() {
    // With a key limit of 1 and only the left index finger, only the home-row key 'f' is eligible.
    val chars =
      generator.buildAvailableCharacters(
        config(
          mode = PracticeMode.LEFT_HAND,
          fingers = Finger.encodeSelectedFingers(0, Finger.INDEX),
          keyLimit = 1,
        )
      )
    assertEquals("f ", chars)
  }

  @Test
  fun buildAvailableCharactersExcludesUnselectedFingers() {
    val chars =
      generator.buildAvailableCharacters(
        config(
          mode = PracticeMode.LEFT_HAND,
          fingers = Finger.encodeSelectedFingers(0, Finger.MIDDLE),
          keyLimit = 6,
        )
      )
    // Left middle finger keys: d, c, e, 3 -> only lowercase letters d, c, e plus the space.
    assertEquals("dce ", chars)
  }

  @Test
  fun buildAvailableCharactersBothHandsMergesBothMaps() {
    val chars =
      generator.buildAvailableCharacters(
        config(
          mode = PracticeMode.BOTH_HANDS,
          fingers = Finger.encodeSelectedFingers(0, Finger.INDEX),
          keyLimit = 1,
        )
      )
    // Left index home key 'f' and right index home key 'j'.
    assertEquals("fj ", chars)
  }

  @Test
  fun generateProducesOnlyEligibleCharacters() {
    val config =
      config(
        mode = PracticeMode.LEFT_HAND,
        fingers = Finger.encodeSelectedFingers(0, Finger.INDEX),
        keyLimit = 2,
      )
    val eligible = generator.buildAvailableCharacters(config).toSet()
    val text = generator.generate(config)
    assertTrue("Expected non-empty text", text.isNotEmpty())
    for (c in text) {
      assertTrue("Character '$c' is not in the eligible set $eligible", c in eligible)
    }
  }

  @Test
  fun generateNeverStartsOrEndsWithSpace() {
    repeat(50) {
      val text = generator.generate(config())
      assertTrue("Text should not start with a space: '$text'", text.first() != ' ')
      assertTrue("Text should not end with a space: '$text'", text.last() != ' ')
    }
  }

  @Test
  fun generateNeverHasConsecutiveSpaces() {
    repeat(50) {
      val text = generator.generate(config())
      assertFalse("Text has consecutive spaces: '$text'", "  " in text)
    }
  }

  @Test
  fun generateNeverExceedsMaxWordLength() {
    repeat(200) {
      val text = generator.generate(config())
      for (word in text.split(" ")) {
        assertTrue(
          "Word '$word' exceeds the max word length of 7 in text: '$text'",
          word.length <= 7,
        )
      }
    }
  }

  @Test
  fun generateNeverExceedsMaxTextLength() {
    repeat(200) {
      val text = generator.generate(config())
      assertTrue(
        "Text exceeds the max text length of 64: '$text'",
        text.length <= 64,
      )
    }
  }

  @Test
  fun generateSeparatesWordsWithASingleSpace() {
    repeat(200) {
      val text = generator.generate(config())
      // No leading/trailing space and no consecutive spaces means every space separates exactly
      // two words.
      assertTrue("Text should not start with a space: '$text'", text.first() != ' ')
      assertTrue("Text should not end with a space: '$text'", text.last() != ' ')
      assertFalse("Text has consecutive spaces: '$text'", "  " in text)
    }
  }

  @Test
  fun generateReturnsFallbackCharacterWhenOnlySpaceIsAvailable() {
    // No fingers selected -> no letters, only a space -> the non-space set is empty, so the
    // generator returns the single fallback character (matches the original behavior).
    val text = generator.generate(config(fingers = 0))
    assertEquals("a", text)
  }

  @Test
  fun generateRandomTextWithOnlySpacesReturnsFallbackCharacter() {
    assertEquals("a", generator.generateRandomText("   "))
  }

  @Test
  fun generateIsDeterministicForTheSameSeed() {
    val a = PracticeTextGenerator(Random(7)).generate(config())
    val b = PracticeTextGenerator(Random(7)).generate(config())
    assertEquals(a, b)
  }
}
