package com.github.pyctam.touchtypingpractice.config

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.options.Configurable
import java.awt.event.ItemEvent
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.ButtonGroup
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

class Configuration : Configurable {
  private val logger: Logger = Logger.getInstance(Configuration::class.java)

  private var leftHandRadioButton: JRadioButton? = null
  private var rightHandRadioButton: JRadioButton? = null
  private var bothHandsRadioButton: JRadioButton? = null
  private var keyRadiusSpinner: JSpinner? = null

  private var allFingersRadioButton: JRadioButton? = null
  private var specificFingersRadioButton: JRadioButton? = null
  private val fingerCheckBoxes: MutableList<JCheckBox> = mutableListOf()

  private var settings: Settings = loadSettings()

  override fun getDisplayName(): String {
    return "Touch Typing Practice"
  }

  override fun createComponent(): JPanel {
    val panel = JPanel()
    panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)

    // Practice Mode Radio Buttons (Left/Right/Both Hands)
    val practiceModePanel =
      JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        add(JLabel("Practice Mode:"))
      }

    leftHandRadioButton = JRadioButton("Left Hand")
    rightHandRadioButton = JRadioButton("Right Hand")
    bothHandsRadioButton = JRadioButton("Both Hands")

    val handGroup = ButtonGroup()
    handGroup.add(leftHandRadioButton)
    handGroup.add(rightHandRadioButton)
    handGroup.add(bothHandsRadioButton)

    // Set initial selected option based on settings
    when (settings.hands) {
      Hands.LEFT_HAND -> leftHandRadioButton?.isSelected = true
      Hands.RIGHT_HAND -> rightHandRadioButton?.isSelected = true
      Hands.BOTH_HANDS -> bothHandsRadioButton?.isSelected = true
    }

    leftHandRadioButton?.addActionListener { settings.hands = Hands.LEFT_HAND }
    rightHandRadioButton?.addActionListener { settings.hands = Hands.RIGHT_HAND }
    bothHandsRadioButton?.addActionListener { settings.hands = Hands.BOTH_HANDS }

    practiceModePanel.add(leftHandRadioButton)
    practiceModePanel.add(rightHandRadioButton)
    practiceModePanel.add(bothHandsRadioButton)
    panel.add(practiceModePanel)

    // Key Radius Spinner
    val keyRadiusLabel = JLabel("Key Radius (0 to 3):")
    keyRadiusSpinner = JSpinner(SpinnerNumberModel(settings.keyRadius, 0, 3, 1))
    keyRadiusSpinner?.addChangeListener { settings.keyRadius = keyRadiusSpinner?.value as Int }
    panel.add(keyRadiusLabel)
    panel.add(keyRadiusSpinner)

    // Finger Selection Panel
    val fingerSelectionPanel =
      JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        border = BorderFactory.createTitledBorder("Finger Selection")
      }

    allFingersRadioButton =
      JRadioButton("Practice all fingers").apply {
        isSelected = true
        addItemListener {
          if (it.stateChange == ItemEvent.SELECTED) {
            enableFingerSelection(false)
          }
        }
      }

    specificFingersRadioButton =
      JRadioButton("Select specific fingers").apply {
        addItemListener {
          if (it.stateChange == ItemEvent.SELECTED) {
            enableFingerSelection(true)
          }
        }
      }

    val fingerGroup = ButtonGroup()
    fingerGroup.add(allFingersRadioButton)
    fingerGroup.add(specificFingersRadioButton)

    fingerSelectionPanel.add(allFingersRadioButton)
    fingerSelectionPanel.add(specificFingersRadioButton)

    val fingerNames = listOf("Thumb", "Index", "Middle", "Ring", "Little")
    fingerNames.forEach { fingerName ->
      val checkBox = JCheckBox(fingerName).apply { isEnabled = false }
      fingerCheckBoxes.add(checkBox)
      fingerSelectionPanel.add(checkBox)
    }

    panel.add(fingerSelectionPanel)

    return panel
  }

  override fun isModified(): Boolean {
    val currentSettings = loadSettings()
    val selectedFingers = fingerCheckBoxes.filter { it.isSelected }.map { it.text }
    return currentSettings.hands != settings.hands ||
      currentSettings.keyRadius != settings.keyRadius ||
      currentSettings.allFingers != settings.allFingers ||
      currentSettings.selectedFingers != selectedFingers
  }

  override fun apply() {
    // Save settings
    val selectedFingers = fingerCheckBoxes.filter { it.isSelected }.map { it.text }
    settings.selectedFingers = selectedFingers
    settings.allFingers = allFingersRadioButton?.isSelected == true

    saveSettings(settings)
  }

  override fun reset() {
    settings = loadSettings()

    when (settings.hands) {
      Hands.LEFT_HAND -> leftHandRadioButton?.isSelected = true
      Hands.RIGHT_HAND -> rightHandRadioButton?.isSelected = true
      Hands.BOTH_HANDS -> bothHandsRadioButton?.isSelected = true
    }

    keyRadiusSpinner?.value = settings.keyRadius

    if (settings.allFingers) {
      allFingersRadioButton?.isSelected = true
      enableFingerSelection(false)
    } else {
      specificFingersRadioButton?.isSelected = true
      enableFingerSelection(true)
      fingerCheckBoxes.forEach { it.isSelected = settings.selectedFingers.contains(it.text) }
    }
  }

  private fun enableFingerSelection(enable: Boolean) {
    fingerCheckBoxes.forEach { it.isEnabled = enable }
  }

  private fun saveSettings(settings: Settings) {
    val properties = PropertiesComponent.getInstance()
    properties.setValue("practiceMode", settings.hands.name)
    properties.setValue("keyRadius", settings.keyRadius.toString())
    properties.setValue("allFingers", settings.allFingers.toString())
    properties.setValue("selectedFingers", settings.selectedFingers.joinToString(","))
    logger.info("Settings saved: $settings")
  }

  private fun loadSettings(): Settings {
    val properties = PropertiesComponent.getInstance()
    val hands = Hands.valueOf(properties.getValue("practiceMode", Hands.BOTH_HANDS.name))
    val keyRadius = properties.getInt("keyRadius", 1)
    val allFingers = properties.getBoolean("allFingers", true)
    val selectedFingers =
      properties.getValue("selectedFingers", "").split(",").filter { it.isNotEmpty() }

    return Settings(hands, keyRadius, allFingers, selectedFingers)
  }
}
