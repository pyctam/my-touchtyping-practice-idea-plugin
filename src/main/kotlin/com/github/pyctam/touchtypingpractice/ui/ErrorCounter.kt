package com.github.pyctam.touchtypingpractice.ui

import javax.swing.SwingUtilities
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

/**
 * Tracks the cumulative number of typing errors and notifies listeners of count changes.
 *
 * The count only grows: [increment] adds newly introduced errors, and corrections never decrease
 * it. The counter is reset to zero only when the sample text is reset (via [setCount]).
 *
 * Uses the listener pattern to decouple error tracking from UI updates, making the class more
 * testable and reusable.
 */
class ErrorCounter {
  private var count = 0
  private val changeListeners = mutableListOf<ChangeListener>()

  fun getCount(): Int = count

  /** Adds [delta] newly introduced errors to the cumulative count. */
  fun increment(delta: Int = 1) {
    count += delta
    notifyListeners()
  }

  /** Replaces the count (used to reset the counter to zero with a fresh sample text). */
  fun setCount(newCount: Int) {
    count = newCount
    notifyListeners()
  }

  fun addChangeListener(listener: ChangeListener) {
    changeListeners.add(listener)
  }

  private fun notifyListeners() {
    SwingUtilities.invokeLater {
      val event = ChangeEvent(this)
      changeListeners.forEach { it.stateChanged(event) }
    }
  }
}
