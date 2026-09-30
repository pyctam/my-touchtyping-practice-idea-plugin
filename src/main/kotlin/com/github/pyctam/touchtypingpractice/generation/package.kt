/**
 * Pure, IDE-independent practice-text generation.
 *
 * [PracticeTextGenerator] builds the eligible character set from the [KeyboardLayout] and produces
 * a random practice string for a given [PracticeTextConfig]. All randomness is injectable, so the
 * generation rules are deterministic and unit-testable without an IntelliJ application container.
 */
package com.github.pyctam.touchtypingpractice.generation
