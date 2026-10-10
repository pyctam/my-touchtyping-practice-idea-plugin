package com.github.pyctam.touchtypingpractice.config

/**
 * How practice text is generated.
 *
 * @property RANDOM legacy mode: random runs of letters drawn from the enabled keys.
 * @property EXACT_WORDS only real English words whose every character is typed with an enabled key.
 * @property ADAPTED_WORDS real English words with unavailable letters removed, ranked to favor
 *   recognizable, high-retention fragments.
 */
enum class GenerationMode {
  RANDOM,
  EXACT_WORDS,
  ADAPTED_WORDS
}
