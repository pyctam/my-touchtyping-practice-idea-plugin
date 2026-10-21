/**
 * Pure, IDE-independent practice-text generation.
 *
 * [PracticeTextGenerator] builds the eligible character set from the [KeyboardLayout] and produces
 * a random practice string (random-letter mode) for a given [PracticeTextConfig].
 * [WordExerciseGenerator] produces word-based practice text (exact and adapted English words) from
 * the bundled [WordList], adapting each entry to the enabled keys via [adaptWord]. [enabledKeysFor]
 * is the shared source of the eligible keys for both generators. All randomness is injectable, so
 * the generation rules are deterministic and unit-testable without an IntelliJ application
 * container.
 */
package com.github.pyctam.touchtypingpractice.generation
