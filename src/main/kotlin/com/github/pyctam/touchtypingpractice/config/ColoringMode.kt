package com.github.pyctam.touchtypingpractice.config

/**
 * How the sample text and the typed text are colored.
 *
 * @property NONE no coloring; every character keeps the default foreground color.
 * @property HAND left-hand keys in blue, right-hand keys in purple.
 * @property FINGER each finger of each hand has its own distinct color.
 */
enum class ColoringMode {
  NONE,
  HAND,
  FINGER
}
