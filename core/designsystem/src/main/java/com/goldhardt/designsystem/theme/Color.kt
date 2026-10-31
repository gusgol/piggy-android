package com.goldhardt.designsystem.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// Material 3 Light Scheme Tokens (from Google Stitch DESIGN.md)
// ============================================================================
val primaryLight = Color(0xFF006948)
val onPrimaryLight = Color(0xFFFFFFFF)
val primaryContainerLight = Color(0xFF00855D)
val onPrimaryContainerLight = Color(0xFFF5FFF7)
val inversePrimaryLight = Color(0xFF68DBA9)

val secondaryLight = Color(0xFFA93349)
val onSecondaryLight = Color(0xFFFFFFFF)
val secondaryContainerLight = Color(0xFFFE7488)
val onSecondaryContainerLight = Color(0xFF730425)

val tertiaryLight = Color(0xFF006947)
val onTertiaryLight = Color(0xFFFFFFFF)
val tertiaryContainerLight = Color(0xFF00855B)
val onTertiaryContainerLight = Color(0xFFF5FFF6)

val errorLight = Color(0xFFBA1A1A)
val onErrorLight = Color(0xFFFFFFFF)
val errorContainerLight = Color(0xFFFFDAD6)
val onErrorContainerLight = Color(0xFF93000A)

val backgroundLight = Color(0xFFFAF8FF)
val onBackgroundLight = Color(0xFF131B2E)
val surfaceLight = Color(0xFFFAF8FF)
val onSurfaceLight = Color(0xFF131B2E)
val surfaceVariantLight = Color(0xFFDAE2FD)
val onSurfaceVariantLight = Color(0xFF3D4A42)
val surfaceTintLight = Color(0xFF006C4A)

val outlineLight = Color(0xFF6D7A72)
val outlineVariantLight = Color(0xFFBCCAC0)
val scrimLight = Color(0xFF000000)

val inverseSurfaceLight = Color(0xFF283044)
val inverseOnSurfaceLight = Color(0xFFEEF0FF)

val surfaceDimLight = Color(0xFFD2D9F4)
val surfaceBrightLight = Color(0xFFFAF8FF)
val surfaceContainerLowestLight = Color(0xFFFFFFFF)
val surfaceContainerLowLight = Color(0xFFF2F3FF)
val surfaceContainerLight = Color(0xFFEAEDFF)
val surfaceContainerHighLight = Color(0xFFE2E7FF)
val surfaceContainerHighestLight = Color(0xFFDAE2FD)

// ============================================================================
// Material 3 Fixed Role Tokens (from Google Stitch DESIGN.md)
// ============================================================================
val primaryFixed = Color(0xFF85F8C4)
val primaryFixedDim = Color(0xFF68DBA9)
val onPrimaryFixed = Color(0xFF002114)
val onPrimaryFixedVariant = Color(0xFF005137)

val secondaryFixed = Color(0xFFFFDADC)
val secondaryFixedDim = Color(0xFFFFB2B9)
val onSecondaryFixed = Color(0xFF400010)
val onSecondaryFixedVariant = Color(0xFF891933)

val tertiaryFixed = Color(0xFF6FFBBE)
val tertiaryFixedDim = Color(0xFF4EDEA3)
val onTertiaryFixed = Color(0xFF002113)
val onTertiaryFixedVariant = Color(0xFF005236)

// ============================================================================
// Material 3 Dark Scheme Tokens (aligned with Stitch Fixed & Dark Counterparts)
// ============================================================================
val primaryDark = Color(0xFF68DBA9)
val onPrimaryDark = Color(0xFF003824)
val primaryContainerDark = Color(0xFF006948)
val onPrimaryContainerDark = Color(0xFF85F8C4)
val inversePrimaryDark = Color(0xFF006948)

val secondaryDark = Color(0xFFFFB2B9)
val onSecondaryDark = Color(0xFF67001E)
val secondaryContainerDark = Color(0xFF891933)
val onSecondaryContainerDark = Color(0xFFFFDADC)

val tertiaryDark = Color(0xFF4EDEA3)
val onTertiaryDark = Color(0xFF003824)
val tertiaryContainerDark = Color(0xFF006947)
val onTertiaryContainerDark = Color(0xFF6FFBBE)

val errorDark = Color(0xFFFFB4AB)
val onErrorDark = Color(0xFF690005)
val errorContainerDark = Color(0xFF93000A)
val onErrorContainerDark = Color(0xFFFFDAD6)

val backgroundDark = Color(0xFF0B0F17)
val onBackgroundDark = Color(0xFFEEF0FF)
val surfaceDark = Color(0xFF0B0F17)
val onSurfaceDark = Color(0xFFEEF0FF)
val surfaceVariantDark = Color(0xFF1E293B)
val onSurfaceVariantDark = Color(0xFFBCCAC0)
val surfaceTintDark = Color(0xFF68DBA9)

val outlineDark = Color(0xFF6D7A72)
val outlineVariantDark = Color(0xFF334155)
val scrimDark = Color(0xFF000000)

val inverseSurfaceDark = Color(0xFFEEF0FF)
val inverseOnSurfaceDark = Color(0xFF283044)

val surfaceDimDark = Color(0xFF0B0F17)
val surfaceBrightDark = Color(0xFF283044)
val surfaceContainerLowestDark = Color(0xFF070A10)
val surfaceContainerLowDark = Color(0xFF131B2A)
val surfaceContainerDark = Color(0xFF131B2A)
val surfaceContainerHighDark = Color(0xFF1E293B)
val surfaceContainerHighestDark = Color(0xFF283548)

// ============================================================================
// Piggy Extended Brand, Category & Surface Palette (from DESIGN.md)
// ============================================================================
object PiggyColors {
    // Primary Brand (Emerald Growth)
    val Emerald700 = Color(0xFF047857) // Active states, deep brand contrast, header indicators
    val Emerald600 = Color(0xFF059669) // Core interactive buttons, primary icons, active nav tabs
    val Emerald500 = Color(0xFF10B981) // Positive balance deltas, progress rings, growth stats
    val Emerald50 = Color(0xFFECFDF5)  // Pill background fills, success chip containers, selected tints
    val MintBloom = Color(0xFFF0FDF4)  // Total Balance summary banner soft gradient end

    // Playful Brand Accent (Piggy Rose)
    val Rose600 = Color(0xFFE11D48)    // Piggy avatar accents, micro-badges, overbudget warnings
    val Rose400 = Color(0xFFFB7185)    // Brand warmth, delightful highlights, empty-state illustrations
    val Rose50 = Color(0xFFFFF1F2)     // Gentle alert backgrounds, housing category pill tint

    // Functional Category Tokens
    val CategoryHousing = Color(0xFFF43F5E)
    val CategoryHousingTint = Color(0xFFFFF1F2)

    val CategoryGroceries = Color(0xFF059669)
    val CategoryGroceriesTint = Color(0xFFECFDF5)

    val CategoryDiningOut = Color(0xFFD97706)
    val CategoryDiningOutTint = Color(0xFFFFFBEB)

    val CategoryTransport = Color(0xFF0284C7)
    val CategoryTransportTint = Color(0xFFF0F9FF)

    val CategoryUtilities = Color(0xFF0891B2)
    val CategoryUtilitiesTint = Color(0xFFECFEFF)

    val CategoryCoffeeLeisure = Color(0xFF854D0E)
    val CategoryCoffeeLeisureTint = Color(0xFFFEFCE8)

    // Neutrals & Surface Slate (Light)
    val CanvasBase = Color(0xFFF8FAFC)
    val SurfacePure = Color(0xFFFFFFFF)
    val SurfaceSubtle = Color(0xFFF1F5F9)
    val BorderHairline = Color(0xFFE2E8F0)
    val TextStrong = Color(0xFF0F172A)
    val TextMuted = Color(0xFF64748B)
    val TextSubtle = Color(0xFF94A3B8)

    // Dark Mode Counterparts
    val DarkCanvas = Color(0xFF0B0F17)
    val DarkSurfacePrimary = Color(0xFF131B2A)
    val DarkSurfaceSecondary = Color(0xFF1E293B)
    val DarkBorderHairline = Color(0xFF334155)
}
