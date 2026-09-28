# Touch Typing Practice — Branch Analysis & Feature Recommendations

> Generated: 2026-09-28
> Scope: full review of the current branch (11 source files, `plugin.xml`, `UI.properties`, docs).

---

## 1. What the plugin does today

A right-side tool window where a random practice string is generated, the user types it into a
`JTextArea`, mismatches get highlighted in the reference pane, and an error counter updates a
status bar. Settings (font size, hand mode, key limit per finger, finger bitmask) live in
`Settings.kt` and hot-reload the UI via a MessageBus topic.

---

## 2. Key findings (bugs / dead code)

| # | Issue                                                                                                                                                                                                                                                                                                       | Location                                                               |
|---|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------|
| 1 | **WPM is hardcoded to 0** — status bar always shows `"Typing speed: 0 WPM"`. No timing code exists anywhere.                                                                                                                                                                                                | `TouchTypingToolWindowFactory.kt:194`                                  |
| 2 | **Numbers/punctuation are dead code** — `shouldIncludeNumbers()` / `shouldIncludePunctuation()` unconditionally return `false`; the `NUMBERS`/`PUNCTUATION` constants are never used.                                                                                                                       | `PracticeTextGeneratorService.kt:241-253`                              |
| 3 | **`SAMPLE_TEXTS` is nearly dead** — 13 curated pangrams are defined but only used as a fallback when the char set is empty (which basically never happens).                                                                                                                                                 | `PracticeTextGeneratorService.kt:73-88`                                |
| 4 | **`TouchTypingSessionService` is an empty stub** — just a TODO comment.                                                                                                                                                                                                                                     | `services/MyProjectService.kt`                                         |
| 5 | **Zero tests** — `src/test/kotlin` is empty; only leftover template `testData/rename` files remain.                                                                                                                                                                                                         | `src/test/`                                                            |
| 6 | **Excessive logging** — `logger.info` on every keystroke in the document listener and the `apply()` method is ~60% log statements.                                                                                                                                                                          | `TouchTypingDocumentListener.kt`, `Configuration.kt`                   |
| 7 | **Duplicated code** — the R-key reset `KeyListener` is copy-pasted in both `createInitialContent()` and `resetPractice()`; `createTypingPane()` and `createTypingInputComponents()` are near-identical.                                                                                                     | `TouchTypingToolWindowFactory.kt`, `TouchTypingUIComponentsFactory.kt` |
| 8 | **Stale comment** — says "length between 1 and 24" but the code is `nextInt(1, 128)`.                                                                                                                                                                                                                       | `PracticeTextGeneratorService.kt:270`                                  |
| 9 | **Template leftovers** — README is still the template ToDo list, `projectService` message in `UI.properties`, `"(2)"` suffix in the Configuration name, unused `PADDING`/`PADDING_COMFORTABLE` constants, unused `ErrorCounter.increment()/reset()/removeChangeListener()`, unused `currentListener` field. | various                                                                |

---

## 3. Features to ADD (highest value first)

1. **Real WPM + accuracy** — the status bar already promises it. Track session start time,
   compute `5 * (correctChars/5) / minutes`, show accuracy %. This is the single most expected
   feature for a typing plugin.
2. **Implement `TouchTypingSessionService`** — session history (WPM/accuracy/duration per
   session), persisted via `PersistentStateComponent`, with a small "stats" tab or popup. This is
   the natural home for it and the stub was built for this.
3. **Wire up numbers/punctuation** — add two checkboxes to `Configuration.kt` and make
   `shouldIncludeNumbers/Punctuation` read from `Settings`. The plumbing is 90% done.
4. **Text length setting** — currently random 1–127 chars; add a spinner (e.g., 20/50/100/200)
   to settings.
5. **Difficulty levels / curated text** — actually use `SAMPLE_TEXTS` as a "real text" mode vs.
   the current "random chars" mode. A mode selector (Random / Words / Sentences) is a common
   differentiator.
6. **Per-finger error analysis** — the generator already maps keys→fingers; track which
   keys/fingers produce the most errors and show a "weak fingers" summary. Very on-theme for this
   plugin.
7. **Completion notification** — a `Notification` when the text is completed (the green highlight
   exists but is easy to miss).
8. **Action + shortcut** — register an `AnAction` (e.g., `Ctrl+Alt+T`) to toggle the tool window;
   currently there's no way to open it without finding it in the tool window bar.
9. **Custom text input** — let users paste/type their own practice text instead of only generated
   ones.
10. **Tests** — at minimum unit tests for `PracticeTextGeneratorService` (char set building, key
    limits) and `Finger` bitmask logic; these are pure logic and easy to test without the IDE.

---

## 4. Things to REMOVE / clean up

- The always-false `shouldIncludeNumbers/Punctuation` (or wire them — see above)
- Verbose `logger.info` per-keystroke logging → `debug` or remove
- Duplicated R-key listener → extract to a single method
- Duplicated `createTypingPane`/`createTypingInputComponents` → one should delegate to the other
- Template leftovers: README ToDo section, `projectService` message, `"(2)"` in the configurable
  name, unused constants/methods
- The `errorCounter!!` non-null assertions (it's a `lateinit var`, the `!!` is redundant)

---

## 5. Recommended priority

1. **Real WPM/accuracy**
2. **Session stats in the existing stub service**
3. **Wire up numbers/punctuation + text length**

These three turn the plugin from a demo into a genuinely useful practice tool with the least new
surface area.

