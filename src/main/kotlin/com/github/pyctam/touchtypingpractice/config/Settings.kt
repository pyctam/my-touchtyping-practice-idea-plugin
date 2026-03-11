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
 * Listener interface for settings changes. Implementations will be notified when settings are
 * modified and persisted.
 */
interface SettingsChangeListener {
  fun onSettingsChanged()
}

/**
 * Touch Typing Practice settings persisted at application level using IntelliJ's
 * PersistentStateComponent framework. This replaces the legacy PropertiesComponent approach with
 * proper versioned XML serialization.
 */
@Service
@State(name = "TouchTypingPracticeSettings", storages = [Storage("touchTypingPractice.xml")])
class Settings : PersistentStateComponent<Settings> {
  var textFontSize: Int = DEFAULT_TEXT_FONT_SIZE
  var practiceMode: PracticeMode = BOTH_HANDS
  var keyLimitPerFinger: Int = 1
  var useAllFingers: Boolean = true
  var selectedFingers: Int = 0

  fun unSelectedAllFingers() {
    this.selectedFingers = 0
  }

  override fun getState(): Settings = this

  override fun loadState(state: Settings) {
    XmlSerializerUtil.copyBean(state, this)
  }

  companion object {
    const val DEFAULT_TEXT_FONT_SIZE = 13 // IntelliJ IDEA standard editor font size

    val SETTINGS_CHANGE_TOPIC: Topic<SettingsChangeListener> =
      Topic.create("TouchTypingPractice.SettingsChange", SettingsChangeListener::class.java)

    @Suppress("unused")
    fun getInstance(): Settings =
      ApplicationManager.getApplication().getService(Settings::class.java)
  }
}
