package com.github.pyctam.touchtypingpractice.config

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.options.Configurable
import java.awt.*
import javax.swing.*

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

    // Practice Mode Section
    val practiceModePanel =
      createResponsiveSection("Practice Mode:").apply {
        leftHandRadioButton = JRadioButton("Left Hand")
        rightHandRadioButton = JRadioButton("Right Hand")
        bothHandsRadioButton = JRadioButton("Both Hands")

        val handGroup = ButtonGroup()
        handGroup.add(leftHandRadioButton)
        handGroup.add(rightHandRadioButton)
        handGroup.add(bothHandsRadioButton)

        when (settings.hands) {
          Hands.LEFT_HAND -> leftHandRadioButton?.isSelected = true
          Hands.RIGHT_HAND -> rightHandRadioButton?.isSelected = true
          Hands.BOTH_HANDS -> bothHandsRadioButton?.isSelected = true
        }

        leftHandRadioButton?.addActionListener { settings.hands = Hands.LEFT_HAND }
        rightHandRadioButton?.addActionListener { settings.hands = Hands.RIGHT_HAND }
        bothHandsRadioButton?.addActionListener { settings.hands = Hands.BOTH_HANDS }

        val radioContainer =
          JPanel(FlowLayout(FlowLayout.LEFT, 10, 5)).apply {
            add(leftHandRadioButton)
            add(rightHandRadioButton)
            add(bothHandsRadioButton)
          }

        add(radioContainer, BorderLayout.CENTER)
      }
    panel.add(practiceModePanel)

    // Key Radius Section
    val keyRadiusPanel =
      createSectionPanel("Key Radius (0 to 3):").apply {
        keyRadiusSpinner =
          JSpinner(SpinnerNumberModel(settings.keyRadius, 0, 3, 1)).apply {
            addChangeListener { settings.keyRadius = value as Int }
          }
        add(keyRadiusSpinner, BorderLayout.CENTER)
      }
    panel.add(keyRadiusPanel)

    // Finger Selection Section
    val fingerSelectionPanel =
      createSectionPanel("Finger Selection:").apply {
        val radioPanel = JPanel().apply { layout = BoxLayout(this, BoxLayout.Y_AXIS) }

        allFingersRadioButton =
          JRadioButton("Practice all fingers").apply {
            isSelected = true
            addItemListener { if (isSelected) enableFingerSelection(false) }
          }

        specificFingersRadioButton =
          JRadioButton("Select specific fingers").apply {
            addItemListener { if (isSelected) enableFingerSelection(true) }
          }

        val fingerGroup = ButtonGroup()
        fingerGroup.add(allFingersRadioButton)
        fingerGroup.add(specificFingersRadioButton)

        radioPanel.add(allFingersRadioButton)
        radioPanel.add(specificFingersRadioButton)

        val fingerCheckPanel =
          JPanel(GridLayout(0, 2)).apply {
            isEnabled = false
            val fingerNames = listOf("Thumb", "Index", "Middle", "Ring", "Little")
            fingerNames.forEach { fingerName ->
              val checkBox = JCheckBox(fingerName).apply { isEnabled = false }
              fingerCheckBoxes.add(checkBox)
              add(checkBox)
            }
          }

        add(radioPanel, BorderLayout.NORTH)
        add(fingerCheckPanel, BorderLayout.CENTER)
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

  private fun createResponsiveSection(title: String): JPanel {
    return JPanel(BorderLayout()).apply {
      border = BorderFactory.createTitledBorder(title)
      layout = BorderLayout(5, 5)
    }
  }

  private fun createSectionPanel(title: String): JPanel {
    return JPanel(BorderLayout()).apply {
      border = BorderFactory.createTitledBorder(title)
      layout = BorderLayout(5, 5)
    }
  }
}
