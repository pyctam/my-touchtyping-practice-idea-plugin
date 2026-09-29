package com.github.pyctam.touchtypingpractice.config

/**
 * Mutable working model for the "Finger Selection" settings group.
 *
 * Holds the in-progress finger bitmask while the settings dialog is open and encapsulates the
 * transitions driven by the "Use All Fingers" shortcut and the individual finger checkboxes.
 *
 * The model is deliberately UI-free so its behavior can be unit-tested without a Swing dialog. The
 * dialog keeps its checkboxes in sync with [mask] and commits it to [Settings] on Apply.
 */
class FingerSelection(initialMask: Int = Finger.ALL_MASK) {

  /** The in-progress selection bitmask. */
  var mask: Int = initialMask
    private set

  /** True when every finger is selected (the "Use All Fingers" shortcut is checked). */
  val isAllSelected: Boolean
    get() = mask == Finger.ALL_MASK

  /** True when no finger is selected (an invalid state that must be rejected on Apply). */
  val isEmpty: Boolean
    get() = mask == 0

  /** Whether [finger] is currently selected. */
  fun isSelected(finger: Finger): Boolean = Finger.isFingerSelected(mask, finger)

  /**
   * Applies the "Use All Fingers" shortcut: selects every finger when [checked], clears the whole
   * selection otherwise.
   */
  fun setUseAllFingers(checked: Boolean) {
    mask = if (checked) Finger.ALL_MASK else 0
  }

  /** Sets or clears [finger] according to [checked], leaving the other fingers untouched. */
  fun setFinger(finger: Finger, checked: Boolean) {
    mask =
      if (checked) {
        Finger.encodeSelectedFingers(mask, finger)
      } else {
        Finger.clearFinger(mask, finger)
      }
  }
}
