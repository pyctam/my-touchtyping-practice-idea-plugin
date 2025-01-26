package com.github.pyctam.touchtypingpractice.config

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.panel

class Configuration2 : BoundConfigurable("Touch Typing Practice (2)") {
  override fun createPanel(): DialogPanel {
    return panel {
      group("Practice Mode:") {
        // UI elements go here
      }
      group("Key Radius (0 to 3):") {
        // UI elements go here
      }
      group("Finger Selection:") {
        // UI elements go here
      }
    }
  }
}
