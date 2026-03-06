package com.github.pyctam.touchtypingpractice.toolWindow

import com.github.pyctam.touchtypingpractice.config.Settings
import com.github.pyctam.touchtypingpractice.services.PracticeTextGeneratorService
import com.github.pyctam.touchtypingpractice.ui.ErrorCounter
import com.github.pyctam.touchtypingpractice.ui.TouchTypingUIComponentsFactory.Companion.PADDING
import com.github.pyctam.touchtypingpractice.ui.TouchTypingUIComponentsFactory.Companion.createMainPanel
import com.github.pyctam.touchtypingpractice.ui.TouchTypingUIComponentsFactory.Companion.createTypingArea
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.content.ContentFactory
import com.intellij.util.ui.JBFont
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.components.BorderLayoutPanel

/**
 * ToolWindow factory for the Touch Typing Practice plugin. Creates the main UI for practicing
 * typing with real-time feedback.
 *
 * Implements DumbAware to allow the tool window to be available during IDE indexing operations.
 */
class TouchTypingToolWindowFactory : ToolWindowFactory, DumbAware {

  override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    val settings = Settings.getInstance()
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

    // Register listener to update status label when error count changes
    errorCounter.addChangeListener {
      statusLabel.text = "Typing speed: 0 WPM | Typing errors: ${errorCounter.getCount()}"
    }

    val panel = BorderLayoutPanel()
    panel.border = JBUI.Borders.empty(PADDING) // Uniform padding
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
}
