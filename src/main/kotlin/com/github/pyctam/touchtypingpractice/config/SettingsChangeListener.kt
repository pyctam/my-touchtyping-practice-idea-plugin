package com.github.pyctam.touchtypingpractice.config

/**
 * Listener for settings changes.
 *
 * Subscribers are notified over the MessageBus (see [Settings.SETTINGS_CHANGE_TOPIC]) after
 * settings are applied and persisted, enabling hot-reload of dependent UI such as the tool window.
 */
fun interface SettingsChangeListener {
  fun onSettingsChanged()
}
