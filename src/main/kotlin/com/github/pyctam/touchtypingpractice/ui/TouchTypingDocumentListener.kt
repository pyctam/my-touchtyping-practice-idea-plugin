package com.github.pyctam.touchtypingpractice.ui

import com.intellij.openapi.diagnostic.Logger
import com.intellij.ui.JBColor
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
 * the typed text, increments the [errorCounter] by the number of newly introduced mismatches (the
 * count is cumulative and never decreases on corrections), and highlights the whole reference text
 * in green when it is completed with zero errors.
 *
 * Each character in the typing area is colored by hand (blue for left-hand keys, purple for
 * right-hand keys) via [HandColors.applyTo], matching the reference pane.
 *
 * A [TextLengthLimiterFilter] is installed on the typing area to prevent typing beyond the
 * reference length, to block new input while errors are unresolved, and to make completion a
 * terminal state (no edits until the text is reset).
 *
 * Insertions are reported to [stats] (typing speed / accuracy), which ends the session when the
 * text is completed.
 *
 * @param typingArea the editable area the user types into.
 * @param referenceTextPane the non-editable pane showing the text to type.
 * @param originalText the reference text being typed.
 * @param errorCounter the cumulative counter incremented by newly introduced mismatches.
 * @param stats the session typing-speed/accuracy tracker fed by insertions and errors.
 * @param onCompletionChanged callback that fires when completion state changes.
 */
class TouchTypingDocumentListener(
  private val typingArea: JTextPane,
  private val referenceTextPane: JTextPane,
  private val originalText: String,
  private val errorCounter: ErrorCounter,
  private val stats: TypingStats,
  private val onCompletionChanged: (Boolean) -> Unit = {},
) : DocumentListener {

  private val logger = Logger.getInstance(TouchTypingDocumentListener::class.java)

  /** Last reported completion state, so [onCompletionChanged] fires only on transitions. */
  private var lastCompletionState = false

  /** Last observed mismatch count, so only newly introduced errors increment the counter. */
  private var lastMismatchCount = 0

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
    // Report the inserted characters to the session stats (starts the clock on the first one).
    stats.recordInsertion(event?.length ?: 0)
    updateHighlights()
  }

  override fun removeUpdate(event: DocumentEvent?) {
    updateHighlights()
  }

  override fun changedUpdate(event: DocumentEvent?) {
    // Not needed for plain text.
  }

  /** Recomputes the mismatch highlights, hand colors, and error count on the EDT. */
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

        // Cumulative counting: only newly introduced mismatches are added; corrections (the
        // mismatch count dropping) never decrease the counter.
        if (mismatchCount > lastMismatchCount) {
          errorCounter.increment(mismatchCount - lastMismatchCount)
        }
        lastMismatchCount = mismatchCount
        stats.setErrors(errorCounter.getCount())

        // Highlight the whole sample text green when it is completed without errors.
        val completed = typed.length == originalText.length && mismatchCount == 0
        if (completed) {
          highlighter.addHighlight(0, originalText.length, completionPainter)
          logger.info("Sample text completed without errors - highlighted in green")
        }
        if (completed != lastCompletionState) {
          lastCompletionState = completed
          onCompletionChanged(completed)
          // Completion is a terminal state: end the session and freeze the WPM/accuracy.
          if (completed) stats.complete()
        }

        // Apply per-hand colors to the typed text (blue for left-hand, purple for right-hand).
        HandColors.applyTo(typingArea, typed)
      } catch (t: Throwable) {
        logger.warn("Failed to update highlights", t)
      }
    }
  }
}
