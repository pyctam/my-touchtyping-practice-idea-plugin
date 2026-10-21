/**
 * Tool window lifecycle and UI for the practice tool window.
 *
 * [TouchTypingToolWindowFactory] manages the tool-window lifecycle and hot-reload on settings
 * changes, while [TouchTypingToolWindow] owns the actual UI construction, real-time feedback
 * wiring, and the Enter-to-reset gesture. [ToggleTouchTypingPracticeAction] is the action that
 * opens or closes the tool window, bound to the double-stroke shortcut.
 */
package com.github.pyctam.touchtypingpractice.toolWindow
