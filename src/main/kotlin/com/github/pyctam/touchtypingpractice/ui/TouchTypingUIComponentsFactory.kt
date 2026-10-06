package com.github.pyctam.touchtypingpractice.ui

import com.github.pyctam.touchtypingpractice.config.TextFont
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.components.BorderLayoutPanel
import java.awt.Font
import javax.swing.JTextPane

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
   * Each character is colored by hand (blue for left-hand keys, purple for right-hand keys) via
   * [HandColors.applyTo].
   *
   * @param typingText the text to display.
   * @param fontFamily the font family to render with.
   * @param fontSizePt font size in points.
   */
  fun createTextPane(typingText: String, fontFamily: String, fontSizePt: Int = 13): JTextPane {
    val pane = JTextPane()
    pane.text = typingText
    pane.isEditable = false
    pane.isFocusable = false
    pane.font = applyFont(pane.font, fontFamily, fontSizePt)
    pane.border = JBUI.Borders.compound(JBUI.Borders.empty(PADDING_SMALL))
    HandColors.applyTo(pane, typingText)
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
   * The typing area is a [JTextPane] so each character can be colored by hand (blue for left-hand
   * keys, purple for right-hand keys), matching the reference pane.
   *
   * @param referenceTextPane the reference pane to highlight mismatches against.
   * @param typingText the original text being typed.
   * @param errorCounter the counter updated with the current mismatch count.
   * @param fontFamily the font family to render with.
   * @param fontSizePt font size in points.
   * @param onCompletionChanged a callback to be called when the typing input is completed.
   * @return the scroll pane and typing area needed to reset the input later.
   */
  fun createTypingInputComponents(
    referenceTextPane: JTextPane,
    typingText: String,
    errorCounter: ErrorCounter,
    fontFamily: String,
    fontSizePt: Int = 13,
    onCompletionChanged: (Boolean) -> Unit = {}
  ): TypingInputComponents {
    val typingArea = createTypingArea(fontFamily, fontSizePt)
    val listener =
      TouchTypingDocumentListener(
        typingArea,
        referenceTextPane,
        typingText,
        errorCounter,
        onCompletionChanged
      )
    typingArea.document.addDocumentListener(listener)

    val scrollPane = JBScrollPane(typingArea)
    scrollPane.border =
      JBUI.Borders.compound(
        JBUI.Borders.empty(0, PADDING_SMALL, PADDING_SMALL, PADDING_SMALL),
        JBUI.Borders.customLine(JBColor.border(), 1, 0, 0, 0)
      )
    return TypingInputComponents(scrollPane, typingArea)
  }

  /** Creates a word-wrapping [JTextPane] for typing input with per-hand character colors. */
  private fun createTypingArea(fontFamily: String, fontSizePt: Int): JTextPane {
    val typingArea = JTextPane()
    typingArea.font = applyFont(UIUtil.getLabelFont(), fontFamily, fontSizePt)
    typingArea.border = JBUI.Borders.compound(JBUI.Borders.empty(PADDING_SMALL))
    return typingArea
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
