package com.github.pyctam.touchtypingpractice.ui

import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import kotlin.math.roundToInt

/**
 * Tracks typing speed (WPM) and accuracy for a single practice session.
 *
 * A session starts on the first insertion ([recordInsertion]) and ends when the practice text is
 * completed ([complete]). Completion is a terminal state: the clock stops, the values freeze, and
 * further [recordInsertion]/[setErrors] calls are ignored until [reset] starts a new session (a
 * fresh practice text).
 *
 * WPM uses the standard 5-character convention (one "word" = 5 characters, including spaces) and
 * counts only correct characters: `net WPM = (insertions - errors) / 5 / minutes`. Accuracy is
 * `(insertions - errors) / insertions * 100`. Both values are 0 before the first insertion.
 *
 * The clock is injectable so the math is unit-testable without real time.
 */
class TypingStats(private val clock: () -> Long = { System.currentTimeMillis() }) {

  private var insertions = 0
  private var errors = 0
  private var startTimeMs: Long? = null
  private var endTimeMs: Long? = null
  private val changeListeners = mutableListOf<ChangeListener>()

  /**
   * Net words per minute, or 0 before the first insertion. While the session is active the current
   * clock time is used as the end; after [complete] the frozen end time is used.
   */
  val wpm: Int
    get() {
      val start = startTimeMs ?: return 0
      val end = endTimeMs ?: clock()
      val minutes = (end - start) / 60_000.0
      if (minutes <= 0.0) return 0
      val correctChars = (insertions - errors).coerceAtLeast(0)
      return ((correctChars / 5.0) / minutes).roundToInt()
    }

  /** Accuracy percentage (0–100), or 0 before the first insertion. */
  val accuracy: Int
    get() {
      if (insertions == 0) return 0
      val correctChars = (insertions - errors).coerceAtLeast(0)
      return (correctChars * 100.0 / insertions).roundToInt()
    }

  /** Records [count] inserted characters; starts the session clock on the first call. */
  fun recordInsertion(count: Int) {
    if (endTimeMs != null) return // terminal state
    if (startTimeMs == null) startTimeMs = clock()
    insertions += count
    notifyListeners()
  }

  /** Updates the cumulative error count for the current session. */
  fun setErrors(count: Int) {
    if (endTimeMs != null) return // terminal state
    if (count == errors) return
    errors = count
    notifyListeners()
  }

  /** Ends the session (the text was completed); freezes the clock and the values. */
  fun complete() {
    if (endTimeMs != null) return
    if (startTimeMs == null) startTimeMs = clock()
    endTimeMs = clock()
    notifyListeners()
  }

  /** Starts a new session (a fresh practice text). */
  fun reset() {
    insertions = 0
    errors = 0
    startTimeMs = null
    endTimeMs = null
    notifyListeners()
  }

  fun addChangeListener(listener: ChangeListener) {
    changeListeners.add(listener)
  }

  private fun notifyListeners() {
    val event = ChangeEvent(this)
    changeListeners.forEach { it.stateChanged(event) }
  }
}
