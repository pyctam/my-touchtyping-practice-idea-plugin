package com.github.pyctam.touchtypingpractice.config

import com.github.pyctam.touchtypingpractice.config.Hands.BOTH_HANDS

data class Settings(
    var practiceMode: Hands = BOTH_HANDS,
    var keyRadius: Int = 1,
    var allFingers: Boolean = true,
    var selectedFingers: List<String> = emptyList()
) {
    companion object {
        const val PROPERTY_PRACTICE_MODE = "conf.practiceMode"
    }
}
