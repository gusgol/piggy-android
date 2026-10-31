package com.goldhardt.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.goldhardt.designsystem.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val plusJakartaSansGoogleFont = GoogleFont("Plus Jakarta Sans")

val plusJakartaSansFontFamily = FontFamily(
    Font(
        googleFont = plusJakartaSansGoogleFont,
        fontProvider = provider,
        weight = FontWeight.Normal
    ),
    Font(
        googleFont = plusJakartaSansGoogleFont,
        fontProvider = provider,
        weight = FontWeight.Medium
    ),
    Font(
        googleFont = plusJakartaSansGoogleFont,
        fontProvider = provider,
        weight = FontWeight.SemiBold
    ),
    Font(
        googleFont = plusJakartaSansGoogleFont,
        fontProvider = provider,
        weight = FontWeight.Bold
    )
)

val bodyFontFamily = plusJakartaSansFontFamily
val displayFontFamily = plusJakartaSansFontFamily

private const val TABULAR_NUMS = "tnum"

/**
 * Custom financial & semantic typography tokens defined by the Piggy Design System.
 * All currency/numerical styles enforce tabular numerals (`tnum`) for vertical alignment.
 */
object PiggyTypography {
    val displayCurrency = TextStyle(
        fontFamily = plusJakartaSansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.03).em,
        fontFeatureSettings = TABULAR_NUMS
    )

    val displayCurrencyMobile = TextStyle(
        fontFamily = plusJakartaSansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.025).em,
        fontFeatureSettings = TABULAR_NUMS
    )

    val headlineLg = TextStyle(
        fontFamily = plusJakartaSansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.02).em
    )

    val headlineMd = TextStyle(
        fontFamily = plusJakartaSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.015).em
    )

    val titleSm = TextStyle(
        fontFamily = plusJakartaSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.01).em
    )

    val bodyMd = TextStyle(
        fontFamily = plusJakartaSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.em
    )

    val bodySm = TextStyle(
        fontFamily = plusJakartaSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.01.em
    )

    val currencyItem = TextStyle(
        fontFamily = plusJakartaSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.01).em,
        fontFeatureSettings = TABULAR_NUMS
    )

    val labelCaps = TextStyle(
        fontFamily = plusJakartaSansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.05.em
    )

    val badgeLabel = TextStyle(
        fontFamily = plusJakartaSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.01.em
    )
}

private val baseline = Typography()

val AppTypography = Typography(
    displayLarge = PiggyTypography.displayCurrency,
    displayMedium = PiggyTypography.displayCurrencyMobile,
    displaySmall = baseline.displaySmall.copy(fontFamily = displayFontFamily),
    headlineLarge = PiggyTypography.headlineLg,
    headlineMedium = PiggyTypography.headlineMd,
    headlineSmall = baseline.headlineSmall.copy(
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.SemiBold
    ),
    titleLarge = PiggyTypography.headlineMd,
    titleMedium = PiggyTypography.titleSm,
    titleSmall = PiggyTypography.titleSm,
    bodyLarge = PiggyTypography.bodyMd.copy(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = PiggyTypography.bodyMd,
    bodySmall = PiggyTypography.bodySm,
    labelLarge = PiggyTypography.titleSm.copy(fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = PiggyTypography.badgeLabel,
    labelSmall = PiggyTypography.labelCaps,
)
