package com.github.pyctam.touchtypingpractice.config

import com.github.pyctam.touchtypingpractice.UIBundle
import com.github.pyctam.touchtypingpractice.UIBundle.CONFIG_FINGER_SELECTION_CHECKBOX_SPECIFIC_FINGERS
import com.github.pyctam.touchtypingpractice.UIBundle.CONFIG_FINGER_SELECTION_CHECKBOX_USE_ALL_FINGERS
import com.github.pyctam.touchtypingpractice.UIBundle.CONFIG_FINGER_SELECTION_ERROR_AT_LEAST_ONE
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
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBLabel
import com.intellij.ui.dsl.builder.bind
import com.intellij.ui.dsl.builder.bindIntValue
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.UIUtil
import javax.swing.JCheckBox

class Configuration : BoundConfigurable("Touch Typing Practice (2)") {
  private val logger: Logger = Logger.getInstance(Configuration::class.java)
  private val settings: Settings = Settings.getInstance()
  private var dialogPanel: DialogPanel? = null
  private val fingerCheckboxes: MutableMap<Finger, JCheckBox> = mutableMapOf()
  private var useAllFingersCheckbox: JCheckBox? = null
  private var fingerSelectionErrorLabel: JBLabel? = null

  // Working copy of the finger selection while the dialog is open. The checkboxes edit this value
  // and the UI is kept in sync with it; it is only committed to [Settings] on Apply. This keeps
  // Cancel safe and lets isModified() detect finger changes.
  private var workingSelectedFingers: Int = Finger.ALL_MASK

  // Guards against re-entrancy while the UI is being synced programmatically from the model.
  private var isSyncingUI = false

  override fun createPanel(): DialogPanel {
    workingSelectedFingers = settings.selectedFingers
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
          // "Use All Fingers" is a shortcut: checked iff all fingers are selected. Checking it
          // selects all fingers in one click; unchecking it clears the selection so the user can
          // pick specific fingers.
          val useAll = checkBox(textUseAllFingers).component
          useAll.isSelected = workingSelectedFingers == Finger.ALL_MASK
          useAll.addActionListener { onUseAllFingersToggled() }
          useAllFingersCheckbox = useAll
        }

        val titleSpecificFingers =
          UIBundle.message(CONFIG_FINGER_SELECTION_CHECKBOX_SPECIFIC_FINGERS)

        buttonsGroup(titleSpecificFingers) {
          for (finger in Finger.entries) {
            row {
              val box = checkBox(finger.label).component
              box.isSelected = Finger.isFingerSelected(workingSelectedFingers, finger)
              box.addActionListener { onFingerToggled(finger) }
              fingerCheckboxes[finger] = box
            }
          }
        }

        row {
          val errorLabel =
            JBLabel("").apply {
              foreground = UIUtil.getErrorForeground()
              isVisible = false
            }
          cell(errorLabel)
          fingerSelectionErrorLabel = errorLabel
        }
      }
    }
    return dialogPanel!!
  }

  /**
   * Handles a user click on the "Use All Fingers" checkbox: selects all fingers when checked,
   * clears the selection when unchecked, then syncs the individual finger checkboxes.
   */
  private fun onUseAllFingersToggled() {
    if (isSyncingUI) return
    val value = useAllFingersCheckbox?.isSelected ?: return
    logger.info("User toggled 'Use All Fingers' to: $value")
    workingSelectedFingers = if (value) Finger.ALL_MASK else 0
    syncFingerSelectionUI()
  }

  /**
   * Handles a user click on an individual finger checkbox: sets or clears that finger's bit in the
   * working selection, then syncs the "Use All Fingers" checkbox and the other finger checkboxes.
   */
  private fun onFingerToggled(finger: Finger) {
    if (isSyncingUI) return
    val value = fingerCheckboxes[finger]?.isSelected ?: return
    logger.info("User toggled $finger to: $value")
    if (value) {
      workingSelectedFingers = Finger.encodeSelectedFingers(workingSelectedFingers, finger)
    } else {
      workingSelectedFingers = workingSelectedFingers and (1 shl finger.ordinal).inv()
    }
    syncFingerSelectionUI()
  }

  /**
   * Syncs the "Use All Fingers" checkbox, all finger checkboxes, and the validation error label
   * from [workingSelectedFingers]. The [isSyncingUI] guard prevents the programmatic checkbox
   * updates from being treated as user actions.
   */
  private fun syncFingerSelectionUI() {
    isSyncingUI = true
    try {
      logger.info(
        "syncFingerSelectionUI: workingSelectedFingers=${workingSelectedFingers} (binary: ${workingSelectedFingers.toString(2).padStart(5, '0')})"
      )
      useAllFingersCheckbox?.isSelected = workingSelectedFingers == Finger.ALL_MASK
      for ((finger, box) in fingerCheckboxes) {
        box.isSelected = Finger.isFingerSelected(workingSelectedFingers, finger)
      }
      updateFingerSelectionError()
    } finally {
      isSyncingUI = false
    }
  }

  /**
   * Shows the "At least one finger should be selected" warning under the Finger Selection group
   * when no fingers are selected, and hides it otherwise.
   */
  private fun updateFingerSelectionError() {
    val errorLabel = fingerSelectionErrorLabel ?: return
    val noFingersSelected = workingSelectedFingers == 0
    errorLabel.text =
      if (noFingersSelected) UIBundle.message(CONFIG_FINGER_SELECTION_ERROR_AT_LEAST_ONE) else ""
    errorLabel.isVisible = noFingersSelected
  }

  override fun isModified(): Boolean {
    // The finger checkboxes are not DSL-bound, so the DSL's own isModified() does not see them.
    // Compare the working value against the saved value to detect finger-selection changes.
    return super.isModified() || workingSelectedFingers != settings.selectedFingers
  }

  override fun apply() {
    logger.info(
      "apply: workingSelectedFingers=${workingSelectedFingers} (binary: ${workingSelectedFingers.toString(2).padStart(5, '0')})"
    )

    // Commits the non-finger fields (font size, practice mode, key limit) to [Settings].
    super.apply()

    if (workingSelectedFingers == 0) {
      // Block saving an invalid state: show the warning under the Finger Selection group and keep
      // the dialog open.
      updateFingerSelectionError()
      throw ConfigurationException(UIBundle.message(CONFIG_FINGER_SELECTION_ERROR_AT_LEAST_ONE))
    }

    settings.selectedFingers = workingSelectedFingers
    logger.info("apply: committed selectedFingers=${settings.selectedFingers}")

    // Publish settings change event to notify listeners (e.g., ToolWindow)
    ApplicationManager.getApplication()
      .messageBus
      .syncPublisher(Settings.SETTINGS_CHANGE_TOPIC)
      .onSettingsChanged()
  }

  override fun reset() {
    // Reloads the non-finger fields (font size, practice mode, key limit) from [Settings].
    super.reset()
    workingSelectedFingers = settings.selectedFingers
    syncFingerSelectionUI()
  }
}
