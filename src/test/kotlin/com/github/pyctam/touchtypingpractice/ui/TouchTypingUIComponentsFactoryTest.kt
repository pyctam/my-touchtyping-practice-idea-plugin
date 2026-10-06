package com.github.pyctam.touchtypingpractice.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * Unit tests for [TouchTypingUIComponentsFactory.createTextPane]: verifies that the reference text
 * pane is non-focusable and non-editable, and that the text is set correctly.
 */
class TouchTypingUIComponentsFactoryTest {

  @Test
  fun createTextPaneIsNotFocusable() {
    val pane = TouchTypingUIComponentsFactory.createTextPane("hello", "Monospaced")

    assertFalse(
      "Reference text pane should not be focusable so the IDE cannot place the cursor in it",
      pane.isFocusable
    )
  }

  @Test
  fun createTextPaneIsNotEditable() {
    val pane = TouchTypingUIComponentsFactory.createTextPane("hello", "Monospaced")

    assertFalse("Reference text pane should not be editable", pane.isEditable)
  }

  @Test
  fun createTextPaneSetsText() {
    val text = "the quick brown fox"
    val pane = TouchTypingUIComponentsFactory.createTextPane(text, "Monospaced")

    assertEquals("Reference text pane should contain the provided text", text, pane.text)
  }

  @Test
  fun createTextPaneHandlesEmptyText() {
    val pane = TouchTypingUIComponentsFactory.createTextPane("", "Monospaced")

    assertEquals("Empty text should remain empty", "", pane.text)
    assertFalse("Empty reference text pane should still not be focusable", pane.isFocusable)
  }
}
