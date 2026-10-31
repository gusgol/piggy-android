package com.goldhardt.designsystem.components

import android.content.res.Configuration
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationItemColors
import androidx.compose.material3.NavigationItemIconPosition
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarArrangement
import androidx.compose.material3.ShortNavigationBarDefaults
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.ShortNavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.goldhardt.designsystem.R
import com.goldhardt.designsystem.theme.PiggyColors
import com.goldhardt.designsystem.theme.PiggyShapeTokens
import com.goldhardt.designsystem.theme.PiggySpacing
import com.goldhardt.designsystem.theme.PiggyTheme
import com.goldhardt.designsystem.theme.PiggyTypography
import com.goldhardt.designsystem.theme.isPiggyDarkTheme
import com.goldhardt.designsystem.theme.onTertiaryFixed
import com.goldhardt.designsystem.theme.primaryFixed

/**
 * Floating capsule bottom navigation bar from the Piggy Wealth & Expense Design System,
 * built on top of Material 3 [ShortNavigationBar].
 *
 * Specs (Stitch `c375329552f248eeb980cb12b0dd0c43` & `DESIGN.md`):
 * - Floating capsule suspended above the navigation bar safe area (`16.dp` bottom margin, `20.dp` horizontal margin)
 * - `64.dp` height (Material 3 `ShortNavigationBar` default `ContainerHeight`)
 * - Pill shape (`PiggyShapeTokens.Pill`)
 * - Translucent surface container with crisp `1.dp` hairline border and ambient emerald-tinted shadow
 */
@Composable
fun PiggyNavigationBar(
    modifier: Modifier = Modifier,
    arrangement: ShortNavigationBarArrangement = ShortNavigationBarDefaults.arrangement,
    content: @Composable () -> Unit,
) {
    val isDark = isPiggyDarkTheme()
    val containerColor = if (isDark) {
        PiggyColors.DarkSurfacePrimary.copy(alpha = 0.92f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.90f)
    }
    val borderColor = if (isDark) {
        PiggyColors.DarkBorderHairline.copy(alpha = 0.80f)
    } else {
        PiggyColors.BorderHairline.copy(alpha = 0.80f)
    }
    val shadowColor = if (isDark) {
        MaterialTheme.colorScheme.scrim.copy(alpha = 0.50f)
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(
                start = PiggySpacing.margin,
                end = PiggySpacing.margin,
                top = PiggySpacing.spaceSm,
                bottom = PiggySpacing.spaceLg,
            ),
        contentAlignment = Alignment.Center,
    ) {
        ShortNavigationBar(
            modifier = Modifier
                .widthIn(max = 384.dp)
                .height(PiggyNavigationBarDefaults.BarHeight)
                .shadow(
                    elevation = 12.dp,
                    shape = PiggyShapeTokens.Pill,
                    ambientColor = shadowColor,
                    spotColor = shadowColor,
                )
                .clip(PiggyShapeTokens.Pill)
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = PiggyShapeTokens.Pill,
                ),
            containerColor = containerColor,
            contentColor = MaterialTheme.colorScheme.onSurface,
            windowInsets = WindowInsets(
                left = PiggySpacing.spaceSm,
                right = PiggySpacing.spaceSm,
                top = PiggySpacing.space2xs,
                bottom = PiggySpacing.space2xs,
            ),
            arrangement = arrangement,
            content = content,
        )
    }
}

/**
 * Navigation item inside [PiggyNavigationBar], extending Material 3 [ShortNavigationBarItem]
 * with horizontal (`Start`) icon placement and Piggy Design System active pill colors.
 */
@Composable
fun PiggyNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconPosition: NavigationItemIconPosition = NavigationItemIconPosition.Start,
    colors: NavigationItemColors = PiggyNavigationBarDefaults.itemColors(),
) {
    ShortNavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Box(
                modifier = Modifier.defaultMinSize(minHeight = 32.dp),
                contentAlignment = Alignment.Center,
            ) {
                icon()
            }
        },
        label = {
            Box(
                modifier = Modifier.defaultMinSize(minHeight = 32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = PiggyTypography.badgeLabel.copy(
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold
                    ),
                    maxLines = 1,
                )
            }
        },
        modifier = modifier,
        enabled = enabled,
        iconPosition = iconPosition,
        colors = colors,
    )
}

object PiggyNavigationBarDefaults {
    /** Height of the outer floating navigation capsule. */
    val BarHeight = 72.dp

    /**
     * Total vertical space occupied by the floating [PiggyNavigationBar] (72.dp capsule + 16.dp
     * bottom margin + 8.dp top breathing room), excluding system navigation bar insets.
     * Use with `navigationBarsPadding()` on scrollable content or floating action buttons so
     * content scrolls behind the translucent bar without being obscured at the bottom.
     */
    val FloatingBottomClearance = 96.dp

    @Composable
    fun itemColors(): NavigationItemColors {
        val isDark = isPiggyDarkTheme()
        return ShortNavigationBarItemDefaults.colors(
            selectedIconColor = if (isDark) primaryFixed else onTertiaryFixed,
            selectedTextColor = if (isDark) primaryFixed else onTertiaryFixed,
            selectedIndicatorColor = if (isDark) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
            } else {
                primaryFixed
            },
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(name = "PiggyNavigationBar - Light", showBackground = true)
@Composable
private fun PiggyNavigationBarPreview() {
    PiggyTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            PiggyNavigationBar {
                PiggyNavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_receipt_long),
                            contentDescription = "Expenses",
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    label = "Expenses",
                )
                PiggyNavigationBarItem(
                    selected = false,
                    onClick = {},
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_pie_chart),
                            contentDescription = "Categories",
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    label = "Categories",
                )
                PiggyNavigationBarItem(
                    selected = false,
                    onClick = {},
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_trending_up),
                            contentDescription = "Trends",
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    label = "Trends",
                )
            }
        }
    }
}

@Preview(
    name = "PiggyNavigationBar - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun PiggyNavigationBarDarkPreview() {
    PiggyTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            PiggyNavigationBar {
                PiggyNavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_receipt_long),
                            contentDescription = "Expenses",
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    label = "Expenses",
                )
                PiggyNavigationBarItem(
                    selected = false,
                    onClick = {},
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_pie_chart),
                            contentDescription = "Categories",
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    label = "Categories",
                )
                PiggyNavigationBarItem(
                    selected = false,
                    onClick = {},
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_trending_up),
                            contentDescription = "Trends",
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    label = "Trends",
                )
            }
        }
    }
}

