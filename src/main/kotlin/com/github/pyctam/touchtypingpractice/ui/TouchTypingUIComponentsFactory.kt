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

class TouchTypingUIComponentsFactory {
  companion object {
    const val PADDING = 4

    fun createMainPanel(): BorderLayoutPanel {
      return BorderLayoutPanel().apply {
        border =
          JBUI.Borders.compound(
            JBUI.Borders.empty(4), // optional padding
            JBUI.Borders.customLine(JBColor.border(), 1) // or a custom etched-like border
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
      // Wrap the text pane into a scroll pane for the splitter
      val textScrollPane = JBScrollPane(richTextPane)
      textScrollPane.border = JBUI.Borders.empty()

      val typingPane = createTypingPane(richTextPane, typingText, errorCounter, textFontSize)

      val splitter = JBSplitter(true, 0.25f)
      splitter.setHonorComponentsMinimumSize(true)
      splitter.firstComponent = textScrollPane
      splitter.secondComponent = typingPane
      splitter.border = JBUI.Borders.empty(PADDING) // Padding inside splitter
      return splitter
    }

    // Return the actual JTextPane so callers can attach listeners/highlighters
    fun createTextPane(typingText: String, fontSizePt: Int = 12): JTextPane {
      val pane = JTextPane()
      pane.text = typingText
      pane.isEditable = false
      pane.font = applyFontSize(pane.font, fontSizePt)
      pane.border =
        JBUI.Borders.compound(
          JBUI.Borders.empty(4), // optional padding
          JBUI.Borders.customLine(JBColor.border(), 1) // or a custom etched-like border
        )

      return pane
    }

    // Accept the reference text pane and the original text so the listener can highlight mismatches
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
      scrollPane.border =
        JBUI.Borders.compound(
          JBUI.Borders.empty(4), // optional padding
          JBUI.Borders.customLine(JBColor.border(), 1) // or a custom etched-like border
        )
      return scrollPane
    }

    private fun applyFontSize(baseFont: Font, sizePt: Int): Font {
      return baseFont.deriveFont(sizePt.toFloat())
    }
  }
}
