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
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.bind
import com.intellij.ui.dsl.builder.bindIntValue
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import javax.swing.JCheckBox
import javax.swing.SwingUtilities

class Configuration : BoundConfigurable("Touch Typing Practice (2)") {
  private val logger: Logger = Logger.getInstance(Configuration::class.java)
  private val settings: Settings = Settings.getInstance()
  private var dialogPanel: DialogPanel? = null
  private val fingerCheckboxes: MutableMap<Finger, Cell<JCheckBox>> = mutableMapOf()

  override fun createPanel(): DialogPanel {
    dialogPanel = panel {
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
        row {
          checkBox(textUseAllFingers)
            .bindSelected(
              { settings.useAllFingers },
              { value ->
                logger.info("User toggled 'Use All Fingers' to: $value")
                settings.useAllFingers = value
                updateFingerSelectionBasedOnUseAll()
                updateFingerCheckboxesUI()
              }
            )
        }

        val titleSpecificFingers =
          UIBundle.message(CONFIG_FINGER_SELECTION_CHECKBOX_SPECIFIC_FINGERS)

        buttonsGroup(titleSpecificFingers) {
          for (finger in Finger.entries) {
            row {
              val checkBoxCell =
                checkBox(finger.label)
                  .bindSelected(
                    { isFingerSelected(finger) },
                    { value ->
                      logger.info("User toggled $finger to: $value")
                      if (value) {
                        // Add finger
                        settings.selectedFingers =
                          Finger.encodeSelectedFingers(settings.selectedFingers, finger)
                      } else {
                        // Remove finger
                        settings.selectedFingers =
                          settings.selectedFingers xor (1 shl finger.ordinal)
                      }
                      updateUseAllFingersBasedOnSelection()
                      updateFingerCheckboxesUI()
                    }
                  )
              fingerCheckboxes[finger] = checkBoxCell
            }
          }
        }
      }
    }
    return dialogPanel!!
  }

  /**
   * Updates the UI state of individual finger checkboxes based on current model state. This
   * directly updates the checkbox components without calling apply().
   */
  private fun updateFingerCheckboxesUI() {
    logger.info("updateFingerCheckboxesUI called")
    SwingUtilities.invokeLater {
      logger.info(
        "Updating finger checkboxes UI: useAllFingers=${settings.useAllFingers}, selectedFingers=${settings.selectedFingers} (binary: ${settings.selectedFingers.toString(2).padStart(5, '0')})"
      )
      for ((finger, checkBoxCell) in fingerCheckboxes) {
        val isSelected = Finger.isFingerSelected(settings.selectedFingers, finger)
        logger.info("Setting $finger checkbox to isSelected=$isSelected")
        checkBoxCell.component.isSelected = isSelected
      }
    }
  }

  override fun apply() {
    logger.info("============ APPLY START ============")
    logger.info(
      "Before apply: useAllFingers=${settings.useAllFingers}, selectedFingers=${settings.selectedFingers} (binary: ${settings.selectedFingers.toString(2).padStart(5, '0')})"
    )

    settings.unSelectedAllFingers()
    logger.info("After unSelectedAllFingers: selectedFingers=${settings.selectedFingers}")

    super.apply()
    logger.info(
      "After super.apply: useAllFingers=${settings.useAllFingers}, selectedFingers=${settings.selectedFingers} (binary: ${settings.selectedFingers.toString(2).padStart(5, '0')})"
    )

    // Log current state
    val selectedFingersList =
      Finger.entries
        .filter { Finger.isFingerSelected(settings.selectedFingers, it) }
        .map { it.label }
    logger.info("apply: Selected fingers after super.apply: $selectedFingersList")

    // Check if all fingers are selected
    val allFingersSelected =
      Finger.entries.all { finger -> Finger.isFingerSelected(settings.selectedFingers, finger) }

    logger.info("apply: allFingersSelected=$allFingersSelected")
    logger.info(
      "apply: selectedFingers=${settings.selectedFingers}, Finger.entries.size=${Finger.entries.size}"
    )

    // If all fingers are selected, enable "Use All Fingers"
    if (allFingersSelected) {
      logger.info("apply: All fingers selected, enabling useAllFingers")
      settings.useAllFingers = true
    } else if (settings.selectedFingers == 0) {
      // If no fingers are selected, enable "Use All Fingers" and select all
      logger.info("apply: No fingers selected (invalid state), auto-correcting...")
      settings.useAllFingers = true
      for (finger in Finger.entries) {
        settings.selectedFingers = Finger.encodeSelectedFingers(settings.selectedFingers, finger)
      }
      logger.info(
        "apply: After auto-correction: useAllFingers=${settings.useAllFingers}, selectedFingers=${settings.selectedFingers}"
      )
    } else {
      // If some (but not all) fingers are selected, disable "Use All Fingers"
      logger.info("apply: Some (but not all) fingers selected, disabling useAllFingers")
      settings.useAllFingers = false
    }

    logger.info(
      "Before save: useAllFingers=${settings.useAllFingers}, selectedFingers=${settings.selectedFingers}"
    )
    logger.info("============ APPLY END ============")
  }

  override fun reset() {
    super.reset()
  }

  private fun isFingerSelected(finger: Finger): Boolean {
    return Finger.isFingerSelected(settings.selectedFingers, finger)
  }

  /**
   * Updates finger selections when "Use All Fingers" is toggled.
   *
   * When "Use All Fingers" is checked: Select all fingers. When "Use All Fingers" is unchecked:
   * Clear all selections (let user select specific fingers).
   */
  private fun updateFingerSelectionBasedOnUseAll() {
    logger.info("updateFingerSelectionBasedOnUseAll: useAllFingers=${settings.useAllFingers}")

    if (settings.useAllFingers) {
      // Select all fingers
      logger.info("Selecting all fingers because useAllFingers=true")
      settings.selectedFingers = 0
      for (finger in Finger.entries) {
        settings.selectedFingers = Finger.encodeSelectedFingers(settings.selectedFingers, finger)
      }
      logger.info(
        "After selecting all: selectedFingers=${settings.selectedFingers} (binary: ${settings.selectedFingers.toString(2).padStart(5, '0')})"
      )
    } else {
      // Uncheck all fingers (let user select specific ones)
      logger.info("Clearing all fingers because useAllFingers=false")
      settings.selectedFingers = 0
      logger.info("After clearing: selectedFingers=${settings.selectedFingers}")
    }
  }

  /**
   * Updates "Use All Fingers" checkbox based on individual finger selections.
   *
   * Rules:
   * - If all 5 fingers are selected: Check "Use All Fingers"
   * - If some (but not all) fingers are selected: Uncheck "Use All Fingers"
   * - If no fingers are selected: Check "Use All Fingers" (invalid state prevention)
   */
  private fun updateUseAllFingersBasedOnSelection() {
    logger.info(
      "updateUseAllFingersBasedOnSelection: selectedFingers=${settings.selectedFingers} (binary: ${settings.selectedFingers.toString(2).padStart(5, '0')})"
    )

    val allFingersSelected =
      Finger.entries.all { finger -> Finger.isFingerSelected(settings.selectedFingers, finger) }
    val noFingersSelected = settings.selectedFingers == 0

    logger.info("allFingersSelected=$allFingersSelected, noFingersSelected=$noFingersSelected")

    when {
      allFingersSelected -> {
        logger.info("All fingers selected, enabling 'Use All Fingers'")
        settings.useAllFingers = true
      }
      noFingersSelected -> {
        logger.info(
          "No fingers selected (invalid state), auto-enabling 'Use All Fingers' and selecting all"
        )
        settings.useAllFingers = true
        for (finger in Finger.entries) {
          settings.selectedFingers = Finger.encodeSelectedFingers(settings.selectedFingers, finger)
        }
        logger.info("After auto-correction: selectedFingers=${settings.selectedFingers}")
      }
      else -> {
        logger.info("Some (but not all) fingers selected, disabling 'Use All Fingers'")
        settings.useAllFingers = false
      }
    }

    val selectedFingersList =
      Finger.entries
        .filter { Finger.isFingerSelected(settings.selectedFingers, it) }
        .map { it.label }
    logger.info(
      "Final state: useAllFingers=${settings.useAllFingers}, selectedFingers=$selectedFingersList"
    )
  }
}
