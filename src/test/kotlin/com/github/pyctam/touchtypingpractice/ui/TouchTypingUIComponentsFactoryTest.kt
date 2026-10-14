package com.github.pyctam.touchtypingpractice.ui

import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.Transferable
import javax.swing.JComponent
import javax.swing.ScrollPaneConstants
import javax.swing.TransferHandler
import javax.swing.text.StyledDocument
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

  // --- No-paste tests ---
  //
  // The typing area is a practice surface: the user is expected to *type* the reference text, not
  // copy it in. The factory installs a NoPasteTransferHandler that rejects every import, so both
  // Ctrl+V paste (plain or rich) and drag-and-drop are no-ops, while the document stays a
  // StyledDocument so per-hand coloring (HandColors.applyTo) keeps working.

  @Test
  fun typingAreaRejectsAllPaste() {
    val components = createTypingInputComponents()
    val typingArea = components.typingArea
    val handler = typingArea.transferHandler

    assertFalse(
      "Typing area should reject plain-text paste (the user must type, not paste)",
      handler.canImport(createTransferSupport(typingArea, StringSelection("hello")))
    )
    assertFalse(
      "Typing area should reject the HTML flavor (rich-text paste)",
      handler.canImport(createTransferSupport(typingArea, HtmlTransferable()))
    )
  }

  @Test
  fun typingAreaKeepsStyledDocument() {
    val components = createTypingInputComponents()

    assertTrue(
      "Typing area document must stay a StyledDocument so HandColors.applyTo can color per character",
      components.typingArea.document is StyledDocument
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

  /** Wraps [transferable] in a [TransferHandler.TransferSupport] for [component]. */
  private fun createTransferSupport(
    component: JComponent,
    transferable: Transferable
  ): TransferHandler.TransferSupport = TransferHandler.TransferSupport(component, transferable)

  /** A [Transferable] that offers only the HTML flavor, simulating a rich-text clipboard. */
  private class HtmlTransferable : Transferable {
    private val htmlFlavor = DataFlavor("text/html", "HTML")

    override fun getTransferDataFlavors(): Array<DataFlavor> = arrayOf(htmlFlavor)

    override fun isDataFlavorSupported(flavor: DataFlavor): Boolean = flavor == htmlFlavor

    override fun getTransferData(flavor: DataFlavor): Any = "<html><body>hello</body></html>"
  }
}
