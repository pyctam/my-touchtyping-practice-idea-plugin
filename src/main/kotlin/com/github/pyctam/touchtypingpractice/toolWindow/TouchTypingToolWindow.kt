package com.github.pyctam.touchtypingpractice.toolWindow

import com.github.pyctam.touchtypingpractice.config.Settings
import com.github.pyctam.touchtypingpractice.services.PracticeTextGeneratorService
import com.github.pyctam.touchtypingpractice.ui.ErrorCounter
import com.github.pyctam.touchtypingpractice.ui.ResetKeyDetector
import com.github.pyctam.touchtypingpractice.ui.TouchTypingUIComponentsFactory
import com.github.pyctam.touchtypingpractice.ui.TouchTypingUIComponentsFactory.PADDING_SMALL
import com.intellij.openapi.diagnostic.Logger
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBFont
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.components.BorderLayoutPanel
import java.awt.Cursor
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JTextArea
import javax.swing.JTextPane

/**
 * Owns the Touch Typing Practice tool window UI and its state.
 *
 * Builds the panel (sample text, reset link, typing input, status bar), wires the real-time
 * feedback, and handles the reset gesture. Kept separate from [TouchTypingToolWindowFactory] so the
 * factory only manages tool-window lifecycle and hot-reload, while this class manages the UI.
 *
 * A fresh instance (and therefore a fresh [ErrorCounter]) is created each time the content is
 * (re)built, which avoids accumulating change listeners across hot-reloads.
 */
class TouchTypingToolWindow {

  private val logger = Logger.getInstance(TouchTypingToolWindow::class.java)
  private val errorCounter = ErrorCounter()
  private val textGenerator = PracticeTextGeneratorService()

  private lateinit var referenceTextPane: JTextPane
  private lateinit var typingArea: JTextArea
  private lateinit var typingInputPanel: JBScrollPane
  private lateinit var mainPanel: BorderLayoutPanel

  /** Builds the full tool window panel with a freshly generated practice text. */
  fun buildContent(): BorderLayoutPanel {
    val settings = Settings.getInstance()
    val typingText = textGenerator.generatePracticeText()

    referenceTextPane =
      TouchTypingUIComponentsFactory.createTextPane(typingText, settings.textFontSize)
    val sampleTextPanel = TouchTypingUIComponentsFactory.createSampleTextPanel(referenceTextPane)
    val resetPanel = createResetPanel()
    val typingInput =
      TouchTypingUIComponentsFactory.createTypingInputComponents(
        referenceTextPane,
        typingText,
        errorCounter,
        settings.textFontSize
      )
    typingInputPanel = typingInput.scrollPane
    typingArea = typingInput.typingArea

    val statusPanel = createStatusPanel()

    val northPanel = BorderLayoutPanel()
    northPanel.addToCenter(sampleTextPanel)
    northPanel.addToBottom(resetPanel)

    mainPanel = TouchTypingUIComponentsFactory.createMainPanel()
    mainPanel.addToTop(northPanel)
    mainPanel.addToCenter(typingInputPanel)
    mainPanel.addToBottom(statusPanel)

    typingArea.addKeyListener(ResetKeyDetector(::reset))
    return mainPanel
  }

  /** Regenerates the practice text and updates the UI in place. */
  fun reset() {
    val settings = Settings.getInstance()
    val newTypingText = textGenerator.generatePracticeText()

    referenceTextPane.text = newTypingText
    // Ensure any completion highlight (light green background) is removed on reset.
    referenceTextPane.highlighter.removeAllHighlights()
    errorCounter.setCount(0)

    val newInput =
      TouchTypingUIComponentsFactory.createTypingInputComponents(
        referenceTextPane,
        newTypingText,
        errorCounter,
        settings.textFontSize
      )
    val newTypingInputPanel = newInput.scrollPane

    mainPanel.remove(typingInputPanel)
    mainPanel.addToCenter(newTypingInputPanel)
    mainPanel.revalidate()
    mainPanel.repaint()

    typingInputPanel = newTypingInputPanel
    typingArea = newInput.typingArea
    typingArea.addKeyListener(ResetKeyDetector(::reset))
    typingArea.requestFocusInWindow()

    logger.info("Practice reset: new text generated and UI updated")
  }

  /** Requests focus on the typing area (called after the window is shown). */
  fun requestFocus() {
    typingArea.requestFocusInWindow()
  }

  /**
   * Creates the status panel showing typing speed and error count. Uses IntelliJ's standard spacing
   * (8px padding) and medium-weight typography.
   */
  private fun createStatusPanel(): BorderLayoutPanel {
    val statusLabel = JBLabel("Typing speed: 0 WPM | Typing errors: 0")
    statusLabel.font = JBFont.medium()
    errorCounter.addChangeListener {
      statusLabel.text = "Typing speed: 0 WPM | Typing errors: ${errorCounter.getCount()}"
    }
    statusLabel.border = JBUI.Borders.compound(JBUI.Borders.empty(PADDING_SMALL))

    val panel = BorderLayoutPanel()
    panel.background = UIUtil.getPanelBackground()
    panel.addToLeft(statusLabel)
    // Fixed height derived from the label plus vertical padding, so the panel keeps a stable
    // visual weight without being cramped.
    val totalHeight = statusLabel.preferredSize.height + JBUI.scale(PADDING_SMALL) * 2
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
   * Creates the reset panel with a right-aligned hyperlink that resets the practice text and input.
   */
  private fun createResetPanel(): BorderLayoutPanel {
    val resetLabel = JBLabel("<html><a href=''>Reset</a></html>")
    resetLabel.addMouseListener(
      object : MouseAdapter() {
        override fun mouseClicked(e: MouseEvent) {
          reset()
        }
      }
    )
    resetLabel.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
    resetLabel.toolTipText = "Reset practice text and clear input (press R 3 times quickly)"

    val panel = BorderLayoutPanel()
    panel.addToRight(resetLabel)
    panel.border = JBUI.Borders.empty(PADDING_SMALL)
    return panel
  }
}
