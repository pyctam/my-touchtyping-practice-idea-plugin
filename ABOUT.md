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

The plugin is written in **Kotlin**, targets **IntelliJ IDEA 2023.3 – 2025.3** (builds `233` –
`253.*`), and is built with the IntelliJ Platform Gradle Plugin on **JVM 17**.

---

## Part 1 — What is already implemented

### Tool window & practice flow

- A **right-anchored tool window** ("Touch Typing Practice") that is `DumbAware`, so it stays
  available even while the IDE is indexing.
- A **reference text pane** showing the text to type, and a separate **typing input area** where you
  enter it.
- **Real-time feedback**: as you type, each character that does not match the reference is
  highlighted in the reference pane, and the whole text is highlighted in green when you complete it
  with zero errors.
- A **status bar** showing typing speed (WPM) and the current error count.
- A **Reset** hyperlink, plus a keyboard shortcut (press **R three times** within 3 seconds) that
  regenerates a fresh practice text and clears the input.
- **Hot-reload**: settings changes are published over a `MessageBus` topic and the tool window
  rebuilds itself without an IDE restart.
- A `DocumentFilter` that prevents typing beyond the length of the reference text and blocks new
  input while there are unresolved errors.

### Settings & configuration

- A **project-level settings page** ("Touch Typing Practice") with:
    - **Text font size** (8–24 pt) for both the sample text and the typing area.
    - **Practice mode**: Left Hand, Right Hand, or Both Hands.
    - **Key limit per finger** (1–6), controlling how many keys each finger may be assigned.
    - **Finger selection**: pick specific fingers (Thumb, Index, Middle, Ring, Little) or use the
      "Use All Fingers" shortcut, with validation that at least one finger is selected.
- Settings are persisted with a type-safe `PersistentStateComponent` (stored in
  `touchTypingPractice.xml`), replacing the legacy `PropertiesComponent` approach.

### Practice text generation

- An **application-level `PracticeTextGeneratorService`** that builds a character set from the
  configured hand, fingers, and per-finger key limit, then generates a random practice string.
- A per-hand, per-finger **keyboard layout map** (home row first, then extensions) drives which keys
  are eligible.
- Generation rules: random length, no leading/trailing spaces, no consecutive spaces, and roughly a
  10% space frequency. A set of curated pangrams is kept as a fallback.

### UI & architecture

- A small **UI components factory** following the IntelliJ Design System (8px base-unit spacing,
  `JBColor` for automatic light/dark theme support, `JBUI` for DPI scaling).
- An **`ErrorCounter`** using the listener pattern to decouple error tracking from the UI.
- A **`TouchTypingSessionService`** project-level service scaffolded as the home for future session
  state and statistics.
- **Unit tests** covering the finger bitmask logic, settings state normalization, and the interactive
  finger-selection behavior of the settings dialog.
- Build tooling: **Spotless** (ktfmt, Google style), **Kover** (coverage), and **Qodana** (code
  quality).

---

## Part 2 — What still has to be implemented

### Core features (highest value first)

1. **Real WPM and accuracy** — the status bar currently shows a hardcoded `0 WPM`; there is no
   timing code. Track session start time and compute words-per-minute and accuracy percentage.
2. **Session statistics** — `TouchTypingSessionService` is still an empty stub. Implement session
   history (WPM, accuracy, duration per session), persist it via `PersistentStateComponent`, and
   surface it in a small stats tab or popup.
3. **Numbers & punctuation** — `shouldIncludeNumbers()` / `shouldIncludePunctuation()` always return
   `false`, so the `NUMBERS` / `PUNCTUATION` sets are dead code. Add settings checkboxes and wire
   them into the generator.
4. **Text length setting** — text length is currently a random value; add a setting (e.g. 20 / 50 /
   100 / 200 characters).
5. **Difficulty levels / curated text** — the curated `SAMPLE_TEXTS` are only used as a fallback.
   Add a mode selector (Random / Words / Sentences) so real text can be practiced.
6. **Per-finger error analysis** — the generator already maps keys to fingers; track which fingers
   produce the most errors and show a "weak fingers" summary.
7. **Completion notification** — show an IDE `Notification` when a text is completed (the green
   highlight is easy to miss).
8. **Action + shortcut** — register an `AnAction` (e.g. `Ctrl+Alt+T`) to toggle the tool window;
   today there is no way to open it without finding it in the tool window bar.
9. **Custom text input** — let users paste or type their own practice text instead of only generated
   ones.

### Cleanup & quality

- **Reduce logging** — there is verbose `logger.info` output on every keystroke and in the settings
  `apply()` path; move to `debug` or remove.
- **Remove duplication** — the R-key reset `KeyListener` is copy-pasted in two places, and
  `createTypingPane()` / `createTypingInputComponents()` are near-identical.
- **Fix stale comments** — e.g. a comment says text length is "1–24" while the code uses
  `nextInt(1, 128)`.
- **Remove template leftovers** — the README still contains the template ToDo list, an unused
  `projectService` message, a `"(2)"` suffix in the configurable name, and a few unused constants
  and methods.
- **More tests** — add unit tests for `PracticeTextGeneratorService` (character-set building, key
  limits) to complement the existing config tests.

---

## Quick reference

| Item        | Value                                                    |
|-------------|----------------------------------------------------------|
| Plugin name | Touch Typing Practice                                    |
| Plugin ID   | `com.github.pyctam.touchtypingpractice`                  |
| Version     | 0.0.1                                                    |
| Language    | Kotlin (JVM 17)                                          |
| Target IDE  | IntelliJ IDEA 2023.3 – 2025.3 (`233` – `253.*`)          |
| Build       | Gradle 8.10.2 + IntelliJ Platform Gradle Plugin          |
| Source      | `src/main/kotlin/com/github/pyctam/touchtypingpractice/` |

