package com.github.pyctam.touchtypingpractice.ui

import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent

/**
 * Detects the "press R [REQUIRED_PRESSES] times within [TIME_WINDOW_MS]" gesture and invokes
 * [onReset].
 *
 * Extracted from the tool window so the gesture logic lives in one place and is reused for both the
 * initial content and the reset flow (previously the listener was copy-pasted in two places).
 */
class ResetKeyDetector(
  private val onReset: () -> Unit,
  private val requiredPresses: Int = REQUIRED_PRESSES,
  private val timeWindowMs: Long = TIME_WINDOW_MS,
) : KeyAdapter() {

  private var pressCount = 0
  private var lastPressTime = 0L

  override fun keyPressed(e: KeyEvent?) {
    val keyChar = e?.keyChar ?: return
    if (keyChar != RESET_KEY_LOWER && keyChar != RESET_KEY_UPPER) return

    val now = System.currentTimeMillis()
    if (now - lastPressTime > timeWindowMs) {
      pressCount = 0
    }
    pressCount++
    lastPressTime = now
    if (pressCount >= requiredPresses) {
      pressCount = 0
      onReset()
      e.consume()
    }
  }

  private companion object {
    private const val RESET_KEY_LOWER = 'r'
    private const val RESET_KEY_UPPER = 'R'
    private const val REQUIRED_PRESSES = 3
    private const val TIME_WINDOW_MS = 3000L
  }
}
