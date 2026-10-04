package com.github.pyctam.touchtypingpractice.ui

import javax.swing.JTextPane
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for [HandColors]: verifies the character-to-hand-color mapping and the
 * [HandColors.applyTo] styling behavior.
 */
class HandColorsTest {

  // --- colorFor() mapping tests ---

  @Test
  fun leftHandKeysMapToBlue() {
    val leftHandChars = "fgvbrt45dce3sxw2azq"
    for (c in leftHandChars) {
      val color = HandColors.colorFor(c)
      assertNotNull("Expected a color for left-hand key '$c'", color)
      assertEquals("Left-hand key '$c' should map to the LEFT color", HandColors.LEFT, color)
    }
  }

  @Test
  fun rightHandKeysMapToPurple() {
    val rightHandChars = "jhnmuy67k,i8l.o9;'/p[]\\-=0"
    for (c in rightHandChars) {
      val color = HandColors.colorFor(c)
      assertNotNull("Expected a color for right-hand key '$c'", color)
      assertEquals("Right-hand key '$c' should map to the RIGHT color", HandColors.RIGHT, color)
    }
  }

  @Test
  fun spaceMapsToNull() {
    assertNull("Space should not have a hand color", HandColors.colorFor(' '))
  }

  @Test
  fun uppercaseLettersMapToNull() {
    assertNull("Uppercase 'A' should not have a hand color", HandColors.colorFor('A'))
    assertNull("Uppercase 'J' should not have a hand color", HandColors.colorFor('J'))
  }

  @Test
  fun allLowercaseLettersAreMapped() {
    val allLowercase = "abcdefghijklmnopqrstuvwxyz"
    val mappedCount = allLowercase.count { HandColors.colorFor(it) != null }
    assertEquals("All 26 lowercase letters should have a hand color", 26, mappedCount)
  }

  // --- applyTo() tests ---

  @Test
  fun applyToDoesNotThrowAndPreservesText() {
    val pane = JTextPane()
    pane.text = "hello world"

    HandColors.applyTo(pane, "hello world")

    assertEquals("Text should be preserved after applyTo", "hello world", pane.text)
  }

  @Test
  fun applyToIsIdempotent() {
    val pane = JTextPane()
    pane.text = "hello"

    HandColors.applyTo(pane, "hello")
    HandColors.applyTo(pane, "hello")

    assertEquals("Text should be preserved after repeated applyTo", "hello", pane.text)
  }

  @Test
  fun applyToHandlesEmptyText() {
    val pane = JTextPane()
    pane.text = ""

    HandColors.applyTo(pane, "")

    assertEquals("Empty text should remain empty", "", pane.text)
  }
}
