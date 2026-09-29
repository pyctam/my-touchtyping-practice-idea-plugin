package com.github.pyctam.touchtypingpractice.config

enum class Finger(val label: String) {
  THUMB("Thumb"),
  INDEX("Index"),
  MIDDLE("Middle"),
  RING("Ring"),
  LITTLE("Little");

  companion object {
    /** Bitmask with all fingers selected (Thumb, Index, Middle, Ring, Little). */
    val ALL_MASK: Int = entries.fold(0) { mask, finger -> mask or (1 shl finger.ordinal) }

    fun encodeSelectedFingers(encodedValue: Int, vararg fingers: Finger): Int {
      return fingers.fold(encodedValue) { accumulator, finger ->
        accumulator or (1 shl finger.ordinal)
      }
    }

    fun isFingerSelected(encoded: Int, finger: Finger): Boolean {
      return (encoded and (1 shl finger.ordinal)) != 0
    }
  }
}
