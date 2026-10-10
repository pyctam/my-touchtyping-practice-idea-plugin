package com.github.pyctam.touchtypingpractice.generation

import org.junit.Assert.assertEquals
import org.junit.Test

/** Unit tests for the [adaptWord] letter-filtering rule. */
class WordAdaptationTest {

  @Test
  fun flashAdaptsToFlshWhenAIsUnavailable() {
    // Explicit requirement: FLASH -> FLSH when A is unavailable (enabled keys F, L, S, H).
    assertEquals("FLSH", adaptWord("FLASH", setOf('f', 'l', 's', 'h')))
    assertEquals("flsh", adaptWord("flash", setOf('f', 'l', 's', 'h')))
  }

  @Test
  fun plantAdaptsToPlntWhenAIsUnavailable() {
    assertEquals("plnt", adaptWord("plant", setOf('p', 'l', 'n', 't', 's')))
  }

  @Test
  fun strongAdaptsToStrngWhenOIsUnavailable() {
    assertEquals("strng", adaptWord("strong", setOf('s', 't', 'r', 'n', 'g')))
  }

  @Test
  fun trainAdaptsToTrinWhenAIsUnavailable() {
    assertEquals("trin", adaptWord("train", setOf('t', 'r', 'i', 'n')))
  }

  @Test
  fun gardenAdaptsToGrdenWhenAIsUnavailable() {
    assertEquals("grden", adaptWord("garden", setOf('g', 'r', 'd', 'e', 'n')))
  }

  @Test
  fun throughAdaptsToHghWithIndexFingerKeys() {
    // Enabled keys f, g, j, h (index fingers, key limit 2, both hands).
    assertEquals("hgh", adaptWord("through", setOf('f', 'g', 'j', 'h')))
  }

  @Test
  fun orderOfKeptCharactersIsPreserved() {
    assertEquals("hng", adaptWord("things", setOf('f', 'g', 'j', 'h', 'n')))
    assertEquals("gh", adaptWord("right", setOf('f', 'g', 'j', 'h')))
  }

  @Test
  fun originalCaseOfKeptCharactersIsPreserved() {
    assertEquals("FLSH", adaptWord("FLASH", setOf('f', 'l', 's', 'h')))
    assertEquals("Flsh", adaptWord("Flash", setOf('f', 'l', 's', 'h')))
  }

  @Test
  fun noEnabledLettersYieldsEmptyResult() {
    assertEquals("", adaptWord("was", setOf('f', 'g', 'j', 'h')))
    assertEquals("", adaptWord("more", setOf('f', 'g', 'j', 'h')))
  }

  @Test
  fun allLettersEnabledYieldsUnchangedWord() {
    assertEquals("flash", adaptWord("flash", setOf('f', 'l', 'a', 's', 'h')))
  }

  @Test
  fun repeatedLettersAreAllKept() {
    assertEquals("ff", adaptWord("staff", setOf('f', 'g', 'j', 'h')))
    assertEquals("hh", adaptWord("which", setOf('f', 'g', 'j', 'h')))
  }
}
