package com.github.pyctam.touchtypingpractice.generation

import com.github.pyctam.touchtypingpractice.config.Finger
import com.github.pyctam.touchtypingpractice.config.GenerationMode
import com.github.pyctam.touchtypingpractice.config.PracticeMode

/**
 * Immutable snapshot of the settings that drive practice-text generation.
 *
 * Decoupling these values from the IDE-specific
 * [com.github.pyctam.touchtypingpractice.config.Settings] lets the pure [PracticeTextGenerator] be
 * unit-tested without an application container.
 *
 * @property practiceMode which hand(s) to draw keys from.
 * @property selectedFingers bitmask of enabled fingers, see [Finger].
 * @property keyLimitPerFinger maximum number of keys taken per finger (per hand).
 * @property generationMode how the practice text is generated (random letters, exact words, or
 *   adapted words).
 */
data class PracticeTextConfig(
  val practiceMode: PracticeMode,
  val selectedFingers: Int,
  val keyLimitPerFinger: Int,
  val generationMode: GenerationMode = GenerationMode.ADAPTED_WORDS,
)
