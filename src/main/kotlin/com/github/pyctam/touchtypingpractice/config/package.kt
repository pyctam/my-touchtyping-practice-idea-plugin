/**
 * Settings model and the settings (Configurable) UI.
 *
 * [Settings] is the persisted application-level state; [SettingsChangeListener] is the MessageBus
 * listener published after changes so dependent UI can hot-reload. [Configuration] is the settings
 * page. The supporting model types are [Finger] (bitmask helpers), [FingerSelection] (the dialog's
 * working copy), [PracticeMode] (which hand(s) to practice), and [TextFont] (available font
 * families).
 */
package com.github.pyctam.touchtypingpractice.config
