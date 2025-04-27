package com.github.pyctam.touchtypingpractice.toolWindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.JBSplitter
import com.intellij.ui.StatusPanel
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.util.ui.JBFont
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.components.BorderLayoutPanel
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JTextArea
import javax.swing.JTextPane

class TouchTypingToolWindowFactory : ToolWindowFactory {
  override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    val typingArea = createTypingArea()
    val statusPanel = createStatusPanel()
    val mainPanel = createMainPanel()

    mainPanel.add(typingArea, BorderLayout.CENTER)
    mainPanel.add(statusPanel, BorderLayout.SOUTH)

    val contentFactory = ContentFactory.getInstance()
    val content = contentFactory.createContent(mainPanel, null, false)
    toolWindow.contentManager.addContent(content)
  }

  private fun createMainPanel(): BorderLayoutPanel {
    return BorderLayoutPanel()
  }

  private fun createStatusPanel(): JBPanel<StatusPanel> {
    val statusLabel = JBLabel("Typing speed: 0 WPM")
    statusLabel.font = JBFont.medium()

    val panel = JBPanel<StatusPanel>()
    panel.layout = BorderLayout()
    panel.add(statusLabel, BorderLayout.WEST)
    panel.preferredSize = Dimension(0, 30)
    panel.background = UIUtil.getPanelBackground()

    return panel
  }

  private fun createTypingArea(): JBSplitter {
    val textPane = createTextPane()
    val typingPane = createTypingPane()

    val splitter = JBSplitter(true, 0.25f)
    splitter.setHonorComponentsMinimumSize(true)
    splitter.firstComponent = textPane
    splitter.secondComponent = typingPane

    return splitter
  }

  private fun createTypingPane(): JBScrollPane {
    val typingArea = JTextArea()
    typingArea.lineWrap = true
    typingArea.wrapStyleWord = true

    return JBScrollPane(typingArea)
  }

  private fun createTextPane(): JBScrollPane {
    val richTextPane = JTextPane()
    richTextPane.contentType = "text/html"
    richTextPane.text = createText()
    richTextPane.isEditable = false

    return JBScrollPane(richTextPane)
  }

  private fun createText(): String {
    return """
            <html>
            <body>
            <h2>Welcome to the Typing Practice</h2>
            <p>Type the text you see above in the input area below!</p>
            </body>
            </html>
        """
      .trimIndent()
  }
}
