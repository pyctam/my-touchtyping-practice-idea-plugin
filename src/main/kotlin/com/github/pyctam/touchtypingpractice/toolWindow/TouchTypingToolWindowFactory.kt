package com.github.pyctam.touchtypingpractice.toolWindow

import com.github.pyctam.touchtypingpractice.config.Settings
import com.github.pyctam.touchtypingpractice.config.SettingsChangeListener
import com.github.pyctam.touchtypingpractice.services.PracticeTextGeneratorService
import com.github.pyctam.touchtypingpractice.ui.ErrorCounter
import com.github.pyctam.touchtypingpractice.ui.TouchTypingDocumentListener
import com.github.pyctam.touchtypingpractice.ui.TouchTypingUIComponentsFactory.Companion.PADDING_SMALL
import com.github.pyctam.touchtypingpractice.ui.TouchTypingUIComponentsFactory.Companion.createMainPanel
import com.github.pyctam.touchtypingpractice.ui.TouchTypingUIComponentsFactory.Companion.createSampleTextPanel
import com.github.pyctam.touchtypingpractice.ui.TouchTypingUIComponentsFactory.Companion.createTextPane
import com.github.pyctam.touchtypingpractice.ui.TouchTypingUIComponentsFactory.Companion.createTypingInputComponents
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.content.ContentFactory
import com.intellij.util.ui.JBFont
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.components.BorderLayoutPanel
import java.awt.Toolkit
import java.awt.event.KeyEvent
import javax.swing.AbstractAction
import javax.swing.JTextArea
import javax.swing.JTextPane
import javax.swing.KeyStroke
import javax.swing.SwingUtilities
import javax.swing.event.DocumentListener

/**
 * ToolWindow factory for the Touch Typing Practice plugin. Creates the main UI for practicing
 * typing with real-time feedback.
 *
 * Implements DumbAware to allow the tool window to be available during IDE indexing operations.
 *
 * Supports hot-reload: subscribes to settings changes via MessageBus and recreates the UI when
 * settings are modified without requiring IDE restart.
 *
 * UI Design:
 * - Uses IntelliJ's 8px base unit spacing system (12px standard padding)
 * - Follows IntelliJ Typography Hierarchy (body text = 13pt regular, status = medium weight)
 * - Automatic dark/light theme support via JBColor
 */
class TouchTypingToolWindowFactory : ToolWindowFactory, DumbAware {
  private val logger: Logger = Logger.getInstance(TouchTypingToolWindowFactory::class.java)

  // Fields to hold references for reset functionality
  private var referenceTextPane: JTextPane? = null
  private var typingArea: JTextArea? = null
  private var currentListener: DocumentListener? = null
  private var errorCounter: ErrorCounter? = null

  override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    logger.info("Creating Touch Typing Practice tool window content")
    errorCounter = ErrorCounter()
    // Create and add initial content
    val mainPanel = createInitialContent(errorCounter!!)
    val contentFactory = ContentFactory.getInstance()
    val content = contentFactory.createContent(mainPanel, null, false)
    toolWindow.contentManager.addContent(content)

    // Subscribe to settings changes to support hot-reload
    val messageBus = ApplicationManager.getApplication().messageBus
    messageBus
      .connect()
      .subscribe(
        Settings.SETTINGS_CHANGE_TOPIC,
        object : SettingsChangeListener {
          override fun onSettingsChanged() {
            logger.info("Settings changed - recreating tool window content")
            SwingUtilities.invokeLater {
              try {
                // Remove all existing content
                toolWindow.contentManager.removeAllContents(true)
                // Recreate and add new content with updated settings
                val updatedMainPanel = createInitialContent(errorCounter!!)
                val updatedContent = contentFactory.createContent(updatedMainPanel, null, false)
                toolWindow.contentManager.addContent(updatedContent)
                logger.info("Tool window content recreated successfully")
              } catch (e: Exception) {
                logger.error("Failed to recreate tool window content: ${e.message}", e)
              }
            }
          }
        }
      )
  }

  /**
   * Creates the initial content panel with all UI components. Separated into a method so it can be
   * called both on first creation and when settings change.
   */
  private fun createInitialContent(errorCounter: ErrorCounter): BorderLayoutPanel {
    val settings = Settings.getInstance()
    val textGenerator = PracticeTextGeneratorService()
    val typingText = textGenerator.generatePracticeText()

    // Create reference text pane with current font size - used by both sample text and typing
    // input panels
    referenceTextPane = createTextPane(typingText, settings.textFontSize)

    // Create individual panels: sample text and typing input, both using the same reference pane
    val sampleTextPanel = createSampleTextPanel(referenceTextPane!!)
    val resetPanel = createResetPanel()
    val typingInputComponents =
      createTypingInputComponents(
        referenceTextPane!!,
        typingText,
        errorCounter,
        settings.textFontSize
      )
    val typingInputPanel = typingInputComponents.scrollPane

    // Store references for reset functionality
    this.typingArea = typingInputComponents.typingArea
    this.currentListener = typingInputComponents.listener
    this.errorCounter = errorCounter

    // Create status panel with fixed height
    val statusPanel = createStatusPanel(errorCounter)

    // Create north panel with sample text and reset hyperlink
    val northPanel = BorderLayoutPanel()
    northPanel.addToCenter(sampleTextPanel)
    northPanel.addToBottom(resetPanel)

    // Add all panels directly to main container: top (sample text + reset) + center (typing) +
    // bottom (status fixed)
    val mainPanel = createMainPanel()
    mainPanel.addToTop(northPanel)
    mainPanel.addToCenter(typingInputPanel)
    mainPanel.addToBottom(statusPanel)

    return mainPanel
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
    statusLabel.border =
      JBUI.Borders.compound(
        JBUI.Borders.empty(PADDING_SMALL),
        // JBUI.Borders.customLine(JBColor.border(), 1)
      )

    val panel = BorderLayoutPanel()
    // IntelliJ standard: 12px padding (1.5 x 8px base unit) with 1px top border for visual
    // separation from typing area
    // panel.border =
    //  JBUI.Borders.compound(
    //    JBUI.Borders.empty(PADDING),
    //    JBUI.Borders.customLine(JBColor.border(), 1, 0, 0, 0) // 1px border on top only
    //  )
    panel.background = UIUtil.getPanelBackground()
    panel.addToLeft(statusLabel)
    // Dynamically set minimum and preferred height based on font + padding
    // Ensures status panel has proper visual weight without being too cramped
    val labelHeight = statusLabel.preferredSize.height
    val verticalPadding = JBUI.scale(PADDING_SMALL) * 2 // 12px top + 12px bottom = 24px total
    val totalHeight = labelHeight + verticalPadding
    panel.minimumSize = JBUI.size(0, totalHeight)
    panel.preferredSize = JBUI.size(0, totalHeight)
    panel.border =
      JBUI.Borders.compound(
        JBUI.Borders.empty(0, PADDING_SMALL, 0, PADDING_SMALL),
        JBUI.Borders.customLine(JBColor.border(), 1, 0, 0, 0)
      )
    return panel
  }

  /**
   * Creates the reset panel with a hyperlink to reset the practice text and input. The hyperlink is
   * right-aligned and includes a platform-independent shortcut (Ctrl/Cmd+R).
   */
  private fun createResetPanel(): BorderLayoutPanel {
    val resetLabel = JBLabel("<html><a href=''>Reset</a></html>")
    resetLabel.addMouseListener(
      object : java.awt.event.MouseAdapter() {
        override fun mouseClicked(e: java.awt.event.MouseEvent) {
          resetPractice()
        }
      }
    )
    resetLabel.cursor = java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR)

    // Platform-independent shortcut
    val shortcutKey = if (SystemInfo.isMac) "Cmd+R" else "Ctrl+R"
    resetLabel.toolTipText = "Reset practice text and clear input ($shortcutKey)"

    // Add shortcut handling
    val shortcut =
      KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().menuShortcutKeyMask)
    resetLabel.inputMap.put(shortcut, "reset")
    resetLabel.actionMap.put(
      "reset",
      object : AbstractAction() {
        override fun actionPerformed(e: java.awt.event.ActionEvent?) {
          resetPractice()
        }
      }
    )

    val panel = BorderLayoutPanel()
    panel.addToRight(resetLabel)
    panel.border = JBUI.Borders.empty(PADDING_SMALL)
    return panel
  }

  /**
   * Resets the practice text and clears the typing input. Regenerates new text using current
   * settings, updates the reference pane, clears the typing area, resets the error counter, and
   * updates the document listener.
   */
  private fun resetPractice() {
    val settings = Settings.getInstance()
    val textGenerator = PracticeTextGeneratorService()
    val newTypingText = textGenerator.generatePracticeText()

    // Update reference text pane
    referenceTextPane?.text = newTypingText

    // Clear typing area
    typingArea?.text = ""

    // Reset error counter
    errorCounter?.setCount(0)

    // Remove old listener and add new one with updated text
    typingArea?.document?.removeDocumentListener(currentListener)
    val newListener =
      TouchTypingDocumentListener(typingArea!!, referenceTextPane!!, newTypingText, errorCounter!!)
    typingArea?.document?.addDocumentListener(newListener)
    currentListener = newListener

    logger.info("Practice reset: new text generated and UI updated")
  }
}
