package com.github.pyctam.touchtypingpractice.generation

import com.github.pyctam.touchtypingpractice.config.Finger
import com.github.pyctam.touchtypingpractice.config.GenerationMode
import com.github.pyctam.touchtypingpractice.config.PracticeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [WordList] loading and normalization.
 *
 * These tests run without an IDE: the bundled resource is on the test classpath and the
 * normalization rules are exercised through [WordList.fromLines].
 */
class WordListTest {

  @Test
  fun bundledWordListResourceLoads() {
    val wordList = WordList.load()
    assertTrue("Expected the bundled word list to be non-empty", wordList.size > 0)
    // The bundled Google 10,000 English Words list has 10,000 unique words.
    assertEquals(10_000, wordList.size)
  }

  @Test
  fun bundledWordListIsOrderedByFrequency() {
    // The source list is ordered by frequency (most common first); the first word is "the".
    val wordList = WordList.load()
    assertEquals("the", wordList.words.first().word)
    assertEquals(0, wordList.words.first().index)
  }

  @Test
  fun bundledWordListEntriesAreNormalizedAndUnique() {
    val wordList = WordList.load()
    val seen = HashSet<String>()
    wordList.words.forEachIndexed { position, entry ->
      assertTrue(
        "Expected lowercase letters only, got '${entry.word}'",
        entry.word.all { it in 'a'..'z' }
      )
      assertTrue("Expected unique words, duplicate '${entry.word}'", seen.add(entry.word))
      assertEquals("Expected index to match list position", position, entry.index)
    }
  }

  @Test
  fun letterMaskReflectsWordLetters() {
    assertEquals(1 shl ('f' - 'a'), WordList.letterMask("f"))
    // "flash" = f, l, a, s, h (note the 'a').
    val expected = listOf('f', 'l', 'a', 's', 'h').fold(0) { m, c -> m or (1 shl (c - 'a')) }
    assertEquals(expected, WordList.letterMask("flash"))
  }

  @Test(expected = WordListException::class)
  fun missingResourceThrowsActionableError() {
    WordList.loadLines("/does-not-exist-english.txt")
  }

  @Test
  fun fromLinesSkipsBlankLines() {
    val wordList = WordList.fromLines(listOf("the", "", "   ", "of"))
    assertEquals(listOf("the", "of"), wordList.words.map { it.word })
  }

  @Test
  fun fromLinesRemovesDuplicatesKeepingFirstOccurrence() {
    val wordList = WordList.fromLines(listOf("the", "of", "the", "and"))
    assertEquals(listOf("the", "of", "and"), wordList.words.map { it.word })
    assertEquals(0, wordList.words[0].index)
    assertEquals(1, wordList.words[1].index)
    assertEquals(2, wordList.words[2].index)
  }

  @Test
  fun fromLinesRejectsInvalidEntries() {
    // Digits, punctuation, and mixed-case-with-digits entries are not pure letter words.
    val wordList = WordList.fromLines(listOf("the", "123", "don't", "café", "ok2", "of"))
    assertEquals(listOf("the", "of"), wordList.words.map { it.word })
  }

  @Test
  fun fromLinesNormalizesCaseAndWhitespace() {
    val wordList = WordList.fromLines(listOf("  The  ", "THE", "of"))
    assertEquals(listOf("the", "of"), wordList.words.map { it.word })
  }

  @Test
  fun fromLinesEmptyInputYieldsEmptyList() {
    assertEquals(0, WordList.fromLines(emptyList()).size)
    assertEquals(0, WordList.fromLines(listOf("", "   ")).size)
  }

  @Test
  fun loadIsCachedAcrossCalls() {
    assertTrue(
      "Expected the same cached instance on repeated loads",
      WordList.load() === WordList.load()
    )
  }

  @Test
  fun enabledLettersDeriveFromKeyboardLayout() {
    // Both hands, index fingers, key limit 2 -> f, g (left) + j, h (right).
    val config =
      PracticeTextConfig(
        practiceMode = PracticeMode.BOTH_HANDS,
        selectedFingers = Finger.encodeSelectedFingers(0, Finger.INDEX),
        keyLimitPerFinger = 2,
        generationMode = GenerationMode.ADAPTED_WORDS,
      )
    assertEquals(setOf('f', 'g', 'j', 'h'), WordExerciseGenerator.enabledLetters(config))
  }

  @Test
  fun enabledLettersLeftHandOnly() {
    val config =
      PracticeTextConfig(
        practiceMode = PracticeMode.LEFT_HAND,
        selectedFingers = Finger.encodeSelectedFingers(0, Finger.INDEX),
        keyLimitPerFinger = 2,
        generationMode = GenerationMode.ADAPTED_WORDS,
      )
    assertEquals(setOf('f', 'g'), WordExerciseGenerator.enabledLetters(config))
  }

  @Test
  fun enabledLettersRightHandOnly() {
    val config =
      PracticeTextConfig(
        practiceMode = PracticeMode.RIGHT_HAND,
        selectedFingers = Finger.encodeSelectedFingers(0, Finger.INDEX),
        keyLimitPerFinger = 2,
        generationMode = GenerationMode.ADAPTED_WORDS,
      )
    assertEquals(setOf('j', 'h'), WordExerciseGenerator.enabledLetters(config))
  }

  @Test
  fun enabledLettersSingleFingerSingleKey() {
    val config =
      PracticeTextConfig(
        practiceMode = PracticeMode.LEFT_HAND,
        selectedFingers = Finger.encodeSelectedFingers(0, Finger.INDEX),
        keyLimitPerFinger = 1,
        generationMode = GenerationMode.ADAPTED_WORDS,
      )
    assertEquals(setOf('f'), WordExerciseGenerator.enabledLetters(config))
  }

  @Test
  fun enabledLettersMultipleFingers() {
    // Both hands, index + middle fingers, key limit 1 -> f (left index), j (right index),
    // d (left middle), k (right middle).
    val config =
      PracticeTextConfig(
        practiceMode = PracticeMode.BOTH_HANDS,
        selectedFingers = Finger.encodeSelectedFingers(0, Finger.INDEX, Finger.MIDDLE),
        keyLimitPerFinger = 1,
        generationMode = GenerationMode.ADAPTED_WORDS,
      )
    assertEquals(setOf('f', 'j', 'd', 'k'), WordExerciseGenerator.enabledLetters(config))
  }

  @Test
  fun enabledLettersAllFingersIncludeAllLetters() {
    val config =
      PracticeTextConfig(
        practiceMode = PracticeMode.BOTH_HANDS,
        selectedFingers = Finger.ALL_MASK,
        keyLimitPerFinger = 6,
        generationMode = GenerationMode.ADAPTED_WORDS,
      )
    assertEquals(
      PracticeTextGenerator.LOWERCASE_LETTERS.toSet(),
      WordExerciseGenerator.enabledLetters(config)
    )
  }
}
