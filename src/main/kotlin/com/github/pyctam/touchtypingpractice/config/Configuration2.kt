package com.github.pyctam.touchtypingpractice.config

import com.github.pyctam.touchtypingpractice.config.Hands.BOTH_HANDS
import com.github.pyctam.touchtypingpractice.config.Hands.LEFT_HAND
import com.github.pyctam.touchtypingpractice.config.Hands.RIGHT_HAND
import com.github.pyctam.touchtypingpractice.config.Settings.Companion.PROPERTY_PRACTICE_MODE
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.bind
import com.intellij.ui.dsl.builder.panel

class Configuration2 : BoundConfigurable("Touch Typing Practice (2)") {
    private val logger: Logger = Logger.getInstance(Configuration2::class.java)

    private var settings: Settings = loadSettings();

    override fun createPanel(): DialogPanel {
        return panel {
            group("Practice Mode:") {
                buttonsGroup {
                    row {
                        comment(
                            "Select which hand you want to use for touch typing practice. 'Left Hand' focuses on " +
                                    "left-hand keys, 'Right Hand' trains right-hand keys, and 'Both Hands' provides " +
                                    "a full-keyboard experience."
                        )
                    }
                    row {
                        radioButton("Left Hand", LEFT_HAND)
                        radioButton("Right Hand", RIGHT_HAND)
                        radioButton("Both Hands", BOTH_HANDS)
                    }
                }.bind({
                    settings.practiceMode

                }, {
                    settings.practiceMode = it
                })
            }
            group("Key Limit Per Finger (1-6):") {
                row {
                    comment(
                        "Defines the maximum number of keys each finger can be assigned during practice. A lower " +
                                "value enforces strict finger placement, while a higher value allows more " +
                                "flexibility in key coverage."
                    )
                }
            }
            group("Finger Selection:") {
                row {
                    comment(
                        "Choose which fingers to use for touch typing practice. Select specific fingers or enable " +
                                "all fingers for a full-hand experience. This setting helps tailor the practice " +
                                "to your typing style and comfort level."
                    )
                }
            }
        }
    }

    override fun apply() {
        super.apply()
        saveSettings();
    }

    override fun reset() {
        super.reset()
        saveSettings();
    }

    private fun loadSettings(): Settings {
        val properties = PropertiesComponent.getInstance()

        val practiceModeName = properties.getValue(PROPERTY_PRACTICE_MODE, BOTH_HANDS.name);
        val practiceMode = Hands.valueOf(practiceModeName)


        val keyRadius = properties.getInt("keyRadius", 1)
        val allFingers = properties.getBoolean("allFingers", true)
        val selectedFingers =
            properties.getValue("selectedFingers", "").split(",").filter { it.isNotEmpty() }

        return Settings(practiceMode, keyRadius, allFingers, selectedFingers)
    }

    private fun saveSettings() {
        val practiceMode = this.settings.practiceMode.name;

        val properties = PropertiesComponent.getInstance()
        properties.setValue(PROPERTY_PRACTICE_MODE, practiceMode)
        properties.setValue("keyRadius", this.settings.keyRadius.toString())
        properties.setValue("allFingers", this.settings.allFingers.toString())
        properties.setValue("selectedFingers", this.settings.selectedFingers.joinToString(","))
        logger.info("Settings saved: ${this.settings}")
    }
}
