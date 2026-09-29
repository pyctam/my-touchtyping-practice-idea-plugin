package com.github.pyctam.touchtypingpractice.config

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the [Finger] bitmask helpers. These are the foundation of the "Finger Selection"
 * settings: the selected fingers are stored as a bitmask where bit N corresponds to the Nth entry
 * of [Finger.entries].
 */
class FingerTest {
  @Test
  fun allMaskHasAllFiveBitsSet() {
    assertEquals(0b11111, Finger.ALL_MASK)
    for (finger in Finger.entries) {
      assertTrue(
        "Expected $finger to be selected in ALL_MASK",
        Finger.isFingerSelected(Finger.ALL_MASK, finger)
      )
    }
  }

  @Test
  fun encodeSetsOnlyTheGivenFingerBit() {
    val mask = Finger.encodeSelectedFingers(0, Finger.THUMB)
    assertTrue(Finger.isFingerSelected(mask, Finger.THUMB))
    for (finger in Finger.entries) {
      if (finger != Finger.THUMB) {
        assertFalse("Expected $finger to be unselected", Finger.isFingerSelected(mask, finger))
      }
    }
  }

  @Test
  fun encodeIsAccumulative() {
    var mask = Finger.encodeSelectedFingers(0, Finger.THUMB)
    mask = Finger.encodeSelectedFingers(mask, Finger.INDEX)
    assertTrue(Finger.isFingerSelected(mask, Finger.THUMB))
    assertTrue(Finger.isFingerSelected(mask, Finger.INDEX))
    assertFalse(Finger.isFingerSelected(mask, Finger.MIDDLE))
  }

  @Test
  fun encodeAllFingersEqualsAllMask() {
    val mask = Finger.encodeSelectedFingers(0, *Finger.entries.toTypedArray())
    assertEquals(Finger.ALL_MASK, mask)
  }

  @Test
  fun isFingerSelectedChecksExactlyOneBitPerFinger() {
    for (finger in Finger.entries) {
      val mask = Finger.encodeSelectedFingers(0, finger)
      for (other in Finger.entries) {
        assertEquals(
          "Expected isFingerSelected($mask, $other) to be ${other == finger}",
          other == finger,
          Finger.isFingerSelected(mask, other)
        )
      }
    }
  }

  @Test
  fun clearingABitWithInverseMaskLeavesOthersIntact() {
    var mask = Finger.ALL_MASK
    mask = mask and (1 shl Finger.INDEX.ordinal).inv()
    assertFalse(Finger.isFingerSelected(mask, Finger.INDEX))
    for (finger in Finger.entries) {
      if (finger != Finger.INDEX) {
        assertTrue("Expected $finger to remain selected", Finger.isFingerSelected(mask, finger))
      }
    }
  }

  @Test
  fun emptyMaskHasNoFingersSelected() {
    for (finger in Finger.entries) {
      assertFalse(Finger.isFingerSelected(0, finger))
    }
  }
}
