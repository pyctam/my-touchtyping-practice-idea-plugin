package com.github.pyctam.touchtypingpractice.ui

import com.intellij.openapi.diagnostic.Logger
import com.intellij.ui.JBColor
import javax.swing.JTextArea
import javax.swing.JTextPane
import javax.swing.SwingUtilities
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.AbstractDocument
import javax.swing.text.AttributeSet
import javax.swing.text.DefaultHighlighter
import javax.swing.text.DocumentFilter

class TouchTypingDocumentListener(
  private val typingArea: JTextArea,
  private val referenceTextPane: JTextPane,
  private val originalText: String,
  private val errorCounter: ErrorCounter
) : DocumentListener {
  private val logger: Logger = Logger.getInstance(TouchTypingDocumentListener::class.java)
  private val mismatchPainter =
    DefaultHighlighter.DefaultHighlightPainter(JBColor(0xFFCCCC, 0xFFCCCC))
  // Light green background (theme-aware) shown on the sample text when typing is completed
  // without errors
  private val completionPainter =
    DefaultHighlighter.DefaultHighlightPainter(JBColor(0xC8E6C9, 0x2E5233))

  init {
    // Install a DocumentFilter to prevent typing beyond the original text length
    val doc = typingArea.document
    if (doc is AbstractDocument) {
      doc.documentFilter = TextLengthLimiterFilter(originalText.length)
      logger.info("TextLengthLimiterFilter installed with maxLength=${originalText.length}")
    }
  }

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
        var mismatchCount = 0

        for (i in 0 until compareLen) {
          if (typed[i] != originalText[i]) {
            // highlight single character at position i
            highlighter.addHighlight(i, i + 1, mismatchPainter)
            mismatchCount++
          }
        }

        // Update error counter with the current mismatch count (cumulative, not decreasing)
        errorCounter.setCount(mismatchCount)

        // Highlight the entire sample text with a light green background when the user has
        // typed the full text without any errors, so they can see the text is completed
        if (typed.length == originalText.length && mismatchCount == 0) {
          highlighter.addHighlight(0, originalText.length, completionPainter)
          logger.info("Sample text completed without errors - highlighted in green")
        }
      } catch (t: Throwable) {
        logger.warn("Failed to update highlights", t)
      }
    }
  }

  /**
   * DocumentFilter that prevents typing beyond the maximum text length. Any attempt to insert text
   * that would exceed the limit is silently ignored.
   */
  private inner class TextLengthLimiterFilter(private val maxLength: Int) : DocumentFilter() {
    override fun insertString(fb: FilterBypass, offset: Int, string: String, attr: AttributeSet?) {
      val currentText = fb.document.getText(0, fb.document.length)
      if (hasErrors(currentText)) {
        logger.info("Rejected insertion - errors exist")
        return
      }

      val currentLength = fb.document.length
      val newLength = currentLength + string.length

      logger.info(
        "insertString: currentLength=$currentLength, stringLength=${string.length}, newLength=$newLength, maxLength=$maxLength"
      )

      // Only allow insertion if it doesn't exceed the max length
      if (newLength <= maxLength) {
        super.insertString(fb, offset, string, attr)
        logger.info("insertString: Allowed insertion")
      } else {
        logger.info("insertString: Rejected insertion - would exceed max length")
      }
    }

    override fun replace(
      fb: FilterBypass,
      offset: Int,
      length: Int,
      text: String,
      attrs: AttributeSet?
    ) {
      val currentText = fb.document.getText(0, fb.document.length)
      if (hasErrors(currentText)) {
        logger.info("Rejected replacement - errors exist")
        return
      }

      val currentLength = fb.document.length
      val newLength = currentLength - length + text.length

      logger.info(
        "replace: currentLength=$currentLength, replaceLength=$length, textLength=${text.length}, newLength=$newLength, maxLength=$maxLength"
      )

      // Only allow replace if it doesn't exceed the max length
      if (newLength <= maxLength) {
        super.replace(fb, offset, length, text, attrs)
        logger.info("replace: Allowed replacement")
      } else {
        logger.info("replace: Rejected replacement - would exceed max length")
      }
    }

    override fun remove(fb: FilterBypass, offset: Int, length: Int) {
      logger.info("remove: offset=$offset, length=$length")
      // Always allow removals (delete/backspace)
      super.remove(fb, offset, length)
    }
  }

  private fun hasErrors(typed: String): Boolean {
    val compareLen = minOf(typed.length, originalText.length)
    for (i in 0 until compareLen) {
      if (typed[i] != originalText[i]) return true
    }
    return false
  }
}
