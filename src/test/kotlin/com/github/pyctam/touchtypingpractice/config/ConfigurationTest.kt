package com.github.pyctam.touchtypingpractice.config

import com.intellij.openapi.options.ConfigurationException
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.runInEdtAndGet
import com.intellij.testFramework.runInEdtAndWait
import javax.swing.JCheckBox
import javax.swing.JComponent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests the interactive behavior of the "Finger Selection" settings in [Configuration].
 *
 * The selected fingers are stored as a bitmask ([Settings.selectedFingers]) and "Use All Fingers"
 * is a derived shortcut (checked iff all five fingers are selected). These tests drive the actual
 * checkboxes (via [JCheckBox.doClick]) and assert the cross-syncing behavior:
 * - Initial state: "Use All Fingers" checked and all fingers checked.
 * - Unchecking one or more fingers unchecks "Use All Fingers".
 * - Selecting all fingers checks "Use All Fingers".
 * - Checking "Use All Fingers" selects all fingers in one click.
 * - Applying with no fingers selected shows a warning and blocks the save.
 */
class ConfigurationTest : BasePlatformTestCase() {
  private val fingerLabels = listOf("Thumb", "Index", "Middle", "Ring", "Little")
  private val useAllLabel = "Use All Fingers"

  private lateinit var configuration: Configuration
  private lateinit var panel: JComponent

  private fun settings(): Settings = Settings.getInstance()

  /**
   * Creates a fresh [Configuration] and its panel (on the EDT), seeding the saved finger selection
   * with [initialSelectedFingers] so the dialog opens in that state.
   */
  private fun createPanel(initialSelectedFingers: Int): JComponent {
    settings().selectedFingers = initialSelectedFingers
    configuration = Configuration()
    val create: () -> JComponent = { configuration.createComponent() }
    panel = runInEdtAndGet(create)
    return panel
  }

  private fun checkBoxWithText(text: String): JCheckBox =
    findCheckBoxes(panel).first { it.text == text }

  private fun findCheckBoxes(root: JComponent): List<JCheckBox> {
    val result = mutableListOf<JCheckBox>()
    fun walk(component: JComponent) {
      if (component is JCheckBox) {
        result.add(component)
      }
      for (child in component.components) {
        if (child is JComponent) {
          walk(child)
        }
      }
    }
    walk(root)
    return result
  }

  private fun click(box: JCheckBox) {
    runInEdtAndWait { box.doClick() }
  }

  @Test
  fun testInitialStateShowsUseAllFingersAndAllFingersChecked() {
    createPanel(Finger.ALL_MASK)
    assertTrue(checkBoxWithText(useAllLabel).isSelected)
    for (label in fingerLabels) {
      assertTrue("Expected $label to be checked", checkBoxWithText(label).isSelected)
    }
  }

  @Test
  fun testCheckingUseAllFingersSelectsAllFingers() {
    // Start with only the thumb selected, so "Use All Fingers" is unchecked.
    createPanel(Finger.encodeSelectedFingers(0, Finger.THUMB))
    assertFalse(checkBoxWithText(useAllLabel).isSelected)

    click(checkBoxWithText(useAllLabel))

    assertTrue(checkBoxWithText(useAllLabel).isSelected)
    for (label in fingerLabels) {
      assertTrue(
        "Expected $label to be checked after 'Use All Fingers'",
        checkBoxWithText(label).isSelected
      )
    }
  }

  @Test
  fun testUncheckingAFingerUnchecksUseAllFingers() {
    createPanel(Finger.ALL_MASK)
    assertTrue(checkBoxWithText(useAllLabel).isSelected)

    click(checkBoxWithText("Thumb"))

    assertFalse(checkBoxWithText(useAllLabel).isSelected)
    assertFalse(checkBoxWithText("Thumb").isSelected)
    for (label in fingerLabels) {
      if (label != "Thumb") {
        assertTrue("Expected $label to remain checked", checkBoxWithText(label).isSelected)
      }
    }
  }

  @Test
  fun testSelectingAllFingersChecksUseAllFingers() {
    // Start with only the thumb selected.
    createPanel(Finger.encodeSelectedFingers(0, Finger.THUMB))
    assertFalse(checkBoxWithText(useAllLabel).isSelected)

    for (label in fingerLabels) {
      if (label != "Thumb") {
        click(checkBoxWithText(label))
      }
    }

    assertTrue(
      "Expected 'Use All Fingers' to be checked once all fingers are selected",
      checkBoxWithText(useAllLabel).isSelected
    )
  }

  @Test
  fun testUncheckingUseAllFingersClearsAllFingers() {
    createPanel(Finger.ALL_MASK)
    assertTrue(checkBoxWithText(useAllLabel).isSelected)

    click(checkBoxWithText(useAllLabel))

    assertFalse(checkBoxWithText(useAllLabel).isSelected)
    for (label in fingerLabels) {
      assertFalse("Expected $label to be cleared", checkBoxWithText(label).isSelected)
    }
  }

  @Test
  fun testApplyWithNoFingersSelectedBlocksAndKeepsSettings() {
    val original = Finger.encodeSelectedFingers(0, Finger.THUMB)
    createPanel(original)

    for (label in fingerLabels) {
      val box = checkBoxWithText(label)
      if (box.isSelected) {
        click(box)
      }
    }

    assertTrue(
      "Expected the dialog to be modified after clearing all fingers",
      configuration.isModified()
    )

    assertThrows(ConfigurationException::class.java) { configuration.apply() }

    // The invalid selection must not be committed.
    assertEquals(original, settings().selectedFingers)
  }

  @Test
  fun testApplyCommitsSelectionAndClearsModified() {
    createPanel(Finger.ALL_MASK)

    click(checkBoxWithText("Thumb"))
    val expected = Finger.ALL_MASK and (1 shl Finger.THUMB.ordinal).inv()
    assertTrue(configuration.isModified())

    configuration.apply()

    assertEquals(expected, settings().selectedFingers)
    assertFalse(
      "Expected the dialog to be unmodified after a successful apply",
      configuration.isModified()
    )
  }

  @Test
  fun testResetRestoresSavedSelection() {
    val saved = Finger.encodeSelectedFingers(0, Finger.THUMB, Finger.INDEX)
    createPanel(saved)

    // Make a change: check Middle (starts unchecked).
    click(checkBoxWithText("Middle"))
    assertTrue(checkBoxWithText("Middle").isSelected)

    runInEdtAndWait { configuration.reset() }

    assertEquals(saved, settings().selectedFingers)
    assertFalse(checkBoxWithText(useAllLabel).isSelected)
    assertTrue(checkBoxWithText("Thumb").isSelected)
    assertTrue(checkBoxWithText("Index").isSelected)
    assertFalse(checkBoxWithText("Middle").isSelected)
  }
}
