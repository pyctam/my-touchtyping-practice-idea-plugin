package com.github.pyctam.touchtypingpractice.ui

import javax.swing.SwingUtilities
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

/**
 * Tracks typing errors and notifies listeners of count changes.
 *
 * Uses the listener pattern to decouple error tracking from UI updates, making the class more
 * testable and reusable.
 */
class ErrorCounter {
  private var count = 0
  private val changeListeners = mutableListOf<ChangeListener>()

  @Suppress("unused")
  fun increment() {
    count++
    notifyListeners()
  }

  fun getCount(): Int = count

  @Suppress("unused")
  fun reset() {
    count = 0
    notifyListeners()
  }

  fun setCount(newCount: Int) {
    count = newCount
    notifyListeners()
  }

  fun addChangeListener(listener: ChangeListener) {
    changeListeners.add(listener)
  }

  @Suppress("unused")
  fun removeChangeListener(listener: ChangeListener) {
    changeListeners.remove(listener)
  }

  private fun notifyListeners() {
    SwingUtilities.invokeLater {
      val event = ChangeEvent(this)
      changeListeners.forEach { it.stateChanged(event) }
    }
  }
}
