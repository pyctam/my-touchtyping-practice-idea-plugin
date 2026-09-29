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
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBLabel
import com.intellij.ui.dsl.builder.bind
import com.intellij.ui.dsl.builder.bindIntValue
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.UIUtil
import javax.swing.JCheckBox

/**
 * Project-level settings page for Touch Typing Practice.
 *
 * The font size, practice mode, and key-limit fields are bound to [Settings] directly through the
 * IntelliJ UI DSL. The finger selection is not DSL-bound (it is a bitmask with a derived "Use All
 * Fingers" shortcut), so it is edited through a [FingerSelection] working copy and committed to
 * [Settings] on Apply. This keeps Cancel safe and lets [isModified] detect changes.
 */
class Configuration : BoundConfigurable("Touch Typing Practice") {

  private val settings: Settings = Settings.getInstance()

  /** Working copy of the finger selection while the dialog is open. */
  private var fingerSelection: FingerSelection = FingerSelection()

  private var useAllFingersCheckbox: JCheckBox? = null
  private val fingerCheckboxes: MutableMap<Finger, JCheckBox> = mutableMapOf()
  private var fingerSelectionErrorLabel: JBLabel? = null

  // Guards against re-entrancy while the UI is synced programmatically from the model.
  private var isSyncingUI = false

  override fun createPanel(): DialogPanel {
    fingerSelection = FingerSelection(settings.selectedFingers)
    return panel {
      group(UIBundle.message(TEXT_FONT_SIZE_TITLE)) {
        row { comment(UIBundle.message(UIBundle.TEXT_FONT_SIZE_HINT)) }
        row { spinner(8..24, 1).bindIntValue(settings::textFontSize) }
      }

      group(UIBundle.message(CONFIG_PRACTICE_MODE_TITLE)) {
        buttonsGroup {
            row { comment(UIBundle.message(CONFIG_PRACTICE_MODE_HINT)) }
            row {
              radioButton(UIBundle.message(CONFIG_PRACTICE_MODE_RADIO_LEFTHAND), LEFT_HAND)
              radioButton(UIBundle.message(CONFIG_PRACTICE_MODE_RADIO_RIGHTHAND), RIGHT_HAND)
              radioButton(UIBundle.message(CONFIG_PRACTICE_MODE_RADIO_BOTHHANDS), BOTH_HANDS)
            }
          }
          .bind({ settings.practiceMode }, { settings.practiceMode = it })
      }

      group(UIBundle.message(CONFIG_KEY_LIMIT_PER_FINDER_TITLE)) {
        row { comment(UIBundle.message(CONFIG_KEY_LIMIT_PER_FINDER_HINT)) }
        row { spinner(1..6).bindIntValue(settings::keyLimitPerFinger) }
      }

      group(UIBundle.message(CONFIG_FINGER_SELECTION_TITLE)) {
        row { comment(UIBundle.message(CONFIG_FINGER_SELECTION_HINT)) }

        // "Use All Fingers" is a shortcut: checked iff all fingers are selected. Checking it
        // selects all fingers in one click; unchecking it clears the selection so the user can
        // pick specific fingers.
        row {
          val useAll =
            checkBox(UIBundle.message(CONFIG_FINGER_SELECTION_CHECKBOX_USE_ALL_FINGERS)).component
          useAll.isSelected = fingerSelection.isAllSelected
          useAll.addActionListener { onUseAllFingersToggled() }
          useAllFingersCheckbox = useAll
        }

        buttonsGroup(UIBundle.message(CONFIG_FINGER_SELECTION_CHECKBOX_SPECIFIC_FINGERS)) {
          for (finger in Finger.entries) {
            row {
              val box = checkBox(finger.label).component
              box.isSelected = fingerSelection.isSelected(finger)
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
  }

  /** Handles a click on the "Use All Fingers" checkbox: updates the model, then syncs the UI. */
  private fun onUseAllFingersToggled() {
    if (isSyncingUI) return
    val value = useAllFingersCheckbox?.isSelected ?: return
    fingerSelection.setUseAllFingers(value)
    syncFingerSelectionUI()
  }

  /** Handles a click on an individual finger checkbox: updates the model, then syncs the UI. */
  private fun onFingerToggled(finger: Finger) {
    if (isSyncingUI) return
    val value = fingerCheckboxes[finger]?.isSelected ?: return
    fingerSelection.setFinger(finger, value)
    syncFingerSelectionUI()
  }

  /**
   * Syncs the "Use All Fingers" checkbox, all finger checkboxes, and the validation error label
   * from [fingerSelection]. The [isSyncingUI] guard prevents the programmatic checkbox updates from
   * being treated as user actions.
   */
  private fun syncFingerSelectionUI() {
    isSyncingUI = true
    try {
      useAllFingersCheckbox?.isSelected = fingerSelection.isAllSelected
      for ((finger, box) in fingerCheckboxes) {
        box.isSelected = fingerSelection.isSelected(finger)
      }
      updateFingerSelectionError()
    } finally {
      isSyncingUI = false
    }
  }

  /**
   * Shows the "at least one finger" warning under the Finger Selection group when no fingers are
   * selected, and hides it otherwise.
   */
  private fun updateFingerSelectionError() {
    val errorLabel = fingerSelectionErrorLabel ?: return
    val noFingersSelected = fingerSelection.isEmpty
    errorLabel.text =
      if (noFingersSelected) UIBundle.message(CONFIG_FINGER_SELECTION_ERROR_AT_LEAST_ONE) else ""
    errorLabel.isVisible = noFingersSelected
  }

  override fun isModified(): Boolean {
    // The finger checkboxes are not DSL-bound, so the DSL's own isModified() does not see them.
    // Compare the working value against the saved value to detect finger-selection changes.
    return super.isModified() || fingerSelection.mask != settings.selectedFingers
  }

  override fun apply() {
    // Commits the non-finger fields (font size, practice mode, key limit) to [Settings].
    super.apply()

    if (fingerSelection.isEmpty) {
      // Block saving an invalid state: show the warning and keep the dialog open.
      updateFingerSelectionError()
      throw ConfigurationException(UIBundle.message(CONFIG_FINGER_SELECTION_ERROR_AT_LEAST_ONE))
    }

    settings.selectedFingers = fingerSelection.mask

    // Publish a settings-change event to notify listeners (e.g., the tool window).
    ApplicationManager.getApplication()
      .messageBus
      .syncPublisher(Settings.SETTINGS_CHANGE_TOPIC)
      .onSettingsChanged()
  }

  override fun reset() {
    // Reloads the non-finger fields (font size, practice mode, key limit) from [Settings].
    super.reset()
    fingerSelection = FingerSelection(settings.selectedFingers)
    syncFingerSelectionUI()
  }
}
