# Touch Typing Practice

> **Practice touch typing right inside your IDE.** Touch Typing Practice adds a dedicated tool
> window to IntelliJ IDEA where a practice string is generated for you and you type it out with
> real-time, character-by-character feedback. Configure the practice to your hand, the specific
> fingers you want to train, and how many keys each finger may use — then watch mismatches get
> highlighted instantly and your error count update as you go.

---

## What this plugin is about

Touch Typing Practice is an IntelliJ Platform plugin that turns your IDE into a touch-typing
trainer. Instead of leaving the editor to use a web-based typing site, you get a right-side tool
window that generates practice text tailored to your settings, lets you type it directly, and gives
you immediate visual feedback on every keystroke. It is built to help you build and reinforce proper
finger placement and home-row habits while you work.

The plugin is written in **Kotlin**, targets **IntelliJ IDEA 2023.3 and later** (build `233` and up,
with no upper bound), and is built with the IntelliJ Platform Gradle Plugin on **JVM 17**.

---

## Part 1 — What is already implemented

### Tool window & practice flow

- A **right-anchored tool window** ("Touch Typing Practice") that is `DumbAware`, so it stays
  available even while the IDE is indexing.
- A **toggle shortcut** (`ToggleTouchTypingPracticeAction`, registered in `plugin.xml`) that opens
  the tool window when it is closed and hides it when it is open. It is bound to the double-stroke
  `Ctrl+Alt+P, P` on Windows/Linux and `Control+Option+P, P` on macOS in the default keymap. Because
  the shortcut is bound through the Action System (not a raw key listener), the action appears in
  Settings | Keymap, participates in conflict detection, and keeps working when the user rebinds it.
- A **reference text pane** showing the text to type, and a separate **typing input area** where you
  enter it.
- **Real-time feedback**: as you type, each character that does not match the reference is
  highlighted in the reference pane, and the whole text is highlighted in green when you complete it
  with zero errors.
- **Per-hand letter coloring**: each character in both the reference text pane and the typing area
  is colored by hand — left-hand keys in blue, right-hand keys in purple. The colors are
  theme-aware and distinct from the semantic colors (red for mismatches, green for completion).
  The character-to-hand mapping is derived from the `KeyboardLayout`.
- A **status bar** showing the cumulative error count (the WPM area and its `" | "` separator are
  hidden until WPM is implemented).
- A **Reset** hyperlink (lowercase "reset"), plus an **Enter** key shortcut that regenerates a fresh
  practice text and clears the input once the text has been completed (highlighted in green). While
  the text is incomplete, Enter is ignored. When the text is completed and highlighted in green, a
  hidden **"Hit Enter to "** hint appears to the left of the reset link (muted grayish color, same
  font as the link) so the user knows they can press Enter to reset; it stays hidden in all other
  states.
- **Hot-reload**: settings changes are published over a `MessageBus` topic and the tool window
  rebuilds itself without an IDE restart.
- A `DocumentFilter` that prevents typing beyond the length of the reference text and blocks new
  input while there are unresolved errors.

### Settings & configuration

- A **project-level settings page** ("Touch Typing Practice") with:
    - **Text font/size**: a font selector for the sample text and typing area, plus a font size
      spinner (8–24 pt). Fonts with true (OpenType) small caps — Georgia, Palatino Linotype,
      Garamond, Calibri, Verdana, and Copperplate Gothic — are listed first as featured fonts,
      followed by every other font installed on the system. On first open, the first available
      featured font is preselected automatically.
    - **Practice mode**: Left Hand, Right Hand, or Both Hands.
    - **Word generation mode**: Random Letters (legacy), Exact Words, or Adapted Words (default).
    - **Key limit per finger** (1–6), controlling how many keys each finger may be assigned.
    - **Finger selection**: pick specific fingers (Thumb, Index, Middle, Ring, Little) or use the
      "Use All Fingers" shortcut, with validation that at least one finger is selected.
- Settings are persisted with a type-safe `PersistentStateComponent` (stored in
  `touchTypingPractice.xml`), replacing the legacy `PropertiesComponent` approach.

### Practice text generation

- An **application-level `PracticeTextGeneratorService`** — a thin adapter over pure,
  IDE-independent generators — that builds the set of enabled keys from the configured hand,
  fingers, and per-finger key limit, then generates a practice string according to the selected
  generation mode.
- A per-hand, per-finger **keyboard layout map** (home row first, then extensions) drives which keys
  are eligible.
- **Random Letters mode** (legacy): the text contains a random number of words in
  `[MIN_WORDS_COUNT, MAX_WORDS_COUNT]` (5–20), approximating the length of a modern English
  sentence. Each word is a random run of lowercase letters with a length in `[1, MAX_WORD_LENGTH]`
  (1–7). Words are separated by a single space, and the total length is 9–159 characters.
  `MIN_WORDS_COUNT`, `MAX_WORDS_COUNT`, and `MAX_WORD_LENGTH` are documented as candidates for
  future UI settings.
- **Word-based modes** (Exact Words, Adapted Words): exercises are generated from the bundled
  **Google 10,000 English Words** list (`src/main/resources/google-10000-english.txt`, source:
  <https://github.com/first20hours/google-10000-english>). `WordList` loads and normalizes the
  resource once (classpath load, lowercase, de-duplicated, invalid entries rejected) and caches it.
  `WordExerciseGenerator` adapts each word to the enabled keys: Exact Words keeps only words typed
  entirely with enabled keys; Adapted Words omits unavailable letters (preserving order) and ranks
  candidates by retained letters, retention ratio, source frequency (list order as proxy), and a
  vowel bonus, de-duplicating identical adapted strings. Candidate pools are cached per
  (mode, enabled-key mask). If the word list is missing, the service falls back to Random Letters.
  See the README "Word List and Source Attribution" section for dataset attribution and licensing.
- The generators take an injectable `Random`, so their output is deterministic and unit-testable.

### UI & architecture

- A small **UI components factory** following the IntelliJ Design System (8px base-unit spacing,
  `JBColor` for automatic light/dark theme support, `JBUI` for DPI scaling).
- An **`ErrorCounter`** using the listener pattern to decouple error tracking from the UI. The
  count is cumulative: it increments on every newly introduced error, never decreases on
  corrections, and resets only with the sample text.
- A **`TouchTypingSessionService`** project-level service scaffolded as the home for future session
  state and statistics.
- **Unit tests** covering the finger bitmask logic, settings state normalization, the interactive
  finger-selection behavior of the settings dialog, the pure text generator and keyboard layout, the
  Enter-to-reset key detector, the completion callback that drives the "Hit Enter to reset"
  hint, and the tool-window toggle action (hide when visible, activate when hidden).
- Build tooling: **Spotless** (ktfmt, Google style), **Kover** (coverage), and **Qodana** (code
  quality).

---

## Part 2 — What still has to be implemented

### Core features (highest value first)

1. **Real WPM and accuracy** — the status bar's WPM area (and its `" | "` separator) is hidden
   until implemented; there is no timing code. Track session start time, compute words-per-minute
   and accuracy percentage, and reveal the WPM label in the status bar.
2. **Session statistics** — `TouchTypingSessionService` is still an empty stub. Implement session
   history (WPM, accuracy, duration per session), persist it via `PersistentStateComponent`, and
   surface it in a small stats tab or popup.
3. **Numbers & punctuation** — the generator currently draws only lowercase letters (plus a space);
   numbers and punctuation are not supported. Add settings checkboxes and extend the character set
   to include them.
4. **Text length setting** — the text is now sentence-length (word count in `[MIN_WORDS_COUNT,
   MAX_WORDS_COUNT]` = 5–20, each word ≤ `MAX_WORD_LENGTH` = 7, total 9–159 characters). Add a
   setting to control the length, e.g. by exposing `MIN_WORDS_COUNT`, `MAX_WORDS_COUNT`, and
   `MAX_WORD_LENGTH` as UI inputs.
5. **Difficulty levels / curated text** — practice text is always random characters. Add a mode
   selector (Random / Words / Sentences) so real text can be practiced.
6. **Per-finger error analysis** — the generator already maps keys to fingers; track which fingers
   produce the most errors and show a "weak fingers" summary.
7. **Custom text input** — let users paste or type their own practice text instead of only generated
   ones.

### Cleanup & quality

- **Reduce logging** — the per-keystroke `logger.info` output has been removed; a few remaining
  `logger.info` calls (reset, completion, tool-window lifecycle) could be moved to `debug`.
- **Remove duplication** — the reset key handling is now a single reusable `EnterKeyDetector`
  (previously a copy-pasted R-key `KeyListener`); it is still instantiated in two places
  (`buildContent()` and `reset()`).
- **Fix stale comments** — the generation rules are now sentence-length (word count in
  `[MIN_WORDS_COUNT, MAX_WORDS_COUNT]` = 5–20, each word ≤ `MAX_WORD_LENGTH` = 7, total 9–159
  characters); keep comments in sync as the generator evolves.
- **Remove template leftovers** — the `projectService` message, the `"(2)"` configurable-name
  suffix, and unused constants/methods have been removed; the README still contains the template
  ToDo list.
- **More tests** — unit tests now cover config, the pure text generator, the keyboard layout, and
  the Enter-to-reset detector; add more as new features land.

---

## Quick reference

| Item        | Value                                                         |
|-------------|---------------------------------------------------------------|
| Plugin name | Touch Typing Practice                                         |
| Plugin ID   | `com.github.pyctam.touchtypingpractice`                       |
| Version     | 0.1.8                                                         |
| Language    | Kotlin (JVM 17)                                               |
| Target IDE  | IntelliJ IDEA 2023.3 and later (`233` and up, no upper bound) |
| Build       | Gradle 8.10.2 + IntelliJ Platform Gradle Plugin               |
| Source      | `src/main/kotlin/com/github/pyctam/touchtypingpractice/`      |
