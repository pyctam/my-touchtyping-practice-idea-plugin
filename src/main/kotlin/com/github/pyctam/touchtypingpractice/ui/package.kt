/**
 * Swing/IntelliJ UI building blocks for the practice tool window.
 *
 * [TouchTypingUIComponentsFactory] constructs the themed components (reference pane, typing area,
 * panels). [TouchTypingDocumentListener] provides real-time mismatch highlighting and completion
 * feedback, guarded by [TextLengthLimiterFilter]. [TextComparison] is the pure helper for comparing
 * typed text against the reference text (mismatch indices, error detection). [TextColors] applies
 * the per-character coloring (none, by hand, or by finger) to the reference and typing panes.
 * [ErrorCounter] tracks the mismatch count and notifies listeners, [TypingStats] tracks typing
 * speed (WPM) and accuracy for the session, and [EnterKeyDetector] detects the Enter key press that
 * resets the practice text once it has been completed.
 */
package com.github.pyctam.touchtypingpractice.ui
