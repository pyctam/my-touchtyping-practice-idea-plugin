package com.github.pyctam.touchtypingpractice.ui

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [TypingStats]: the WPM/accuracy math (5-character convention, net WPM), the
 * session lifecycle (start on first insertion, freeze on completion, reset), and the zero initial
 * state.
 *
 * A fake clock (a mutable [Long]) is injected so the math is deterministic without real time.
 */
class TypingStatsTest {

  /** A controllable clock: starts at 0 and is advanced explicitly by the tests. */
  private class FakeClock {
    var now: Long = 0
  }

  private fun statsWith(clock: FakeClock) = TypingStats { clock.now }

  @Test
  fun zeroBeforeFirstInsertion() {
    val stats = TypingStats()

    assertEquals("WPM must be 0 before the first insertion", 0, stats.wpm)
    assertEquals("Accuracy must be 0 before the first insertion", 0, stats.accuracy)
  }

  @Test
  fun wpmUsesFiveCharacterConvention() {
    val clock = FakeClock()
    val stats = statsWith(clock)

    // 11 correct characters in 30 seconds: (11 / 5) / 0.5 = 4.4 -> 4 WPM.
    stats.recordInsertion(11)
    clock.now = 30_000

    assertEquals("Expected 4 WPM for 11 correct chars in 30s", 4, stats.wpm)
  }

  @Test
  fun wpmSubtractsErrors() {
    val clock = FakeClock()
    val stats = statsWith(clock)

    // 13 insertions, 2 errors -> 11 correct chars in 30s: (11 / 5) / 0.5 = 4.4 -> 4 WPM.
    stats.recordInsertion(13)
    stats.setErrors(2)
    clock.now = 30_000

    assertEquals("Expected 4 net WPM for 13 insertions with 2 errors in 30s", 4, stats.wpm)
  }

  @Test
  fun wpmIsZeroWhenNoTimeHasElapsed() {
    val clock = FakeClock()
    val stats = statsWith(clock)

    // 100 correct chars, but the clock has not advanced (0 elapsed) -> 0 WPM (no division blow-up).
    stats.recordInsertion(100)

    assertEquals("Expected 0 WPM when no time has elapsed", 0, stats.wpm)
  }

  @Test
  fun accuracyIsCorrectCharsOverInsertions() {
    val stats = TypingStats()

    // 13 insertions, 2 errors -> 11/13 = 84.6% -> 85%.
    stats.recordInsertion(13)
    stats.setErrors(2)

    assertEquals("Expected 85% accuracy for 11 correct of 13 insertions", 85, stats.accuracy)
  }

  @Test
  fun accuracyIs100WithoutErrors() {
    val stats = TypingStats()

    stats.recordInsertion(11)

    assertEquals("Expected 100% accuracy for a clean run", 100, stats.accuracy)
  }

  @Test
  fun completeFreezesValues() {
    val clock = FakeClock()
    val stats = statsWith(clock)

    stats.recordInsertion(11)
    clock.now = 30_000
    stats.complete()
    val wpmAtCompletion = stats.wpm
    val accuracyAtCompletion = stats.accuracy

    // Time keeps passing and (rejected) edits are reported: the values must stay frozen.
    clock.now = 120_000
    stats.recordInsertion(5)
    stats.setErrors(9)

    assertEquals("WPM must stay frozen after completion", wpmAtCompletion, stats.wpm)
    assertEquals("Accuracy must stay frozen after completion", accuracyAtCompletion, stats.accuracy)
  }

  @Test
  fun completeWithoutInsertionYieldsZero() {
    val clock = FakeClock()
    val stats = statsWith(clock)

    clock.now = 30_000
    stats.complete()

    assertEquals("WPM must be 0 when the session had no insertions", 0, stats.wpm)
    assertEquals("Accuracy must be 0 when the session had no insertions", 0, stats.accuracy)
  }

  @Test
  fun resetStartsANewSession() {
    val clock = FakeClock()
    val stats = statsWith(clock)

    stats.recordInsertion(11)
    stats.setErrors(2)
    clock.now = 30_000
    stats.complete()

    stats.reset()

    assertEquals("WPM must be 0 after reset", 0, stats.wpm)
    assertEquals("Accuracy must be 0 after reset", 0, stats.accuracy)

    // A new session starts on the next insertion, with a fresh clock.
    clock.now = 60_000
    stats.recordInsertion(10)
    clock.now = 120_000

    assertEquals("Expected 2 WPM for 10 correct chars in 1 min after reset", 2, stats.wpm)
    assertEquals("Expected 100% accuracy in the new session", 100, stats.accuracy)
  }

  @Test
  fun setErrorsWithoutInsertionDoesNotNotify() {
    val stats = TypingStats()
    var notifications = 0
    stats.addChangeListener { notifications++ }

    // No insertion yet: setting the same (zero) error count is a no-op.
    stats.setErrors(0)

    assertEquals("Expected no notification for an unchanged error count", 0, notifications)
  }

  @Test
  fun listenersAreNotifiedOnChange() {
    val stats = TypingStats()
    var notifications = 0
    stats.addChangeListener { notifications++ }

    stats.recordInsertion(5)
    stats.setErrors(1)
    stats.setErrors(1) // unchanged -> no notification
    stats.complete()

    assertEquals(
      "Expected notifications for insertion, error change, and completion",
      3,
      notifications
    )
  }
}
