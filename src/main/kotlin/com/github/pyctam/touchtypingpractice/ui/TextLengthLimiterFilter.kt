package com.github.pyctam.touchtypingpractice.ui

import com.intellij.openapi.diagnostic.Logger
import javax.swing.text.AttributeSet
import javax.swing.text.DocumentFilter

/**
 * [DocumentFilter] that prevents the typing area from growing beyond [maxLength] characters.
 *
 * Insertions and replacements that would exceed the limit are silently ignored, while removals
 * (delete/backspace) are always allowed. When the current text already contains a mismatch against
 * [originalText], new input is blocked until the user corrects it (the user must fix errors before
 * continuing).
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
    // Always allow removals (delete/backspace).
    super.remove(fb, offset, length)
  }

  /** Whether the current document text has any mismatch against [originalText]. */
  private fun hasErrors(fb: FilterBypass): Boolean {
    val typed = fb.document.getText(0, fb.document.length)
    val compareLen = minOf(typed.length, originalText.length)
    for (i in 0 until compareLen) {
      if (typed[i] != originalText[i]) return true
    }
    return false
  }
}
