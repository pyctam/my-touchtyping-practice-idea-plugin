package com.github.pyctam.touchtypingpractice.ui

import com.github.pyctam.touchtypingpractice.config.Finger
import com.github.pyctam.touchtypingpractice.config.PracticeMode
import com.github.pyctam.touchtypingpractice.generation.KeyboardLayout
import com.intellij.ui.JBColor
import java.awt.Color
import javax.swing.JTextPane
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants

/**
 * Per-hand letter colors for the practice text.
 *
 * Left-hand keys are rendered in [LEFT] (blue) and right-hand keys in [RIGHT] (purple). Both colors
 * are theme-aware [JBColor]s and are deliberately distinct from the plugin's semantic colors: red
 * (mismatch), green (completion), and yellow (warning).
 *
 * The character-to-color mapping is derived from [KeyboardLayout] so it stays in sync with the key
 * assignments used by the text generator.
 */
object HandColors {

  /** Left-hand key color: blue (theme-aware). */
  val LEFT: JBColor = JBColor(0x1976D2, 0x64B5F6)

  /** Right-hand key color: purple (theme-aware). */
  val RIGHT: JBColor = JBColor(0x6A1B9A, 0xBA68C8)

  /** Large enough to include every key in the layout. */
  private const val ALL_KEYS_LIMIT = 100

  /** Maps each single-character key to its hand color. Space and multi-char keys are excluded. */
  private val charToColor: Map<Char, Color> = buildCharToColorMap()

  /**
   * Returns the hand color for [c], or `null` if the character is not a single-character key in the
   * keyboard layout (e.g. space, multi-char keys like "tab").
   */
  fun colorFor(c: Char): Color? = charToColor[c]

  /**
   * Applies per-character hand colors to [pane] for the given [text].
   *
   * Characters that map to a hand color get that color as their foreground; all other characters
   * keep the pane's default foreground. Safe to call repeatedly (e.g. on every document change)
   * because [javax.swing.text.StyledDocument.setCharacterAttributes] with `replace = true`
   * overwrites any previous per-character attributes.
   */
  fun applyTo(pane: JTextPane, text: String) {
    val doc = pane.styledDocument
    for (i in text.indices) {
      val color = colorFor(text[i]) ?: continue
      val attrs = SimpleAttributeSet()
      StyleConstants.setForeground(attrs, color)
      doc.setCharacterAttributes(i, 1, attrs, true)
    }
  }

  private fun buildCharToColorMap(): Map<Char, Color> {
    val map = mutableMapOf<Char, Color>()
    for (finger in Finger.entries) {
      for (key in KeyboardLayout.keysFor(PracticeMode.LEFT_HAND, finger, ALL_KEYS_LIMIT)) {
        if (key.length == 1 && key[0] != ' ') map[key[0]] = LEFT
      }
      for (key in KeyboardLayout.keysFor(PracticeMode.RIGHT_HAND, finger, ALL_KEYS_LIMIT)) {
        if (key.length == 1 && key[0] != ' ') map[key[0]] = RIGHT
      }
    }
    return map
  }
}
