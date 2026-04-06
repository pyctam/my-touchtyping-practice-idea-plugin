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
        // border =
        //  JBUI.Borders.compound(
        //    JBUI.Borders.empty(PADDING),
        //    JBUI.Borders.customLine(JBColor.border(), 1)
        //  )
        // background = UIUtil.getPanelBackground()
      }
    }
    /**
     * Creates the reference text pane showing the text to type. Public factory method. Uses 12px
     * standard padding with subtle border styling.
     */
    fun createTextPane(typingText: String, fontSizePt: Int = 13): JTextPane {
      val pane = JTextPane()
      pane.text = typingText
      pane.isEditable = false
      pane.font = applyFontSize(pane.font, fontSizePt)
      // IntelliJ standard: 12px padding with light border for definition
      pane.border =
        JBUI.Borders.compound(
          JBUI.Borders.empty(PADDING_SMALL),
          // JBUI.Borders.customLine(JBColor.border(), 1)
        )
      return pane
    }
    /**
     * Creates the sample text panel showing the text to type. Public factory method for composing
     * layouts at the ToolWindow level. Uses the provided reference pane. Uses 12px standard padding
     * with subtle border styling.
     */
    fun createSampleTextPanel(referenceTextPane: JTextPane): JBScrollPane {
      val textScrollPane = JBScrollPane(referenceTextPane)
      textScrollPane.border =
        JBUI.Borders.compound(
          JBUI.Borders.empty(0, PADDING_SMALL, PADDING_SMALL, PADDING_SMALL),
          JBUI.Borders.customLine(JBColor.border(), 1, 0, 0, 0)
        )
      return textScrollPane
    }
    /**
     * Creates the typing input panel with document listener for real-time feedback. Public factory
     * method for composing layouts at the ToolWindow level. Uses 12px standard padding for
     * consistency with sample text panel.
     */
    fun createTypingInputPanel(
      referenceTextPane: JTextPane,
      typingText: String,
      errorCounter: ErrorCounter,
      fontSizePt: Int = 13
    ): JBScrollPane {
      val typingPane = createTypingPane(referenceTextPane, typingText, errorCounter, fontSizePt)
      return typingPane
    }
    /**
     * Creates the input typing pane with document listener for real-time feedback. Uses 12px
     * standard padding for consistency with reference text pane.
     */
    fun createTypingPane(
      referenceTextPane: JTextPane,
      originalText: String,
      errorCounter: ErrorCounter,
      fontSizePt: Int = 13
    ): JBScrollPane {
      val typingArea = JTextArea()
      typingArea.lineWrap = true
      typingArea.wrapStyleWord = true
      typingArea.font = applyFontSize(UIUtil.getLabelFont(), fontSizePt)
      typingArea.border =
        JBUI.Borders.compound(
          JBUI.Borders.empty(PADDING_SMALL),
          // JBUI.Borders.customLine(JBColor.border(), 1)
        )

      val listener =
        TouchTypingDocumentListener(typingArea, referenceTextPane, originalText, errorCounter)
      typingArea.document.addDocumentListener(listener)
      val scrollPane = JBScrollPane(typingArea)
      // IntelliJ standard: 12px padding with light border for visual consistency
      scrollPane.border =
        JBUI.Borders.compound(
          JBUI.Borders.empty(0, PADDING_SMALL, PADDING_SMALL, PADDING_SMALL),
          JBUI.Borders.customLine(JBColor.border(), 1, 0, 0, 0)
        )
      return scrollPane
    }
    /**
     * Creates the typing input components with document listener for real-time feedback. Returns a
     * data class with the scroll pane, typing area, and listener for reset functionality.
     */
    fun createTypingInputComponents(
      referenceTextPane: JTextPane,
      typingText: String,
      errorCounter: ErrorCounter,
      fontSizePt: Int = 13
    ): TypingInputComponents {
      val typingArea = JTextArea()
      typingArea.lineWrap = true
      typingArea.wrapStyleWord = true
      typingArea.font = applyFontSize(UIUtil.getLabelFont(), fontSizePt)
      typingArea.border =
        JBUI.Borders.compound(
          JBUI.Borders.empty(PADDING_SMALL),
          // JBUI.Borders.customLine(JBColor.border(), 1)
        )

      val listener =
        TouchTypingDocumentListener(typingArea, referenceTextPane, typingText, errorCounter)
      typingArea.document.addDocumentListener(listener)
      val scrollPane = JBScrollPane(typingArea)
      // IntelliJ standard: 12px padding with light border for visual consistency
      scrollPane.border =
        JBUI.Borders.compound(
          JBUI.Borders.empty(0, PADDING_SMALL, PADDING_SMALL, PADDING_SMALL),
          JBUI.Borders.customLine(JBColor.border(), 1, 0, 0, 0)
        )
      return TypingInputComponents(scrollPane, typingArea, listener)
    }

    private fun applyFontSize(baseFont: Font, sizePt: Int): Font {
      return baseFont.deriveFont(sizePt.toFloat())
    }
  }
}

/** Data class to hold the typing input panel components for reset functionality. */
data class TypingInputComponents(
  val scrollPane: JBScrollPane,
  val typingArea: JTextArea,
  val listener: DocumentListener
)
