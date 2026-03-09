# Touch Typing Practice Plugin - Design System Reference

## Quick Reference Card

### Spacing Scale (IntelliJ 8px Base Unit)

```
┌─ 8px Base Unit Grid ──────────────────────────────────┐
│                                                        │
│  [8px]  [8px]  [8px]  [8px]  [8px]  [8px]            │
│   ─────┬──────┬──────┬──────┬──────┬──────            │
│        │ 1    │ 1.5  │ 2    │ 2.5  │ 3 units         │
│   ─────┼──────┼──────┼──────┼──────┼──────            │
│   8px  │ 12px │ 16px │ 20px │ 24px │                 │
│        │ ▲    │      │      │      │                 │
│        │ ├─ PADDING (Active)                          │
│        │ │    │      │      │      │                 │
│        └─┴────┴──────┴──────┴──────┴──────            │
│                                                        │
└────────────────────────────────────────────────────────┘
```

### Spacing Constants

```kotlin
// IntelliJ Design System
const val PADDING = 12              // 1.5 units (standard)
const val PADDING_SMALL = 8         // 1 unit (compact)
const val PADDING_COMFORTABLE = 16  // 2 units (spacious)
```

### Usage Examples

```kotlin
// Standard padding (most common)
border = JBUI.Borders.empty(PADDING)  // 12px all sides

// Compound: Padding + Border
border = JBUI.Borders.compound(
    JBUI.Borders.empty(PADDING),
    JBUI.Borders.customLine(JBColor.border(), 1)
)

// Asymmetric padding
border = JBUI.Borders.empty(PADDING_SMALL, PADDING, PADDING_SMALL, PADDING)
// Result: 8px left/right, 12px top/bottom
```

---

## Typography Hierarchy

```
┌─ IntelliJ Typography System ──────────────────────────┐
│                                                        │
│  Header (13pt Bold)                                   │
│  ═══════════════════════════════════════════════      │
│  Section titles, panel headers                        │
│                                                        │
│  Body (12pt Regular)                                  │
│  ───────────────────────────────────────────────      │
│  Main text content, descriptions, labels              │
│                                                        │
│  ▲ Status (12pt Medium)                               │
│  ├─ Emphasis labels, important information             │
│                                                        │
│  Small (11pt Regular)                                 │
│  ・・・・・・・・・・・・・・・・・・・・・・・     │
│  Secondary info, hints, captions                      │
│                                                        │
└────────────────────────────────────────────────────────┘
```

### Font Implementation

```kotlin
// Header (13pt Bold)
label.font = JBFont.bold()

// Body (12pt Regular)
label.font = UIUtil.getDefaultFont()

// Medium emphasis (12pt Medium)
label.font = JBFont.medium()

// Small text (11pt)
label.font = UIUtil.getLabelFont().deriveFont(11f)
```

---

## Component Layout Pattern

```
┌──────────────────────────────────────────────────────┐
│ Main Container (PADDING = 12px all sides)            │
│                                                       │
│  ┌─────────────────────────────────────────────────┐ │
│  │ Component A                                      │ │
│  │ (PADDING = 12px)                               │ │
│  │ Border: 1px edge                                │ │
│  └─────────────────────────────────────────────────┘ │
│                                                       │
│  ┌─────────────────────────────────────────────────┐ │
│  │ Component B                                      │ │
│  │ (PADDING = 12px)                               │ │
│  │ Border: 1px edge                                │ │
│  └─────────────────────────────────────────────────┘ │
│                                                       │
│  ┌─────────────────────────────────────────────────┐ │
│  │ Status Panel (PADDING = 12px)                   │ │
│  │ Medium font, consistent spacing                 │ │
│  └─────────────────────────────────────────────────┘ │
│                                                       │
└──────────────────────────────────────────────────────┘
```

---

## Theme Support (Automatic)

### Light Theme
```
┌──────────────────────────┐
│ Light Gray Background    │
│ ┌───────────────────────┐│
│ │ White Component       ││
│ │ Gray Border (1px)     ││
│ │ Black Text            ││
│ └───────────────────────┘│
└──────────────────────────┘
```

### Dark Theme (Same Code)
```
┌──────────────────────────┐
│ Dark Gray Background     │
│ ┌───────────────────────┐│
│ │ Dark Component        ││
│ │ Light Gray Border     ││
│ │ White Text            ││
│ └───────────────────────┘│
└──────────────────────────┘
```

### Implementation
```kotlin
// Automatic theme support - no code changes needed
background = UIUtil.getPanelBackground()      // Adapts to theme
border = JBUI.Borders.customLine(JBColor.border(), 1)  // Adapts to theme
```

---

## DPI Scaling (Automatic)

```
96 DPI (Standard)     144 DPI (High)      192 DPI (Very High)
┌──────────────┐      ┌──────────────┐    ┌──────────────┐
│ 12px padding │      │ 18px padding │    │ 24px padding │
│              │      │              │    │              │
│ 1x scale     │      │ 1.5x scale   │    │ 2x scale     │
└──────────────┘      └──────────────┘    └──────────────┘

// Automatic via JBUI.scale()
val paddingScaled = JBUI.scale(PADDING)  // Scales per DPI
```

---

## Implementation Checklist

When adding new UI components:

### Spacing
- [ ] Use `PADDING = 12` for standard spacing
- [ ] Use `JBUI.Borders.empty(PADDING)` for spacing
- [ ] Use `JBUI.Borders.compound()` for padding + border
- [ ] Consider future `PADDING_SMALL` or `PADDING_COMFORTABLE`

### Typography
- [ ] Headers use `JBFont.bold()`
- [ ] Body text uses `UIUtil.getDefaultFont()`
- [ ] Emphasis uses `JBFont.medium()`
- [ ] Small text uses `UIUtil.getLabelFont().deriveFont(11f)`

### Colors & Borders
- [ ] Background uses `UIUtil.getPanelBackground()`
- [ ] Borders use `JBColor.border()`
- [ ] Text uses IntelliJ default colors
- [ ] No hard-coded RGB values

### Component Sizing
- [ ] Use `JBUI.scale()` for DPI-aware sizing
- [ ] Set both minimum and preferred sizes
- [ ] Calculate heights including padding
- [ ] Test on high-DPI displays (150%, 200%)

### Testing
- [ ] Test in Light theme
- [ ] Test in Dark theme
- [ ] Test on standard DPI (96 DPI / 1x)
- [ ] Test on high DPI (150% / 144 DPI / 1.5x)
- [ ] Verify no hard-coded colors appear

---

## Common Patterns

### Simple Component with Padding

```kotlin
val component = JBLabel("Text")
component.border = JBUI.Borders.empty(PADDING)
```

### Component with Border Definition

```kotlin
val component = JTextPane()
component.border = JBUI.Borders.compound(
    JBUI.Borders.empty(PADDING),
    JBUI.Borders.customLine(JBColor.border(), 1)
)
```

### Dynamic Height Calculation

```kotlin
val labelHeight = label.preferredSize.height
val verticalPadding = JBUI.scale(PADDING) * 2  // Top + Bottom
val totalHeight = labelHeight + verticalPadding

component.minimumSize = JBUI.size(0, totalHeight)
component.preferredSize = JBUI.size(0, totalHeight)
```

### Asymmetric Padding

```kotlin
// Left: 8px, Top: 12px, Right: 8px, Bottom: 12px
border = JBUI.Borders.empty(
    PADDING_SMALL,  // left
    PADDING,        // top
    PADDING_SMALL,  // right
    PADDING         // bottom
)
```

---

## Visual Grid Reference

### 12px Grid (IntelliJ Standard)

```
+---+---+---+---+---+---+---+---+
| 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 |  (12px = 1.5 units)
+---+---+---+---+---+---+---+---+
  ↑           ↑           ↑
  8px        12px        16px
```

### Common Dimensions

```
PADDING = 12px
├─ Component height + PADDING * 2 = Total height
├─ Example: Label (20px) + 12px*2 = 44px total
└─ Proper visual proportion

PADDING_SMALL = 8px
├─ Compact spacing between small elements
├─ Internal component padding
└─ Reserved for future layouts

PADDING_COMFORTABLE = 16px
├─ Spacious layouts for accessibility
├─ Large visual separation
└─ Reserved for future variations
```

---

## Troubleshooting

### Issue: Components look too close together
**Solution**: Verify using `PADDING = 12`, not other values
```kotlin
// ❌ Wrong
border = JBUI.Borders.empty(4)

// ✅ Correct
border = JBUI.Borders.empty(PADDING)  // 12px
```

### Issue: Theme colors not adapting
**Solution**: Use JBColor instead of hard-coded colors
```kotlin
// ❌ Wrong
border = JBUI.Borders.customLine(Color.GRAY, 1)

// ✅ Correct
border = JBUI.Borders.customLine(JBColor.border(), 1)
```

### Issue: Layout looks cramped on high-DPI
**Solution**: Use JBUI.scale() for all dimensions
```kotlin
// ❌ Wrong
val height = 24  // Fixed size

// ✅ Correct
val height = JBUI.scale(24)  // Scales per DPI
```

### Issue: Text too small on dark backgrounds
**Solution**: Use IntelliJ default fonts via UIUtil
```kotlin
// ❌ Wrong
font = Font("Arial", 10)

// ✅ Correct
font = UIUtil.getDefaultFont()
```

---

## Quick Command Reference

### Run Build with Formatting
```bash
./gradlew spotlessApply  # Apply code formatting
./gradlew build -x test  # Build without tests
```

### Test Theme Changes
1. Open IntelliJ IDEA
2. Settings → Appearance & Behavior → Appearance
3. Switch between Light/Dark themes
4. Plugin UI automatically adapts

### Verify High-DPI Scaling
1. Linux: Change DPI in display settings
2. Windows: Settings → Display → Scale
3. Plugin UI scales automatically via JBUI

---

## Further Learning

- **UI_DESIGN_GUIDELINES.md** - Comprehensive design documentation
- **IMPLEMENTATION_GUIDE.md** - Architecture and patterns
- **CODE_EXAMPLES.md** - Ready-to-use code snippets
- **IntelliJ Platform Documentation** - Official IntelliJ SDK docs

---

**Version**: 1.0
**Last Updated**: March 7, 2026
**Status**: Ready for Use ✅


