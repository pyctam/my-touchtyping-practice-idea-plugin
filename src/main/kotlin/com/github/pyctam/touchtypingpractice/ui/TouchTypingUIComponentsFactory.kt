package com.github.pyctam.touchtypingpractice.ui

import com.github.pyctam.touchtypingpractice.config.ColoringMode
import com.github.pyctam.touchtypingpractice.config.TextFont
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.components.BorderLayoutPanel
import java.awt.Font
import javax.swing.JTextPane
import javax.swing.ScrollPaneConstants
import javax.swing.TransferHandler
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.DefaultCaret

/**
 * UI components factory for the Touch Typing Practice plugin. Follows the IntelliJ IDEA Design
 * System with an 8px base-unit spacing scale.
 *
 * All colors use [JBColor] for automatic light/dark theme support and [JBUI] for DPI scaling.
 *
 * Exposed as a Kotlin `object` because it is a stateless factory: there is no reason to instantiate
 * it.
 */
object TouchTypingUIComponentsFactory {

  /** Compact padding (1 x 8px base unit). */
  const val PADDING_SMALL = 8

  /** Creates the root container for the tool window content. */
  fun createMainPanel(): BorderLayoutPanel = BorderLayoutPanel()

  /**
   * Creates the reference text pane showing the text to type.
   *
   * Each character is colored according to [coloringMode] (none, by hand, or by finger) via
   * [TextColors.applyTo]. Long lines wrap at the pane width (no horizontal scrollbar).
   *
   * @param typingText the text to display.
   * @param fontFamily the font family to render with.
   * @param fontSizePt font size in points.
   * @param coloringMode how the characters are colored.
   */
  fun createTextPane(
    typingText: String,
    fontFamily: String,
    fontSizePt: Int = 13,
    coloringMode: ColoringMode = ColoringMode.FINGER
  ): JTextPane {
    val pane = createWrappingTextPane()
    pane.text = typingText
    pane.isEditable = false
    pane.isFocusable = false
    pane.font = applyFont(pane.font, fontFamily, fontSizePt)
    pane.border = JBUI.Borders.compound(JBUI.Borders.empty(PADDING_SMALL))
    TextColors.applyTo(pane, typingText, coloringMode)
    return pane
  }

  /**
   * Creates the scrollable sample-text panel wrapping the provided reference pane.
   *
   * @param referenceTextPane the reference pane to display.
   */
  fun createSampleTextPanel(referenceTextPane: JTextPane): JBScrollPane {
    val scrollPane = JBScrollPane(referenceTextPane)
    // The reference pane wraps words, so a horizontal scrollbar is never needed.
    scrollPane.horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
    scrollPane.border =
      JBUI.Borders.compound(
        JBUI.Borders.empty(0, PADDING_SMALL, PADDING_SMALL, PADDING_SMALL),
        JBUI.Borders.customLine(JBColor.border(), 1, 0, 0, 0)
      )
    return scrollPane
  }

  /**
   * Creates the typing input area with a [TouchTypingDocumentListener] for real-time feedback.
   *
   * The typing area is a [JTextPane] so each character can be colored according to [coloringMode]
   * (none, by hand, or by finger), matching the reference pane.
   *
   * @param referenceTextPane the reference pane to highlight mismatches against.
   * @param typingText the original text being typed.
   * @param errorCounter the counter updated with the current mismatch count.
   * @param stats the session typing-speed/accuracy tracker fed by insertions and errors.
   * @param fontFamily the font family to render with.
   * @param fontSizePt font size in points.
   * @param coloringMode how the characters are colored.
   * @param onCompletionChanged a callback to be called when the typing input is completed.
   * @return the scroll pane and typing area needed to reset the input later.
   */
  fun createTypingInputComponents(
    referenceTextPane: JTextPane,
    typingText: String,
    errorCounter: ErrorCounter,
    stats: TypingStats,
    fontFamily: String,
    fontSizePt: Int = 13,
    coloringMode: ColoringMode = ColoringMode.FINGER,
    onCompletionChanged: (Boolean) -> Unit = {}
  ): TypingInputComponents {
    val typingArea = createTypingArea(fontFamily, fontSizePt)
    val listener =
      TouchTypingDocumentListener(
        typingArea,
        referenceTextPane,
        typingText,
        errorCounter,
        stats,
        coloringMode,
        onCompletionChanged
      )
    typingArea.document.addDocumentListener(listener)

    val scrollPane = JBScrollPane(typingArea)
    // The typing area wraps words, so a horizontal scrollbar is never needed.
    scrollPane.horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
    scrollPane.border =
      JBUI.Borders.compound(
        JBUI.Borders.empty(0, PADDING_SMALL, PADDING_SMALL, PADDING_SMALL),
        JBUI.Borders.customLine(JBColor.border(), 1, 0, 0, 0)
      )
    return TypingInputComponents(scrollPane, typingArea)
  }

  /**
   * Creates a word-wrapping [JTextPane] for the typing area.
   *
   * The typing area accepts keyboard input only: a [TypingOnlyTransferHandler] rejects every
   * clipboard and drag-and-drop import, so the reference text can only be entered by typing it.
   *
   * The typing area is selection-free: `select` is overridden to collapse any selection to a caret
   * at the end of the text, `getSelectedText` always reports no selected text, a [PinnedEndCaret]
   * pins the caret to the end on every caret movement (arrow keys, Home/End, mouse click), and a
   * [DocumentListener] re-pins the caret to the end after every document change (typing). Together
   * these make the typing area a pure forward-typing surface: the caret always sits right after the
   * last typed character, so Backspace targets the last (possibly mistyped) character, and no text
   * can ever be selected.
   *
   * The document stays a [javax.swing.text.StyledDocument] (a `PlainTextEditorKit` would install a
   * `PlainDocument`), so [TextColors.applyTo] can still color each character.
   */
  private fun createTypingArea(fontFamily: String, fontSizePt: Int): JTextPane {
    val typingArea =
      object : JTextPane() {
        override fun getScrollableTracksViewportWidth(): Boolean = true

        // Collapse any selection (mouse drag, Shift+arrows, programmatic select) to a caret at
        // the end of the text, so the typing area is selection-free.
        override fun select(start: Int, end: Int) {
          (caret as? PinnedEndCaret)?.pinToEnd() ?: setCaretPosition(document.length)
        }

        // The typing area is selection-free: always report no selected text.
        override fun getSelectedText(): String = ""
      }
    typingArea.font = applyFont(UIUtil.getLabelFont(), fontFamily, fontSizePt)
    typingArea.border = JBUI.Borders.compound(JBUI.Borders.empty(PADDING_SMALL))
    typingArea.transferHandler = TypingOnlyTransferHandler()
    typingArea.caret = PinnedEndCaret(typingArea)
    // Re-pin the caret to the end after every document change (typing), since document changes
    // do not trigger a caret movement on their own.
    typingArea.document.addDocumentListener(
      object : DocumentListener {
        override fun insertUpdate(e: DocumentEvent?) = pinCaretToEnd(typingArea)

        override fun removeUpdate(e: DocumentEvent?) = pinCaretToEnd(typingArea)

        override fun changedUpdate(e: DocumentEvent?) = Unit
      }
    )
    return typingArea
  }

  /** Moves the caret of [pane] to the end of its document (collapsing any selection). */
  private fun pinCaretToEnd(pane: JTextPane) {
    (pane.caret as? PinnedEndCaret)?.pinToEnd() ?: pane.setCaretPosition(pane.document.length)
  }

  /**
   * Creates a [JTextPane] that wraps long lines at the viewport width instead of growing
   * horizontally.
   *
   * Unlike `JTextArea`, a [JTextPane] does not wrap by default: its view reports
   * `getScrollableTracksViewportWidth()` as `false`, so a long line forces a horizontal scrollbar.
   * Overriding it to `true` makes the view track the viewport width and wrap.
   */
  private fun createWrappingTextPane(): JTextPane =
    object : JTextPane() {
      override fun getScrollableTracksViewportWidth(): Boolean = true
    }

  /**
   * Derives a font with the given family and size from [baseFont]. If [fontFamily] is not installed
   * on the system, the base font's family is kept (the JVM would otherwise fall back to a logical
   * font, which looks inconsistent across platforms).
   */
  private fun applyFont(baseFont: Font, fontFamily: String, fontSizePt: Int): Font {
    val family = if (TextFont.isAvailable(fontFamily)) fontFamily else baseFont.family
    return Font(family, baseFont.style, 0).deriveFont(fontSizePt.toFloat())
  }
}

/** Holds the typing input components needed to reset the input later. */
data class TypingInputComponents(val scrollPane: JBScrollPane, val typingArea: JTextPane)

/**
 * A [TransferHandler] that rejects every clipboard and drag-and-drop import, so the typing area
 * accepts keyboard input only.
 */
private class TypingOnlyTransferHandler : TransferHandler() {

  override fun canImport(transferSupport: TransferSupport): Boolean = false

  override fun importData(transferSupport: TransferSupport): Boolean = false
}

/**
 * A [javax.swing.text.Caret] that is permanently pinned to the end of the document and never
 * selects.
 *
 * Every attempt to move the caret (arrow keys, Home/End, mouse click, programmatic
 * [javax.swing.text.JTextComponent.select]) is collapsed to a zero-length caret at the end of the
 * text. This makes the typing area a pure forward-typing surface: the caret always sits right after
 * the last typed character, so Backspace targets the last (possibly mistyped) character, and no
 * text can ever be selected.
 *
 * [pinToEnd] moves the caret to the end of the document. It is called from the component's
 * [javax.swing.text.JTextComponent.select] override, from a [javax.swing.event.DocumentListener]
 * that re-pins after every document change, and from [setDot] (the single entry point for all caret
 * movement in Swing). A re-entrancy guard prevents infinite recursion. The component additionally
 * overrides [javax.swing.text.JTextComponent.getSelectedText] to always report no selected text, so
 * the typing area is selection-free even when the internal mark lags behind the dot.
 */
private class PinnedEndCaret(
  private val textComponent: javax.swing.text.JTextComponent,
) : DefaultCaret() {

  private var isPinning = false

  /** Moves the caret to the end of the document. */
  fun pinToEnd() {
    if (isPinning) return
    isPinning = true
    try {
      super.setDot(textComponent.document.length)
    } finally {
      isPinning = false
    }
  }

  override fun setDot(pos: Int) {
    pinToEnd()
  }
}
