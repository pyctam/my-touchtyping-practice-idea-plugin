package com.github.pyctam.touchtypingpractice.ui

import com.github.pyctam.touchtypingpractice.config.ColoringMode
import com.github.pyctam.touchtypingpractice.config.Finger
import com.github.pyctam.touchtypingpractice.config.Hand
import com.github.pyctam.touchtypingpractice.generation.KeyboardLayout
import com.intellij.ui.JBColor
import java.awt.Color
import javax.swing.JTextPane
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants

/**
 * Per-character colors for the practice text, honoring the configured [ColoringMode].
 *
 * In [ColoringMode.HAND] left-hand keys are rendered in [LEFT] (blue) and right-hand keys in
 * [RIGHT] (purple). In [ColoringMode.FINGER] every finger of every hand has its own distinct color
 * from [FINGER_COLORS]. In [ColoringMode.NONE] no coloring is applied. All colors are theme-aware
 * [JBColor]s and are deliberately distinct from the plugin's semantic colors: red (mismatch), green
 * (completion), and yellow (warning).
 *
 * The character-to-color mapping is derived from [KeyboardLayout] so it stays in sync with the key
 * assignments used by the text generator.
 */
object TextColors {

  /** Left-hand key color: blue (theme-aware). */
  val LEFT: JBColor = JBColor(0x1976D2, 0x64B5F6)

  /** Right-hand key color: purple (theme-aware). */
  val RIGHT: JBColor = JBColor(0x6A1B9A, 0xBA68C8)

  /**
   * Distinct theme-aware color for every (hand, finger) pair.
   *
   * The ten colors are chosen to be easily recognizable in both light and dark themes and to stay
   * distinct from each other and from the semantic colors (red mismatch, green completion).
   */
  val FINGER_COLORS: Map<Pair<Hand, Finger>, JBColor> =
    mapOf(
      Hand.LEFT to Finger.THUMB to JBColor(0xC62828, 0xEF5350),
      Hand.LEFT to Finger.INDEX to JBColor(0x1565C0, 0x64B5F6),
      Hand.LEFT to Finger.MIDDLE to JBColor(0x2E7D32, 0x81C784),
      Hand.LEFT to Finger.RING to JBColor(0x6A1B9A, 0xBA68C8),
      Hand.LEFT to Finger.LITTLE to JBColor(0x00838F, 0x4DD0E1),
      Hand.RIGHT to Finger.THUMB to JBColor(0xE65100, 0xFF8A65),
      Hand.RIGHT to Finger.INDEX to JBColor(0xAD1457, 0xF06292),
      Hand.RIGHT to Finger.MIDDLE to JBColor(0xF9A825, 0xFFE082),
      Hand.RIGHT to Finger.RING to JBColor(0x283593, 0x7986CB),
      Hand.RIGHT to Finger.LITTLE to JBColor(0x5D4037, 0xBCAAA4),
    )

  /** Large enough to include every key in the layout. */
  private const val ALL_KEYS_LIMIT = 100

  /** Maps each single-character key to its hand color. Space and multi-char keys are excluded. */
  private val charToHandColor: Map<Char, Color> = buildCharToColorMap { hand, _ -> handColor(hand) }

  /** Maps each single-character key to its finger color. Space and multi-char keys are excluded. */
  private val charToFingerColor: Map<Char, Color> = buildCharToColorMap { hand, finger ->
    FINGER_COLORS.getValue(hand to finger)
  }

  /**
   * Returns the color for [c] under [mode], or `null` if the character is not a single-character
   * key in the keyboard layout (e.g. space, multi-char keys like "tab") or [mode] is
   * [ColoringMode.NONE].
   */
  fun colorFor(c: Char, mode: ColoringMode): Color? =
    when (mode) {
      ColoringMode.NONE -> null
      ColoringMode.HAND -> charToHandColor[c]
      ColoringMode.FINGER -> charToFingerColor[c]
    }

  /**
   * Applies per-character colors to [pane] for the given [text] according to [mode].
   *
   * Characters that map to a color get that color as their foreground; all other characters keep
   * the pane's default foreground. In [ColoringMode.NONE] nothing is applied. Safe to call
   * repeatedly (e.g. on every document change) because
   * [javax.swing.text.StyledDocument.setCharacterAttributes] with `replace = true` overwrites any
   * previous per-character attributes.
   */
  fun applyTo(pane: JTextPane, text: String, mode: ColoringMode) {
    if (mode == ColoringMode.NONE) return
    val doc = pane.styledDocument
    for (i in text.indices) {
      val color = colorFor(text[i], mode) ?: continue
      val attrs = SimpleAttributeSet()
      StyleConstants.setForeground(attrs, color)
      doc.setCharacterAttributes(i, 1, attrs, true)
    }
  }

  private fun handColor(hand: Hand): JBColor = if (hand == Hand.LEFT) LEFT else RIGHT

  private fun buildCharToColorMap(colorFor: (Hand, Finger) -> Color): Map<Char, Color> {
    val map = mutableMapOf<Char, Color>()
    for (hand in Hand.entries) {
      for (finger in Finger.entries) {
        for (key in KeyboardLayout.keysFor(hand.toPracticeMode(), finger, ALL_KEYS_LIMIT)) {
          if (key.length == 1 && key[0] != ' ') map[key[0]] = colorFor(hand, finger)
        }
      }
    }
    return map
  }
}
