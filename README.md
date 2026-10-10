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
character-by-character feedback. Each letter is colored by hand — left-hand keys in blue,
right-hand keys in purple — so you can see at a glance which finger should press which key.
When you complete the text with no errors it is highlighted in green, a "Hit Enter to reset" hint
appears next to the reset link, and pressing **Enter** loads a fresh sample.

Practice with English words: the bundled word-frequency list is adapted to the keys you currently
practice, so every generated character can be typed with your enabled keys. Choose between
**Adapted Words** (list entries with unavailable letters removed, favoring recognizable fragments),
**Exact Words** (only entries typed entirely with your enabled keys), or the classic **Random
Letters** mode.

Open or close the tool window with a single shortcut: **Ctrl+Alt+P, P** on Windows/Linux and
**Control+Option+P, P** on macOS (a double-stroke, bound through the Action System so it shows up
in Settings | Keymap and can be rebound).

Configure the practice to your hand, the specific fingers you want to train, how many keys each
finger may use, and the word generation mode — then watch mismatches get highlighted instantly and
your error count grow as you go. The error total is cumulative: correcting a mistake never
decreases it, and it only resets when you load a fresh sample. Pick the font and size for the
practice text from a selector that features fonts with true small caps (Georgia, Palatino Linotype,
Garamond, Calibri, Verdana, Copperplate Gothic) ahead of your other installed fonts.
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

## Word List and Source Attribution

The word-based practice modes (**Adapted Words** and **Exact Words**) generate exercises from the
bundled English word list at `src/main/resources/google-10000-english.txt`.

- **Dataset:** Google 10,000 English Words — the 10,000 most frequent tokens, one per line,
  ordered by frequency (most common first).
- **Source repository:** <https://github.com/first20hours/google-10000-english>
- **Word-list file:** <https://github.com/first20hours/google-10000-english/blob/master/google-10000-english.txt>
- **Use in this plugin:** the dataset is used to generate English-word-based typing exercises that
  are adapted to the keys currently enabled in the lesson. This project does not author or maintain
  the word list; it is bundled as-is for offline use.

> **Note:** This is a **word-frequency list, not a dictionary of verified English words.** The
> 10,000 entries were selected based on their frequency in the Google Web Trillion Word Corpus,
> which was derived from text on public web pages. Frequency in a corpus does not guarantee that
> an entry is a valid English word — the list may include single letters, state abbreviations
> (e.g. `nj`, `nv`, `nh`), and other non-standard tokens. In **Exact Words** mode, only entries
> that are fully typeable with the enabled keys are offered, so with a small key set the pool may
> be dominated by these short non-word tokens.

### How the word list is loaded

`WordList` (in `src/main/kotlin/.../generation/WordList.kt`) reads the resource from the classpath
(`/google-10000-english.txt`) of the packaged plugin — never from a machine-specific filesystem
path. The list is loaded and parsed **once** per application and cached: blank lines are skipped,
words are normalized to lowercase, duplicates are removed (first occurrence wins, preserving the
source frequency order), and entries that are not pure letter words are rejected. If the resource
is missing or unreadable, a clear error is logged and the plugin falls back to the Random Letters
mode.

### How to update or replace the dataset

Replace the contents of `src/main/resources/google-10000-english.txt` with a new one-word-per-line
list (lowercase letters are expected; other entries are skipped safely). The file is picked up
automatically by the Gradle build and included in the packaged plugin. If you replace the dataset
with one from a different source, update the attribution above and verify the new source's license
before distributing the plugin.

### How exact-word and adapted-word generation work

- **Exact Words:** only list entries whose every character is typed with an enabled key are
  used. Candidates are ranked by source frequency (the list order is used as a frequency proxy —
  the source file does not provide explicit frequency values).
- **Adapted Words:** each list entry is filtered to the enabled keys — characters whose keys
  are not enabled are omitted, the original order is preserved, and the result is ranked to favor
  high-retention, common, recognizable fragments (e.g. `through` → `hgh` when only the index-finger
  keys `f, g, j, h` are enabled). The original source entry is retained with each adapted result for
  debugging and analytics. Adapted strings are letter-filtered fragments and are not presented as
  correctly spelled English words.
- Candidate pools are precomputed per (mode, enabled-key set) and cached, so the list is not
  rescanned on every generation request.

### License and redistribution

Per the source repository's own notice, the data files are derived from the **Google Web Trillion
Word Corpus** (as described by Thorsten Brants and Alex Franz), distributed by the **Linguistic
Data Consortium**; subsets of the corpus were distributed by **Peter Norvig**; corpus editing and
cleanup was done by **Josh Kaufman**. The source states that **educational and personal/research
use is permitted** under the LDC license, Norvig's MIT license for his contributions, and US fair
use doctrine, and that **commercial use is not recommended without licensing the data from the
Linguistic Data Consortium**. This plugin is distributed free of charge; if you plan to use or
redistribute the bundled word list commercially, review the LDC terms first.

---
