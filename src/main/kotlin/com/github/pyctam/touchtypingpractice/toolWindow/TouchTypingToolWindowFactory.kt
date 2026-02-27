package com.github.pyctam.touchtypingpractice.toolWindow

import com.github.pyctam.touchtypingpractice.config.PracticeMode
import com.github.pyctam.touchtypingpractice.config.PracticeMode.BOTH_HANDS
import com.github.pyctam.touchtypingpractice.config.Settings
import com.github.pyctam.touchtypingpractice.config.Settings.Companion.DEFAULT_TEXT_FONT_SIZE
import com.github.pyctam.touchtypingpractice.config.Settings.Companion.PROPERTY_KEY_LIMIT_PER_FINGER
import com.github.pyctam.touchtypingpractice.config.Settings.Companion.PROPERTY_PRACTICE_MODE
import com.github.pyctam.touchtypingpractice.config.Settings.Companion.PROPERTY_SELECTED_FINGERS
import com.github.pyctam.touchtypingpractice.config.Settings.Companion.PROPERTY_TEXT_FONT_SIZE
import com.github.pyctam.touchtypingpractice.config.Settings.Companion.PROPERTY_USE_ALL_FINGERS
import com.github.pyctam.touchtypingpractice.services.PracticeTextGeneratorService
import com.github.pyctam.touchtypingpractice.ui.ErrorCounter
import com.github.pyctam.touchtypingpractice.ui.TouchTypingUIComponentsFactory.Companion.PADDING
import com.github.pyctam.touchtypingpractice.ui.TouchTypingUIComponentsFactory.Companion.createMainPanel
import com.github.pyctam.touchtypingpractice.ui.TouchTypingUIComponentsFactory.Companion.createTypingArea
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.content.ContentFactory
import com.intellij.util.ui.JBFont
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.components.BorderLayoutPanel

class TouchTypingToolWindowFactory : ToolWindowFactory {

  override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    val settings = loadSettings()
    val textGenerator = PracticeTextGeneratorService()
    val typingText = textGenerator.generatePracticeText()
    val errorCounter = ErrorCounter()
    val typingArea = createTypingArea(typingText, errorCounter, settings.textFontSize)
    val statusPanel = createStatusPanel(errorCounter)

    val mainPanel = createMainPanel()
    mainPanel.addToCenter(typingArea)
    mainPanel.addToBottom(statusPanel)

    val contentFactory = ContentFactory.getInstance()
    val content = contentFactory.createContent(mainPanel, null, false)
    toolWindow.contentManager.addContent(content)
  }

  private fun createStatusPanel(errorCounter: ErrorCounter): BorderLayoutPanel {
    val statusLabel = JBLabel("Typing speed: 0 WPM | Typing errors: 0")
    statusLabel.font = JBFont.medium()

    // Store reference to status label in error counter for updates
    errorCounter.statusLabel = statusLabel

    val panel = BorderLayoutPanel()
    panel.border = JBUI.Borders.empty(PADDING, PADDING) // Padding: top-bottom 4px, left-right 8px
    panel.background = UIUtil.getPanelBackground()
    panel.addToLeft(statusLabel)

    // Dynamically set minimum and preferred height based on font + padding
    val labelHeight = statusLabel.preferredSize.height
    val verticalPadding = JBUI.scale(PADDING) * 2 // 4px top + 4px bottom
    val totalHeight = labelHeight + verticalPadding

    panel.minimumSize = JBUI.size(0, totalHeight)
    panel.preferredSize = JBUI.size(0, totalHeight)

    return panel
  }

  /**
   * Loads current practice settings from IntelliJ properties.
   *
   * @return The current Settings configuration
   */
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
}
