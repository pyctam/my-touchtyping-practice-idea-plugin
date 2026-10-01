package com.github.pyctam.touchtypingpractice.ui

import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent

/**
 * Detects the Enter key press on the typing area and invokes [onReset] when the practice text has
 * been completed (i.e. [isCompleted] returns true).
 *
 * The Enter key is always consumed so that no newline is inserted into the typing area: while the
 * text is incomplete a newline would create a mismatch and block further input, and after
 * completion the input is locked anyway.
 *
 * @param onReset invoked when Enter is pressed and the text is completed.
 * @param isCompleted predicate reporting whether the typed text matches the reference text.
 */
class EnterKeyDetector(
  private val onReset: () -> Unit,
  private val isCompleted: () -> Boolean,
) : KeyAdapter() {

  override fun keyPressed(e: KeyEvent?) {
    if (e?.keyCode != KeyEvent.VK_ENTER) return
    e.consume()
    if (isCompleted()) {
      onReset()
    }
  }
}
