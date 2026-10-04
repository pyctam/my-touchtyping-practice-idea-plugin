package com.github.pyctam.touchtypingpractice.ui

import javax.swing.JTextPane
import javax.swing.SwingUtilities
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for the completion callback ([TouchTypingDocumentListener] `onCompletionChanged`) that
 * drives the "Hit Enter to reset" hint visibility.
 *
 * The listener is a pure Swing component (no IDE services), so it can be exercised directly: type
 * into the [JTextPane], flush the EDT (the listener posts its work via
 * [SwingUtilities.invokeLater]), and assert the callback fired with the expected completion state.
 */
class TouchTypingDocumentListenerTest {

  private val originalText = "hello world"

  /** Blocks until the EDT has processed the listener's posted highlight update. */
  private fun flushEdt() {
    SwingUtilities.invokeAndWait {}
  }

  /**
   * Builds a typing area wired to a listener that records completion states via [callback] and
   * updates [errorCounter].
   */
  private fun createTypingArea(
    errorCounter: ErrorCounter = ErrorCounter(),
    callback: (Boolean) -> Unit = {}
  ): JTextPane {
    val typingArea = JTextPane()
    val referenceTextPane = JTextPane()
    referenceTextPane.text = originalText
    val listener =
      TouchTypingDocumentListener(
        typingArea,
        referenceTextPane,
        originalText,
        errorCounter,
        callback
      )
    typingArea.document.addDocumentListener(listener)
    return typingArea
  }

  @Test
  fun completionFiresTrueWhenTextFullyTyped() {
    val states = mutableListOf<Boolean>()
    val typingArea = createTypingArea { states.add(it) }

    typingArea.text = originalText
    flushEdt()

    assertEquals("Expected a single completion=true callback", listOf(true), states)
  }

  @Test
  fun incompleteTextFromInitialStateDoesNotFire() {
    // The hint is hidden by default (initial completion state is false), so typing incomplete text
    // produces no transition and the callback must not fire.
    val states = mutableListOf<Boolean>()
    val typingArea = createTypingArea { states.add(it) }

    typingArea.text = "hello"
    flushEdt()

    assertEquals(
      "Expected no callback for incomplete text from the initial state",
      emptyList<Boolean>(),
      states
    )
  }

  @Test
  fun callbackFiresOnlyOnStateTransitions() {
    val states = mutableListOf<Boolean>()
    val typingArea = createTypingArea { states.add(it) }

    // Two incomplete edits: both stay in the initial (false) state, so nothing fires.
    typingArea.text = "he"
    flushEdt()
    typingArea.text = "hel"
    flushEdt()

    assertEquals(
      "Expected the callback to fire only on transitions, not for repeated incomplete states",
      emptyList<Boolean>(),
      states
    )
  }

  @Test
  fun completionThenCorrectionFiresTrueThenFalse() {
    val states = mutableListOf<Boolean>()
    val typingArea = createTypingArea { states.add(it) }

    typingArea.text = originalText
    flushEdt()
    // Remove one character -> incomplete again.
    typingArea.text = originalText.dropLast(1)
    flushEdt()

    assertEquals(
      "Expected true on completion then false after correction",
      listOf(true, false),
      states
    )
  }

  @Test
  fun mismatchAtSameLengthFromInitialStateDoesNotFire() {
    // Same length as the original but a wrong character -> not completed. From the initial (false)
    // state this is not a transition, so the callback must not fire.
    val states = mutableListOf<Boolean>()
    val typingArea = createTypingArea(ErrorCounter()) { states.add(it) }

    typingArea.text = originalText.replace("hello", "hxllo")
    flushEdt()

    assertEquals(
      "Expected no callback for a same-length mismatch from the initial state",
      emptyList<Boolean>(),
      states
    )
  }

  // --- Cumulative error counter tests ---
  //
  // Note: the TextLengthLimiterFilter blocks insertions/replacements while the document has
  // errors (the user must correct before continuing), so the tests simulate realistic
  // character-by-character input: insertions via replaceRange, backspaces via document.remove
  // (the filter always allows removals, mirroring the real BackSpace action).

  @Test
  fun counterIncrementsOnWrongCharacter() {
    val errorCounter = ErrorCounter()
    val typingArea = createTypingArea(errorCounter)

    // Type "h" then a wrong "x" (expected "e"): one mismatch appears.
    typingArea.replaceRange("hx", 0, 0)
    flushEdt()

    assertEquals("Expected one error after a wrong character", 1, errorCounter.getCount())
  }

  @Test
  fun counterStaysUnchangedAfterCorrection() {
    val errorCounter = ErrorCounter()
    val typingArea = createTypingArea(errorCounter)

    // Introduce one error: type "hx" instead of "he".
    typingArea.replaceRange("hx", 0, 0)
    flushEdt()
    // Correct it: backspace the "x" (delete the char at index 1), then type "e".
    typingArea.document.remove(1, 1)
    flushEdt()
    typingArea.replaceRange("e", 1, 1)
    flushEdt()

    assertEquals(
      "Expected the error to stay counted after the correction",
      1,
      errorCounter.getCount()
    )
  }

  @Test
  fun counterAccumulatesAcrossMultipleErrors() {
    val errorCounter = ErrorCounter()
    val typingArea = createTypingArea(errorCounter)

    // First error: type "hx" instead of "he", then correct it.
    typingArea.replaceRange("hx", 0, 0)
    flushEdt()
    typingArea.document.remove(1, 1)
    flushEdt()
    typingArea.replaceRange("e", 1, 1)
    flushEdt()
    assertEquals("Expected one error after the first mistake", 1, errorCounter.getCount())

    // Continue correctly up to "hello wor".
    typingArea.replaceRange("llo wor", 2, 2)
    flushEdt()

    // Second error: type "k" instead of "l", then correct it and finish the text.
    typingArea.replaceRange("k", 9, 9)
    flushEdt()
    typingArea.document.remove(9, 1)
    flushEdt()
    typingArea.replaceRange("ld", 9, 9)
    flushEdt()

    assertEquals("Expected the count to accumulate across corrections", 2, errorCounter.getCount())
  }
}
