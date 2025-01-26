package com.github.pyctam.touchtypingpractice.config

import com.github.pyctam.touchtypingpractice.config.Hands.BOTH_HANDS

data class Settings(
  var hands: Hands = BOTH_HANDS, // default is both hands
  var keyRadius: Int = 1 // default radius value
)
