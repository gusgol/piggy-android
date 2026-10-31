package com.goldhardt.designsystem.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Shape tokens defined by the Piggy Wealth & Expense Design System.
 * - sm: 4.dp (0.25rem)
 * - DEFAULT: 8.dp (0.5rem)
 * - md: 12.dp (0.75rem) — Inner interactive elements, input fields
 * - lg: 16.dp (1rem) — Inner category wrappers, charts
 * - card: 20.dp (1.25rem) — Base surfaces & content cards
 * - xl: 24.dp (1.5rem) — Large modal sheets
 * - full: CircleShape (9999px) — Interactive pills, buttons & badges
 */
val PiggyShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

object PiggyShapeTokens {
    val Sm = RoundedCornerShape(4.dp)
    val Default = RoundedCornerShape(8.dp)
    val Md = RoundedCornerShape(12.dp)
    val Lg = RoundedCornerShape(16.dp)
    val Card = RoundedCornerShape(20.dp)
    val Xl = RoundedCornerShape(24.dp)
    val Pill = CircleShape
}

/**
 * Spacing tokens defined by the Piggy Wealth & Expense Design System.
 */
object PiggySpacing {
    val space2xs = 4.dp    // 0.25rem
    val spaceXs = 6.dp     // 0.375rem
    val spaceSm = 8.dp     // 0.5rem
    val spaceMd = 12.dp    // 0.75rem
    val spaceLg = 16.dp    // 1rem
    val spaceXl = 20.dp    // 1.25rem
    val space2xl = 24.dp   // 1.5rem
    val space3xl = 32.dp   // 2rem

    val gutterSm = 12.dp   // 0.75rem
    val gutter = 16.dp     // 1rem
    val marginSm = 16.dp   // 1rem (compact outer canvas margin)
    val margin = 20.dp     // 1.25rem (standard outer canvas margin)
}

