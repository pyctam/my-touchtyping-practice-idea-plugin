/**
 * Swing/IntelliJ UI building blocks for the practice tool window.
 *
 * [TouchTypingUIComponentsFactory] constructs the themed components (reference pane, typing area,
 * panels). [TouchTypingDocumentListener] provides real-time mismatch highlighting and completion
 * feedback, guarded by [TextLengthLimiterFilter]. [ErrorCounter] tracks the mismatch count and
 * notifies listeners, and [EnterKeyDetector] detects the Enter key press that resets the practice
 * text once it has been completed.
 */
package com.github.pyctam.touchtypingpractice.ui
