# Changelog

## Unreleased

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
