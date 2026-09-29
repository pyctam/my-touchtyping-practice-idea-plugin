package com.github.pyctam.touchtypingpractice.config

/**
 * Which hand(s) to practice.
 *
 * @property LEFT_HAND only left-hand keys.
 * @property RIGHT_HAND only right-hand keys.
 * @property BOTH_HANDS keys from both hands.
 */
enum class PracticeMode {
  LEFT_HAND,
  RIGHT_HAND,
  BOTH_HANDS
}
