package com.github.pyctam.touchtypingpractice.ui

import javax.swing.JTextArea
import javax.swing.JTextPane
import javax.swing.SwingUtilities
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for the completion callback ([TouchTypingDocumentListener] `onCompletionChanged`) that
 * drives the "Hit Enter to reset" hint visibility.
 *
 * The listener is a pure Swing component (no IDE services), so it can be exercised directly: type
 * into the [JTextArea], flush the EDT (the listener posts its work via
 * [SwingUtilities.invokeLater]), and assert the callback fired with the expected completion state.
 */
class TouchTypingDocumentListenerTest {

  private val originalText = "hello world"

  /** Blocks until the EDT has processed the listener's posted highlight update. */
  private fun flushEdt() {
    SwingUtilities.invokeAndWait {}
  }

  /** Builds a typing area wired to a listener that records completion states via [callback]. */
  private fun createTypingArea(callback: (Boolean) -> Unit): JTextArea {
    val typingArea = JTextArea()
    val referenceTextPane = JTextPane()
    referenceTextPane.text = originalText
    val listener =
      TouchTypingDocumentListener(
        typingArea,
        referenceTextPane,
        originalText,
        ErrorCounter(),
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
    val typingArea = createTypingArea { states.add(it) }

    typingArea.text = originalText.replace("hello", "hxllo")
    flushEdt()

    assertEquals(
      "Expected no callback for a same-length mismatch from the initial state",
      emptyList<Boolean>(),
      states
    )
  }
}
