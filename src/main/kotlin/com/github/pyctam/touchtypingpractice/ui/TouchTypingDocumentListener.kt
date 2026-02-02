package com.github.pyctam.touchtypingpractice.ui

import com.intellij.openapi.diagnostic.Logger
import java.awt.Color
import javax.swing.JTextArea
import javax.swing.JTextPane
import javax.swing.SwingUtilities
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.DefaultHighlighter

class TouchTypingDocumentListener(
  private val typingArea: JTextArea,
  private val referenceTextPane: JTextPane,
  private val originalText: String
) : DocumentListener {
  private val logger: Logger = Logger.getInstance(TouchTypingDocumentListener::class.java)
  private val mismatchPainter = DefaultHighlighter.DefaultHighlightPainter(Color(0xFFCCCC))

  override fun insertUpdate(event: DocumentEvent?) {
    updateHighlights()
  }

  override fun removeUpdate(event: DocumentEvent?) {
    updateHighlights()
  }

  override fun changedUpdate(event: DocumentEvent?) {
    // not needed for plain text
  }

  private fun updateHighlights() {
    // Ensure we update UI on EDT
    SwingUtilities.invokeLater {
      try {
        val typed = typingArea.text
        val highlighter = referenceTextPane.highlighter
        highlighter.removeAllHighlights()

        val compareLen = minOf(typed.length, originalText.length)
        for (i in 0 until compareLen) {
          if (typed[i] != originalText[i]) {
            // highlight single character at position i
            highlighter.addHighlight(i, i + 1, mismatchPainter)
          }
        }

        // If typed is longer than original, optionally highlight the remainder of the original text
        // (no-op for now). If you want to show extra typed characters, you'd need to display them
        // in the reference pane as well or handle them differently.
      } catch (t: Throwable) {
        logger.warn("Failed to update highlights", t)
      }
    }
  }
}
