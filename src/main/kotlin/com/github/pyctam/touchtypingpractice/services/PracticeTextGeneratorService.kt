package com.github.pyctam.touchtypingpractice.services

import com.github.pyctam.touchtypingpractice.config.GenerationMode
import com.github.pyctam.touchtypingpractice.config.Settings
import com.github.pyctam.touchtypingpractice.generation.PracticeTextConfig
import com.github.pyctam.touchtypingpractice.generation.PracticeTextGenerator
import com.github.pyctam.touchtypingpractice.generation.WordExerciseGenerator
import com.github.pyctam.touchtypingpractice.generation.WordList
import com.github.pyctam.touchtypingpractice.generation.WordListException
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger

/**
 * Application-level service that generates practice text for the current [Settings].
 *
 * This is a thin adapter over the pure generators: it reads the live settings, maps them to a
 * [PracticeTextConfig], and delegates to the [PracticeTextGenerator] (random letters) or the
 * [WordExerciseGenerator] (exact/adapted English words) based on the configured [GenerationMode].
 * Keeping the actual logic in the generators keeps this class trivial and the logic unit-testable
 * without an IDE.
 */
@Service
class PracticeTextGeneratorService {

  private val logger = Logger.getInstance(PracticeTextGeneratorService::class.java)

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
        generationMode = settings.generationMode,
      )
    return generateFor(config)
  }

  /**
   * Generates a practice text for [config], dispatching on [config.generationMode].
   *
   * If the bundled word list is missing or unreadable, word-based modes fall back to the
   * random-letter generator so the tool window keeps working.
   */
  fun generateFor(config: PracticeTextConfig): String {
    if (config.generationMode == GenerationMode.RANDOM) {
      return PracticeTextGenerator().generate(config)
    }
    return try {
      WordExerciseGenerator(WordList.load()).generate(config)
    } catch (e: WordListException) {
      logger.error("Falling back to random-letter generation", e)
      PracticeTextGenerator().generate(config)
    }
  }
}
