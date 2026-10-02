# TODO

## Features

1. **Real WPM + accuracy** — the status bar currently shows a hardcoded `0 WPM`. Track session
   start time, compute words-per-minute and accuracy %, and show them in the status bar.
2. **Session statistics** — session history (WPM, accuracy, duration per session), persisted via
   `PersistentStateComponent`, surfaced in a small stats tab or popup. (The current
   `TouchTypingSessionService` is an empty scaffold — see Bugs.)
3. **Numbers & punctuation** — the generator currently draws only lowercase letters plus a space.
   Add settings checkboxes and extend the character set.
4. **Text length setting** — the text is now sentence-length: word count in `[MIN_WORDS_COUNT,
   MAX_WORDS_COUNT]` (5–20), each word ≤ `MAX_WORD_LENGTH` (7), total 9–159 characters. Add a
   setting to control the length, e.g. by exposing `MIN_WORDS_COUNT`, `MAX_WORDS_COUNT`, and
   `MAX_WORD_LENGTH` as UI inputs.
5. **Difficulty levels / curated text** — add a mode selector (Random / Words / Sentences) so real
   text can be practiced.
6. **Per-finger error analysis** — the generator already maps keys to fingers; track which fingers
   produce the most errors and show a "weak fingers" summary.
7. **Completion notification** — show an IDE `Notification` when a text is completed (the green
   highlight is easy to miss).
8. **Action + shortcut** — register an `AnAction` (e.g., `Ctrl+Alt+T`) to toggle the tool window;
   today there is no way to open it without finding it in the tool window bar.
9. **Custom text input** — let users paste or type their own practice text instead of only
   generated ones.
10. **Count errors during the text typing** — reset the counter only when the text resets.
11. Use different colors for letters for left and right hands when generating the text.
12. Show "Hit to Enter to [reset]" when text typing is completed, and it is highlighted with the green color. Keep the "
    Hit to Enter to " hidden in all other use cases.

## Bugs

### Correctness

1. **`ErrorCounter` listener leak on hot-reload** — `TouchTypingToolWindowFactory` creates one
   `TouchTypingToolWindow` and reuses it on every settings change, but `createStatusPanel()` calls
   `errorCounter.addChangeListener { ... }` on every build and `ErrorCounter` has no
   `removeChangeListener`. After N settings changes there are N listeners updating N dead labels.
   The `TouchTypingToolWindow` KDoc ("a fresh instance is created each time the content is
   (re)built") is stale/wrong. Fix: create a new `TouchTypingToolWindow` per rebuild, or give
   `ErrorCounter` a single replaceable listener.
   *`toolWindow/TouchTypingToolWindowFactory.kt`, `toolWindow/TouchTypingToolWindow.kt`,
   `ui/ErrorCounter.kt`.*
2. **Settings dialog: Cancel does not roll back most fields** — in `Configuration.createPanel()`,
   `bindItem({ settings.textFontFamily }, ...)`, `bindIntValue(settings::textFontSize)`, and
   `bind({ settings.practiceMode }, ...)` write straight into the persisted `Settings` on every UI
   change. Only the finger selection (working copy `FingerSelection`) is Cancel-safe. Either bind
   all fields to a working copy, or document that those fields apply live.
   *`config/Configuration.kt`.*
3. **`Settings.getState()` returns the live instance** — `override fun getState(): Settings = this`
   exposes the live object as serializable state. Return a copy (a small data class) so the
   serializer never holds the live instance.
   *`config/Settings.kt`.*

### Duplication / dead code

4. **Mismatch comparison implemented twice** — `TouchTypingDocumentListener.updateHighlights()` and
   `TextLengthLimiterFilter.hasErrors()` contain the same char-by-char compare loop. Extract one
   pure helper, e.g. `object TextComparison { fun mismatchIndices(typed, original): IntArray;
   fun hasErrors(typed, original): Boolean }`. Also the natural home for the future WPM/accuracy
   and per-finger error analysis logic.
   *`ui/TouchTypingDocumentListener.kt`, `ui/TextLengthLimiterFilter.kt`.*
5. **`buildContent()` and `reset()` duplicate input wiring** — same
   `createTypingInputComponents(...)` + same `EnterKeyDetector(::reset) { ... }` block in both
   methods. Extract `private fun attachTypingInput(text: String): TypingInputComponents`. Also
   `mainPanel.remove(typingInputPanel)` before `addToCenter(...)` is redundant —
   `BorderLayoutPanel.addToCenter` replaces the previous center component.
   *`toolWindow/TouchTypingToolWindow.kt`.*
6. **Border construction duplicated in the UI factory** — `createSampleTextPanel()` and
   `createTypingInputComponents()` build the identical compound border; extract
   `private fun sectionBorder()`. Also `createMainPanel()` is a one-line wrapper with no logic —
   inline it.
   *`ui/TouchTypingUIComponentsFactory.kt`.*
7. **`PracticeTextGeneratorService` is registered but never used as a service** —
   `TouchTypingToolWindow` does `PracticeTextGeneratorService()` (manual construction) instead of
   `application.service<PracticeTextGeneratorService>()`. Pick one: keep the `@Service` and look it
   up properly, or drop the service class and call `PracticeTextGenerator().generate(config)`
   directly.
   *`services/PracticeTextGeneratorService.kt`, `toolWindow/TouchTypingToolWindow.kt`.*
8. **Dead code / template leftovers** —
    - `TouchTypingSessionService` is an empty scaffold with `@Suppress("unused")`, yet it is a
      registered `@Service(Service.Level.PROJECT)` — instantiated and logging for **every project**
      for no reason. Delete it until the session-stats feature exists.
    - `src/test/testData/rename/foo.xml` / `foo_after.xml` are plugin-template leftovers with no
      corresponding test.

### Style / consistency

9. **Typo in public API** — `CONFIG_KEY_LIMIT_PER_FINDER_*` in `UIBundle.kt`: "FINDER" should be
   "FINGER" (the `UI.properties` keys say `key-limit-per-finder` too — fix both).
10. **Inconsistent i18n** — the settings page is fully bundled, but the tool window hardcodes
    `"Typing speed: 0 WPM | Typing errors: 0"`, `"Reset"`, and the tooltip. Move them to
    `UIBundle`; the status format string is also duplicated between the initial text and the
    listener — extract a `formatStatus(wpm, errors)` function.
11. **Redundant EDT hops** — `TouchTypingDocumentListener.updateHighlights()` wraps work in
    `SwingUtilities.invokeLater`, but `DocumentListener` callbacks already fire on the EDT. Same in
    `ErrorCounter.notifyListeners()` (double `invokeLater`). Also `catch (t: Throwable)` around the
    highlight logic is broader than needed.
12. **Java-style accessors** — `ErrorCounter.getCount()/setCount()` → a Kotlin `var count: Int`
    with the notification in the setter.
13. **Manual service lookup** — `Settings.getInstance()` → `application.service<Settings>()`
    (3 call sites).
14. **`TextFont` is a data class in name only** — it wraps a `String`, but every API
    (`allAvailableFamilies()`, `effectiveFamily()`, `Settings.textFontFamily`) uses raw `String`;
    only `DEFAULT` is a `TextFont`. Either use the type consistently or collapse it to an `object`
    of functions.
15. **Unnecessary defensiveness** — `KeyboardLayout.keysFor` uses `.orEmpty()` on maps that are
    total over `Finger.entries` — plain indexing is clearer.
16. **Side effect in `createPanel()`** — `Configuration.createPanel()` mutates
    `settings.textFontFamily` when the panel is created (normalization that persists without
    Apply). Consider normalizing in `Settings.loadState`/getter instead.
17. **MessageBus connection not scoped** — the factory subscribes with `connect()` (no scope) — it
    lives for the application lifetime per tool-window creation. `connect(project)` (or the tool
    window's content) would auto-dispose it.
18. **Logging** — remaining `logger.info` calls (reset, completion, tool-window lifecycle) →
    `debug`.
