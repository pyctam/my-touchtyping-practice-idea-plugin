package com.github.pyctam.touchtypingpractice.toolWindow

import com.github.pyctam.touchtypingpractice.config.Settings
import com.github.pyctam.touchtypingpractice.services.PracticeTextGeneratorService
import com.github.pyctam.touchtypingpractice.ui.ErrorCounter
import com.github.pyctam.touchtypingpractice.ui.TouchTypingUIComponentsFactory.Companion.PADDING
import com.github.pyctam.touchtypingpractice.ui.TouchTypingUIComponentsFactory.Companion.createMainPanel
import com.github.pyctam.touchtypingpractice.ui.TouchTypingUIComponentsFactory.Companion.createSampleTextPanel
import com.github.pyctam.touchtypingpractice.ui.TouchTypingUIComponentsFactory.Companion.createTypingInputPanel
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.JBColor
import com.intellij.ui.JBSplitter
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
 *
 * UI Design:
 * - Uses IntelliJ's 8px base unit spacing system (12px standard padding)
 * - Follows IntelliJ Typography Hierarchy (body text = 12pt regular, status = medium weight)
 * - Automatic dark/light theme support via JBColor
 */
class TouchTypingToolWindowFactory : ToolWindowFactory, DumbAware {

  override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    val settings = Settings.getInstance()
    val textGenerator = PracticeTextGeneratorService()
    val typingText = textGenerator.generatePracticeText()
    val errorCounter = ErrorCounter()

    // Create individual panels: sample text and typing input with 50/50 height split
    val sampleTextPanel = createSampleTextPanel(typingText, settings.textFontSize)
    val typingInputPanel = createTypingInputPanel(typingText, errorCounter, settings.textFontSize)

    // Compose sample text and typing panels in a vertical splitter with equal proportion
    val contentSplitter = JBSplitter(true, 0.5f)
    contentSplitter.setHonorComponentsMinimumSize(true)
    contentSplitter.firstComponent = sampleTextPanel
    contentSplitter.secondComponent = typingInputPanel
    contentSplitter.border = JBUI.Borders.empty(PADDING)

    // Create status panel with fixed height
    val statusPanel = createStatusPanel(errorCounter)

    // Add all panels to main container: splitter (center) + status (bottom)
    val mainPanel = createMainPanel()
    mainPanel.addToCenter(contentSplitter)
    mainPanel.addToBottom(statusPanel)

    val contentFactory = ContentFactory.getInstance()
    val content = contentFactory.createContent(mainPanel, null, false)
    toolWindow.contentManager.addContent(content)
  }

  /**
   * Creates the status panel with typing speed and error count. Uses IntelliJ's standard spacing
   * (12px padding) and typography (medium weight).
   */
  private fun createStatusPanel(errorCounter: ErrorCounter): BorderLayoutPanel {
    // Status label with IntelliJ medium font weight for emphasis
    val statusLabel = JBLabel("Typing speed: 0 WPM | Typing errors: 0")
    statusLabel.font = JBFont.medium()

    // Register listener to update status label when error count changes
    errorCounter.addChangeListener {
      statusLabel.text = "Typing speed: 0 WPM | Typing errors: ${errorCounter.getCount()}"
    }

    val panel = BorderLayoutPanel()
    // IntelliJ standard: 12px padding (1.5 x 8px base unit) with 1px top border for visual
    // separation from typing area
    panel.border =
      JBUI.Borders.compound(
        JBUI.Borders.empty(PADDING),
        JBUI.Borders.customLine(JBColor.border(), 1, 0, 0, 0) // 1px border on top only
      )
    panel.background = UIUtil.getPanelBackground()
    panel.addToLeft(statusLabel)

    // Dynamically set minimum and preferred height based on font + padding
    // Ensures status panel has proper visual weight without being too cramped
    val labelHeight = statusLabel.preferredSize.height
    val verticalPadding = JBUI.scale(PADDING) * 2 // 12px top + 12px bottom = 24px total
    val totalHeight = labelHeight + verticalPadding

    panel.minimumSize = JBUI.size(0, totalHeight)
    panel.preferredSize = JBUI.size(0, totalHeight)

    return panel
  }
}
