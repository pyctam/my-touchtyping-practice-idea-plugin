package com.github.pyctam.touchtypingpractice.config

import com.github.pyctam.touchtypingpractice.config.Hands.BOTH_HANDS

data class Settings(
    var practiceMode: Hands = BOTH_HANDS,
    var keyLimitPerFinger: Int = 1,
    var allFingers: Boolean = true,
    var selectedFingers: List<String> = emptyList()
) {
    companion object {
        const val PROPERTY_PRACTICE_MODE = "conf.practiceMode"
        const val PROPERTY_KEY_LIMIT_PER_FINGER = "conf.keyLimitPerFinger"
    }
}
