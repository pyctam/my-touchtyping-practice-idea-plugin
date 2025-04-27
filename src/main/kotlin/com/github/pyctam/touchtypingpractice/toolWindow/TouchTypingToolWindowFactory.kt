package com.github.pyctam.touchtypingpractice.toolWindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.util.ui.JBFont
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.components.BorderLayoutPanel
import javax.swing.JTextArea
import javax.swing.JTextPane

class TouchTypingToolWindowFactory : ToolWindowFactory {
  override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    val typingArea = createTypingArea()
    val statusPanel = createStatusPanel()
    val mainPanel = createMainPanel()

    mainPanel.addToCenter(typingArea)
    mainPanel.addToBottom(statusPanel)

    val contentFactory = ContentFactory.getInstance()
    val content = contentFactory.createContent(mainPanel, null, false)
    toolWindow.contentManager.addContent(content)
  }

  private fun createMainPanel(): BorderLayoutPanel {
    return BorderLayoutPanel()
  }

  private fun createStatusPanel(): BorderLayoutPanel {
    val statusLabel = JBLabel("Typing speed: 0 WPM")
    statusLabel.font = JBFont.medium()

    val panel = BorderLayoutPanel()
    panel.border = JBUI.Borders.empty(4, 8) // Padding: top-bottom 4px, left-right 8px
    panel.background = UIUtil.getPanelBackground()
    panel.addToLeft(statusLabel)

    // Dynamically set minimum and preferred height based on font + padding
    val labelHeight = statusLabel.preferredSize.height
    val verticalPadding = JBUI.scale(4) * 2 // 4px top + 4px bottom
    val totalHeight = labelHeight + verticalPadding

    panel.minimumSize = JBUI.size(0, totalHeight)
    panel.preferredSize = JBUI.size(0, totalHeight)

    return panel
  }

  private fun createTypingArea(): JBSplitter {
    val textPane = createTextPane()
    val typingPane = createTypingPane()

    val splitter = JBSplitter(true, 0.25f)
    splitter.setHonorComponentsMinimumSize(true)
    splitter.firstComponent = textPane
    splitter.secondComponent = typingPane
    splitter.border = JBUI.Borders.empty(8) // Padding inside splitter
    return splitter
  }

  private fun createTypingPane(): JBScrollPane {
    val typingArea = JTextArea()
    typingArea.lineWrap = true
    typingArea.wrapStyleWord = true

    val scrollPane = JBScrollPane(typingArea)
    scrollPane.border = JBUI.Borders.empty() // Remove ugly default border
    return scrollPane
  }

  private fun createTextPane(): JBScrollPane {
    val richTextPane = JTextPane()
    richTextPane.contentType = "text/html"
    richTextPane.text = createText()
    richTextPane.isEditable = false

    val scrollPane = JBScrollPane(richTextPane)
    scrollPane.border = JBUI.Borders.empty() // Clean native look
    return scrollPane
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
