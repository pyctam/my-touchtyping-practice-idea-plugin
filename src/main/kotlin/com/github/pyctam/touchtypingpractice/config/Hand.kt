package com.github.pyctam.touchtypingpractice.config

/**
 * A hand, used to key per-finger colors and to select one hand's keys in the [PracticeMode] space.
 */
enum class Hand {
  LEFT,
  RIGHT;

  /** The [PracticeMode] that selects only this hand's keys. */
  fun toPracticeMode(): PracticeMode =
    when (this) {
      LEFT -> PracticeMode.LEFT_HAND
      RIGHT -> PracticeMode.RIGHT_HAND
    }
}
