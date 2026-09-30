package com.github.pyctam.touchtypingpractice.config

import java.awt.GraphicsEnvironment

/**
 * A font family name selected for the practice text and typing area.
 *
 * The settings UI offers a curated list of fonts with true (OpenType) small caps support first,
 * followed by every other font family installed on the system.
 */
data class TextFont(val family: String) {

  companion object {
    /**
     * Suggested fonts with true small caps, listed in the order they should appear at the top of
     * the font selector.
     * - Common serif fonts with true small caps: Georgia, Palatino Linotype, Garamond.
     * - Common sans-serif fonts with true small caps: Calibri, Verdana.
     * - Dedicated all-caps/small-caps fonts: Copperplate Gothic.
     */
    val FEATURED_FAMILIES: List<String> =
      listOf("Georgia", "Palatino Linotype", "Garamond", "Calibri", "Verdana", "Copperplate Gothic")

    /** Default font family: the first featured font available on this system, else the first. */
    val DEFAULT: TextFont =
      TextFont(FEATURED_FAMILIES.firstOrNull { isAvailable(it) } ?: FEATURED_FAMILIES.first())

    /**
     * All font families available on the system: featured families first (in [FEATURED_FAMILIES]
     * order), then every other installed family in alphabetical order.
     */
    fun allAvailableFamilies(): List<String> {
      val installed = availableSystemFamilies()
      val featured = FEATURED_FAMILIES.filter { it in installed }
      val rest =
        installed
          .filter { it !in FEATURED_FAMILIES }
          .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it })
      return featured + rest
    }

    /** True if the font family is installed on this system. */
    fun isAvailable(family: String): Boolean = family in availableSystemFamilies()

    /** All font family names installed on the system. */
    private fun availableSystemFamilies(): Set<String> =
      try {
        GraphicsEnvironment.getLocalGraphicsEnvironment().availableFontFamilyNames.toSet()
      } catch (_: Exception) {
        // Headless environments (e.g., unit tests without a display) may not expose fonts.
        emptySet()
      }
  }
}
