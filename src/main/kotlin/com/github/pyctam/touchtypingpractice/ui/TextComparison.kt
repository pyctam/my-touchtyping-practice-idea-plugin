package com.github.pyctam.touchtypingpractice.ui

/**
 * Pure helper for comparing typed text against the original reference text.
 *
 * Centralizes the character-by-character mismatch logic that was previously duplicated in
 * [TouchTypingDocumentListener.updateHighlights] and [TextLengthLimiterFilter.hasErrors]. Also the
 * natural home for future WPM/accuracy and per-finger error analysis logic.
 */
object TextComparison {

  /**
   * Returns the indices at which [typed] and [original] differ.
   *
   * Only the first [minOf][kotlin.minOf]([typed].length, [original].length) characters are
   * compared; any extra characters in [typed] beyond [original].length are not considered
   * mismatches (they are handled separately by the length-limit logic).
   */
  fun mismatchIndices(typed: String, original: String): IntArray {
    val compareLen = minOf(typed.length, original.length)
    val indices = ArrayList<Int>(compareLen)
    for (i in 0 until compareLen) {
      if (typed[i] != original[i]) indices.add(i)
    }
    return indices.toIntArray()
  }

  /** Whether [typed] has any mismatch against [original] within the compared prefix. */
  fun hasErrors(typed: String, original: String): Boolean {
    val compareLen = minOf(typed.length, original.length)
    for (i in 0 until compareLen) {
      if (typed[i] != original[i]) return true
    }
    return false
  }

  /**
   * Whether the character at [index] in [typed] mismatches [original].
   *
   * Returns `false` if [index] is out of bounds for either string.
   */
  fun isMismatchAt(typed: String, original: String, index: Int): Boolean {
    if (index < 0 || index >= typed.length || index >= original.length) return false
    return typed[index] != original[index]
  }
}
