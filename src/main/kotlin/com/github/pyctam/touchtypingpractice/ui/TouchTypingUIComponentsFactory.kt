package com.github.pyctam.touchtypingpractice.ui

import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.components.BorderLayoutPanel
import java.awt.BorderLayout
import javax.swing.JTextArea
import javax.swing.JTextPane

class TouchTypingUIComponentsFactory {
  companion object {
    const val PADDING = 4

    fun createMainPanel(): BorderLayoutPanel {
      return BorderLayoutPanel()
    }

    fun createTypingArea(typingText: String, errorCounter: ErrorCounter): JBSplitter {
      val richTextPane = createTextPane(typingText)
      // Wrap the text pane into a scroll pane for the splitter
      val textScrollPane = JBScrollPane(richTextPane)
      textScrollPane.border = JBUI.Borders.empty()

      val typingPane = createTypingPane(richTextPane, typingText, errorCounter)

      val splitter = JBSplitter(true, 0.25f)
      splitter.setHonorComponentsMinimumSize(true)
      splitter.firstComponent = textScrollPane
      splitter.secondComponent = typingPane
      splitter.border = JBUI.Borders.empty(PADDING) // Padding inside splitter
      return splitter
    }

    // Return the actual JTextPane so callers can attach listeners/highlighters
    fun createTextPane(typingText: String): JTextPane {
      val richTextPane = JTextPane()
      richTextPane.contentType = "text/plain"
      richTextPane.text = typingText
      richTextPane.isEditable = false

      // We keep the panel creation in case layout consumers need it elsewhere
      val textPanel = BorderLayoutPanel()
      textPanel.name = "Text Panel"
      textPanel.border = JBUI.Borders.empty(PADDING) // Adding padding around the JTextPane
      textPanel.add(richTextPane, BorderLayout.CENTER)

      return richTextPane
    }

    // Accept the reference text pane and the original text so the listener can highlight mismatches
    fun createTypingPane(
      referenceTextPane: JTextPane,
      originalText: String,
      errorCounter: ErrorCounter
    ): JBScrollPane {
      val typingArea = JTextArea()
      typingArea.lineWrap = true
      typingArea.wrapStyleWord = true

      val listener =
        TouchTypingDocumentListener(typingArea, referenceTextPane, originalText, errorCounter)
      typingArea.document.addDocumentListener(listener)

      val scrollPane = JBScrollPane(typingArea)
      scrollPane.border = JBUI.Borders.empty() // Remove ugly default border
      return scrollPane
    }
  }
}
