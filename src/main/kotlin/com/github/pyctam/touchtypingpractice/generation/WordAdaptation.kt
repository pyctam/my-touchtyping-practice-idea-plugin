package com.github.pyctam.touchtypingpractice.generation

/**
 * Adapts [word] to a set of enabled keys by keeping only the characters whose lowercase form is in
 * [enabledLetters], preserving the original order and the original case of the kept characters.
 *
 * Characters whose keys are not enabled are omitted. The result may be empty (when no character is
 * enabled) and is a letter-filtered fragment of [word], not necessarily a valid English word.
 *
 * Examples: `adaptWord("flash", setOf('f', 'l', 's', 'h'))` returns `"flsh"`; `adaptWord("FLASH",
 * setOf('f', 'l', 's', 'h'))` returns `"FLSH"`.
 *
 * @param word the source word.
 * @param enabledLetters the enabled letters, in lowercase.
 * @return the adapted string (possibly empty).
 */
fun adaptWord(word: String, enabledLetters: Set<Char>): String =
  word.filter { it.lowercaseChar() in enabledLetters }
