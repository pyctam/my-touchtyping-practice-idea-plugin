package com.github.pyctam.touchtypingpractice.ui

import com.intellij.ui.JBColor
import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.components.BorderLayoutPanel
import java.awt.Font
import javax.swing.JTextArea
import javax.swing.JTextPane

/**
 * UI Components Factory for the Touch Typing Practice plugin. Follows IntelliJ IDEA Design System
 * with 8px base unit spacing.
 *
 * Spacing Scale (IntelliJ Standard):
 * - 8px (1 unit): Compact spacing
 * - 12px (1.5 units): Standard padding (primary)
 * - 16px (2 units): Comfortable spacing
 * - 20px (2.5 units): Generous spacing
 *
 * All colors use JBColor for automatic light/dark theme support.
 */
class TouchTypingUIComponentsFactory {
  companion object {
    // IntelliJ 8px base unit spacing (1.5 units = 12px standard padding)
    const val PADDING = 12
    @Suppress("unused") const val PADDING_SMALL = 8 // Reserved for future compact layouts
    @Suppress("unused") const val PADDING_COMFORTABLE = 16 // Reserved for spacious layouts

    fun createMainPanel(): BorderLayoutPanel {
      return BorderLayoutPanel().apply {
        // 12px standard padding with subtle border for visual separation
        border =
          JBUI.Borders.compound(
            JBUI.Borders.empty(PADDING),
            JBUI.Borders.customLine(JBColor.border(), 1)
          )
        background = UIUtil.getPanelBackground()
      }
    }

    fun createTypingArea(
      typingText: String,
      errorCounter: ErrorCounter,
      textFontSize: Int
    ): JBSplitter {
      val richTextPane = createTextPane(typingText, textFontSize)
      val textScrollPane = JBScrollPane(richTextPane)
      textScrollPane.border = JBUI.Borders.empty()

      val typingPane = createTypingPane(richTextPane, typingText, errorCounter, textFontSize)

      val splitter = JBSplitter(true, 0.25f)
      splitter.setHonorComponentsMinimumSize(true)
      splitter.firstComponent = textScrollPane
      splitter.secondComponent = typingPane
      // Standard 12px padding between reference text and input area
      splitter.border = JBUI.Borders.empty(PADDING)
      return splitter
    }

    /**
     * Creates the reference text pane showing the text to type. Uses 12px standard padding with
     * subtle border styling.
     */
    fun createTextPane(typingText: String, fontSizePt: Int = 12): JTextPane {
      val pane = JTextPane()
      pane.text = typingText
      pane.isEditable = false
      pane.font = applyFontSize(pane.font, fontSizePt)
      // IntelliJ standard: 12px padding with light border for definition
      pane.border =
        JBUI.Borders.compound(
          JBUI.Borders.empty(PADDING),
          JBUI.Borders.customLine(JBColor.border(), 1)
        )

      return pane
    }

    /**
     * Creates the input typing pane with document listener for real-time feedback. Uses 12px
     * standard padding for consistency with reference text pane.
     */
    fun createTypingPane(
      referenceTextPane: JTextPane,
      originalText: String,
      errorCounter: ErrorCounter,
      fontSizePt: Int = 12
    ): JBScrollPane {
      val typingArea = JTextArea()
      typingArea.lineWrap = true
      typingArea.wrapStyleWord = true
      typingArea.font = applyFontSize(UIUtil.getLabelFont(), fontSizePt)

      val listener =
        TouchTypingDocumentListener(typingArea, referenceTextPane, originalText, errorCounter)
      typingArea.document.addDocumentListener(listener)

      val scrollPane = JBScrollPane(typingArea)
      // IntelliJ standard: 12px padding with light border for visual consistency
      scrollPane.border =
        JBUI.Borders.compound(
          JBUI.Borders.empty(PADDING),
          JBUI.Borders.customLine(JBColor.border(), 1)
        )
      return scrollPane
    }

    private fun applyFontSize(baseFont: Font, sizePt: Int): Font {
      return baseFont.deriveFont(sizePt.toFloat())
    }
  }
}
