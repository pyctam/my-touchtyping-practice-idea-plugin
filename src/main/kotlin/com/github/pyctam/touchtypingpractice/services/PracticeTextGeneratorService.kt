package com.github.pyctam.touchtypingpractice.services

import com.github.pyctam.touchtypingpractice.config.Settings
import com.github.pyctam.touchtypingpractice.generation.PracticeTextConfig
import com.github.pyctam.touchtypingpractice.generation.PracticeTextGenerator
import com.intellij.openapi.components.Service

/**
 * Application-level service that generates practice text for the current [Settings].
 *
 * This is a thin adapter over the pure [PracticeTextGenerator]: it reads the live settings, maps
 * them to a [PracticeTextConfig], and delegates. Keeping the actual logic in the generator keeps
 * this class trivial and the logic unit-testable without an IDE.
 */
@Service
class PracticeTextGeneratorService {

  /**
   * Generates a practice text for the current [Settings].
   *
   * @return a practice text string optimized for the current configuration.
   */
  fun generatePracticeText(): String {
    val settings = Settings.getInstance()
    val config =
      PracticeTextConfig(
        practiceMode = settings.practiceMode,
        selectedFingers = settings.selectedFingers,
        keyLimitPerFinger = settings.keyLimitPerFinger,
      )
    return PracticeTextGenerator().generate(config)
  }
}
