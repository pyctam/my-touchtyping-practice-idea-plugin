package com.github.pyctam.touchtypingpractice.toolWindow

import com.github.pyctam.touchtypingpractice.config.Settings
import com.github.pyctam.touchtypingpractice.config.SettingsChangeListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import javax.swing.SwingUtilities

/**
 * ToolWindow factory for the Touch Typing Practice plugin.
 *
 * Responsible only for tool-window lifecycle: it creates the initial content and subscribes to
 * settings changes to rebuild the content on the EDT (hot-reload). All UI construction and the
 * reset gesture live in [TouchTypingToolWindow].
 *
 * Implements [DumbAware] so the tool window stays available during IDE indexing.
 */
class TouchTypingToolWindowFactory : ToolWindowFactory, DumbAware {

  private val logger = Logger.getInstance(TouchTypingToolWindowFactory::class.java)

  override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    logger.info("Creating Touch Typing Practice tool window content")
    val contentFactory = ContentFactory.getInstance()
    val window = TouchTypingToolWindow()

    toolWindow.contentManager.addContent(
      contentFactory.createContent(window.buildContent(), null, false)
    )
    SwingUtilities.invokeLater { window.requestFocus() }

    // Subscribe to settings changes to support hot-reload.
    ApplicationManager.getApplication()
      .messageBus
      .connect()
      .subscribe(
        Settings.SETTINGS_CHANGE_TOPIC,
        object : SettingsChangeListener {
          override fun onSettingsChanged() {
            logger.info("Settings changed - recreating tool window content")
            SwingUtilities.invokeLater {
              try {
                toolWindow.contentManager.removeAllContents(true)
                toolWindow.contentManager.addContent(
                  contentFactory.createContent(window.buildContent(), null, false)
                )
                window.requestFocus()
                logger.info("Tool window content recreated successfully")
              } catch (e: Exception) {
                logger.error("Failed to recreate tool window content: ${e.message}", e)
              }
            }
          }
        }
      )
  }
}
