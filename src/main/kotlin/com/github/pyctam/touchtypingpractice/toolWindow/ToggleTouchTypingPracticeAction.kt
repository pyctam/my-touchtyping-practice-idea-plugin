package com.github.pyctam.touchtypingpractice.toolWindow

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager

/**
 * Toggles the Touch Typing Practice tool window: activates (opens) it when it is hidden and hides
 * it when it is visible.
 *
 * Registered in `plugin.xml` and bound to the double-stroke shortcut `Ctrl+Alt+P, P`
 * (`Command+Option+P, P` on macOS) in the default keymap. Because the shortcut is bound through the
 * Action System (not a raw key listener), the action appears in Settings | Keymap, participates in
 * conflict detection, and keeps working when the user rebinds it.
 *
 * Implements [DumbAware] so the tool window can be toggled while the IDE is indexing, matching
 * [TouchTypingToolWindowFactory].
 */
class ToggleTouchTypingPracticeAction : AnAction(), DumbAware {

  override fun actionPerformed(event: AnActionEvent) {
    val project = event.project ?: return
    val toolWindow = ToolWindowManager.getInstance(project).getToolWindow(TOOL_WINDOW_ID) ?: return
    toggle(toolWindow)
  }

  override fun update(event: AnActionEvent) {
    // Always enabled when a project is open.
    event.presentation.isEnabled = event.project != null
  }

  companion object {
    /** Must match the `id` attribute of the `<toolWindow>` entry in `plugin.xml`. */
    const val TOOL_WINDOW_ID = "Touch Typing Practice"

    /**
     * Hides the tool window if it is visible, otherwise activates (shows) it.
     *
     * Kept separate from [actionPerformed] so the toggle decision can be unit-tested without a
     * running IDE.
     */
    fun toggle(toolWindow: ToolWindow) {
      if (toolWindow.isVisible) {
        toolWindow.hide(null)
      } else {
        toolWindow.activate(null)
      }
    }
  }
}
