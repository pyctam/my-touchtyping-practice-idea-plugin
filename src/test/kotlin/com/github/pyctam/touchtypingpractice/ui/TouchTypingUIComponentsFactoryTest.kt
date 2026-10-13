package com.github.pyctam.touchtypingpractice.ui

import javax.swing.ScrollPaneConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [TouchTypingUIComponentsFactory.createTextPane]: verifies that the reference text
 * pane is non-focusable and non-editable, that the text is set correctly, and that both the
 * reference and typing panes wrap words (with no horizontal scrollbar).
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

  // --- Word-wrapping tests ---
  //
  // A JTextPane does not wrap by default (unlike JTextArea): its view reports
  // getScrollableTracksViewportWidth() as false, so a long line forces a horizontal scrollbar.
  // The factory overrides it to true so both panes wrap at the viewport width.

  @Test
  fun referenceTextPaneWrapsWords() {
    val pane = TouchTypingUIComponentsFactory.createTextPane("hello world", "Monospaced")

    assertTrue(
      "Reference text pane should wrap long lines at the pane width",
      pane.getScrollableTracksViewportWidth()
    )
  }

  @Test
  fun sampleTextPanelNeverShowsHorizontalScrollBar() {
    val referencePane = TouchTypingUIComponentsFactory.createTextPane("hello world", "Monospaced")
    val scrollPane = TouchTypingUIComponentsFactory.createSampleTextPanel(referencePane)

    assertEquals(
      "Sample text panel should never show a horizontal scrollbar (text wraps)",
      ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER,
      scrollPane.horizontalScrollBarPolicy
    )
  }

  @Test
  fun typingAreaWrapsWords() {
    val components = createTypingInputComponents()

    assertTrue(
      "Typing area should wrap long lines at the pane width",
      components.typingArea.getScrollableTracksViewportWidth()
    )
  }

  @Test
  fun typingInputPanelNeverShowsHorizontalScrollBar() {
    val components = createTypingInputComponents()

    assertEquals(
      "Typing input panel should never show a horizontal scrollbar (text wraps)",
      ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER,
      components.scrollPane.horizontalScrollBarPolicy
    )
  }

  /** Builds the typing input components with a plain reference pane and default stats. */
  private fun createTypingInputComponents(): TypingInputComponents {
    val referencePane = TouchTypingUIComponentsFactory.createTextPane("hello world", "Monospaced")
    return TouchTypingUIComponentsFactory.createTypingInputComponents(
      referencePane,
      "hello world",
      ErrorCounter(),
      TypingStats(),
      "Monospaced"
    )
  }
}
