package com.github.pyctam.touchtypingpractice.config

import com.github.pyctam.touchtypingpractice.config.PracticeMode.BOTH_HANDS
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.messages.Topic
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * Touch Typing Practice settings, persisted at application level via IntelliJ's
 * [PersistentStateComponent] framework (stored in `touchTypingPractice.xml`).
 *
 * After settings are applied, a [SettingsChangeListener] notification is published on
 * [SETTINGS_CHANGE_TOPIC] so dependent UI (the tool window) can hot-reload.
 */
@Service
@State(name = "TouchTypingPracticeSettings", storages = [Storage("touchTypingPractice.xml")])
class Settings : PersistentStateComponent<Settings> {

  /** Font size (pt) for the sample text and typing area. */
  var textFontSize: Int = DEFAULT_TEXT_FONT_SIZE

  /** Which hand(s) to practice. */
  var practiceMode: PracticeMode = BOTH_HANDS

  /** Maximum number of keys each finger may be assigned. */
  var keyLimitPerFinger: Int = 1

  /** Bitmask of enabled fingers, see [Finger]. */
  var selectedFingers: Int = Finger.ALL_MASK

  override fun getState(): Settings = this

  override fun loadState(state: Settings) {
    XmlSerializerUtil.copyBean(state, this)
    // Normalize legacy/invalid states: an empty selection is not allowed, so fall back to all
    // fingers. The legacy <useAllFingers> tag is ignored by the serializer.
    if (selectedFingers == 0) {
      selectedFingers = Finger.ALL_MASK
    }
  }

  companion object {
    /** IntelliJ IDEA standard editor font size. */
    const val DEFAULT_TEXT_FONT_SIZE = 13

    /** Topic published after settings are applied and persisted. */
    val SETTINGS_CHANGE_TOPIC: Topic<SettingsChangeListener> =
      Topic.create("TouchTypingPractice.SettingsChange", SettingsChangeListener::class.java)

    fun getInstance(): Settings =
      ApplicationManager.getApplication().getService(Settings::class.java)
  }
}
