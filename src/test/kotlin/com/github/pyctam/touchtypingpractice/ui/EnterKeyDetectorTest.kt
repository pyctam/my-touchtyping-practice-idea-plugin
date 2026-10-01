package com.github.pyctam.touchtypingpractice.ui

import java.awt.Canvas
import java.awt.event.KeyEvent
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit tests for the [EnterKeyDetector] Enter-to-reset gesture. */
class EnterKeyDetectorTest {

  private fun enterEvent(): KeyEvent =
    KeyEvent(Canvas(), KeyEvent.KEY_PRESSED, 0L, 0, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED)

  private fun otherKeyEvent(): KeyEvent =
    KeyEvent(Canvas(), KeyEvent.KEY_PRESSED, 0L, 0, KeyEvent.VK_A, 'a')

  @Test
  fun enterWhenCompletedInvokesResetAndConsumesEvent() {
    var resetInvoked = false
    val detector = EnterKeyDetector({ resetInvoked = true }) { true }

    val event = enterEvent()
    detector.keyPressed(event)

    assertTrue("Expected reset to be invoked when the text is completed", resetInvoked)
    assertTrue("Expected the Enter event to be consumed", event.isConsumed)
  }

  @Test
  fun enterWhenIncompleteDoesNotInvokeResetButConsumesEvent() {
    var resetInvoked = false
    val detector = EnterKeyDetector({ resetInvoked = true }) { false }

    val event = enterEvent()
    detector.keyPressed(event)

    assertFalse("Expected reset not to be invoked while the text is incomplete", resetInvoked)
    assertTrue(
      "Expected the Enter event to be consumed so no newline is inserted",
      event.isConsumed
    )
  }

  @Test
  fun nonEnterKeyIsIgnoredWhenCompleted() {
    var resetInvoked = false
    val detector = EnterKeyDetector({ resetInvoked = true }) { true }

    val event = otherKeyEvent()
    detector.keyPressed(event)

    assertFalse("Expected reset not to be invoked for a non-Enter key", resetInvoked)
    assertFalse("Expected a non-Enter key to be left unconsumed", event.isConsumed)
  }

  @Test
  fun nonEnterKeyIsIgnoredWhenIncomplete() {
    var resetInvoked = false
    val detector = EnterKeyDetector({ resetInvoked = true }) { false }

    val event = otherKeyEvent()
    detector.keyPressed(event)

    assertFalse("Expected reset not to be invoked for a non-Enter key", resetInvoked)
    assertFalse("Expected a non-Enter key to be left unconsumed", event.isConsumed)
  }

  @Test
  fun nullKeyEventIsIgnored() {
    var resetInvoked = false
    val detector = EnterKeyDetector({ resetInvoked = true }) { true }

    detector.keyPressed(null)

    assertFalse("Expected reset not to be invoked for a null event", resetInvoked)
  }

  @Test
  fun completionPredicateIsEvaluatedAtKeyPressTime() {
    var completed = false
    var resetInvoked = false
    val detector = EnterKeyDetector({ resetInvoked = true }) { completed }

    val event = enterEvent()
    detector.keyPressed(event)
    assertFalse("Expected no reset before completion", resetInvoked)

    completed = true
    detector.keyPressed(event)
    assertTrue("Expected reset after completion", resetInvoked)
  }
}
