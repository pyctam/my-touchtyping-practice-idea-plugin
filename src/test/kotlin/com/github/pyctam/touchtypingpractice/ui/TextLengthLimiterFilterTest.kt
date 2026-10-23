package com.github.pyctam.touchtypingpractice.ui

import javax.swing.JTextPane
import javax.swing.text.AbstractDocument
import javax.swing.text.PlainDocument
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [TextLengthLimiterFilter]: verifies that removals are restricted to the single
 * mistyped character, that correctly typed characters cannot be deleted, that multi-character
 * removals are rejected, that replacements deleting existing text are rejected, and that completion
 * remains a terminal state.
 */
class TextLengthLimiterFilterTest {

  private val originalText = "hello world"

  /** Creates a [PlainDocument] with the filter installed and returns it. */
  private fun createDocument(): PlainDocument {
    val doc = PlainDocument()
    doc.documentFilter = TextLengthLimiterFilter(originalText.length, originalText)
    return doc
  }

  /** Creates a [JTextPane] with the filter installed (for exercising the replace path). */
  private fun createTypingPane(): JTextPane {
    val pane = JTextPane()
    (pane.document as AbstractDocument).documentFilter =
      TextLengthLimiterFilter(originalText.length, originalText)
    return pane
  }

  // --- Single-character typo deletion ---

  @Test
  fun deleteTypoAllowed() {
    val doc = createDocument()
    // Type "h" (correct) then "x" (typo, expected "e").
    doc.insertString(0, "hx", null)
    assertEquals("hx", doc.getText(0, doc.length))

    // Backspace the typo at index 1.
    doc.remove(1, 1)
    assertEquals("Expected the typo to be deleted", "h", doc.getText(0, doc.length))
  }

  @Test
  fun deleteCorrectCharacterBlocked() {
    val doc = createDocument()
    // Type "h" (correct).
    doc.insertString(0, "h", null)
    assertEquals("h", doc.getText(0, doc.length))

    // Attempt to delete the correct character at index 0.
    doc.remove(0, 1)
    assertEquals("Expected the correct character to remain", "h", doc.getText(0, doc.length))
  }

  @Test
  fun deleteCorrectCharacterAfterTypoBlocked() {
    val doc = createDocument()
    // Type "hx" (x is a typo at index 1).
    doc.insertString(0, "hx", null)

    // Attempt to delete the correct "h" at index 0 (not the typo).
    doc.remove(0, 1)
    assertEquals("Expected the correct character to remain", "hx", doc.getText(0, doc.length))
  }

  // --- Multi-character deletion ---

  @Test
  fun multiCharacterDeletionBlocked() {
    val doc = createDocument()
    // Type "he" (both correct).
    doc.insertString(0, "he", null)

    // Attempt to delete two characters.
    doc.remove(0, 2)
    assertEquals(
      "Expected multi-character deletion to be blocked",
      "he",
      doc.getText(0, doc.length)
    )
  }

  @Test
  fun multiCharacterDeletionWithTypoBlocked() {
    val doc = createDocument()
    // Type "hx" (x is a typo at index 1).
    doc.insertString(0, "hx", null)

    // Attempt to delete both characters (including the typo).
    doc.remove(0, 2)
    assertEquals(
      "Expected multi-character deletion to be blocked even with a typo",
      "hx",
      doc.getText(0, doc.length)
    )
  }

  // --- Replacement that deletes existing text ---

  @Test
  fun replaceWithDeletionBlocked() {
    val pane = createTypingPane()
    // Type "h" (correct).
    pane.document.insertString(0, "h", null)

    // Select the character at index 0 and type "e" over it: this goes through
    // replace(offset=0, length=1, text="e"), which must be rejected.
    pane.select(0, 1)
    pane.replaceSelection("e")

    assertEquals("Expected the replacement to be blocked", "h", pane.text)
  }

  @Test
  fun pureInsertionStillAllowed() {
    val doc = createDocument()
    // Type "h" (correct).
    doc.insertString(0, "h", null)

    // A pure insertion (length == 0 in replace) should still work.
    doc.insertString(1, "e", null)
    assertEquals("Expected the insertion to be allowed", "he", doc.getText(0, doc.length))
  }

  // --- Completion is a terminal state ---

  @Test
  fun deleteAfterCompletionBlocked() {
    val doc = createDocument()
    // Type the full text correctly.
    doc.insertString(0, originalText, null)
    assertEquals(originalText, doc.getText(0, doc.length))

    // Attempt to delete the last character (which is correct, and the text is completed).
    doc.remove(originalText.length - 1, 1)
    assertEquals(
      "Expected deletion after completion to be blocked",
      originalText,
      doc.getText(0, doc.length)
    )
  }

  // --- Zero-length removal (no-op) ---

  @Test
  fun zeroLengthRemovalAllowed() {
    val doc = createDocument()
    // Zero-length removal is a no-op and should be allowed (e.g. from setText on empty doc).
    doc.remove(0, 0)
    assertEquals("Expected empty document to remain empty", "", doc.getText(0, doc.length))
  }

  // --- Insertion blocked while errors exist (existing behavior) ---

  @Test
  fun insertionBlockedWhileErrorsExist() {
    val doc = createDocument()
    // Type "hx" (x is a typo).
    doc.insertString(0, "hx", null)

    // Attempt to insert more characters while the typo is unresolved.
    doc.insertString(2, "l", null)
    assertEquals(
      "Expected insertion to be blocked while errors exist",
      "hx",
      doc.getText(0, doc.length)
    )
  }

  @Test
  fun insertionAllowedAfterCorrection() {
    val doc = createDocument()
    // Type "hx" (x is a typo), then correct it.
    doc.insertString(0, "hx", null)
    doc.remove(1, 1)
    doc.insertString(1, "e", null)
    assertEquals("he", doc.getText(0, doc.length))

    // Now insertion should be allowed again.
    doc.insertString(2, "l", null)
    assertEquals(
      "Expected insertion to be allowed after correction",
      "hel",
      doc.getText(0, doc.length)
    )
  }
}
