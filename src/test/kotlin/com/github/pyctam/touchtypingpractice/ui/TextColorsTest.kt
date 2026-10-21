package com.github.pyctam.touchtypingpractice.ui

import com.github.pyctam.touchtypingpractice.config.ColoringMode
import com.github.pyctam.touchtypingpractice.config.Finger
import com.github.pyctam.touchtypingpractice.config.Hand
import javax.swing.JTextPane
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [TextColors]: verifies the character-to-color mapping for each [ColoringMode] and
 * the [TextColors.applyTo] styling behavior.
 */
class TextColorsTest {

  // --- colorFor() mapping tests: HAND mode ---

  @Test
  fun handModeLeftHandKeysMapToBlue() {
    val leftHandChars = "fgvbrt45dce3sxw2azq"
    for (c in leftHandChars) {
      val color = TextColors.colorFor(c, ColoringMode.HAND)
      assertNotNull("Expected a color for left-hand key '$c'", color)
      assertEquals("Left-hand key '$c' should map to the LEFT color", TextColors.LEFT, color)
    }
  }

  @Test
  fun handModeRightHandKeysMapToPurple() {
    val rightHandChars = "jhnmuy67k,i8l.o9;'/p[]\\-=0"
    for (c in rightHandChars) {
      val color = TextColors.colorFor(c, ColoringMode.HAND)
      assertNotNull("Expected a color for right-hand key '$c'", color)
      assertEquals("Right-hand key '$c' should map to the RIGHT color", TextColors.RIGHT, color)
    }
  }

  // --- colorFor() mapping tests: FINGER mode ---

  @Test
  fun fingerModeLeftHandKeysMapToTheirFingerColors() {
    val leftHandChars = "fgvbrt45dce3sxw2azq"
    for (c in leftHandChars) {
      val color = TextColors.colorFor(c, ColoringMode.FINGER)
      assertNotNull("Expected a color for left-hand key '$c'", color)
      val expected = TextColors.FINGER_COLORS.getValue(Hand.LEFT to fingerOf(c))
      assertEquals("Left-hand key '$c' should map to its finger color", expected, color)
    }
  }

  @Test
  fun fingerModeRightHandKeysMapToTheirFingerColors() {
    val rightHandChars = "jhnmuy67k,i8l.o9;'/p[]\\-=0"
    for (c in rightHandChars) {
      val color = TextColors.colorFor(c, ColoringMode.FINGER)
      assertNotNull("Expected a color for right-hand key '$c'", color)
      val expected = TextColors.FINGER_COLORS.getValue(Hand.RIGHT to fingerOf(c))
      assertEquals("Right-hand key '$c' should map to its finger color", expected, color)
    }
  }

  @Test
  fun fingerModeSameFingerTypeOnDifferentHandsHasDifferentColors() {
    // 'f' is the left index finger, 'j' is the right index finger: distinct colors.
    val leftIndex = TextColors.colorFor('f', ColoringMode.FINGER)
    val rightIndex = TextColors.colorFor('j', ColoringMode.FINGER)
    assertNotNull(leftIndex)
    assertNotNull(rightIndex)
    assertTrue("Left and right index fingers should have distinct colors", leftIndex != rightIndex)
  }

  @Test
  fun fingerModeAllTenFingerColorsAreDistinct() {
    val colors = TextColors.FINGER_COLORS.values.toList()
    assertEquals("Expected 10 finger colors", 10, colors.size)
    assertEquals("All 10 finger colors should be distinct", 10, colors.toSet().size)
  }

  // --- colorFor() mapping tests: NONE mode and unmapped characters ---

  @Test
  fun noneModeMapsEverythingToNull() {
    assertNull(
      "Space should not have a color in NONE mode",
      TextColors.colorFor(' ', ColoringMode.NONE)
    )
    assertNull(
      "Letter 'f' should not have a color in NONE mode",
      TextColors.colorFor('f', ColoringMode.NONE)
    )
  }

  @Test
  fun spaceMapsToNullInAllModes() {
    for (mode in ColoringMode.entries) {
      assertNull("Space should not have a color in $mode", TextColors.colorFor(' ', mode))
    }
  }

  @Test
  fun uppercaseLettersMapToNull() {
    for (mode in ColoringMode.entries) {
      assertNull("Uppercase 'A' should not have a color in $mode", TextColors.colorFor('A', mode))
      assertNull("Uppercase 'J' should not have a color in $mode", TextColors.colorFor('J', mode))
    }
  }

  @Test
  fun allLowercaseLettersAreMappedInHandAndFingerModes() {
    val allLowercase = "abcdefghijklmnopqrstuvwxyz"
    for (mode in listOf(ColoringMode.HAND, ColoringMode.FINGER)) {
      val mappedCount = allLowercase.count { TextColors.colorFor(it, mode) != null }
      assertEquals("All 26 lowercase letters should have a color in $mode", 26, mappedCount)
    }
  }

  // --- applyTo() tests ---

  @Test
  fun applyToDoesNotThrowAndPreservesText() {
    val pane = JTextPane()
    pane.text = "hello world"

    TextColors.applyTo(pane, "hello world", ColoringMode.FINGER)

    assertEquals("Text should be preserved after applyTo", "hello world", pane.text)
  }

  @Test
  fun applyToIsIdempotent() {
    val pane = JTextPane()
    pane.text = "hello"

    TextColors.applyTo(pane, "hello", ColoringMode.FINGER)
    TextColors.applyTo(pane, "hello", ColoringMode.FINGER)

    assertEquals("Text should be preserved after repeated applyTo", "hello", pane.text)
  }

  @Test
  fun applyToHandlesEmptyText() {
    val pane = JTextPane()
    pane.text = ""

    TextColors.applyTo(pane, "", ColoringMode.FINGER)

    assertEquals("Empty text should remain empty", "", pane.text)
  }

  @Test
  fun applyToInNoneModeDoesNotThrow() {
    val pane = JTextPane()
    pane.text = "hello world"

    TextColors.applyTo(pane, "hello world", ColoringMode.NONE)

    assertEquals("Text should be preserved after applyTo in NONE mode", "hello world", pane.text)
  }

  /** Returns the [Finger] that types [c] in the QWERTY layout (per-hand, per-finger). */
  private fun fingerOf(c: Char): Finger {
    val left =
      mapOf(
        'f' to Finger.INDEX,
        'g' to Finger.INDEX,
        'v' to Finger.INDEX,
        'b' to Finger.INDEX,
        't' to Finger.INDEX,
        'r' to Finger.INDEX,
        '4' to Finger.INDEX,
        '5' to Finger.INDEX,
        'd' to Finger.MIDDLE,
        'c' to Finger.MIDDLE,
        'e' to Finger.MIDDLE,
        '3' to Finger.MIDDLE,
        's' to Finger.RING,
        'x' to Finger.RING,
        'w' to Finger.RING,
        '2' to Finger.RING,
        'a' to Finger.LITTLE,
        'z' to Finger.LITTLE,
        'q' to Finger.LITTLE,
      )
    val right =
      mapOf(
        'j' to Finger.INDEX,
        'h' to Finger.INDEX,
        'n' to Finger.INDEX,
        'm' to Finger.INDEX,
        'u' to Finger.INDEX,
        'y' to Finger.INDEX,
        '6' to Finger.INDEX,
        '7' to Finger.INDEX,
        'k' to Finger.MIDDLE,
        ',' to Finger.MIDDLE,
        'i' to Finger.MIDDLE,
        '8' to Finger.MIDDLE,
        'l' to Finger.RING,
        '.' to Finger.RING,
        'o' to Finger.RING,
        '9' to Finger.RING,
        ';' to Finger.LITTLE,
        '\'' to Finger.LITTLE,
        '/' to Finger.LITTLE,
        'p' to Finger.LITTLE,
        '[' to Finger.LITTLE,
        ']' to Finger.LITTLE,
        '\\' to Finger.LITTLE,
        '-' to Finger.LITTLE,
        '=' to Finger.LITTLE,
        '0' to Finger.LITTLE,
      )
    return left[c] ?: right[c] ?: error("Unexpected character '$c'")
  }
}
