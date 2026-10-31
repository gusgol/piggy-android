package com.goldhardt.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorInt

/**
 * Semantic category palette resolved from a category's stored color hex (or name fallback),
 * guaranteeing harmonious pastel containers and WCAG-legible dots/labels in both Light & Dark themes.
 */
@Immutable
data class CategoryColorPalette(
    val dot: Color,
    val avatarContainer: Color,
    val avatarContent: Color,
    val badgeContainer: Color,
    val badgeContent: Color,
)

@Composable
fun isPiggyDarkTheme(): Boolean =
    MaterialTheme.colorScheme.background.luminance() < 0.5f

@Composable
fun piggyCardContainerColor(): Color =
    if (isPiggyDarkTheme()) PiggyColors.DarkSurfacePrimary
    else MaterialTheme.colorScheme.surfaceContainerLowest

@Composable
fun piggyCardBorderColor(): Color =
    if (isPiggyDarkTheme()) PiggyColors.DarkBorderHairline
    else MaterialTheme.colorScheme.surfaceContainer

@Composable
fun piggySubtleContainerColor(): Color =
    if (isPiggyDarkTheme()) PiggyColors.DarkSurfaceSecondary
    else MaterialTheme.colorScheme.surfaceContainerLow

@Composable
fun rememberCategoryColorPalette(
    colorHex: String?,
    categoryName: String? = null,
): CategoryColorPalette {
    val isDark = isPiggyDarkTheme()
    return remember(colorHex, categoryName, isDark) {
        resolveCategoryColorPalette(
            colorHex = colorHex,
            categoryName = categoryName,
            isDark = isDark,
        )
    }
}

fun resolveCategoryColorPalette(
    colorHex: String?,
    categoryName: String? = null,
    isDark: Boolean = false,
): CategoryColorPalette {
    val seedColor = parseHexOrNull(colorHex) ?: semanticSeedForName(categoryName)
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(seedColor.toArgb(), hsl)
    val hue = hsl[0]
    val saturation = hsl[1]

    if (saturation < 0.12f) {
        return if (isDark) {
            CategoryColorPalette(
                dot = PiggyColors.TextSubtle,
                avatarContainer = PiggyColors.DarkSurfaceSecondary,
                avatarContent = onSurfaceDark,
                badgeContainer = PiggyColors.DarkSurfaceSecondary,
                badgeContent = onSurfaceDark,
            )
        } else {
            CategoryColorPalette(
                dot = PiggyColors.TextMuted,
                avatarContainer = PiggyColors.SurfaceSubtle,
                avatarContent = PiggyColors.TextStrong,
                badgeContainer = PiggyColors.SurfaceSubtle,
                badgeContent = PiggyColors.TextMuted,
            )
        }
    }

    return if (isDark) {
        val vivid = hslColor(hue, saturation.coerceIn(0.55f, 0.85f), 0.68f)
        val tintBase = hslColor(hue, saturation.coerceIn(0.50f, 0.80f), 0.58f)
        val label = hslColor(hue, saturation.coerceIn(0.55f, 0.85f), 0.80f)
        CategoryColorPalette(
            dot = vivid,
            avatarContainer = tintBase.copy(alpha = 0.22f),
            avatarContent = label,
            badgeContainer = tintBase.copy(alpha = 0.18f),
            badgeContent = label,
        )
    } else {
        val dot = hslColor(hue, saturation.coerceIn(0.62f, 0.86f), 0.44f)
        val avatarBg = hslColor(hue, saturation.coerceIn(0.50f, 0.88f), 0.91f)
        val badgeBg = hslColor(hue, saturation.coerceIn(0.50f, 0.88f), 0.95f)
        val deepContent = hslColor(hue, saturation.coerceIn(0.62f, 0.86f), 0.30f)
        CategoryColorPalette(
            dot = dot,
            avatarContainer = avatarBg,
            avatarContent = deepContent,
            badgeContainer = badgeBg,
            badgeContent = deepContent,
        )
    }
}

private fun hslColor(hue: Float, saturation: Float, lightness: Float): Color =
    Color(ColorUtils.HSLToColor(floatArrayOf(hue, saturation, lightness)))

private fun parseHexOrNull(hex: String?): Color? {
    if (hex.isNullOrBlank()) return null
    val normalized = if (hex.startsWith("#")) hex else "#$hex"
    return try {
        Color(normalized.toColorInt())
    } catch (_: IllegalArgumentException) {
        null
    }
}

private fun semanticSeedForName(name: String?): Color {
    val lower = name?.trim()?.lowercase().orEmpty()
    return when {
        "hous" in lower || "rent" in lower || "home" in lower -> PiggyColors.CategoryHousing
        "grocer" in lower || "market" in lower -> PiggyColors.CategoryGroceries
        "din" in lower || "food" in lower || "restaur" in lower -> PiggyColors.CategoryDiningOut
        "transp" in lower || "metro" in lower || "car" in lower || "bus" in lower -> PiggyColors.CategoryTransport
        "util" in lower || "bill" in lower || "electr" in lower -> PiggyColors.CategoryUtilities
        "coffee" in lower || "cafe" in lower || "leis" in lower -> PiggyColors.CategoryCoffeeLeisure
        else -> PiggyColors.Emerald600
    }
}

