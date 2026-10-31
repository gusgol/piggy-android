---
name: design-system
description: >-
  Piggy Wealth & Expense Design System specification (exported from Google Stitch) and Jetpack Compose (:core:designsystem) mapping.
  Use this skill whenever creating, styling, refactoring, or reviewing UI screens, Compose components, themes, colors, typography, shapes, charts, or layouts in Piggy.
---

# Piggy Wealth & Expense Design System

This skill provides the design tokens, brand guidelines, component specifications, and Jetpack Compose mappings for the **Piggy Wealth & Expense Design System** ("Modern Tactile Minimalism with Neo-Fintech Polish").

For the complete verbatim Google Stitch export (including raw YAML tokens and full component specs), read [references/DESIGN.md](./references/DESIGN.md).

---

## 1. Jetpack Compose Implementation (`:core:designsystem`)

All Compose UI must use `PiggyTheme` and tokens from `com.goldhardt.designsystem.theme`:

- **Theme**: `PiggyTheme` in `core/designsystem/src/main/java/com/goldhardt/designsystem/theme/Theme.kt`
- **Colors**: `MaterialTheme.colorScheme` & `PiggyColors` in `core/designsystem/src/main/java/com/goldhardt/designsystem/theme/Color.kt`
- **Typography**: `MaterialTheme.typography` (`AppTypography`) & custom currency/badge styles in `core/designsystem/src/main/java/com/goldhardt/designsystem/theme/Type.kt`
- **Shapes & Spacing**: `MaterialTheme.shapes` (`PiggyShapes`) & `PiggySpacing` in `core/designsystem/src/main/java/com/goldhardt/designsystem/theme/Shape.kt`

---

## 2. Color Architecture

### Material 3 ColorScheme (`MaterialTheme.colorScheme`)
- **Primary (Deep Jade / Emerald)**:
  - Light: `primary = #006948`, `onPrimary = #FFFFFF`, `primaryContainer = #00855D`, `onPrimaryContainer = #F5FFF7`, `inversePrimary = #68DBA9`
  - Dark: `primary = #68DBA9`, `onPrimary = #003824`, `primaryContainer = #006948`, `onPrimaryContainer = #85F8C4`
- **Secondary (Piggy Rose)**:
  - Light: `secondary = #A93349`, `onSecondary = #FFFFFF`, `secondaryContainer = #FE7488`, `onSecondaryContainer = #730425`
  - Dark: `secondary = #FFB2B9`, `onSecondary = #67001E`, `secondaryContainer = #891933`, `onSecondaryContainer = #FFDADC`
- **Tertiary (Emerald Accent)**:
  - Light: `tertiary = #006947`, `onTertiary = #FFFFFF`, `tertiaryContainer = #00855B`, `onTertiaryContainer = #F5FFF6`
- **Surfaces & Neutrals**:
  - Light: `background = #FAF8FF`, `surface = #FAF8FF`, `surfaceContainerLowest = #FFFFFF`, `surfaceContainerLow = #F2F3FF`, `surfaceContainer = #EAEDFF`, `surfaceContainerHigh = #E2E7FF`, `surfaceContainerHighest = #DAE2FD`, `onSurface = #131B2E`, `onSurfaceVariant = #3D4A42`, `outline = #6D7A72`, `outlineVariant = #BCCAC0`
  - Dark: `background = #0B0F17`, `surface = #0B0F17`, `surfaceContainerLow = #131B2A`, `surfaceContainer = #131B2A`, `surfaceContainerHigh = #1E293B`, `outlineVariant = #334155`

### Extended Semantic & Brand Tokens (`PiggyColors`)
- **Primary Brand (Emerald Growth)**:
  - `Emerald700` (`#047857`): Active states, deep brand contrast, header indicators
  - `Emerald600` (`#059669`): Core interactive buttons, primary icons, active navigation tabs
  - `Emerald500` (`#10B981`): Positive balance deltas, progress rings, growth stats
  - `Emerald50` (`#ECFDF5`): Pill background fills, success chip containers, selected state tints
- **Playful Brand Accent (Piggy Rose)**:
  - `Rose600` (`#E11D48`): Piggy avatar accents, micro-badges, overbudget warnings
  - `Rose400` (`#FB7185`): Brand warmth, delightful highlights, empty-state illustrations
  - `Rose50` (`#FFF1F2`): Gentle alert backgrounds, housing category pill tint
- **Functional Category Tokens**:
  - **Housing**: `CategoryHousing` (`#F43F5E`) on `CategoryHousingTint` (`#FFF1F2`)
  - **Groceries**: `CategoryGroceries` (`#059669`) on `CategoryGroceriesTint` (`#ECFDF5`)
  - **Dining Out**: `CategoryDiningOut` (`#D97706`) on `CategoryDiningOutTint` (`#FFFBEB`)
  - **Transport**: `CategoryTransport` (`#0284C7`) on `CategoryTransportTint` (`#F0F9FF`)
  - **Utilities**: `CategoryUtilities` (`#0891B2`) on `CategoryUtilitiesTint` (`#ECFEFF`)
  - **Coffee & Leisure**: `CategoryCoffeeLeisure` (`#854D0E`) on `CategoryCoffeeLeisureTint` (`#FEFCE8`)
- **Slate Neutrals**:
  - `CanvasBase` (`#F8FAFC`), `SurfacePure` (`#FFFFFF`), `SurfaceSubtle` (`#F1F5F9`), `BorderHairline` (`#E2E8F0`), `TextStrong` (`#0F172A`), `TextMuted` (`#64748B`), `TextSubtle` (`#94A3B8`)

---

## 3. Typography (`Plus Jakarta Sans`)

All typography uses **Plus Jakarta Sans** (`plusJakartaSansFontFamily`):
- **Tabular Numerals (`fontFeatureSettings = "tnum"`)**: MUST be applied to all currency and numerical metrics (`PiggyTypography.displayCurrency`, `PiggyTypography.displayCurrencyMobile`, `PiggyTypography.currencyItem`, balances, transaction rows).
- **Scale**:
  - `displayCurrency` (`displayLarge`): 36sp, Bold (700), lineHeight 44sp, letterSpacing -0.03em, `tnum`
  - `displayCurrencyMobile` (`displayMedium`): 30sp, Bold (700), lineHeight 36sp, letterSpacing -0.025em, `tnum`
  - `headlineLarge` (`headline-lg`): 24sp, Bold (700), lineHeight 32sp, letterSpacing -0.02em
  - `headlineMedium` (`headline-md`): 20sp, SemiBold (600), lineHeight 28sp, letterSpacing -0.015em
  - `titleSmall` / `titleMedium` (`title-sm`): 16sp, SemiBold (600), lineHeight 24sp, letterSpacing -0.01em
  - `bodyMedium` (`body-md`): 15sp, Normal (400), lineHeight 22sp, letterSpacing 0em
  - `bodySmall` (`body-sm`): 13sp, Normal (400), lineHeight 18sp, letterSpacing 0.01em
  - `currencyItem`: 16sp, SemiBold (600), lineHeight 24sp, letterSpacing -0.01em, `tnum`
  - `labelCaps` (`labelSmall`): 11sp, Bold (700), lineHeight 16sp, letterSpacing 0.05em (strictly uppercase structural meta labels like `"EXPENSES BY CATEGORY"`, `"TOTAL BALANCE"`)
  - `badgeLabel` (`labelMedium`): 12sp, SemiBold (600), lineHeight 16sp, letterSpacing 0.01em

---

## 4. Layout, Shapes & Component Guidelines

- **Spacing (`PiggySpacing`)**:
  - Outer canvas margins: `20.dp` (`margin`) on standard viewports, `16.dp` (`marginSm`) on compact devices
  - Section vertical rhythm: `20.dp` (`spaceXl`)
  - Card internal padding: `20.dp` for high-impact metric cards, `16.dp` for itemized transaction lists
  - Inter-item stacking: `8.dp` (`spaceSm`) gap when floating, or `0.dp` with `1.dp` hairline dividers (`BorderHairline`) when nested inside grouped cards
- **Shapes (`PiggyShapes`)**:
  - Base surfaces & content cards: `20.dp` (`PiggyShapes.card` / `shapes.large`)
  - Inner interactive elements (inputs, inner category wrappers, charts): `12.dp` (`shapes.medium`) to `16.dp`
  - Interactive pills, buttons & badges: `CircleShape` (`9999px`)
- **Key Components**:
  1. **Expense Cards & Ledger Rows**: Pure white surface (`SurfacePure`), `20.dp` radius, `1.dp` hairline border (`BorderHairline`), `44.dp` circular category icon container filled with pastel tint + `20.dp` icon in category key tone, right-aligned amount in `currencyItem` (`tnum`).
  2. **Category & Fixed/Variable Badges**: `26.dp` height, `horizontal = 10.dp, vertical = 4.dp` padding, pill shape, subdued background tint, `6.dp` circular dot prefix in saturated category color, text in `badgeLabel`.
  3. **Total Balance & Summary Banner**: Vertical gradient from `#FFFFFF` to `#F0FDF4` (`MintBloom`) enclosed by a `1.dp` emerald-tinted border (`Emerald500.copy(alpha = 0.15f)`). Uppercase `labelCaps` label, amount in `displayCurrency` with `Emerald600` currency prefix.
  4. **Floating Navigation Dock (`PiggyNavigationBar` / `PiggyNavigationBarItem`)**: Built on Material 3 `ShortNavigationBar` and `ShortNavigationBarItem` (`iconPosition = NavigationItemIconPosition.Start`). Floating `64.dp` pill capsule (`PiggyShapeTokens.Pill`) suspended `16.dp` above navigation bar safe area with `20.dp` horizontal margins (`PiggySpacing.margin`), translucent `surfaceContainerLowest` container (`0.90f` alpha), `1.dp` hairline border, and `primaryFixed` (`#85F8C4`) active indicator pill with `onTertiaryFixed` (`#002113`) active icon and bold `badgeLabel` text.
  5. **Buttons**: Primary action `52.dp` height, pill shape (`CircleShape`), solid `Emerald600` (`#059669`), white `titleSmall` text.

