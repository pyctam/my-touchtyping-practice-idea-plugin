package com.github.pyctam.touchtypingpractice.generation

import com.github.pyctam.touchtypingpractice.config.Finger
import com.github.pyctam.touchtypingpractice.config.PracticeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit tests for the [KeyboardLayout] key lookup. */
class KeyboardLayoutTest {

  @Test
  fun leftHandIndexHomeRowKeyIsFirst() {
    assertEquals(listOf("f"), KeyboardLayout.keysFor(PracticeMode.LEFT_HAND, Finger.INDEX, 1))
  }

  @Test
  fun keyLimitTakesTheFirstKeysInProgressionOrder() {
    assertEquals(
      listOf("f", "g", "v"),
      KeyboardLayout.keysFor(PracticeMode.LEFT_HAND, Finger.INDEX, 3)
    )
  }

  @Test
  fun keyLimitLargerThanAvailableReturnsAllKeys() {
    // Left middle finger has 4 keys; a limit of 6 returns all of them.
    assertEquals(
      listOf("d", "c", "e", "3"),
      KeyboardLayout.keysFor(PracticeMode.LEFT_HAND, Finger.MIDDLE, 6)
    )
  }

  @Test
  fun rightHandLittleFingerIncludesModifiers() {
    val keys = KeyboardLayout.keysFor(PracticeMode.RIGHT_HAND, Finger.LITTLE, 12)
    assertTrue("Expected 'enter' among right little-finger keys", "enter" in keys)
    assertTrue("Expected 'shift' among right little-finger keys", "shift" in keys)
  }

  @Test
  fun bothHandsAppliesLimitPerHand() {
    // Limit 1 per hand: left index 'f' + right index 'j'.
    assertEquals(listOf("f", "j"), KeyboardLayout.keysFor(PracticeMode.BOTH_HANDS, Finger.INDEX, 1))
  }

  @Test
  fun bothHandsThumbIsSpaceFromBothHands() {
    // Both thumbs map to space; limit 1 per hand yields two spaces.
    assertEquals(listOf(" ", " "), KeyboardLayout.keysFor(PracticeMode.BOTH_HANDS, Finger.THUMB, 1))
  }
}
