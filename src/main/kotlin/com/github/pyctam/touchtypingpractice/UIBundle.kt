package com.github.pyctam.touchtypingpractice

import com.intellij.DynamicBundle
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

@NonNls private const val BUNDLE = "messages.UI"

object UIBundle : DynamicBundle(BUNDLE) {
  const val CONFIG_PRACTICE_MODE_LABEL = "ui.config.practice-mode.title"
  const val CONFIG_PRACTICE_MODE_HINT = "ui.config.practice-mode.hint"
  const val CONFIG_PRACTICE_MODE_RADIO_LEFTHAND = "ui.config.practice-mode.radio.left-hand"
  const val CONFIG_PRACTICE_MODE_RADIO_RIGHTHAND = "ui.config.practice-mode.radio.right-hand"
  const val CONFIG_PRACTICE_MODE_RADIO_BOTHHANDS = "ui.config.practice-mode.radio.both-hands"

  @JvmStatic
  fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any) =
    getMessage(key, *params)

  @Suppress("unused")
  @JvmStatic
  fun messagePointer(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any) =
    getLazyMessage(key, *params)
}
