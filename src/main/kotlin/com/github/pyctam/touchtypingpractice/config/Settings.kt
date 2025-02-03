package com.github.pyctam.touchtypingpractice.config

import com.github.pyctam.touchtypingpractice.config.Finger.INDEX
import com.github.pyctam.touchtypingpractice.config.Finger.LITTLE
import com.github.pyctam.touchtypingpractice.config.Finger.MIDDLE
import com.github.pyctam.touchtypingpractice.config.Finger.RING
import com.github.pyctam.touchtypingpractice.config.Finger.THUMB
import com.github.pyctam.touchtypingpractice.config.PracticeMode.BOTH_HANDS

data class Settings(
  var practiceMode: PracticeMode = BOTH_HANDS,
  var keyLimitPerFinger: Int = 1,
  var useAllFingers: Boolean = true,
  var selectedFingers: MutableMap<Finger, Boolean> =
    mutableMapOf(THUMB to false, INDEX to false, MIDDLE to false, RING to false, LITTLE to false)
) {
  companion object {
    const val PROPERTY_PRACTICE_MODE = "conf.practiceMode"
    const val PROPERTY_KEY_LIMIT_PER_FINGER = "conf.keyLimitPerFinger"
    const val PROPERTY_USE_ALL_FINGERS = "conf.useAllFingers"
  }
}
