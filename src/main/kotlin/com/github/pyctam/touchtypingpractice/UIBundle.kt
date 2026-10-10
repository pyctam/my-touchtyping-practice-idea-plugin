package com.github.pyctam.touchtypingpractice

import com.intellij.DynamicBundle
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

@NonNls private const val BUNDLE = "messages.UI"

object UIBundle : DynamicBundle(UIBundle::class.java, BUNDLE) {
  const val TEXT_FONT_TITLE = "ui.config.text-font.title"
  const val TEXT_FONT_HINT = "ui.config.text-font.hint"
  const val CONFIG_PRACTICE_MODE_TITLE = "ui.config.practice-mode.title"
  const val CONFIG_PRACTICE_MODE_HINT = "ui.config.practice-mode.hint"
  const val CONFIG_PRACTICE_MODE_RADIO_LEFTHAND = "ui.config.practice-mode.radio.left-hand"
  const val CONFIG_PRACTICE_MODE_RADIO_RIGHTHAND = "ui.config.practice-mode.radio.right-hand"
  const val CONFIG_PRACTICE_MODE_RADIO_BOTHHANDS = "ui.config.practice-mode.radio.both-hands"
  const val CONFIG_GENERATION_MODE_TITLE = "ui.config.generation-mode.title"
  const val CONFIG_GENERATION_MODE_HINT = "ui.config.generation-mode.hint"
  const val CONFIG_GENERATION_MODE_RADIO_RANDOM = "ui.config.generation-mode.radio.random"
  const val CONFIG_GENERATION_MODE_RADIO_EXACT = "ui.config.generation-mode.radio.exact-words"
  const val CONFIG_GENERATION_MODE_RADIO_ADAPTED = "ui.config.generation-mode.radio.adapted-words"
  const val CONFIG_KEY_LIMIT_PER_FINDER_TITLE = "ui.config.key-limit-per-finder.title"
  const val CONFIG_KEY_LIMIT_PER_FINDER_HINT = "ui.config.key-limit-per-finder.hint"
  const val CONFIG_FINGER_SELECTION_TITLE = "ui.config.finger-selection.title"
  const val CONFIG_FINGER_SELECTION_HINT = "ui.config.finger-selection.hint"
  const val CONFIG_FINGER_SELECTION_CHECKBOX_USE_ALL_FINGERS =
    "ui.config.finger-selection.checkbox.use-all-fingers"
  const val CONFIG_FINGER_SELECTION_CHECKBOX_SPECIFIC_FINGERS =
    "ui.config.finger-selection.checkbox.specific-fingers"
  const val CONFIG_FINGER_SELECTION_ERROR_AT_LEAST_ONE =
    "ui.config.finger-selection.error.at-least-one"
  const val TOOL_WINDOW_RESET_LINK = "ui.tool-window.reset-link"
  const val TOOL_WINDOW_RESET_HINT = "ui.tool-window.reset-hint"
  const val TOOL_WINDOW_RESET_TOOLTIP = "ui.tool-window.reset-tooltip"
  const val TOOL_WINDOW_STATUS_ERRORS = "ui.tool-window.status-errors"
  const val TOOL_WINDOW_STATUS_WPM = "ui.tool-window.status-wpm"
  const val TOOL_WINDOW_STATUS_SEPARATOR = "ui.tool-window.status-separator"

  @JvmStatic
  fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any) =
    getMessage(key, *params)
}
