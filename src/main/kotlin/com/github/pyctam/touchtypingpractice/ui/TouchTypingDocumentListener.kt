package com.github.pyctam.touchtypingpractice.ui

import com.intellij.openapi.diagnostic.Logger
import com.intellij.ui.JBColor
import javax.swing.JTextArea
import javax.swing.JTextPane
import javax.swing.SwingUtilities
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.AbstractDocument
import javax.swing.text.DefaultHighlighter

/**
 * Provides real-time feedback while the user types the reference text.
 *
 * On every document change it highlights, in the reference pane, each character that does not match
 * the typed text, updates the [errorCounter] with the current mismatch count, and highlights the
 * whole reference text in green when it is completed with zero errors.
 *
 * A [TextLengthLimiterFilter] is installed on the typing area to prevent typing beyond the
 * reference length and to block new input while errors are unresolved.
 *
 * @param typingArea the editable area the user types into.
 * @param referenceTextPane the non-editable pane showing the text to type.
 * @param originalText the reference text being typed.
 * @param errorCounter the counter updated with the current mismatch count.
 */
class TouchTypingDocumentListener(
  private val typingArea: JTextArea,
  private val referenceTextPane: JTextPane,
  private val originalText: String,
  private val errorCounter: ErrorCounter,
) : DocumentListener {

  private val logger = Logger.getInstance(TouchTypingDocumentListener::class.java)

  /** Red background (theme-aware) for a single mismatched character. */
  private val mismatchPainter =
    DefaultHighlighter.DefaultHighlightPainter(JBColor(0xFFCCCC, 0xFFCCCC))

  /** Light green background (theme-aware) shown when the text is completed without errors. */
  private val completionPainter =
    DefaultHighlighter.DefaultHighlightPainter(JBColor(0xC8E6C9, 0x2E5233))

  init {
    // Install a filter to prevent typing beyond the original text length.
    val doc = typingArea.document
    if (doc is AbstractDocument) {
      doc.documentFilter = TextLengthLimiterFilter(originalText.length, originalText)
    }
  }

  override fun insertUpdate(event: DocumentEvent?) {
    updateHighlights()
  }

  override fun removeUpdate(event: DocumentEvent?) {
    updateHighlights()
  }

  override fun changedUpdate(event: DocumentEvent?) {
    // Not needed for plain text.
  }

  /** Recomputes the mismatch highlights and error count on the EDT. */
  private fun updateHighlights() {
    SwingUtilities.invokeLater {
      try {
        val typed = typingArea.text
        val highlighter = referenceTextPane.highlighter
        highlighter.removeAllHighlights()

        val compareLen = minOf(typed.length, originalText.length)
        var mismatchCount = 0
        for (i in 0 until compareLen) {
          if (typed[i] != originalText[i]) {
            highlighter.addHighlight(i, i + 1, mismatchPainter)
            mismatchCount++
          }
        }

        errorCounter.setCount(mismatchCount)

        // Highlight the whole sample text green when it is completed without errors.
        if (typed.length == originalText.length && mismatchCount == 0) {
          highlighter.addHighlight(0, originalText.length, completionPainter)
          logger.info("Sample text completed without errors - highlighted in green")
        }
      } catch (t: Throwable) {
        logger.warn("Failed to update highlights", t)
      }
    }
  }
}
