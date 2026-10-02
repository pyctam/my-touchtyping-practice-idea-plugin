# Touch Typing Practice

![Build](https://github.com/pyctam/my-touchtyping-practice-idea-plugin/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/34632-touch-typing-practice.svg)](https://plugins.jetbrains.com/plugin/34632-touch-typing-practice)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/34632-touch-typing-practice.svg)](https://plugins.jetbrains.com/plugin/34632-touch-typing-practice)

## Template ToDo list

- [x] Create a new [IntelliJ Platform Plugin Template][template] project.
- [ ] Get familiar with the [template documentation][template].
- [ ] Adjust the [pluginGroup](./gradle.properties) and [pluginName](./gradle.properties), as well as
  the [id](./src/main/resources/META-INF/plugin.xml) and [sources package](./src/main/kotlin).
- [ ] Adjust the plugin description in `README` (see [Tips][docs:plugin-description])
- [ ] Review
  the [Legal Agreements](https://plugins.jetbrains.com/docs/marketplace/legal-agreements.html?from=IJPluginTemplate).
- [ ] [Publish a plugin manually](https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html?from=IJPluginTemplate)
  for the first time.
- [ ] Set the `MARKETPLACE_ID` in the above README badges. You can obtain it once the plugin is published to JetBrains
  Marketplace.
- [ ] Set the [Plugin Signing](https://plugins.jetbrains.com/docs/intellij/plugin-signing.html?from=IJPluginTemplate)
  related [secrets](https://github.com/JetBrains/intellij-platform-plugin-template#environment-variables).
- [ ] Set
  the [Deployment Token](https://plugins.jetbrains.com/docs/marketplace/plugin-upload.html?from=IJPluginTemplate).
- [ ] Click the <kbd>Watch</kbd> button on the top of the [IntelliJ Platform Plugin Template][template] to be notified
  about releases containing new features and fixes.

<!-- Plugin description -->
Practice touch typing right inside your IDE. Touch Typing Practice adds a dedicated tool window to
IntelliJ IDEA where a practice string is generated for you and you type it out with real-time,
character-by-character feedback. When you complete the text with no errors it is highlighted in
green, and pressing **Enter** loads a fresh sample.

Configure the practice to your hand, the specific fingers you want to train, and how many keys each
finger may use — then watch mismatches get highlighted instantly and your error count update as you
go. Pick the font and size for the practice text from a selector that features fonts with true
small caps (Georgia, Palatino Linotype, Garamond, Calibri, Verdana, Copperplate Gothic) ahead of
your other installed fonts.
<!-- Plugin description end -->

## Installation

- Using the IDE built-in plugin system:

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "Touch Typing
  Practice"</kbd> >
  <kbd>Install</kbd>

- Using JetBrains Marketplace:

  Go to [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/34632-touch-typing-practice) and install it by
  clicking
  the <kbd>Install to ...</kbd> button in case your IDE is running.

  You can also download the [latest release](https://plugins.jetbrains.com/plugin/34632-touch-typing-practice/versions)
  from
  JetBrains Marketplace and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

- Manually:

  Download the [latest release](https://github.com/pyctam/my-touchtyping-practice-idea-plugin/releases/latest) and
  install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

---

# Install the Plugin Manually

## Prerequisites

- Build the plugin locally using `./gradlew buildPlugin`
- The plugin will be available at `build/distributions/my-touchtyping-practice-idea-plugin-*.zip`

## Installation Steps

### Step 1: Open Settings/Preferences

1. Launch **IntelliJ IDEA 2025.3.2**
2. Open Settings/Preferences:

- **On Linux**: `File` → `Settings`
- **Keyboard shortcut**: `Ctrl + Alt + S`

### Step 2: Navigate to Plugins

In the Settings window:

1. Select **`Plugins`** from the left sidebar
2. The Plugins panel will open

### Step 3: Install from Disk

1. Click the **⚙️ (settings/gear icon)** button in the top-right corner of the Plugins panel
2. Select **`Install plugin from disk...`** from the dropdown menu

### Step 4: Select the Plugin File

1. A file browser dialog will open
2. Navigate to your project directory: `build/distributions/`
3. Select the plugin file:

- Look for: `my-touchtyping-practice-idea-plugin-*.zip`
- Example: `my-touchtyping-practice-idea-plugin-1.0.0.zip`

4. Click **`OK`** to proceed

### Step 5: Restart IntelliJ IDEA

1. A dialog will prompt you to restart the IDE
2. Click **`Restart IDE`** button
3. IntelliJ IDEA will restart and load the plugin

## Verify Installation

After the IDE restarts, confirm the plugin is installed:

1. Open **Settings/Preferences** again (`Ctrl + Alt + S`)
2. Go to **`Plugins`**
3. Search for **`Touch Typing Practice`** in the search box
4. The plugin should appear in the installed plugins list
5. It will show a **checkmark ✓** indicating it's enabled

## Troubleshooting

| Issue                               | Solution                                                                                                                 |
|-------------------------------------|--------------------------------------------------------------------------------------------------------------------------|
| Plugin file not found               | Run `./gradlew buildPlugin` first and check `build/distributions/` directory                                             |
| Installation fails                  | Ensure IntelliJ IDEA is closed before installing                                                                         |
| Plugin doesn't appear after restart | Check IDE logs in `Help` → `Show Log in Explorer`                                                                        |
| Compatibility error                 | Verify plugin targets your IntelliJ IDEA version (check `pluginSinceBuild` and `pluginUntilBuild` in `build.gradle.kts`) |

The plugin is now ready to use in your IntelliJ IDEA!

---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template

[docs:plugin-description]: https://plugins.jetbrains.com/docs/intellij/plugin-user-experience.html#plugin-description-and-presentation
