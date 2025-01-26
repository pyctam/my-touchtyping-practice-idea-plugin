package com.github.pyctam.touchtypingpractice.config

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.options.Configurable
import javax.swing.BoxLayout
import javax.swing.ButtonGroup
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
    private var settings: Settings = Settings() // Retrieve current settings

    override fun getDisplayName(): String {
        return "Touch Typing Practice"
    }

    override fun createComponent(): JPanel {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)

        // Create radio buttons for Practice Mode
        leftHandRadioButton = JRadioButton("Left Hand")
        rightHandRadioButton = JRadioButton("Right Hand")
        bothHandsRadioButton = JRadioButton("Both Hands")

        // Set the initial selected radio button based on settings
        when (settings.hands) {
            Hands.LEFT_HAND -> leftHandRadioButton?.isSelected = true
            Hands.RIGHT_HAND -> rightHandRadioButton?.isSelected = true
            Hands.BOTH_HANDS -> bothHandsRadioButton?.isSelected = true
        }

        // Group the radio buttons so that only one can be selected at a time
        val group = ButtonGroup()
        group.add(leftHandRadioButton)
        group.add(rightHandRadioButton)
        group.add(bothHandsRadioButton)

        // Add listeners to update the settings when a radio button is selected
        leftHandRadioButton?.addActionListener {
            settings.hands = Hands.LEFT_HAND
        }
        rightHandRadioButton?.addActionListener {
            settings.hands = Hands.RIGHT_HAND
        }
        bothHandsRadioButton?.addActionListener {
            settings.hands = Hands.BOTH_HANDS
        }

        // Key Radius Spinner
        val keyRadiusLabel = JLabel("Key Radius (0 to 3)")
        keyRadiusSpinner = JSpinner(SpinnerNumberModel(settings.keyRadius, 0, 3, 1)) // Range from 0 to 3
        keyRadiusSpinner?.addChangeListener {
            settings.keyRadius = keyRadiusSpinner?.value as Int
        }

        // Add components to the panel
        panel.add(JLabel("Practice Mode:"))
        panel.add(leftHandRadioButton)
        panel.add(rightHandRadioButton)
        panel.add(bothHandsRadioButton)
        panel.add(keyRadiusLabel)
        panel.add(keyRadiusSpinner)

        return panel
    }

    override fun isModified(): Boolean {
        // Return true if settings were modified
        val currentSettings = loadSettings()
        return currentSettings.hands != settings.hands || currentSettings.keyRadius != settings.keyRadius
    }

    override fun apply() {
        // Save the new settings
        saveSettings(settings)
    }

    override fun reset() {
        // Reset the UI elements to current settings
        settings = loadSettings()
        when (settings.hands) {
            Hands.LEFT_HAND -> leftHandRadioButton?.isSelected = true
            Hands.RIGHT_HAND -> rightHandRadioButton?.isSelected = true
            Hands.BOTH_HANDS -> bothHandsRadioButton?.isSelected = true
        }
        keyRadiusSpinner?.value = settings.keyRadius
    }

    fun saveSettings(settings: Settings) {
        val properties = PropertiesComponent.getInstance()
        properties.setValue("practiceMode", settings.hands.name)
        logger.info("Saved Practice Mode: ${settings.hands.name}")

        properties.setValue("keyRadius", settings.keyRadius.toString())
        logger.info("Saved Key Radius: ${settings.keyRadius}")
    }

    fun loadSettings(): Settings {
        val properties = PropertiesComponent.getInstance()

        val hands = properties.getValue("practiceMode", Hands.BOTH_HANDS.name)
        logger.info("Loaded Practice Mode: $hands")

        val keyRadius = properties.getInt("keyRadius", 1)
        logger.info("Loaded Key Radius: $keyRadius")

        return Settings(Hands.valueOf(hands), keyRadius)
    }
}