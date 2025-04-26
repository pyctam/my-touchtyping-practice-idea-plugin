package com.github.pyctam.touchtypingpractice.toolWindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBScrollPane
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextArea
import javax.swing.JTextPane

class TouchTypingToolWindowFactory : ToolWindowFactory {
  override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    val typingArea = createTypingArea()
    val statusPanel = createStatusPanel()

    val mainPanel = createMainPanel(toolWindow)
    mainPanel.add(typingArea, BorderLayout.CENTER)
    mainPanel.add(statusPanel, BorderLayout.SOUTH)
  }

  private fun createStatusPanel(): JPanel {
    val statusLabel = JLabel("Typing speed: 0 WPM")

    val statusPanel = JPanel(BorderLayout())
    statusPanel.add(statusLabel, BorderLayout.WEST)
    statusPanel.preferredSize = Dimension(0, 30) // Fixed height

    return statusPanel
  }

  private fun createTypingArea(): JBSplitter {
    val textPane = createTextPane()
    val typingPane = createTypingPane()

    val splitter = JBSplitter(true, 0.25f) // vertical split, 25% for top
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

  private fun createMainPanel(toolWindow: ToolWindow): JPanel {
    val mainPanel = JPanel(BorderLayout())

    // Add main panel to the tool window
    val contentFactory = toolWindow.contentManager.factory
    val content = contentFactory.createContent(mainPanel, null, false)
    toolWindow.contentManager.addContent(content)

    return mainPanel
  }
}
