package com.github.pyctam.touchtypingpractice.config

import com.github.pyctam.touchtypingpractice.config.Hand.BOTH_HANDS

data class Settings(
    var practiceMode: Hand = BOTH_HANDS,
    var keyLimitPerFinger: Int = 1,
    var useAllFingers: Boolean = true,
    var selectedFingers: List<Finger> = emptyList()
) {
    companion object {
        const val PROPERTY_PRACTICE_MODE = "conf.practiceMode"
        const val PROPERTY_KEY_LIMIT_PER_FINGER = "conf.keyLimitPerFinger"
    }
}
