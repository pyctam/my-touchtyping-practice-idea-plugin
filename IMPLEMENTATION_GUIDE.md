# IntelliJ Plugin Development Best Practices Implementation Guide

## Executive Summary

This document provides a comprehensive guide of the optimizations and best practices implemented in the Touch Typing Practice plugin. It serves as both documentation and a reference implementation for IntelliJ IDEA plugin development.

---

## Table of Contents

1. [Architecture Patterns](#architecture-patterns)
2. [Service Management](#service-management)
3. [State Persistence](#state-persistence)
4. [UI Component Design](#ui-component-design)
5. [Tool Window Integration](#tool-window-integration)
6. [Configuration Management](#configuration-management)
7. [Code Quality Standards](#code-quality-standards)
8. [Testing Strategies](#testing-strategies)

---

## Architecture Patterns

### 1. Layered Architecture

The plugin follows a clean layered architecture:

```
┌─────────────────────────────────────┐
│        UI Layer                      │
│ (ToolWindow, Configuration UI)       │
├─────────────────────────────────────┤
│        Business Logic Layer          │
│ (TextGenerator, ErrorTracking)       │
├─────────────────────────────────────┤
│        Model/Data Layer              │
│ (Settings, PracticeMode, Finger)     │
├─────────────────────────────────────┤
│        Service Layer                 │
│ (ApplicationService, ProjectService) │
└─────────────────────────────────────┘
```

**Benefits:**
- Clear separation of concerns
- Easier testing at each layer
- Better code reusability
- Simplified maintenance

### 2. Dependency Injection via Services

The plugin leverages IntelliJ's built-in service framework:

```kotlin
// Get application-level service
val settings = Settings.getInstance()

// Services are singletons managed by the framework
// No manual instantiation needed
```

**Key Principles:**
- Services are singletons (by default)
- Scope determines lifetime (APPLICATION, PROJECT, MODULE)
- Framework handles lazy initialization
- Proper cleanup on IDE shutdown

---

## Service Management

### Application-Level Services

Used for IDE-wide functionality that doesn't depend on a specific project:

```kotlin
@Service
class PracticeTextGeneratorService {
  // Shared across all projects
  // Stateless - doesn't hold project-specific data
}

@Service
@State(name = "TouchTypingPracticeSettings", storages = [...])
class Settings : PersistentStateComponent<Settings> {
  // Global settings for all projects
}
```

**When to use:**
- Global configurations
- Stateless utility services
- Shared resources

### Project-Level Services

Used for functionality scoped to a specific project:

```kotlin
@Service(Service.Level.PROJECT)
class TouchTypingSessionService(project: Project) {
  // Created once per project
  // Can access project-specific data
  // Cleaned up when project closes
}
```

**When to use:**
- Per-project session tracking
- Project-specific state
- Resources that should be cleaned up with the project

### Accessing Services

```kotlin
// Application service
Settings.getInstance()

// Project service
val service = project.getService(TouchTypingSessionService::class.java)
```

---

## State Persistence

### PersistentStateComponent Pattern

Replaces the legacy `PropertiesComponent` approach:

```kotlin
@Service
@State(
  name = "TouchTypingPracticeSettings",
  storages = [Storage("touchTypingPractice.xml")]
)
class Settings : PersistentStateComponent<Settings> {
  var textFontSize: Int = 12
  var practiceMode: PracticeMode = BOTH_HANDS
  
  override fun getState(): Settings = this
  
  override fun loadState(state: Settings) {
    XmlSerializerUtil.copyBean(state, this)
  }
}
```

**XML Storage Location:**
- Windows: `%APPDATA%\JetBrains\IntelliJIdea2025\options\touchTypingPractice.xml`
- macOS: `~/Library/Application Support/JetBrains/IntelliJIdea2025/options/touchTypingPractice.xml`
- Linux: `~/.config/JetBrains/IntelliJIdea2025/options/touchTypingPractice.xml`

**Benefits:**
- Automatic serialization to XML
- Type-safe configuration
- Migration support for schema changes
- IDE-aware backup/restore
- Proper versioning

### Configuration Persistence

```kotlin
class Configuration : BoundConfigurable("Touch Typing Practice") {
  private val settings: Settings = Settings.getInstance()
  
  override fun createPanel(): DialogPanel = panel {
    // DSL builder creates bound components
    spinner(8..24, 1).bindIntValue(settings::textFontSize)
    checkBox("Use All Fingers").bindSelected(
      { settings.useAllFingers },
      { settings.useAllFingers = it }
    )
  }
  
  override fun apply() {
    super.apply() // Auto-saves via PersistentStateComponent
  }
}
```

**Key Points:**
- No manual save/load logic needed
- Framework handles persistence automatically
- Bindings keep UI synchronized with model

---

## UI Component Design

### Decoupled UI Components using Listener Pattern

**Problem:** Tight coupling between business logic and UI

**Solution:** Use standard Swing listener pattern

```kotlin
class ErrorCounter {
  private val changeListeners = mutableListOf<ChangeListener>()
  
  fun setCount(newCount: Int) {
    count = newCount
    notifyListeners()
  }
  
  fun addChangeListener(listener: ChangeListener) {
    changeListeners.add(listener)
  }
  
  private fun notifyListeners() {
    SwingUtilities.invokeLater {
      val event = ChangeEvent(this)
      changeListeners.forEach { it.stateChanged(event) }
    }
  }
}
```

**Usage:**
```kotlin
val errorCounter = ErrorCounter()

// UI component subscribes to changes
errorCounter.addChangeListener { event ->
  statusLabel.text = "Errors: ${errorCounter.getCount()}"
}

// Business logic updates without knowing about UI
errorCounter.setCount(newCount)
```

**Benefits:**
- ErrorCounter is testable without UI dependencies
- Multiple UI components can listen to changes
- Easy to refactor UI without touching business logic
- Follows standard Swing patterns

### UI Builder DSL

Modern IntelliJ provides Kotlin DSL for building UIs:

```kotlin
panel {
  group("Text Font Size") {
    row {
      comment("Font size for practice text")
    }
    row {
      spinner(8..24, 1).bindIntValue(settings::textFontSize)
    }
  }
  
  group("Practice Mode") {
    buttonsGroup {
      row { radioButton("Left Hand", LEFT_HAND) }
      row { radioButton("Right Hand", RIGHT_HAND) }
      row { radioButton("Both Hands", BOTH_HANDS) }
    }.bind({ settings.practiceMode }, { settings.practiceMode = it })
  }
}
```

**Advantages:**
- Type-safe
- Consistent with IntelliJ UI conventions
- Automatic layout management
- Built-in accessibility

---

## Tool Window Integration

### DumbAware Implementation

Allows tool window to be available during IDE indexing:

```kotlin
class TouchTypingToolWindowFactory : ToolWindowFactory, DumbAware {
  override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    // Tool window is available even during indexing
  }
}
```

**When to implement DumbAware:**
- Simple UI components without project dependencies
- Services that don't analyze code
- Reading-only operations

**When NOT to implement DumbAware:**
- Using PSI (code analysis)
- Building indices
- Accessing resolved references

### Tool Window Lifecycle

```kotlin
override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
  // Called once when tool window is first opened
  // Create content and add to toolWindow.contentManager
  
  val content = contentFactory.createContent(myPanel, null, false)
  toolWindow.contentManager.addContent(content)
}

override fun shouldBeAvailable(project: Project): Boolean {
  // Optional: Control visibility
  return true
}
```

---

## Configuration Management

### Creating Project-Specific Settings

```kotlin
@State(
  name = "MyPluginSettings",
  storages = [Storage(StoragePathMacros.WORKSPACE_FILE)]
)
class MySettings : PersistentStateComponent<MySettings> {
  // Stored in .idea/workspace.xml
  // Per-project configuration
}
```

### Creating Application Settings

```kotlin
@State(
  name = "MyPluginAppSettings",
  storages = [Storage("myPlugin.xml")]
)
class MyAppSettings : PersistentStateComponent<MyAppSettings> {
  // Stored in IDE options directory
  // Shared across all projects
}
```

### Settings Versioning

For future compatibility:

```kotlin
@State(
  name = "TouchTypingSettings",
  storages = [Storage("touchTyping.xml")]
)
class Settings : PersistentStateComponent<Settings> {
  var version: Int = 1 // Add for future migrations
  
  override fun loadState(state: Settings) {
    if (state.version < 2) {
      // Migration logic here
    }
    XmlSerializerUtil.copyBean(state, this)
  }
}
```

---

## Code Quality Standards

### 1. Kotlin Formatting

Uses ktfmt with Google style:

```bash
./gradlew spotlessApply  # Fix formatting
./gradlew spotlessCheck  # Verify formatting
```

### 2. Import Organization

- Remove unused imports
- Use specific imports (not wildcards)
- Group related imports

### 3. Nullability and Safety

```kotlin
// Prefer non-nullable types
val settings: Settings = Settings.getInstance()

// Use safe calls when nullable
val result = service?.getValue()

// Use Elvis operator with defaults
val count = errorCounter.getCount() ?: 0
```

### 4. Logging

```kotlin
private val logger: Logger = 
  Logger.getInstance(MyClass::class.java)

logger.info("Operation completed")
logger.warn("Warning message", exception)
logger.error("Error occurred", throwable)
```

### 5. Documentation

```kotlin
/**
 * Generates practice text based on current settings.
 *
 * @param settings The practice configuration
 * @return Practice text string optimized for the settings
 * @throws IllegalArgumentException if settings are invalid
 */
fun generateText(settings: Settings): String {
  // Implementation
}
```

---

## Testing Strategies

### Unit Testing Services

```kotlin
@Test
fun testErrorCounterListeners() {
  val counter = ErrorCounter()
  var notificationCount = 0
  
  counter.addChangeListener {
    notificationCount++
  }
  
  counter.setCount(5)
  assertEquals(1, notificationCount)
  assertEquals(5, counter.getCount())
}
```

### Testing Settings Persistence

```kotlin
@Test
fun testSettingsPersistence() {
  val settings = Settings.getInstance()
  settings.textFontSize = 16
  
  // Simulate IDE shutdown/restart
  settings.loadState(settings)
  
  assertEquals(16, settings.textFontSize)
}
```

### Testing Tool Windows

- Use IntelliJ's test framework
- Mock ProjectService dependencies
- Test UI component creation

---

## Common Pitfalls and Solutions

### ❌ Pitfall 1: Direct UI Component References

```kotlin
// WRONG
class Service {
  var statusLabel: JBLabel? = null
  
  fun updateStatus() {
    statusLabel?.text = "Done"
  }
}
```

✅ **Solution:** Use listener pattern

```kotlin
// CORRECT
class Service {
  private val changeListeners = mutableListOf<ChangeListener>()
  
  fun addChangeListener(listener: ChangeListener) {
    changeListeners.add(listener)
  }
  
  fun updateStatus() {
    notifyListeners()
  }
}
```

### ❌ Pitfall 2: Manual Service Management

```kotlin
// WRONG
val settings = Settings(...)  // Manual creation
```

✅ **Solution:** Use framework services

```kotlin
// CORRECT
val settings = Settings.getInstance()  // Framework-managed
```

### ❌ Pitfall 3: Blocking EDT

```kotlin
// WRONG
override fun createToolWindowContent(...) {
  val text = expensiveOperation()  // Blocks UI thread
}
```

✅ **Solution:** Use background tasks

```kotlin
// CORRECT
override fun createToolWindowContent(...) {
  ProgressManager.getInstance().run(object : Task.Backgroundable(...) {
    override fun run(indicator: ProgressIndicator) {
      val text = expensiveOperation()
      SwingUtilities.invokeLater {
        updateUI(text)
      }
    }
  })
}
```

### ❌ Pitfall 4: Missing Service Scope

```kotlin
// WRONG
@Service
class MyService  // Scope unclear

// CORRECT
@Service(Service.Level.PROJECT)
class MyService(project: Project)  // Clear scope
```

---

## Future Enhancements

Based on the current architecture, these features can be easily added:

1. **Session Statistics**
   - Use TouchTypingSessionService for per-session tracking
   - Store to PersistentStateComponent

2. **Real-time Analytics**
   - Add event listeners for typing metrics
   - Integrate with AnAction for event tracking

3. **Cloud Sync**
   - Export settings via PersistentStateComponent
   - Use IDE's settings sync mechanism

4. **Plugin Settings Dialog**
   - Extend Configuration class
   - Add new UI sections

5. **Code Analysis Integration**
   - Create PROJECT-level service for PSI analysis
   - Use proper inspection framework

---

## Resources

- [IntelliJ Platform SDK](https://plugins.jetbrains.com/docs/intellij/)
- [Services API](https://plugins.jetbrains.com/docs/intellij/plugin-services.html)
- [Settings](https://plugins.jetbrains.com/docs/intellij/persisting-state-of-components.html)
- [Tool Windows](https://plugins.jetbrains.com/docs/intellij/tool-windows.html)
- [UI DSL](https://plugins.jetbrains.com/docs/intellij/kotlin-ui-dsl-version-2.html)

---

## Summary

This implementation represents professional-grade IntelliJ plugin development with:
- ✅ Clean architecture
- ✅ Proper service management
- ✅ Type-safe state persistence
- ✅ Decoupled UI components
- ✅ Framework-compliant patterns
- ✅ Code quality standards
- ✅ Future-ready design

