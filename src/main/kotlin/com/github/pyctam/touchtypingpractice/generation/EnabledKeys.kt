package com.github.pyctam.touchtypingpractice.generation

import com.github.pyctam.touchtypingpractice.config.Finger

/**
 * Computes the keys enabled by a [PracticeTextConfig]: the concatenation of the keys for every
 * selected finger, in [Finger] declaration order, limited by the per-finger key limit and the
 * practice mode (see [KeyboardLayout]).
 *
 * Shared by the random-letter [PracticeTextGenerator] and the word-based [WordExerciseGenerator] so
 * both derive the eligible keys from the same source.
 */
fun enabledKeysFor(config: PracticeTextConfig): String {
  val sb = StringBuilder()
  for (finger in Finger.entries) {
    if (Finger.isFingerSelected(config.selectedFingers, finger)) {
      sb.append(
        KeyboardLayout.keysFor(config.practiceMode, finger, config.keyLimitPerFinger)
          .joinToString("")
      )
    }
  }
  return sb.toString()
}
