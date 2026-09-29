package com.github.pyctam.touchtypingpractice.ui

import com.intellij.ui.JBColor
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.components.BorderLayoutPanel
import java.awt.Font
import javax.swing.JTextArea
import javax.swing.JTextPane
import javax.swing.event.DocumentListener

/**
 * UI components factory for the Touch Typing Practice plugin. Follows the IntelliJ IDEA Design
 * System with an 8px base-unit spacing scale.
 *
 * Spacing scale (IntelliJ standard):
 * - 8px (1 unit): compact spacing ([PADDING_SMALL])
 * - 12px (1.5 units): standard padding ([PADDING])
 * - 16px (2 units): comfortable spacing ([PADDING_COMFORTABLE])
 *
 * All colors use [JBColor] for automatic light/dark theme support and [JBUI] for DPI scaling.
 *
 * Exposed as a Kotlin `object` because it is a stateless factory: there is no reason to instantiate
 * it.
 */
object TouchTypingUIComponentsFactory {

  /** Standard padding (1.5 x 8px base unit). */
  const val PADDING = 12

  /** Compact padding (1 x 8px base unit). */
  const val PADDING_SMALL = 8

  /** Comfortable padding (2 x 8px base unit). */
  const val PADDING_COMFORTABLE = 16

  /** Creates the root container for the tool window content. */
  fun createMainPanel(): BorderLayoutPanel = BorderLayoutPanel()

  /**
   * Creates the reference text pane showing the text to type.
   *
   * @param typingText the text to display.
   * @param fontSizePt font size in points.
   */
  fun createTextPane(typingText: String, fontSizePt: Int = 13): JTextPane {
    val pane = JTextPane()
    pane.text = typingText
    pane.isEditable = false
    pane.font = applyFontSize(pane.font, fontSizePt)
    pane.border = JBUI.Borders.compound(JBUI.Borders.empty(PADDING_SMALL))
    return pane
  }

  /**
   * Creates the scrollable sample-text panel wrapping the provided reference pane.
   *
   * @param referenceTextPane the reference pane to display.
   */
  fun createSampleTextPanel(referenceTextPane: JTextPane): JBScrollPane {
    val scrollPane = JBScrollPane(referenceTextPane)
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
   * @param referenceTextPane the reference pane to highlight mismatches against.
   * @param typingText the original text being typed.
   * @param errorCounter the counter updated with the current mismatch count.
   * @param fontSizePt font size in points.
   * @return the scroll pane, typing area, and listener needed to reset the input later.
   */
  fun createTypingInputComponents(
    referenceTextPane: JTextPane,
    typingText: String,
    errorCounter: ErrorCounter,
    fontSizePt: Int = 13
  ): TypingInputComponents {
    val typingArea = createTypingArea(fontSizePt)
    val listener =
      TouchTypingDocumentListener(typingArea, referenceTextPane, typingText, errorCounter)
    typingArea.document.addDocumentListener(listener)

    val scrollPane = JBScrollPane(typingArea)
    scrollPane.border =
      JBUI.Borders.compound(
        JBUI.Borders.empty(0, PADDING_SMALL, PADDING_SMALL, PADDING_SMALL),
        JBUI.Borders.customLine(JBColor.border(), 1, 0, 0, 0)
      )
    return TypingInputComponents(scrollPane, typingArea, listener)
  }

  /** Creates a word-wrapping, non-editable-styled [JTextArea] for typing input. */
  private fun createTypingArea(fontSizePt: Int): JTextArea {
    val typingArea = JTextArea()
    typingArea.lineWrap = true
    typingArea.wrapStyleWord = true
    typingArea.font = applyFontSize(UIUtil.getLabelFont(), fontSizePt)
    typingArea.border = JBUI.Borders.compound(JBUI.Borders.empty(PADDING_SMALL))
    return typingArea
  }

  private fun applyFontSize(baseFont: Font, sizePt: Int): Font =
    baseFont.deriveFont(sizePt.toFloat())
}

/** Holds the typing input components needed to reset the input later. */
data class TypingInputComponents(
  val scrollPane: JBScrollPane,
  val typingArea: JTextArea,
  val listener: DocumentListener
)
