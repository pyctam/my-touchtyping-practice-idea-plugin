# Code Examples Reference

Quick reference for common patterns used in the Touch Typing Practice plugin.

## 1. Accessing Application Settings

```kotlin
// Get the application-level settings service
val settings = Settings.getInstance()

// Access configuration values
val fontSize = settings.textFontSize
val mode = settings.practiceMode
val useAllFingers = settings.useAllFingers

// Update values (automatically persisted)
settings.textFontSize = 14
settings.practiceMode = PracticeMode.LEFT_HAND
```

## 2. Creating a Tool Window

```kotlin
class MyToolWindowFactory : ToolWindowFactory, DumbAware {
  override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    // Get services
    val settings = Settings.getInstance()
    val textGen = PracticeTextGeneratorService()
    
    // Create UI components
    val mainPanel = BorderLayoutPanel()
    val contentArea = createContentArea()
    
    // Build layout
    mainPanel.addToCenter(contentArea)
    
    // Add to tool window
    val content = ContentFactory.getInstance()
      .createContent(mainPanel, null, false)
    toolWindow.contentManager.addContent(content)
  }
}
```

## 3. Observing Value Changes with Listeners

```kotlin
// Create observable value tracker
val errorCounter = ErrorCounter()

// Add listener for changes
errorCounter.addChangeListener { event ->
  val count = (event.source as ErrorCounter).getCount()
  statusLabel.text = "Errors: $count"
}

// Update value
errorCounter.setCount(5)  // Triggers listener
```

## 4. Configuration UI with DSL

```kotlin
class Configuration : BoundConfigurable("My Settings") {
  private val settings: Settings = Settings.getInstance()
  
  override fun createPanel(): DialogPanel = panel {
    // Font size section
    group("Font Size") {
      row {
        spinner(8..24, 1).bindIntValue(settings::textFontSize)
      }
    }
    
    // Practice mode section
    group("Practice Mode") {
      buttonsGroup {
        row {
          radioButton("Left Hand", PracticeMode.LEFT_HAND)
          radioButton("Right Hand", PracticeMode.RIGHT_HAND)
          radioButton("Both Hands", PracticeMode.BOTH_HANDS)
        }
      }.bind({ settings.practiceMode }, { settings.practiceMode = it })
    }
    
    // Finger selection section
    group("Fingers") {
      row {
        checkBox("Use All Fingers").bindSelected(
          { settings.useAllFingers },
          { value ->
            settings.useAllFingers = value
            // Trigger UI updates
          }
        )
      }
    }
  }
}
```

## 5. Logging Best Practices

```kotlin
// Initialize logger (preferably at class level)
private val logger: Logger = Logger.getInstance(MyClass::class.java)

// Information logging
logger.info("Text generator initialized")

// Warning logging
logger.warn("Configuration incomplete, using defaults")

// Error logging with exception
try {
  val text = generateText()
} catch (e: Exception) {
  logger.error("Failed to generate text", e)
}

// Debug information
logger.debug("Character set: $chars")
```

## 6. Service Lifecycle Management

```kotlin
// Application-level service (singleton across IDE)
@Service
class ApplicationService {
  init {
    logger.info("Application service initialized")
  }
  
  fun doSomething() {
    // Implementation
  }
}

// Project-level service (one per project)
@Service(Service.Level.PROJECT)
class ProjectService(private val project: Project) {
  init {
    logger.info("Project service initialized for: ${project.name}")
  }
  
  fun doProjectSpecific() {
    // Implementation using project context
  }
}

// Usage
val appService = ApplicationManager.getApplication()
  .getService(ApplicationService::class.java)

val projService = project.getService(ProjectService::class.java)
```

## 7. Settings Persistence with PersistentStateComponent

```kotlin
@Service
@State(
  name = "MyPluginSettings",
  storages = [Storage("myPlugin.xml")]
)
class Settings : PersistentStateComponent<Settings> {
  var textFontSize: Int = 12
  var practiceMode: PracticeMode = BOTH_HANDS
  var useAllFingers: Boolean = true
  
  // Framework calls this to get current state
  override fun getState(): Settings = this
  
  // Framework calls this to restore state
  override fun loadState(state: Settings) {
    XmlSerializerUtil.copyBean(state, this)
  }
  
  companion object {
    @Suppress("unused")
    fun getInstance(): Settings =
      ApplicationManager.getApplication().getService(Settings::class.java)
  }
}
```

## 8. Building UI Panels

```kotlin
fun createMainPanel(): BorderLayoutPanel {
  val panel = BorderLayoutPanel()
  
  // Set appearance
  panel.border = JBUI.Borders.empty(4)  // Uniform padding
  panel.background = UIUtil.getPanelBackground()
  
  // Create components
  val scrollPane = JBScrollPane(contentArea)
  val statusBar = JBLabel("Status: Ready")
  
  // Arrange layout
  panel.addToCenter(scrollPane)
  panel.addToBottom(statusBar)
  
  // Set size constraints
  panel.minimumSize = JBUI.size(400, 300)
  panel.preferredSize = JBUI.size(800, 600)
  
  return panel
}
```

## 9. Font Handling

```kotlin
// Get base font
val baseFont = UIUtil.getLabelFont()

// Derive new font with size
val largeFont = baseFont.deriveFont(16f)

// Apply to component
label.font = largeFont

// Use JBFont for consistency
label.font = JBFont.medium()
```

## 10. Color Management

```kotlin
// Use JBColor for theme-aware colors
val highlightColor = JBColor(0xFFCCCC, 0xFFCCCC)  // Light red (light/dark theme)
val borderColor = JBColor.border()
val bgColor = UIUtil.getPanelBackground()

// Create painter with theme-aware color
val painter = DefaultHighlighter.DefaultHighlightPainter(highlightColor)
```

## 11. Threading: UI Operations

```kotlin
// Update UI from background thread
SwingUtilities.invokeLater {
  statusLabel.text = "Updated from background"
}

// Run on EDT (Event Dispatch Thread)
ApplicationManager.getApplication().invokeLater {
  // UI update code
}

// Check if on EDT
if (SwingUtilities.isEventDispatchThread()) {
  // Already on EDT
}
```

## 12. Background Task Execution

```kotlin
// Run expensive operation in background
ProgressManager.getInstance().run(object : Task.Backgroundable(
  project,
  "Generating Text",
  true  // cancellable
) {
  override fun run(indicator: ProgressIndicator) {
    indicator.text = "Generating practice text..."
    val result = expensiveOperation()
    
    // Update UI back on EDT
    SwingUtilities.invokeLater {
      updateUI(result)
    }
  }
})
```

## 13. Document Listeners for Text Components

```kotlin
val doc = textArea.document
if (doc is AbstractDocument) {
  doc.documentFilter = MyCustomFilter()
  doc.addDocumentListener(object : DocumentListener {
    override fun insertUpdate(e: DocumentEvent) {
      handleTextChange()
    }
    
    override fun removeUpdate(e: DocumentEvent) {
      handleTextChange()
    }
    
    override fun changedUpdate(e: DocumentEvent) {
      // For styled documents
    }
  })
}
```

## 14. Exception Handling Best Practices

```kotlin
try {
  val result = risky Operation()
  process(result)
} catch (e: IOException) {
  logger.warn("IO error occurred", e)
  showErrorNotification("Cannot read file")
} catch (e: IllegalArgumentException) {
  logger.error("Invalid argument", e)
  throw e  // Re-throw if fatal
} catch (t: Throwable) {
  logger.error("Unexpected error", t)
  showErrorNotification("An unexpected error occurred")
}
```

## 15. Resource Cleanup

```kotlin
class MyToolWindowFactory : ToolWindowFactory {
  private var disposable: Disposable? = null
  
  override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    disposable = Disposer.newDisposable()
    
    // Add cleanup logic
    Disposer.register(disposable!!) {
      // Cleanup code here
      logger.info("Tool window disposing resources")
    }
  }
}
```

## 16. Action Registration

```kotlin
// In plugin.xml
<actions>
  <action id="MyPlugin.MyAction" class="com.example.MyAction" text="My Action">
    <keyboard-shortcut keymap="$default" first-keystroke="ctrl alt M"/>
  </action>
  <group id="MyGroup">
    <action id="MyPlugin.MyAction"/>
  </group>
</actions>

// Action implementation
class MyAction : AnAction() {
  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    logger.info("Action performed in project: ${project.name}")
  }
  
  override fun update(e: AnActionEvent) {
    e.presentation.isEnabledAndVisible = e.project != null
  }
}
```

## 17. Plugin Constants

```kotlin
package com.example.myplugin

object PluginConstants {
  const val PLUGIN_ID = "com.example.myplugin"
  const val SETTINGS_NAME = "MyPluginSettings"
  
  object UI {
    const val TOOL_WINDOW_ID = "My Tool Window"
    const val DEFAULT_PADDING = 4
    const val MIN_WIDTH = 400
    const val MIN_HEIGHT = 300
  }
  
  object Messages {
    const val ERROR_TITLE = "My Plugin Error"
    const val WARNING_TITLE = "My Plugin Warning"
  }
}
```

## 18. Notification Display

```kotlin
// Show notification
val notification = Notification(
  "MyPluginGroup",
  "Title",
  "Message content",
  NotificationType.INFORMATION
)
Notifications.Bus.notify(notification, project)

// Error notification
val errorNotif = Notification(
  "MyPluginGroup",
  "Error",
  "An error occurred",
  NotificationType.ERROR
)
Notifications.Bus.notify(errorNotif, project)
```

## 19. Extension Point Registration

```kotlin
// In plugin.xml
<extensions defaultExtensionNs="com.intellij">
  <toolWindow
    anchor="right"
    factoryClass="com.example.MyToolWindowFactory"
    id="My Tool Window"
    secondary="false"/>
  
  <projectConfigurable
    instance="com.example.MyConfiguration"
    displayName="My Plugin"
    id="com.example.MyPlugin"/>
</extensions>
```

## 20. Quick Access Pattern

```kotlin
// Create extension function for easy access
val Project.myService: MyProjectService
  get() = this.getService(MyProjectService::class.java)

val Application.mySettings: Settings
  get() = ApplicationManager.getApplication().getService(Settings::class.java)

// Usage
val settings = project.myService.settings
val appSettings = ApplicationManager.getApplication().mySettings
```

---

## Quick Checklist for New Features

- [ ] Created appropriate service (APPLICATION or PROJECT level)
- [ ] Settings persist via PersistentStateComponent
- [ ] UI components use listener pattern (not direct references)
- [ ] Logger initialized with correct class reference
- [ ] UI operations use SwingUtilities.invokeLater when needed
- [ ] Thread-safe operations for background tasks
- [ ] Documentation added for public APIs
- [ ] Error handling with proper logging
- [ ] Resources properly disposed
- [ ] Code formatted with `./gradlew spotlessApply`

