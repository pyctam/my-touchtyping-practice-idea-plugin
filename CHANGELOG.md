# Changelog

## Unreleased

### Added

- **Finger-aware English word generation** — practice text can now be generated from the bundled
  Google 10,000 English Words list (`src/main/resources/google-10000-english.txt`, source:
  <https://github.com/first20hours/google-10000-english>), adapted to the keys enabled by the
  current lesson. Note that the source is a **word-frequency list, not a dictionary of verified
  English words** (entries were selected by frequency in the Google Web Trillion Word Corpus, so
  the list may include single letters, state abbreviations, and other non-standard tokens). A new
  **Word Generation Mode** setting offers three modes: **Adapted Words**
  (default) — list entries with unavailable letters removed, ranked to favor recognizable,
  high-retention fragments; **Exact Words** — only list entries typed entirely with the enabled
  keys; and **Random Letters** — the previous random-letter behavior.
- `WordList`: one-time classpath loading and normalization of the bundled word list (blank lines,
  duplicates, and invalid entries handled; missing resource produces a clear error and a fallback
  to Random Letters).
- `WordExerciseGenerator`: key-aware adaptation (e.g. `flash` → `flsh` when `a` is unavailable),
  deterministic candidate ranking (retained letters, retention ratio, source frequency, vowel
  bonus), de-duplication of identical adapted strings, recent-word avoidance, and cached candidate
  pools per (mode, enabled-key mask).
- Unit tests for word-list loading/normalization, adaptation (including `FLASH` → `FLSH`), exact
  and adapted generation, key compliance, ranking determinism, caching, and performance.

### Changed

- `PracticeTextConfig` and `Settings` gained a `generationMode` field (default `ADAPTED_WORDS`);
  the settings dialog gained a "Word Generation Mode" group.
- README and ABOUT now document the word list, its source attribution, and licensing terms.

## 0.1.8

### Added

- **Toggle tool window shortcut** — a new `ToggleTouchTypingPracticeAction` (registered in
  `plugin.xml`) opens the Touch Typing Practice tool window when it is closed and hides it when it
  is open. It is bound to the double-stroke shortcut `Ctrl+Alt+P, P` on Windows/Linux and
  `Control+Option+P, P` on macOS in the default keymap. Because the shortcut is bound through the
  Action System (not a raw key listener), the action appears in Settings | Keymap, participates in
  conflict detection, and keeps working when the user rebinds it.
- Unit tests for `ToggleTouchTypingPracticeAction`: the toggle decision (hide when visible,
  activate when hidden), repeated toggling, and the tool window id constant.

## 0.1.7

### Added

- Unit tests for `TouchTypingUIComponentsFactory.createTextPane`: verifies the reference text pane
  is non-focusable, non-editable, and correctly sets the provided text.

### Changed

- **Auto-focus typing area on tool window open** — the typing area now automatically receives
  keyboard focus and places the caret at the end of any existing text whenever the tool window is
  opened or re-shown. Previously, after closing and reopening the tool window, the user had to
  manually click in the typing area to start typing. The read-only sample text pane is now
  non-focusable, preventing the IDE from placing the cursor in it.

## 0.1.6

### Added

- **Per-hand letter coloring** — each character in both the reference text pane and the typing area
  is now colored by hand: left-hand keys are rendered in blue and right-hand keys in purple. The
  colors are theme-aware (`JBColor`) and distinct from the plugin's semantic colors (red for
  mismatches, green for completion). The character-to-hand mapping is derived from the
  `KeyboardLayout`, so it stays in sync with the key assignments used by the text generator.
  The typing area was converted from a `JTextArea` to a `JTextPane` to support per-character
  styling.
- Unit tests for the `HandColors` character-to-color mapping and the `applyTo` styling behavior.

## 0.1.5

### Added

- Unit tests for the cumulative error counter: the counter increments on a wrong character, stays
  unchanged after a correction, and accumulates across multiple errors.

### Changed

- **Status panel: errors first, WPM hidden** — the status panel now shows the error count first;
  the WPM area and its `" | "` separator are hidden until WPM is actually implemented (they are
  revealed together once the feature lands).
- **Status panel i18n** — the status panel strings ("Typing errors: N", "Typing speed: N WPM", and
  the separator) are now sourced from the `UIBundle` resource bundle (`messages/UI.properties`)
  instead of being hardcoded in the tool window.
- **Cumulative error counter** — the error counter now keeps counting errors even if the user
  corrects the typing: it increments on every newly introduced error and only resets to zero when
  the sample text is reset (via the reset link or Enter). Previously the counter showed the current
  mismatch count and dropped as soon as the user fixed the errors.

## 0.1.4

### Added

- **"Hit Enter to reset" completion hint** — a hidden "Hit Enter to " label now sits to the left of
  the (lowercased) **reset** link under the sample text panel. It is revealed only when the practice
  text is completed and highlighted in green, hinting that the user can press **Enter** to reset; in
  all other states it stays hidden. The hint uses the same font as the reset link but a muted
  (disabled-foreground) color, since it is a secondary hint rather than a primary element.
- Unit tests for the completion callback that drives the hint: it fires only on completion-state
  transitions (true on completion, false after a correction) and not for repeated incomplete states.
- Unit tests for the new generation rules: the word count is within `[MIN_WORDS_COUNT,
  MAX_WORDS_COUNT]`, each word length is within `[1, MAX_WORD_LENGTH]`, and the total length stays
  within the practical range.

### Changed

- **Lowercased reset link** — the reset hyperlink under the sample text panel is now lowercase
  ("reset") so it reads as a single sentence with the "Hit Enter to " hint.
- **Tool-window i18n** — the reset link text, the "Hit Enter to " hint, and the reset tooltip are
  now sourced from the `UIBundle` resource bundle (`messages/UI.properties`) instead of being
  hardcoded in the tool window.
- **Sentence-length practice text** — the generated practice text now contains a random number of
  words in `[MIN_WORDS_COUNT, MAX_WORDS_COUNT]` (5–20), approximating the length of a modern
  English sentence. Each word is a random run of letters with a length in `[1, MAX_WORD_LENGTH]`
  (1–7). The previous `MAX_TEXT_LENGTH` character cap has been replaced by `MAX_WORDS_COUNT` (20);
  the total text length is now 9–159 characters. `MIN_WORDS_COUNT`, `MAX_WORDS_COUNT`, and
  `MAX_WORD_LENGTH` are documented as candidates for future UI settings.
- **Plugin verification** — `verifyPlugin` no longer fails on `INTERNAL_API_USAGES`. The reported
  internal/experimental API usages were false positives: the Kotlin compiler emits bridge methods
  for the `ToolWindowFactory` interface's default methods (`getAnchor()`, `getIcon()`,
  `manage(...)`) even though the plugin only implements the public `createToolWindowContent` entry
  point. The verification `failureLevel` now excludes `INTERNAL_API_USAGES` while keeping the
  compatibility and override-only API checks.
- **Deprecated `DynamicBundle` constructor** — `UIBundle` now uses the class-based
  `DynamicBundle(Class, String)` constructor instead of the deprecated `DynamicBundle(String)`
  constructor, resolving the deprecation warning on newer IDE versions.

## 0.1.3

### Changed

- **Word-based text generation** — the practice text is now built word by word instead of as a
  random character stream. Each word is a random run of letters with a length of at most
  `MAX_WORD_LENGTH` (7, approximating the average English word length; kept as a constant for now,
  to be turned into a settings input later). Words are separated by a single space, and the maximum
  text length was reduced from 127 to 64 characters.

### Added

- Unit tests for the new generation rules: no word exceeds the max word length, the total text
  length stays within the limit, and words are separated by exactly one space.

## 0.1.2

### Changed

- **Enter-to-reset** — replaced the "press R three times" reset gesture with a single **Enter** key
  press. Enter resets the practice text (generates a fresh sample and clears the input) only after
  the text has been completed and highlighted in green; while the text is incomplete, Enter is
  ignored. The mouse **Reset** hyperlink is unchanged.

### Added

- Unit tests for the new Enter-to-reset key detector.

## 0.1.1

### Added

- **Font selector** in the settings ("Text Font/Size" group): choose the font family for the sample
  text and typing area. Fonts with true (OpenType) small caps — Georgia, Palatino Linotype, Garamond,
  Calibri, Verdana, and Copperplate Gothic — are listed first as featured fonts, followed by every
  other font installed on the system. The font size spinner (8–24 pt) is kept on the right side of
  the font selector. On first open, the first available featured font is preselected automatically.

### Changed

- Reorganized the codebase into focused, testable units (pure text-generation logic, a finger-selection model, and
  separated tool-window/UI components) and removed all dead code.

## 0.1.0

### Added

- Initial release of the Touch Typing Practice plugin.
