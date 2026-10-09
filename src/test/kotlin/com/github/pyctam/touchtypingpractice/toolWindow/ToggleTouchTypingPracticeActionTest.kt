package com.github.pyctam.touchtypingpractice.toolWindow

import com.intellij.openapi.wm.ToolWindow
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [ToggleTouchTypingPracticeAction]: the toggle decision (hide when visible,
 * activate when hidden), repeated toggling, and the tool window id constant.
 */
class ToggleTouchTypingPracticeActionTest {

  /**
   * A minimal [ToolWindow] double built with a dynamic proxy: records `hide`/`activate` calls and
   * reports a configurable visibility state, without requiring a running IDE.
   */
  private class FakeToolWindow(initiallyVisible: Boolean) {
    var visible: Boolean = initiallyVisible
      private set

    var hideCalls = 0
      private set

    var activateCalls = 0
      private set

    private val handler: InvocationHandler = InvocationHandler { _, method, _ ->
      when (method.name) {
        "isVisible" -> visible
        "hide" -> {
          hideCalls++
          visible = false
          null
        }
        "activate" -> {
          activateCalls++
          visible = true
          null
        }
        "getId" -> ToggleTouchTypingPracticeAction.TOOL_WINDOW_ID
        else ->
          when (method.returnType) {
            Boolean::class.javaPrimitiveType -> false
            Int::class.javaPrimitiveType -> 0
            Float::class.javaPrimitiveType -> 0f
            else -> null
          }
      }
    }

    val toolWindow: ToolWindow =
      Proxy.newProxyInstance(
        ToolWindow::class.java.classLoader,
        arrayOf(ToolWindow::class.java),
        handler
      ) as ToolWindow
  }

  @Test
  fun toolWindowIdMatchesPluginXml() {
    // Must stay in sync with the <toolWindow id="..."> entry in plugin.xml.
    assertEquals("Touch Typing Practice", ToggleTouchTypingPracticeAction.TOOL_WINDOW_ID)
  }

  @Test
  fun toggleHidesVisibleToolWindow() {
    val fake = FakeToolWindow(initiallyVisible = true)

    ToggleTouchTypingPracticeAction.toggle(fake.toolWindow)

    assertEquals("A visible tool window should be hidden", 1, fake.hideCalls)
    assertEquals("A visible tool window should not be activated", 0, fake.activateCalls)
    assertFalse("The tool window should be hidden after the toggle", fake.visible)
  }

  @Test
  fun toggleActivatesHiddenToolWindow() {
    val fake = FakeToolWindow(initiallyVisible = false)

    ToggleTouchTypingPracticeAction.toggle(fake.toolWindow)

    assertEquals("A hidden tool window should be activated", 1, fake.activateCalls)
    assertEquals("A hidden tool window should not be hidden again", 0, fake.hideCalls)
    assertTrue("The tool window should be visible after the toggle", fake.visible)
  }

  @Test
  fun toggleAlternatesOnRepeatedInvocations() {
    val fake = FakeToolWindow(initiallyVisible = false)

    repeat(3) { ToggleTouchTypingPracticeAction.toggle(fake.toolWindow) }

    assertTrue("After an odd number of toggles the window should be visible", fake.visible)
    assertEquals(2, fake.activateCalls)
    assertEquals(1, fake.hideCalls)
  }
}
