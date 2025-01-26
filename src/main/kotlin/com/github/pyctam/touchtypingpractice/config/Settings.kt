package com.github.pyctam.touchtypingpractice.config

import com.github.pyctam.touchtypingpractice.config.Hands.BOTH_HANDS

data class Settings(
  var hands: Hands = Hands.BOTH_HANDS,
  var keyRadius: Int = 1,
  var allFingers: Boolean = true,
  var selectedFingers: List<String> = emptyList()
)
