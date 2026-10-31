---
name: Piggy Wealth & Expense Design System
colors:
  surface: '#faf8ff'
  surface-dim: '#d2d9f4'
  surface-bright: '#faf8ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f2f3ff'
  surface-container: '#eaedff'
  surface-container-high: '#e2e7ff'
  surface-container-highest: '#dae2fd'
  on-surface: '#131b2e'
  on-surface-variant: '#3d4a42'
  inverse-surface: '#283044'
  inverse-on-surface: '#eef0ff'
  outline: '#6d7a72'
  outline-variant: '#bccac0'
  surface-tint: '#006c4a'
  primary: '#006948'
  on-primary: '#ffffff'
  primary-container: '#00855d'
  on-primary-container: '#f5fff7'
  inverse-primary: '#68dba9'
  secondary: '#a93349'
  on-secondary: '#ffffff'
  secondary-container: '#fe7488'
  on-secondary-container: '#730425'
  tertiary: '#006947'
  on-tertiary: '#ffffff'
  tertiary-container: '#00855b'
  on-tertiary-container: '#f5fff6'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#85f8c4'
  primary-fixed-dim: '#68dba9'
  on-primary-fixed: '#002114'
  on-primary-fixed-variant: '#005137'
  secondary-fixed: '#ffdadc'
  secondary-fixed-dim: '#ffb2b9'
  on-secondary-fixed: '#400010'
  on-secondary-fixed-variant: '#891933'
  tertiary-fixed: '#6ffbbe'
  tertiary-fixed-dim: '#4edea3'
  on-tertiary-fixed: '#002113'
  on-tertiary-fixed-variant: '#005236'
  background: '#faf8ff'
  on-background: '#131b2e'
  surface-variant: '#dae2fd'
typography:
  display-currency:
    fontFamily: Plus Jakarta Sans
    fontSize: 36px
    fontWeight: '700'
    lineHeight: 44px
    letterSpacing: -0.03em
  display-currency-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 30px
    fontWeight: '700'
    lineHeight: 36px
    letterSpacing: -0.025em
  headline-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 32px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
    letterSpacing: -0.015em
  title-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 24px
    letterSpacing: -0.01em
  body-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 15px
    fontWeight: '400'
    lineHeight: 22px
    letterSpacing: 0em
  body-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 13px
    fontWeight: '400'
    lineHeight: 18px
    letterSpacing: 0.01em
  currency-item:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 24px
    letterSpacing: -0.01em
  label-caps:
    fontFamily: Plus Jakarta Sans
    fontSize: 11px
    fontWeight: '700'
    lineHeight: 16px
    letterSpacing: 0.05em
  badge-label:
    fontFamily: Plus Jakarta Sans
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.01em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-sm: 0.75rem
  margin: 1.25rem
  margin-sm: 1rem
  space-2xs: 0.25rem
  space-xs: 0.375rem
  space-sm: 0.5rem
  space-md: 0.75rem
  space-lg: 1rem
  space-xl: 1.25rem
  space-2xl: 1.5rem
  space-3xl: 2rem
---

## Brand & Style

### Brand Personality & Emotional Atmosphere
This design system bridges playful consumer charm with institutional fintech credibility. Rooted in the metaphor of the humble piggy bank, it matures that concept into an empowering, serene financial command center. The emotional response is one of calm mastery over one's finances: reducing money anxiety, celebrating smart saving habits, and transforming mundane daily tracking into tactile, satisfying interactions.

### Design Movement: Modern Tactile Minimalism with Neo-Fintech Polish
The style merges clean Scandinavian minimalism with neo-fintech materiality:
- **Crisp Surfaces & Tonal Depth**: Pure white and warm slate zinc surfaces suspended over pale slate backgrounds, punctuated by razor-sharp 1px hairline translucent borders.
- **Organic Softness**: Generous radii (16px to 24px corner roundings) balanced by tight, engineered metric alignments and tabular data tables.
- **Targeted Dopamine Accents**: The brand’s deep jade green exudes wealth, organic longevity, and institutional stability, while playful pig-inspired rose-coral flourishes inject warmth into achievements, streak badges, and active highlights.

## Colors

### Color Strategy & Palette Architecture
The palette pairs an institutional, organic Emerald with an affectionate Coral Rose accent, anchored by high-clarity Slate neutrals that prevent the interface from feeling muddy.

#### Primary Brand (Emerald Growth)
- **Primary 700 (`#047857`)**: Active states, deep brand contrast, header indicators.
- **Primary 600 (`#059669`)**: Core interactive buttons, primary icons, active navigation tabs.
- **Primary 500 (`#10B981`)**: Positive balance deltas, progress rings, growth stats.
- **Primary 50 (`#ECFDF5`)**: Pill background fills, success chip containers, selected state tints.

#### Playful Brand Accent (Piggy Rose)
- **Rose 600 (`#E11D48`)**: Piggy avatar accents, micro-badges, overbudget warnings.
- **Rose 400 (`#FB7185`)**: Brand warmth, delightful highlights, empty-state illustrations.
- **Rose 50 (`#FFF1F2`)**: Gentle alert backgrounds, housing category pill tint.

#### Functional Category Tokens
- **Housing**: Rose Coral (`#F43F5E`) on background tint (`#FFF1F2`).
- **Groceries**: Emerald Sage (`#059669`) on background tint (`#ECFDF5`).
- **Dining Out**: Honey Amber (`#D97706`) on background tint (`#FFFBEB`).
- **Transport**: Sky Indigo (`#0284C7`) on background tint (`#F0F9FF`).
- **Utilities**: Cyan Electric (`#0891B2`) on background tint (`#ECFEFF`).
- **Coffee & Leisure**: Warm Mocha (`#854D0E`) on background tint (`#FEFCE8`).

#### Neutrals & Surface Slate
- **Canvas Base (`#F8FAFC`)**: Light ambient canvas.
- **Surface Pure (`#FFFFFF`)**: Primary cards, floating modal sheets, elevated sheets.
- **Surface Subtle (`#F1F5F9`)**: Segmented controls, input fields, nested cards.
- **Border Hairline (`#E2E8F0`)**: 1px crisp outline at 60% opacity.
- **Text Strong (`#0F172A`)**: Headings, primary amounts, high-emphasis text.
- **Text Muted (`#64748B`)**: Secondary metadata, dates, unit denotations.
- **Text Subtle (`#94A3B8`)**: Inactive states, placeholder values.

#### Dark Mode Counterparts
- **Dark Canvas (`#0B0F17`)**: Deep obsidian base.
- **Dark Surface Primary (`#131B2A`)**: Elevated financial cards.
- **Dark Surface Secondary (`#1E293B`)**: Chip backgrounds and interactive rows.
- **Dark Border Hairline (`#334155`)**: 1px subtle boundary divider.

## Typography

### Typographic Hierarchy & Financial Precision
Plus Jakarta Sans delivers the optimal balance: geometric clarity reminiscent of modern tech interfaces combined with friendly, open apertures.

- **Tabular Numerals (`font-variant-numeric: tabular-nums`)**: Applied universally to all currency and numerical metrics (`display-currency`, `currency-item`, balances, transaction rows) ensuring characters maintain uniform widths for effortless vertical scanning across ledger entries.
- **Display Figures**: Bold and confident, commanding focus without overwhelming secondary meta-elements.
- **Categorical & Pill Badges**: Styled in `badge-label` with medium-high weight for legibility at small scale.
- **Uppercase Meta Labels**: Reserved strictly for structural labels like "EXPENSES BY CATEGORY" or "TOTAL BALANCE" to establish rigid visual order.

## Layout & Spacing

### Mobile-First Financial Framework
The screen operates on a continuous fluid column bounded by standard mobile margins:
- **Outer Canvas Margins**: Set to `1.25rem` (20px) on standard viewports, reducing to `1rem` (16px) on compact devices.
- **Vertical Rhythm**: Section groups separate by `1.25rem` (20px). Internal card padding standardizes at `1.25rem` (20px) for high-impact metric cards and `1rem` (16px) for itemized transaction lists.
- **Inter-Item Stacking**: List rows enforce an `0.5rem` (8px) gap when floating, or `0px` with 1px dividers when nested inside grouped cards.
- **Safe Area Insets**: Floating action targets maintain a `1.5rem` clearance above system home indicators.

## Elevation & Depth

### Ambient Soft-Depth System
Depth avoids murky, dark shadows. It relies on crisp, light-catching layers and ambient, tinted blurs that simulate cards levitating above the canvas.

- **Layer 0 (Canvas Base)**: `#F8FAFC`, zero shadow, static matte background.
- **Layer 1 (Card Surface)**: `#FFFFFF` paired with a dual shadow:
  - `box-shadow: 0 1px 2px 0 rgba(15, 23, 42, 0.04), 0 4px 16px -2px rgba(15, 23, 42, 0.05);`
  - Border: 1px hairline `rgba(226, 232, 240, 0.8)`.
- **Layer 2 (Floating Action / Modal Pill)**:
  - `box-shadow: 0 8px 24px -4px rgba(5, 150, 105, 0.16), 0 2px 6px 0 rgba(15, 23, 42, 0.04);`
- **Glassmorphic Floating Dock (Bottom Navigation)**:
  - Background: `rgba(255, 255, 255, 0.85)` with `backdrop-filter: blur(16px)`.
  - Border: 1px continuous hairline `rgba(255, 255, 255, 0.6)` on the top edge, `rgba(226, 232, 240, 0.6)` overall.

## Shapes

### Rounded Geometry
The shape language uses progressive rounding based on component role:
- **Base Surfaces & Content Cards**: `1.25rem` (20px) radius creates a soft, approachable frame for transactional data.
- **Inner Interactive Elements**: Input fields, inner category wrappers, and charts utilize `0.75rem` (12px) to `1rem` (16px).
- **Interactive Pills & Badges**: Fully rounded (`9999px`) for buttons, status chips, segmented controls, and category tags.
- **Chart Geometry**: Rounded caps (`stroke-linecap: round`) on circular gauges, and `6px` top radii on daily spending bar graphs.

## Components

### 1. Expense Cards & Ledger Rows
- **Card Container**: Pure white surface, 20px radius, 1px slate-200 border, ambient shadow.
- **Category Icon Circle**: 44px by 44px rounded container (`border-radius: 9999px`) filled with the category's light pastel tint (e.g., `#FFF1F2` for Housing). The icon inside sits centered at 20px, colored in the corresponding category key tone (`#F43F5E`).
- **Typography Arrangement**: Title in `title-sm` Slate-900; sub-metadata (date, status) in `body-sm` Slate-500.
- **Amount Display**: Aligned right, formatted with `currency-item`, Tabular Figures, colored Slate-900.

### 2. Category & Fixed/Variable Badges
- **Pill Architecture**: Height of 26px, padding of 4px 10px, radius 9999px.
- **Visual Treatment**: 
  - Subdued background tint (e.g., `#ECFDF5` for Groceries, `#F1F5F9` for Fixed expense).
  - 6px circular dot indicator positioned prefix to the label with full saturated category color.
  - Text in `badge-label`, medium-weight color matched to the category hue.

### 3. Total Balance & Summary Banner
- **Card Presentation**: Subtle gradient mesh from `#FFFFFF` down to `#F0FDF4` (very soft mint bloom) enclosed by a 1px emerald-tinted border (`rgba(16, 185, 129, 0.15)`).
- **Data Callout**: Label in `label-caps` (`#64748B`), amount rendered in `display-currency` (`#0F172A`) with emerald currency symbol prefix (`#059669`).

### 4. Interactive Charts
- **Category Ring (Donut)**: Thick stroke (14px) with rounded segment dividers and subtle segment gaps (3px). Background track rendered in `#F1F5F9`.
- **Daily Spending Bar Chart**: Bars use 8px width with `border-radius: 4px 4px 0 0`. Inactive bars sit at `#E2E8F0`, while active/highlighted dates use an Emerald gradient (`#10B981` to `#059669`).

### 5. Floating Navigation Dock
- **Container**: Floating capsule suspended 16px above the device base, 64px height, horizontal pill shape (`rounded-full`).
- **Material**: Translucent frosted glass (`rgba(255, 255, 255, 0.88)` + 20px blur).
- **Active State**: Selected icon enclosed in an Emerald Mint pill (`#ECFDF5`) with primary green icon and text (`#059669`).

### 6. Buttons & Inputs
- **Primary Action**: 52px height, 9999px pill radius, solid Emerald 600 (`#059669`), hover/pressed Emerald 700 (`#047857`), text white `title-sm`. Micro-glow shadow on active.
- **Secondary / Ghost Action**: `#F1F5F9` background, `#0F172A` text, hover `#E2E8F0`.
- **Form Checkbox & Toggle**: 20px rounded square (`radius: 6px`), 1.5px border Slate-300; checked state fills with Emerald 600 containing a crisp white tick.