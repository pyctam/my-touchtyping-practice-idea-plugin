package com.github.pyctam.touchtypingpractice.config

import com.github.pyctam.touchtypingpractice.config.PracticeMode.BOTH_HANDS

data class Settings(
  var textFontSize: Int = DEFAULT_TEXT_FONT_SIZE,
  var practiceMode: PracticeMode = BOTH_HANDS,
  var keyLimitPerFinger: Int = 1,
  var useAllFingers: Boolean = true,
  var selectedFingers: Int = 0
) {
  fun unSelectedAllFingers() {
    this.selectedFingers = 0
  }

  companion object {
    const val PROPERTY_PRACTICE_MODE = "conf.practiceMode"
    const val PROPERTY_KEY_LIMIT_PER_FINGER = "conf.keyLimitPerFinger"
    const val PROPERTY_USE_ALL_FINGERS = "conf.useAllFingers"
    const val PROPERTY_SELECTED_FINGERS = "conf.selectedFingers"
    const val PROPERTY_TEXT_FONT_SIZE = "conf.textFontSize"
    const val DEFAULT_TEXT_FONT_SIZE = 12
  }
}
