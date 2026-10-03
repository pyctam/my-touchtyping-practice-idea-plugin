# Changelog

## Unreleased

### Added

- **"Hit Enter to reset" completion hint** — a hidden "Hit Enter to " label now sits to the left of
  the (lowercased) **reset** link under the sample text panel. It is revealed only when the practice
  text is completed and highlighted in green, hinting that the user can press **Enter** to reset; in
  all other states it stays hidden. The hint uses the default font color and the same font as the
  reset link.
- Unit tests for the completion callback that drives the hint: it fires only on completion-state
  transitions (true on completion, false after a correction) and not for repeated incomplete states.
- Unit tests for the new generation rules: the word count is within `[MIN_WORDS_COUNT,
  MAX_WORDS_COUNT]`, each word length is within `[1, MAX_WORD_LENGTH]`, and the total length stays
  within the practical range.

### Changed

- **Lowercased reset link** — the reset hyperlink under the sample text panel is now lowercase
  ("reset") so it reads as a single sentence with the "Hit Enter to " hint.
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
