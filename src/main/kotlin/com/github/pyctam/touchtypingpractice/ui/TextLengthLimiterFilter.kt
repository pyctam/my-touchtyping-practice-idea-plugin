package com.github.pyctam.touchtypingpractice.ui

import com.intellij.openapi.diagnostic.Logger
import javax.swing.text.AttributeSet
import javax.swing.text.DocumentFilter

/**
 * [DocumentFilter] that prevents the typing area from growing beyond [maxLength] characters.
 *
 * Insertions and replacements that would exceed the limit are silently ignored. When the current
 * text already contains a mismatch against [originalText], new input is blocked until the user
 * corrects it (the user must fix errors before continuing).
 *
 * Removals are restricted to the single mistyped character: a removal is allowed only when it
 * removes exactly one character and that character mismatches [originalText]. Correctly typed
 * characters cannot be deleted, and multi-character removals are rejected. Once the text is
 * completed (it matches [originalText] exactly), completion is a terminal state: removals and
 * replacements are blocked until the practice text is reset.
 *
 * Replacements that delete existing text (`length > 0`) are always rejected: the only way to
 * correct a typo is to delete the mistyped character and retype it.
 *
 * Extracted from [TouchTypingDocumentListener] so the input-guarding rule is a single, reusable,
 * testable unit.
 *
 * @param maxLength the maximum allowed length of the typing area.
 * @param originalText the reference text used to detect unresolved errors.
 */
class TextLengthLimiterFilter(
  private val maxLength: Int,
  private val originalText: String,
) : DocumentFilter() {

  private val logger = Logger.getInstance(TextLengthLimiterFilter::class.java)

  override fun insertString(fb: FilterBypass, offset: Int, string: String, attr: AttributeSet?) {
    if (hasErrors(fb)) {
      logger.debug("Rejected insertion - errors exist")
      return
    }
    val newLength = fb.document.length + string.length
    if (newLength <= maxLength) {
      super.insertString(fb, offset, string, attr)
    } else {
      logger.debug("Rejected insertion - would exceed max length $maxLength")
    }
  }

  override fun replace(
    fb: FilterBypass,
    offset: Int,
    length: Int,
    text: String,
    attrs: AttributeSet?
  ) {
    if (isCompleted(fb)) {
      logger.debug("Rejected replacement - text is completed (terminal state)")
      return
    }
    if (length > 0) {
      // No replacement may delete existing text: the only correction path is to delete the
      // mistyped character (via remove) and retype it.
      logger.debug("Rejected replacement - existing text cannot be replaced")
      return
    }
    if (hasErrors(fb)) {
      logger.debug("Rejected replacement - errors exist")
      return
    }
    val newLength = fb.document.length - length + text.length
    if (newLength <= maxLength) {
      super.replace(fb, offset, length, text, attrs)
    } else {
      logger.debug("Rejected replacement - would exceed max length $maxLength")
    }
  }

  override fun remove(fb: FilterBypass, offset: Int, length: Int) {
    if (isCompleted(fb)) {
      logger.debug("Rejected removal - text is completed (terminal state)")
      return
    }
    if (length == 0) {
      // Zero-length removal is a no-op (e.g. from setText on an empty document); allow it.
      super.remove(fb, offset, length)
      return
    }
    if (length != 1) {
      logger.debug("Rejected removal - only single-character deletion is allowed")
      return
    }
    if (
      !TextComparison.isMismatchAt(fb.document.getText(0, fb.document.length), originalText, offset)
    ) {
      logger.debug("Rejected removal - character at $offset is not a typo")
      return
    }
    super.remove(fb, offset, length)
  }

  /** Whether the current document text has any mismatch against [originalText]. */
  private fun hasErrors(fb: FilterBypass): Boolean =
    TextComparison.hasErrors(fb.document.getText(0, fb.document.length), originalText)

  /** Whether the current document text matches [originalText] exactly (completed). */
  private fun isCompleted(fb: FilterBypass): Boolean =
    fb.document.getText(0, fb.document.length) == originalText
}
