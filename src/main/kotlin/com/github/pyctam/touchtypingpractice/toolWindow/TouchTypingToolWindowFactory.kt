package com.github.pyctam.touchtypingpractice.toolWindow

import com.github.pyctam.touchtypingpractice.ui.ErrorCounter
import com.github.pyctam.touchtypingpractice.ui.TouchTypingUIComponentsFactory.Companion.PADDING
import com.github.pyctam.touchtypingpractice.ui.TouchTypingUIComponentsFactory.Companion.createMainPanel
import com.github.pyctam.touchtypingpractice.ui.TouchTypingUIComponentsFactory.Companion.createTextPane
import com.github.pyctam.touchtypingpractice.ui.TouchTypingUIComponentsFactory.Companion.createTypingPane
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
    val typingText = createText()
    val errorCounter = ErrorCounter()
    val textPane = createTextPane(typingText, 14 /*settings.textFontSize*/)
    val typingArea =
      createTypingPane(textPane, typingText, errorCounter, 14 /*settings.textFontSize*/)
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

  private fun createText(): String {
    return "The quick brown fox jumps over the lazy dog.".trimIndent()
  }
}
