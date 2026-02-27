package com.github.pyctam.touchtypingpractice

import com.intellij.DynamicBundle
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

@NonNls private const val BUNDLE = "messages.UI"

object UIBundle : DynamicBundle(BUNDLE) {
  const val TEXT_FONT_SIZE_TITLE = "ui.config.font-size.title"
  const val TEXT_FONT_SIZE_HINT = "ui.config.font-size.hint"
  const val CONFIG_PRACTICE_MODE_TITLE = "ui.config.practice-mode.title"
  const val CONFIG_PRACTICE_MODE_HINT = "ui.config.practice-mode.hint"
  const val CONFIG_PRACTICE_MODE_RADIO_LEFTHAND = "ui.config.practice-mode.radio.left-hand"
  const val CONFIG_PRACTICE_MODE_RADIO_RIGHTHAND = "ui.config.practice-mode.radio.right-hand"
  const val CONFIG_PRACTICE_MODE_RADIO_BOTHHANDS = "ui.config.practice-mode.radio.both-hands"
  const val CONFIG_KEY_LIMIT_PER_FINDER_TITLE = "ui.config.key-limit-per-finder.title"
  const val CONFIG_KEY_LIMIT_PER_FINDER_HINT = "ui.config.key-limit-per-finder.hint"
  const val CONFIG_FINGER_SELECTION_TITLE = "ui.config.finger-selection.title"
  const val CONFIG_FINGER_SELECTION_HINT = "ui.config.finger-selection.hint"
  const val CONFIG_FINGER_SELECTION_CHECKBOX_USE_ALL_FINGERS =
    "ui.config.finger-selection.checkbox.use-all-fingers"
  const val CONFIG_FINGER_SELECTION_CHECKBOX_SPECIFIC_FINGERS =
    "ui.config.finger-selection.checkbox.specific-fingers"

  @JvmStatic
  fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any) =
    getMessage(key, *params)

  @Suppress("unused")
  @JvmStatic
  fun messagePointer(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any) =
    getLazyMessage(key, *params)
}
