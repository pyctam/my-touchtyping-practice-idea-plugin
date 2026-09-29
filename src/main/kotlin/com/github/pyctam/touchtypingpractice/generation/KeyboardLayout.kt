package com.github.pyctam.touchtypingpractice.generation

import com.github.pyctam.touchtypingpractice.config.Finger
import com.github.pyctam.touchtypingpractice.config.PracticeMode
import com.github.pyctam.touchtypingpractice.config.PracticeMode.BOTH_HANDS
import com.github.pyctam.touchtypingpractice.config.PracticeMode.LEFT_HAND
import com.github.pyctam.touchtypingpractice.config.PracticeMode.RIGHT_HAND

/**
 * Static QWERTY keyboard layout, organized by hand and finger.
 *
 * For each finger the keys are ordered by "progression": the home-row key first, then the
 * extensions. This ordering is what makes the per-finger key limit meaningful — a low limit keeps
 * the practice close to the home row, a high limit reaches further.
 *
 * The layout is pure data plus a single lookup, so it is trivially testable and has no IDE
 * dependency.
 */
object KeyboardLayout {

  /** Left-hand keys, home row first. */
  private val LEFT_HAND_KEYS: Map<Finger, List<String>> =
    mapOf(
      Finger.THUMB to listOf(" "),
      Finger.INDEX to listOf("f", "g", "v", "b", "t", "r", "4", "5"),
      Finger.MIDDLE to listOf("d", "c", "e", "3"),
      Finger.RING to listOf("s", "x", "w", "2"),
      Finger.LITTLE to listOf("a", "z", "q", "`", "1", "tab", "capslock", "shift"),
    )

  /** Right-hand keys, home row first. */
  private val RIGHT_HAND_KEYS: Map<Finger, List<String>> =
    mapOf(
      Finger.THUMB to listOf(" "),
      Finger.INDEX to listOf("j", "h", "n", "m", "u", "y", "6", "7"),
      Finger.MIDDLE to listOf("k", ",", "i", "8"),
      Finger.RING to listOf("l", ".", "o", "9"),
      Finger.LITTLE to listOf(";", "'", "/", "p", "[", "]", "\\", "-", "=", "0", "enter", "shift"),
    )

  /**
   * Returns the keys available for [finger] under [mode], limited to [keyLimit] keys per hand.
   *
   * In [BOTH_HANDS] mode the limit is applied independently to each hand, so up to `2 * keyLimit`
   * keys may be returned (left-hand keys first, then right-hand keys).
   */
  fun keysFor(mode: PracticeMode, finger: Finger, keyLimit: Int): List<String> {
    return when (mode) {
      LEFT_HAND -> LEFT_HAND_KEYS[finger].orEmpty().take(keyLimit)
      RIGHT_HAND -> RIGHT_HAND_KEYS[finger].orEmpty().take(keyLimit)
      BOTH_HANDS ->
        LEFT_HAND_KEYS[finger].orEmpty().take(keyLimit) +
          RIGHT_HAND_KEYS[finger].orEmpty().take(keyLimit)
    }
  }
}
