package com.github.pyctam.touchtypingpractice.config

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the [FingerSelection] working model used by the settings dialog.
 *
 * The model is UI-free, so its transitions (the "Use All Fingers" shortcut and individual finger
 * toggles) can be verified without a Swing dialog.
 */
class FingerSelectionTest {

  @Test
  fun defaultsToAllFingersSelected() {
    val selection = FingerSelection()
    assertEquals(Finger.ALL_MASK, selection.mask)
    assertTrue(selection.isAllSelected)
    assertFalse(selection.isEmpty)
  }

  @Test
  fun setUseAllFingersCheckedSelectsEveryFinger() {
    val selection = FingerSelection(0)
    selection.setUseAllFingers(true)
    assertEquals(Finger.ALL_MASK, selection.mask)
    assertTrue(selection.isAllSelected)
  }

  @Test
  fun setUseAllFingersUncheckedClearsSelection() {
    val selection = FingerSelection(Finger.ALL_MASK)
    selection.setUseAllFingers(false)
    assertEquals(0, selection.mask)
    assertTrue(selection.isEmpty)
  }

  @Test
  fun selectingAFingerClearsTheAllSelectedFlag() {
    val selection = FingerSelection(Finger.ALL_MASK)
    selection.setFinger(Finger.THUMB, false)
    assertFalse(selection.isAllSelected)
    assertFalse(selection.isSelected(Finger.THUMB))
    assertTrue(selection.isSelected(Finger.INDEX))
  }

  @Test
  fun selectingAllFingersSetsTheAllSelectedFlag() {
    val selection = FingerSelection(0)
    for (finger in Finger.entries) {
      selection.setFinger(finger, true)
    }
    assertTrue(selection.isAllSelected)
    assertEquals(Finger.ALL_MASK, selection.mask)
  }

  @Test
  fun setFingerLeavesOtherFingersUntouched() {
    val selection = FingerSelection(Finger.encodeSelectedFingers(0, Finger.MIDDLE))
    selection.setFinger(Finger.INDEX, true)
    assertTrue(selection.isSelected(Finger.MIDDLE))
    assertTrue(selection.isSelected(Finger.INDEX))
    assertFalse(selection.isSelected(Finger.RING))
  }

  @Test
  fun clearFingerViaSetFingerFalse() {
    val selection = FingerSelection(Finger.encodeSelectedFingers(0, Finger.INDEX, Finger.RING))
    selection.setFinger(Finger.INDEX, false)
    assertFalse(selection.isSelected(Finger.INDEX))
    assertTrue(selection.isSelected(Finger.RING))
  }
}
