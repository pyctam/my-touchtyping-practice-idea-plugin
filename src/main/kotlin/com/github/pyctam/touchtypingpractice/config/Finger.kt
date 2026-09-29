package com.github.pyctam.touchtypingpractice.config

/**
 * A finger of the hand, used for the "Finger Selection" setting.
 *
 * The selected fingers are persisted as a bitmask where bit N corresponds to the Nth entry of
 * [entries]. The companion helpers ([encodeSelectedFingers], [clearFinger], [isFingerSelected])
 * manipulate that bitmask.
 *
 * @property label human-readable name shown in the settings UI.
 */
enum class Finger(val label: String) {
  THUMB("Thumb"),
  INDEX("Index"),
  MIDDLE("Middle"),
  RING("Ring"),
  LITTLE("Little");

  companion object {
    /** Bitmask with all fingers selected (Thumb, Index, Middle, Ring, Little). */
    val ALL_MASK: Int = entries.fold(0) { mask, finger -> mask or (1 shl finger.ordinal) }

    /** Returns [encodedValue] with the bits for [fingers] set. */
    fun encodeSelectedFingers(encodedValue: Int, vararg fingers: Finger): Int {
      return fingers.fold(encodedValue) { accumulator, finger ->
        accumulator or (1 shl finger.ordinal)
      }
    }

    /** Returns [encodedValue] with the bit for [finger] cleared. */
    fun clearFinger(encodedValue: Int, finger: Finger): Int =
      encodedValue and (1 shl finger.ordinal).inv()

    /** Whether [finger] is selected in [encoded]. */
    fun isFingerSelected(encoded: Int, finger: Finger): Boolean {
      return (encoded and (1 shl finger.ordinal)) != 0
    }
  }
}
