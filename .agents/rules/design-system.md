# Piggy Design System Rule

When creating, modifying, or reviewing Jetpack Compose UI components, screens, or themes in this repository:

1. **Activate the `design-system` Skill**: Read `.agents/skills/design-system/SKILL.md` (and `.agents/skills/design-system/references/DESIGN.md` when detailed specs are needed) to follow the **Piggy Wealth & Expense Design System** ("Modern Tactile Minimalism with Neo-Fintech Polish").
2. **Use `:core:designsystem` Tokens**:
   - Always use `PiggyTheme`, `MaterialTheme.colorScheme`, `PiggyColors`, `PiggyTypography`, `PiggyShapes`, and `PiggySpacing` from `com.goldhardt.designsystem.theme`.
   - Do not hardcode arbitrary hex colors, font families, or corner radii in feature modules.
3. **Financial Typography Precision**:
   - Always use **Plus Jakarta Sans** via `MaterialTheme.typography` / `PiggyTypography`.
   - Apply tabular numerals (`fontFeatureSettings = "tnum"`, e.g. `PiggyTypography.displayCurrency`, `PiggyTypography.currencyItem`) to all currency amounts, balances, and numerical ledger entries.

