package com.github.pyctam.touchtypingpractice.ui

import com.intellij.openapi.diagnostic.Logger
import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.components.BorderLayoutPanel
import java.awt.BorderLayout
import javax.swing.JTextArea
import javax.swing.JTextPane

class TouchTypingUIComponentsFactory {
  companion object {
    private val logger: Logger = Logger.getInstance(TouchTypingUIComponentsFactory::class.java)

    const val PADDING = 4

    fun createMainPanel(): BorderLayoutPanel {
      return BorderLayoutPanel()
    }

    fun createTypingArea(typingText: String): JBSplitter {
      val textPane = createTextPane(typingText)
      val typingPane = createTypingPane()

      val splitter = JBSplitter(true, 0.25f)
      splitter.setHonorComponentsMinimumSize(true)
      splitter.firstComponent = textPane
      splitter.secondComponent = typingPane
      splitter.border = JBUI.Borders.empty(PADDING) // Padding inside splitter
      return splitter
    }

    fun createTextPane(typingText: String): JBScrollPane {
      val richTextPane = JTextPane()
      richTextPane.contentType = "text/plain"
      richTextPane.text = typingText
      richTextPane.isEditable = false

      val textPanel = BorderLayoutPanel()
      textPanel.border = JBUI.Borders.empty(PADDING) // Adding padding around the JTextPane
      textPanel.add(richTextPane, BorderLayout.CENTER)

      val scrollPane = JBScrollPane(richTextPane)
      scrollPane.border = JBUI.Borders.empty() // Clean native look
      return scrollPane
    }

    fun createTypingPane(): JBScrollPane {
      val typingArea = JTextArea()
      typingArea.lineWrap = true
      typingArea.wrapStyleWord = true

      val listener = TouchTypingDocumentListener(typingArea)
      typingArea.document.addDocumentListener(listener)

      val scrollPane = JBScrollPane(typingArea)
      scrollPane.border = JBUI.Borders.empty() // Remove ugly default border
      return scrollPane
    }
  }
}
