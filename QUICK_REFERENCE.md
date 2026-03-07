# Quick Reference Card - IntelliJ Plugin Patterns

## Service Access Pattern

```kotlin
// Application-level service
val settings = Settings.getInstance()

// Project-level service  
val sessionService = project.getService(TouchTypingSessionService::class.java)
```

## Service Declaration Pattern

```kotlin
// APPLICATION level (IDE-wide singleton)
@Service
class MyAppService { }

// PROJECT level (one per project)
@Service(Service.Level.PROJECT)
class MyProjectService(project: Project) { }
```

## Settings Persistence Pattern

```kotlin
@Service
@State(
  name = "SettingName",
  storages = [Storage("file.xml")]
)
class MySettings : PersistentStateComponent<MySettings> {
  var property: String = ""
  
  override fun getState() = this
  override fun loadState(state: MySettings) {
    XmlSerializerUtil.copyBean(state, this)
  }
}
```

## Configuration UI Pattern

```kotlin
class MyConfiguration : BoundConfigurable("Name") {
  private val settings: Settings = Settings.getInstance()
  
  override fun createPanel() = panel {
    group("Title") {
      row {
        spinner(8..24).bindIntValue(settings::fontSize)
      }
    }
  }
}
```

## Tool Window Pattern

```kotlin
class MyToolWindowFactory : ToolWindowFactory, DumbAware {
  override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    val content = ContentFactory.getInstance()
      .createContent(mainPanel, null, false)
    toolWindow.contentManager.addContent(content)
  }
}
```

## Observable Pattern

```kotlin
class Observable {
  private val listeners = mutableListOf<ChangeListener>()
  
  fun addListener(listener: ChangeListener) = listeners.add(listener)
  
  private fun notifyListeners() {
    SwingUtilities.invokeLater {
      listeners.forEach { it.stateChanged(ChangeEvent(this)) }
    }
  }
}
```

## Logging Pattern

```kotlin
private val logger: Logger = Logger.getInstance(ClassName::class.java)

logger.info("Info message")
logger.warn("Warning", exception)
logger.error("Error", throwable)
```

## UI Update Pattern

```kotlin
// From background thread
SwingUtilities.invokeLater {
  uiComponent.text = "Updated"
}
```

## Documentation Pattern

```kotlin
/**
 * Brief description.
 *
 * Longer explanation if needed.
 *
 * @param param Description
 * @return Description
 * @throws ExceptionType When this happens
 */
fun myFunction(param: String): String {
  // Implementation
}
```

## Plugin Configuration (plugin.xml)

```xml
<idea-plugin>
  <id>com.vendor.pluginid</id>
  <name>Plugin Name</name>
  <vendor email="support@vendor.com">Vendor</vendor>
  <description>What does this plugin do?</description>
  <depends>com.intellij.modules.platform</depends>
  
  <extensions defaultExtensionNs="com.intellij">
    <toolWindow 
      id="ToolWindowId"
      factoryClass="com.vendor.MyToolWindowFactory"/>
    <projectConfigurable 
      id="com.vendor.MyPlugin"
      displayName="My Plugin"
      instance="com.vendor.MyConfiguration"/>
  </extensions>
</idea-plugin>
```

## Build Commands

```bash
# Clean build
./gradlew clean build -x test

# Format code
./gradlew spotlessApply

# Run IDE with plugin
./gradlew runIde

# Build distribution
./gradlew buildPlugin
```

## Key Files to Remember

| File | Purpose |
|------|---------|
| `Settings.kt` | Application settings service |
| `Configuration.kt` | Settings UI dialog |
| `TouchTypingToolWindowFactory.kt` | Main tool window |
| `PracticeTextGeneratorService.kt` | Business logic |
| `plugin.xml` | Plugin metadata and extensions |

## Common Pitfalls

❌ **Direct UI References**
```kotlin
service.statusLabel = myLabel  // DON'T
```

✅ **Use Listeners Instead**
```kotlin
service.addListener { notifyUI() }  // DO
```

---

❌ **Manual Service Creation**
```kotlin
val service = MyService()  // DON'T
```

✅ **Use Framework Services**
```kotlin
val service = ApplicationManager.getApplication()
  .getService(MyService::class.java)  // DO
```

---

❌ **Blocking EDT**
```kotlin
override fun createToolWindowContent(...) {
  val data = expensiveOperation()  // DON'T - blocks UI
}
```

✅ **Use Background Tasks**
```kotlin
ProgressManager.getInstance().run(
  object : Task.Backgroundable(...) {
    override fun run(indicator: ProgressIndicator) {
      val data = expensiveOperation()
      SwingUtilities.invokeLater { updateUI(data) }  // DO
    }
  }
)
```

## Documentation Location

| Document | Contents |
|----------|----------|
| `OPTIMIZATION_SUMMARY.md` | What changed and why |
| `IMPLEMENTATION_GUIDE.md` | How to develop new features |
| `CODE_EXAMPLES.md` | 20+ code snippet examples |
| `CODE_EXAMPLES.md` | This quick reference |

---

**Print this page for desk reference!**

