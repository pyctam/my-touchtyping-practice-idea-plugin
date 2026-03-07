# Touch Typing Practice Plugin - UI Design Guidelines

## Overview

This document outlines the UI/UX design standards implemented in the Touch Typing Practice plugin, following **IntelliJ IDEA Platform Best Practices**.

---

## 1. Spacing System (8px Base Unit)

IntelliJ IDEA uses an **8px base unit** as the foundation for all spacing and sizing decisions. This ensures visual consistency and proper visual hierarchy.

### Spacing Scale

| Value | Base Units | Usage | Code |
|-------|-----------|-------|------|
| **8px** | 1 unit | Compact spacing, small gaps | `JBUI.Borders.empty(8)` |
| **12px** | 1.5 units | **Standard padding (primary)** | `JBUI.Borders.empty(12)` |
| **16px** | 2 units | Comfortable spacing | `JBUI.Borders.empty(16)` |
| **20px** | 2.5 units | Generous spacing | `JBUI.Borders.empty(20)` |
| **24px** | 3 units | Large visual separation | `JBUI.Borders.empty(24)` |

### Implementation in Project

**TouchTypingUIComponentsFactory.kt** defines spacing constants:

```kotlin
const val PADDING = 12              // Standard padding (1.5 units)
const val PADDING_SMALL = 8         // Compact spacing (1 unit)
const val PADDING_COMFORTABLE = 16  // Comfortable spacing (2 units)
```

### Usage Examples

```kotlin
// Main panel with standard padding
border = JBUI.Borders.empty(PADDING)  // 12px all sides

// Component with different padding values
border = JBUI.Borders.empty(PADDING_SMALL, PADDING, PADDING_SMALL, PADDING)
```

---

## 2. Typography Hierarchy

IntelliJ IDEA follows a structured typography system to establish visual hierarchy and improve readability.

### Font Scale

| Role | Size | Weight | Usage | Code |
|------|------|--------|-------|------|
| **Header** | 13pt | Bold | Section titles | `JBFont.bold()` |
| **Body** | 12pt | Regular | Main text content | `UIUtil.getDefaultFont()` |
| **Medium** | 12pt | Medium | Status labels, emphasis | `JBFont.medium()` |
| **Small** | 11pt | Regular | Secondary info, hints | `UIUtil.getLabelFont().deriveFont(11f)` |

### Implementation in Project

```kotlin
// Status label with medium weight for emphasis
val statusLabel = JBLabel("Typing speed: 0 WPM | Typing errors: 0")
statusLabel.font = JBFont.medium()  // IntelliJ typography standard

// Text pane with configurable size
val pane = JTextPane()
pane.font = applyFontSize(pane.font, fontSizePt)  // Apply font size dynamically
```

---

## 3. Color & Theme Support

All colors must use **JBColor** for automatic light/dark theme support.

### Color Usage Guidelines

| Component | Light Theme | Dark Theme | Code |
|-----------|------------|-----------|------|
| **Panel Background** | Light Gray | Dark Gray | `UIUtil.getPanelBackground()` |
| **Borders** | Gray | Light Gray | `JBColor.border()` |
| **Text (Primary)** | Black | White | `UIUtil.getLabelForeground()` |
| **Error Highlight** | Light Red | Dark Red | `JBColor(0xFFCCCC, 0x7A4A4A)` |

### Best Practices

✅ **DO:**
```kotlin
// Use JBColor for theme-aware colors
border = JBUI.Borders.customLine(JBColor.border(), 1)
background = UIUtil.getPanelBackground()
```

❌ **DON'T:**
```kotlin
// Avoid hard-coded colors
border = JBUI.Borders.customLine(Color.GRAY, 1)
background = Color.WHITE
```

---

## 4. Component Sizing

Components should use DPI-aware sizing with proper minimum and preferred dimensions.

### DPI Scaling

IntelliJ automatically scales components for different screen densities using **JBUI.scale()**:

```kotlin
// DPI-aware sizing
val scaledHeight = JBUI.scale(24)  // Scales automatically on high-DPI screens

// Creating sizes
panel.minimumSize = JBUI.size(width, height)
panel.preferredSize = JBUI.size(width, height)
```

### Example: Status Panel

```kotlin
val labelHeight = statusLabel.preferredSize.height
val verticalPadding = JBUI.scale(PADDING) * 2  // 12px * 2 = 24px
val totalHeight = labelHeight + verticalPadding

panel.minimumSize = JBUI.size(0, totalHeight)
panel.preferredSize = JBUI.size(0, totalHeight)
```

---

## 5. Borders & Visual Separation

Borders provide visual definition and help establish component hierarchy.

### Border Patterns

**Standard Border (Padding + Edge)**
```kotlin
border = JBUI.Borders.compound(
    JBUI.Borders.empty(PADDING),              // Inner padding: 12px
    JBUI.Borders.customLine(JBColor.border(), 1)  // Edge: 1px border
)
```

**Spacing Only (No Border)**
```kotlin
border = JBUI.Borders.empty(PADDING)  // Just 12px spacing
```

**Small Borders**
```kotlin
border = JBUI.Borders.customLine(JBColor.border(), 1)  // 1px border, no padding
```

### Usage in Project

**Reference Text Pane:**
```kotlin
pane.border = JBUI.Borders.compound(
    JBUI.Borders.empty(PADDING),
    JBUI.Borders.customLine(JBColor.border(), 1)
)
```

**Input Area:**
```kotlin
scrollPane.border = JBUI.Borders.compound(
    JBUI.Borders.empty(PADDING),
    JBUI.Borders.customLine(JBColor.border(), 1)
)
```

---

## 6. Layout & Component Organization

### Main Container Structure

```
┌─ MainPanel (PADDING = 12px all sides) ────────────────────┐
│                                                             │
│  ┌─ TypingArea (Splitter) ──────────────────────────────┐ │
│  │                                                       │ │
│  │  ┌─ ReferenceTextPane (PADDING = 12px) ───────────┐ │ │
│  │  │  (Shows text to type)                           │ │ │
│  │  │  Border: 12px padding + 1px edge               │ │ │
│  │  └───────────────────────────────────────────────┘ │ │
│  │                                                       │ │
│  │  ┌─ InputTypingPane (PADDING = 12px) ────────────┐ │ │
│  │  │  (User types here)                             │ │ │
│  │  │  Border: 12px padding + 1px edge              │ │ │
│  │  └───────────────────────────────────────────────┘ │ │
│  └───────────────────────────────────────────────────┘ │
│                                                         │
│  ┌─ StatusPanel (PADDING = 12px) ────────────────────┐ │
│  │  "Typing speed: 0 WPM | Typing errors: 0"        │ │
│  │  (Medium font weight for emphasis)                │ │
│  └───────────────────────────────────────────────────┘ │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### Key Design Decisions

1. **Splitter Ratio**: 25% reference text, 75% input area
2. **Padding Consistency**: All components use 12px standard padding
3. **Border Style**: Subtle 1px borders with proper padding for definition
4. **Typography**: Medium weight for status labels to establish hierarchy
5. **Component Heights**: Dynamically calculated based on content + padding

---

## 7. Dark/Light Mode Compliance

The plugin automatically adapts to IDE theme settings through **JBColor** and **UIUtil**.

### Automatic Theme Support

```kotlin
// Panel background adapts to theme
background = UIUtil.getPanelBackground()
// Light theme: Light gray
// Dark theme: Dark gray

// Borders adapt to theme
border = JBUI.Borders.customLine(JBColor.border(), 1)
// Light theme: Gray
// Dark theme: Light gray
```

### Testing Theme Changes

In IntelliJ IDEA:
1. **File** → **Settings/Preferences**
2. **Appearance & Behavior** → **Appearance**
3. Switch between **Light** and **Dark** themes
4. Plugin UI automatically adapts without code changes

---

## 8. Implementation Checklist

When adding new UI components to the plugin:

- [ ] Use `PADDING = 12` for standard padding (IntelliJ 8px base unit)
- [ ] Use `JBUI.Borders.empty(PADDING)` for spacing
- [ ] Use `JBUI.Borders.compound()` for padding + border combination
- [ ] Use `JBColor` for all colors (not hard-coded RGB values)
- [ ] Use `UIUtil.getPanelBackground()` for panel backgrounds
- [ ] Use `JBFont.medium()` for emphasis, regular for body text
- [ ] Use `JBUI.scale()` for DPI-aware sizing
- [ ] Test in both Light and Dark themes
- [ ] Ensure proper visual hierarchy with consistent spacing

---

## 9. References

- **IntelliJ Platform UI Guidelines**: https://jetbrains.design/intellij/
- **JetBrains Design System**: https://www.jetbrains.com/help/idea/
- **JBUI Documentation**: IntelliJ SDK
- **8px Base Unit**: Standard in modern UI design (Material Design, iOS HIG)

---

## 10. Future Improvements

Potential enhancements to consider:

1. **Accessibility**: Add high-contrast mode support
2. **Typography**: Implement custom font family options in settings
3. **Spacing**: Add configuration for compact/comfortable/spacious modes
4. **Visual Feedback**: Enhanced selection highlighting with proper contrast
5. **Animations**: Smooth transitions following IntelliJ patterns

---

## Contact & Questions

For questions about UI design implementation or guidelines, refer to:
- **IMPLEMENTATION_GUIDE.md** - Detailed development patterns
- **CODE_EXAMPLES.md** - Ready-to-use code snippets
- **TouchTypingUIComponentsFactory.kt** - Current implementation reference


