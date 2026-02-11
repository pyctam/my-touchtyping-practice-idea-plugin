package com.github.pyctam.touchtypingpractice.config

import com.github.pyctam.touchtypingpractice.UIBundle
import com.github.pyctam.touchtypingpractice.UIBundle.CONFIG_FINGER_SELECTION_CHECKBOX_SPECIFIC_FINGERS
import com.github.pyctam.touchtypingpractice.UIBundle.CONFIG_FINGER_SELECTION_CHECKBOX_USE_ALL_FINGERS
import com.github.pyctam.touchtypingpractice.UIBundle.CONFIG_FINGER_SELECTION_HINT
import com.github.pyctam.touchtypingpractice.UIBundle.CONFIG_FINGER_SELECTION_TITLE
import com.github.pyctam.touchtypingpractice.UIBundle.CONFIG_KEY_LIMIT_PER_FINDER_HINT
import com.github.pyctam.touchtypingpractice.UIBundle.CONFIG_KEY_LIMIT_PER_FINDER_TITLE
import com.github.pyctam.touchtypingpractice.UIBundle.CONFIG_PRACTICE_MODE_HINT
import com.github.pyctam.touchtypingpractice.UIBundle.CONFIG_PRACTICE_MODE_RADIO_BOTHHANDS
import com.github.pyctam.touchtypingpractice.UIBundle.CONFIG_PRACTICE_MODE_RADIO_LEFTHAND
import com.github.pyctam.touchtypingpractice.UIBundle.CONFIG_PRACTICE_MODE_RADIO_RIGHTHAND
import com.github.pyctam.touchtypingpractice.UIBundle.CONFIG_PRACTICE_MODE_TITLE
import com.github.pyctam.touchtypingpractice.UIBundle.TEXT_FONT_SIZE_TITLE
import com.github.pyctam.touchtypingpractice.config.PracticeMode.BOTH_HANDS
import com.github.pyctam.touchtypingpractice.config.PracticeMode.LEFT_HAND
import com.github.pyctam.touchtypingpractice.config.PracticeMode.RIGHT_HAND
import com.github.pyctam.touchtypingpractice.config.Settings.Companion.DEFAULT_TEXT_FONT_SIZE
import com.github.pyctam.touchtypingpractice.config.Settings.Companion.PROPERTY_KEY_LIMIT_PER_FINGER
import com.github.pyctam.touchtypingpractice.config.Settings.Companion.PROPERTY_PRACTICE_MODE
import com.github.pyctam.touchtypingpractice.config.Settings.Companion.PROPERTY_SELECTED_FINGERS
import com.github.pyctam.touchtypingpractice.config.Settings.Companion.PROPERTY_TEXT_FONT_SIZE
import com.github.pyctam.touchtypingpractice.config.Settings.Companion.PROPERTY_USE_ALL_FINGERS
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.MutableProperty
import com.intellij.ui.dsl.builder.bind
import com.intellij.ui.dsl.builder.bindIntValue
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel

class Configuration : BoundConfigurable("Touch Typing Practice (2)") {
  private val settings: Settings = loadSettings()

  // Create a mutable property that handles bidirectional binding
  private val useAllFingersProperty =
    object : MutableProperty<Boolean> {
      override fun get(): Boolean = settings.useAllFingers

      override fun set(value: Boolean) {
        settings.useAllFingers = value
        // When "Use All Fingers" is checked, select all fingers
        if (value) {
          settings.selectedFingers = 0
          for (finger in Finger.entries) {
            settings.selectedFingers =
              Finger.encodeSelectedFingers(settings.selectedFingers, finger)
          }
        }
      }
    }

  override fun createPanel(): DialogPanel {
    return panel {
      val titleTextFontSize = UIBundle.message(TEXT_FONT_SIZE_TITLE)

      group(titleTextFontSize) {
        row {
          val hint = UIBundle.message(UIBundle.TEXT_FONT_SIZE_HINT)
          comment(hint)
        }
        row { spinner(8..24, 1).bindIntValue(settings::textFontSize) }
      }

      val titlePracticeMode = UIBundle.message(CONFIG_PRACTICE_MODE_TITLE)

      group(titlePracticeMode) {
        buttonsGroup {
            row {
              val hint = UIBundle.message(CONFIG_PRACTICE_MODE_HINT)
              comment(hint)
            }
            row {
              val textLeftHand = UIBundle.message(CONFIG_PRACTICE_MODE_RADIO_LEFTHAND)
              val textRightHand = UIBundle.message(CONFIG_PRACTICE_MODE_RADIO_RIGHTHAND)
              val textBothHands = UIBundle.message(CONFIG_PRACTICE_MODE_RADIO_BOTHHANDS)

              radioButton(textLeftHand, LEFT_HAND)
              radioButton(textRightHand, RIGHT_HAND)
              radioButton(textBothHands, BOTH_HANDS)
            }
          }
          .bind({ settings.practiceMode }, { settings.practiceMode = it })
      }

      val titleKeyLimitPerFinger = UIBundle.message(CONFIG_KEY_LIMIT_PER_FINDER_TITLE)

      group(titleKeyLimitPerFinger) {
        row {
          val hint = UIBundle.message(CONFIG_KEY_LIMIT_PER_FINDER_HINT)
          comment(hint)
        }
        row { spinner(1..6).bindIntValue(settings::keyLimitPerFinger) }
      }

      val titleFingerSelection = UIBundle.message(CONFIG_FINGER_SELECTION_TITLE)

      group(titleFingerSelection) {
        row {
          val hint = UIBundle.message(CONFIG_FINGER_SELECTION_HINT)
          comment(hint)
        }

        val textUseAllFingers = UIBundle.message(CONFIG_FINGER_SELECTION_CHECKBOX_USE_ALL_FINGERS)
        row { checkBox(textUseAllFingers).bindSelected(useAllFingersProperty) }

        val titleSpecificFingers =
          UIBundle.message(CONFIG_FINGER_SELECTION_CHECKBOX_SPECIFIC_FINGERS)

        buttonsGroup(titleSpecificFingers) {
          for (finger in Finger.entries) {
            row {
              checkBox(finger.label)
                .bindSelected({ isFingerSelected(finger) }, { selectFinger(finger) })
            }
          }
        }
      }
    }
  }

  override fun apply() {
    settings.unSelectedAllFingers()
    super.apply()

    // Check if all fingers are selected
    val allFingersSelected =
      Finger.entries.all { finger -> Finger.isFingerSelected(settings.selectedFingers, finger) }

    // If all fingers are selected, enable "Use All Fingers"
    if (allFingersSelected) {
      settings.useAllFingers = true
    } else if (settings.selectedFingers == 0) {
      // If no fingers are selected, enable "Use All Fingers" and select all
      settings.useAllFingers = true
      for (finger in Finger.entries) {
        settings.selectedFingers = Finger.encodeSelectedFingers(settings.selectedFingers, finger)
      }
    } else {
      // If some (but not all) fingers are selected, disable "Use All Fingers"
      settings.useAllFingers = false
    }

    saveSettings()
  }

  override fun reset() {
    super.reset()
    saveSettings()
  }

  private fun loadSettings(): Settings {
    val properties = PropertiesComponent.getInstance()

    val textFontSize = properties.getInt(PROPERTY_TEXT_FONT_SIZE, DEFAULT_TEXT_FONT_SIZE)
    val practiceModeName = properties.getValue(PROPERTY_PRACTICE_MODE, BOTH_HANDS.name)
    val practiceMode = PracticeMode.valueOf(practiceModeName)
    val keyLimitPerFinger = properties.getInt(PROPERTY_KEY_LIMIT_PER_FINGER, 1)
    val allFingers = properties.getBoolean(PROPERTY_USE_ALL_FINGERS, true)
    val selectedFingers = properties.getInt(PROPERTY_SELECTED_FINGERS, 0)

    return Settings(textFontSize, practiceMode, keyLimitPerFinger, allFingers, selectedFingers)
  }

  private fun saveSettings() {
    val textFontSize = settings.textFontSize.toString()
    val practiceMode = settings.practiceMode.name
    val keyLimitPerFinger = settings.keyLimitPerFinger.toString()
    val useAllFingers = settings.useAllFingers.toString()
    val selectedFingers = settings.selectedFingers.toString()

    val properties = PropertiesComponent.getInstance()
    properties.setValue(PROPERTY_TEXT_FONT_SIZE, textFontSize)
    properties.setValue(PROPERTY_PRACTICE_MODE, practiceMode)
    properties.setValue(PROPERTY_KEY_LIMIT_PER_FINGER, keyLimitPerFinger)
    properties.setValue(PROPERTY_USE_ALL_FINGERS, useAllFingers)
    properties.setValue(PROPERTY_SELECTED_FINGERS, selectedFingers)
  }

  private fun isFingerSelected(finger: Finger): Boolean {
    return Finger.isFingerSelected(settings.selectedFingers, finger)
  }

  private fun selectFinger(finger: Finger) {
    // Toggle the finger bit
    settings.selectedFingers = settings.selectedFingers xor (1 shl finger.ordinal)
  }
}
