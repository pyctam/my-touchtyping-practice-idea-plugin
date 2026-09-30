package com.github.pyctam.touchtypingpractice.config

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [Settings] state normalization. The finger selection is persisted as a bitmask and
 * an empty selection is invalid, so [Settings.loadState] must normalize a loaded empty mask back to
 * all fingers.
 */
class SettingsTest {
  @Test
  fun defaultSelectedFingersIsAllMask() {
    assertEquals(Finger.ALL_MASK, Settings().selectedFingers)
  }

  @Test
  fun loadStateNormalizesEmptyMaskToAllFingers() {
    val loaded = Settings().apply { selectedFingers = 0 }
    val settings = Settings()
    settings.loadState(loaded)
    assertEquals(Finger.ALL_MASK, settings.selectedFingers)
  }

  @Test
  fun loadStatePreservesPartialSelection() {
    val partial = Finger.encodeSelectedFingers(0, Finger.THUMB, Finger.INDEX)
    val loaded = Settings().apply { selectedFingers = partial }
    val settings = Settings()
    settings.loadState(loaded)
    assertEquals(partial, settings.selectedFingers)
  }

  @Test
  fun loadStatePreservesAllMask() {
    val loaded = Settings().apply { selectedFingers = Finger.ALL_MASK }
    val settings = Settings()
    settings.loadState(loaded)
    assertEquals(Finger.ALL_MASK, settings.selectedFingers)
  }

  @Test
  fun loadStateCopiesOtherFields() {
    val loaded =
      Settings().apply {
        textFontFamily = "Georgia"
        textFontSize = 20
        keyLimitPerFinger = 4
        practiceMode = PracticeMode.LEFT_HAND
        selectedFingers = Finger.encodeSelectedFingers(0, Finger.LITTLE)
      }
    val settings = Settings()
    settings.loadState(loaded)
    assertEquals("Georgia", settings.textFontFamily)
    assertEquals(20, settings.textFontSize)
    assertEquals(4, settings.keyLimitPerFinger)
    assertEquals(PracticeMode.LEFT_HAND, settings.practiceMode)
    assertEquals(Finger.encodeSelectedFingers(0, Finger.LITTLE), settings.selectedFingers)
  }
}
