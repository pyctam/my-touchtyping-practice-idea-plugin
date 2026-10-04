package com.github.pyctam.touchtypingpractice.toolWindow

import com.github.pyctam.touchtypingpractice.UIBundle
import com.github.pyctam.touchtypingpractice.config.Settings
import com.github.pyctam.touchtypingpractice.config.TextFont
import com.github.pyctam.touchtypingpractice.services.PracticeTextGeneratorService
import com.github.pyctam.touchtypingpractice.ui.EnterKeyDetector
import com.github.pyctam.touchtypingpractice.ui.ErrorCounter
import com.github.pyctam.touchtypingpractice.ui.HandColors
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
import java.awt.FlowLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JPanel
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
  private lateinit var typingArea: JTextPane
  private lateinit var typingInputPanel: JBScrollPane
  private lateinit var mainPanel: BorderLayoutPanel
  private lateinit var hitEnterLabel: JBLabel

  /** Builds the full tool window panel with a freshly generated practice text. */
  fun buildContent(): BorderLayoutPanel {
    val settings = Settings.getInstance()
    val fontFamily = TextFont.effectiveFamily(settings.textFontFamily)
    val typingText = textGenerator.generatePracticeText()

    referenceTextPane =
      TouchTypingUIComponentsFactory.createTextPane(typingText, fontFamily, settings.textFontSize)
    val sampleTextPanel = TouchTypingUIComponentsFactory.createSampleTextPanel(referenceTextPane)
    val resetPanel = createResetPanel()
    val typingInput =
      TouchTypingUIComponentsFactory.createTypingInputComponents(
        referenceTextPane,
        typingText,
        errorCounter,
        fontFamily,
        settings.textFontSize,
        onCompletionChanged = { setHitEnterHintVisible(it) }
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

    typingArea.addKeyListener(
      EnterKeyDetector(::reset) { typingArea.text == referenceTextPane.text }
    )
    return mainPanel
  }

  /** Regenerates the practice text and updates the UI in place. */
  fun reset() {
    val settings = Settings.getInstance()
    val newTypingText = textGenerator.generatePracticeText()

    referenceTextPane.text = newTypingText
    // Re-apply per-hand colors to the new reference text.
    HandColors.applyTo(referenceTextPane, newTypingText)
    // Ensure any completion highlight (light green background) is removed on reset.
    referenceTextPane.highlighter.removeAllHighlights()
    errorCounter.setCount(0)
    setHitEnterHintVisible(false)

    val newInput =
      TouchTypingUIComponentsFactory.createTypingInputComponents(
        referenceTextPane,
        newTypingText,
        errorCounter,
        TextFont.effectiveFamily(settings.textFontFamily),
        settings.textFontSize,
        onCompletionChanged = { setHitEnterHintVisible(it) }
      )
    val newTypingInputPanel = newInput.scrollPane

    mainPanel.remove(typingInputPanel)
    mainPanel.addToCenter(newTypingInputPanel)
    mainPanel.revalidate()
    mainPanel.repaint()

    typingInputPanel = newTypingInputPanel
    typingArea = newInput.typingArea
    typingArea.addKeyListener(
      EnterKeyDetector(::reset) { typingArea.text == referenceTextPane.text }
    )
    typingArea.requestFocusInWindow()

    logger.info("Practice reset: new text generated and UI updated")
  }

  /** Requests focus on the typing area (called after the window is shown). */
  fun requestFocus() {
    typingArea.requestFocusInWindow()
  }

  /**
   * Shows or hides the "Hit Enter to " hint next to the reset link.
   *
   * @param visible true when the practice text is completed (highlighted in green).
   */
  private fun setHitEnterHintVisible(visible: Boolean) {
    if (!::hitEnterLabel.isInitialized) return
    if (hitEnterLabel.isVisible == visible) return
    hitEnterLabel.isVisible = visible
    hitEnterLabel.parent?.revalidate()
    hitEnterLabel.parent?.repaint()
  }

  /**
   * Creates the status panel showing the error count and (later) typing speed.
   *
   * The error count is shown first. The WPM label and the `" | "` separator between them are hidden
   * until WPM is actually implemented; they are revealed together once the feature lands. All
   * strings are sourced from [UIBundle]. Uses IntelliJ's standard spacing (8px padding) and
   * medium-weight typography.
   */
  private fun createStatusPanel(): BorderLayoutPanel {
    val errorsLabel = JBLabel(UIBundle.message(UIBundle.TOOL_WINDOW_STATUS_ERRORS, 0))
    errorsLabel.font = JBFont.medium()

    // Hidden until WPM is implemented: the separator (spaces are layout, added here) and the WPM
    // label are revealed together once the feature lands.
    val separatorLabel =
      JBLabel(" " + UIBundle.message(UIBundle.TOOL_WINDOW_STATUS_SEPARATOR) + " ")
    separatorLabel.font = JBFont.medium()
    separatorLabel.isVisible = false

    val wpmLabel = JBLabel(UIBundle.message(UIBundle.TOOL_WINDOW_STATUS_WPM, 0))
    wpmLabel.font = JBFont.medium()
    wpmLabel.isVisible = false

    errorCounter.addChangeListener {
      errorsLabel.text =
        UIBundle.message(UIBundle.TOOL_WINDOW_STATUS_ERRORS, errorCounter.getCount())
    }

    // Left-aligned row: errors (visible) | separator (hidden) | WPM (hidden).
    val statusRow = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
    statusRow.add(errorsLabel)
    statusRow.add(separatorLabel)
    statusRow.add(wpmLabel)
    statusRow.border = JBUI.Borders.empty(PADDING_SMALL)

    val panel = BorderLayoutPanel()
    panel.background = UIUtil.getPanelBackground()
    panel.addToLeft(statusRow)
    // Fixed height derived from the label plus vertical padding, so the panel keeps a stable
    // visual weight without being cramped.
    val totalHeight = errorsLabel.preferredSize.height + JBUI.scale(PADDING_SMALL) * 2
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
   *
   * A hidden "Hit Enter to " hint label sits to the left of the "reset" link. It is revealed only
   * when the practice text is completed (highlighted in green), so the user learns they can press
   * Enter to reset. In all other states the hint stays hidden.
   */
  private fun createResetPanel(): BorderLayoutPanel {
    val resetLabel =
      JBLabel("<html><a href=''>${UIBundle.message(UIBundle.TOOL_WINDOW_RESET_LINK)}</a></html>")
    resetLabel.addMouseListener(
      object : MouseAdapter() {
        override fun mouseClicked(e: MouseEvent) {
          reset()
        }
      }
    )
    resetLabel.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
    resetLabel.toolTipText = UIBundle.message(UIBundle.TOOL_WINDOW_RESET_TOOLTIP)

    // Hidden hint shown only on completion; same font as the reset link, but in a muted
    // (disabled-foreground) color since it is a secondary hint, not a primary element.
    hitEnterLabel = JBLabel(UIBundle.message(UIBundle.TOOL_WINDOW_RESET_HINT) + " ")
    hitEnterLabel.font = resetLabel.font
    hitEnterLabel.foreground = UIUtil.getLabelDisabledForeground()
    hitEnterLabel.isVisible = false

    // Right-aligned group: "Hit Enter to " (hidden by default) immediately left of "reset".
    val linkPanel = JPanel(FlowLayout(FlowLayout.RIGHT, 0, 0))
    linkPanel.add(hitEnterLabel)
    linkPanel.add(resetLabel)

    val panel = BorderLayoutPanel()
    panel.addToRight(linkPanel)
    panel.border = JBUI.Borders.empty(PADDING_SMALL)
    return panel
  }
}
