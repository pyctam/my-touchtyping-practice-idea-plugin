package com.github.pyctam.touchtypingpractice.ui

import com.intellij.ui.components.JBLabel
import javax.swing.SwingUtilities

class ErrorCounter {
  private var count = 0
  var statusLabel: JBLabel? = null

  fun increment() {
    count++
    updateLabel()
  }

  fun getCount(): Int = count

  fun reset() {
    count = 0
    updateLabel()
  }

  fun setCount(newCount: Int) {
    count = newCount
    updateLabel()
  }

  private fun updateLabel() {
    SwingUtilities.invokeLater { statusLabel?.text = "Typing speed: 0 WPM | Typing errors: $count" }
  }
}
