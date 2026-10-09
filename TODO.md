It was# TODO

## Features

1. **Real WPM + accuracy** — the status bar's WPM area (and its `" | "` separator) is hidden until
   implemented. Track session start time, compute words-per-minute and accuracy %, and reveal the
   WPM label in the status bar.
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
7. **Custom text input** — let users paste or type their own practice text instead of only
   generated ones.

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
4. **No word wrap on the typing `JTextPane`** — the KDoc in `createTypingArea()` claims
   "word-wrapping `JTextPane`" but no wrapping is configured. Unlike the old `JTextArea` with
   `setLineWrap(true)`, a `JTextPane` does not wrap by default. Long lines will show a horizontal
   scrollbar. Fix: set `wrapStyleWord = true` and `wrapStyleLength` on the pane, or configure the
   view factory.
   *`ui/TouchTypingUIComponentsFactory.kt`.*
5. **Rich-text paste in the typing area** — `JTextPane` accepts rich-text paste (fonts, colors,
   HTML from browser/Word), unlike the old `JTextArea` which pasted plain text only. Pasted
   attributes persist until the next keystroke re-runs `applyTo`. Fix: set
   `typingArea.editorKit = PlainTextEditorKit()` to restore plain-text-only paste semantics.
   *`ui/TouchTypingUIComponentsFactory.kt`.*

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
10. **Redundant EDT hops** — `TouchTypingDocumentListener.updateHighlights()` wraps work in
    `SwingUtilities.invokeLater`, but `DocumentListener` callbacks already fire on the EDT. Same in
    `ErrorCounter.notifyListeners()` (double `invokeLater`). Also `catch (t: Throwable)` around the
    highlight logic is broader than needed.
11. **Java-style accessors** — `ErrorCounter.getCount()/setCount()` → a Kotlin `var count: Int`
    with the notification in the setter.
12. **Manual service lookup** — `Settings.getInstance()` → `application.service<Settings>()`
    (3 call sites).
13. **`TextFont` is a data class in name only** — it wraps a `String`, but every API
    (`allAvailableFamilies()`, `effectiveFamily()`, `Settings.textFontFamily`) uses raw `String`;
    only `DEFAULT` is a `TextFont`. Either use the type consistently or collapse it to an `object`
    of functions.
14. **Unnecessary defensiveness** — `KeyboardLayout.keysFor` uses `.orEmpty()` on maps that are
    total over `Finger.entries` — plain indexing is clearer.
15. **Side effect in `createPanel()`** — `Configuration.createPanel()` mutates
    `settings.textFontFamily` when the panel is created (normalization that persists without
    Apply). Consider normalizing in `Settings.loadState`/getter instead.
16. **MessageBus connection not scoped** — the factory subscribes with `connect()` (no scope) — it
    lives for the application lifetime per tool-window creation. `connect(project)` (or the tool
    window's content) would auto-dispose it.
17. **Logging** — remaining `logger.info` calls (reset, completion, tool-window lifecycle) →
    `debug`.
18. **O(N) re-color per keystroke** — `HandColors.applyTo` re-colors the entire text on every
    keystroke. The `DocumentEvent` carries the changed offset/length, which is discarded. Not a
    bottleneck at 5–20 words, but won't scale. Fix: drive re-coloring from the `DocumentEvent`
    range (only re-color the inserted/changed characters).
    *`ui/HandColors.kt`, `ui/TouchTypingDocumentListener.kt`.*
19. **Uppercase letters are never colored** — the `KeyboardLayout` only defines lowercase keys, so
    a pasted or shifted uppercase letter stays uncolored. Consider normalizing to lowercase before
    lookup in `colorFor()`.
    *`ui/HandColors.kt`.*
20. **`ALL_KEYS_LIMIT = 100` magic number** — silently breaks if a finger ever gains >100 keys.
    Prefer `Int.MAX_VALUE` or add a `KeyboardLayout.allKeys(mode, finger)` accessor that returns
    the full list without a limit parameter.
    *`ui/HandColors.kt`, `generation/KeyboardLayout.kt`.*
21. **Redundant `text` parameter in `applyTo`** — every call site passes a value equal to
    `pane.text`. Drop the parameter and read `pane.text` internally.
    *`ui/HandColors.kt`.*
22. **Per-character `SimpleAttributeSet` allocation** — a new `SimpleAttributeSet` is allocated per
    character per keystroke. Two static sets (one for LEFT, one for RIGHT) could be reused.
    *`ui/HandColors.kt`.*
23. **Layering: `HandColors` (in `ui`) reaches into `generation` + `config`** — the
    character-to-hand mapping is a domain concept, not a UI concern. Consider moving the mapping
    logic to the `generation` or `config` package.
    *`ui/HandColors.kt`.*
24. **Completion side-effects re-run on every keystroke** — `logger.info` + `addHighlight` for the
    completion state fire on every keystroke while the text is complete, not just on the
    transition into completion. Guard with a state-change check.
    *`ui/TouchTypingDocumentListener.kt`.*
25. **Tests don't verify actual coloring** — `HandColorsTest.applyTo` tests only assert text is
    preserved, never that the foreground attribute was set on the document. Add assertions that
    read back the character attributes and verify the expected color.
    *`test/.../ui/HandColorsTest.kt`.*