package com.goldhardt.designsystem.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.goldhardt.designsystem.R
import com.goldhardt.designsystem.theme.PiggyShapeTokens
import com.goldhardt.designsystem.theme.PiggySpacing
import com.goldhardt.designsystem.theme.PiggyTheme
import com.goldhardt.designsystem.theme.PiggyTypography
import com.goldhardt.designsystem.theme.piggyCardBorderColor
import com.goldhardt.designsystem.theme.piggyCardContainerColor
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * A stateless pill-shaped month selector aligned with the Piggy Design System.
 *
 * Contract:
 * - Input: [month] currently selected YearMonth.
 * - Output: invokes [onMonthChange] with the new YearMonth when user taps arrows.
 * - Optional bounds: [minMonth] and [maxMonth] disable arrows when exceeded.
 */
@Composable
fun MonthSelector(
    month: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    modifier: Modifier = Modifier,
    minMonth: YearMonth? = null,
    maxMonth: YearMonth? = null,
    formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("LLLL yyyy", Locale.getDefault())
) {
    val canGoPrev = minMonth?.let { month.isAfter(it) } ?: true
    val canGoNext = maxMonth?.let { month.isBefore(it) } ?: true
    val formattedMonth = month.format(formatter).replaceFirstChar { ch ->
        if (ch.isLowerCase()) ch.titlecase(Locale.getDefault()) else ch.toString()
    }

    Surface(
        modifier = modifier,
        color = piggyCardContainerColor(),
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = PiggyShapeTokens.Pill,
        border = BorderStroke(1.dp, piggyCardBorderColor()),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PiggySpacing.spaceXs, vertical = PiggySpacing.space2xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { if (canGoPrev) onMonthChange(month.minusMonths(1)) },
                enabled = canGoPrev,
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                    contentDescription = "Previous month",
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = PiggySpacing.spaceSm)
                    .semantics { contentDescription = "Selected month $formattedMonth" }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_calendar_month),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(PiggySpacing.spaceXs))
                Text(
                    text = formattedMonth,
                    style = PiggyTypography.titleSm,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            IconButton(
                onClick = { if (canGoNext) onMonthChange(month.plusMonths(1)) },
                enabled = canGoNext,
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = "Next month",
                )
            }
        }
    }
}

@Preview(name = "Month Selector - Light", showBackground = true)
@Composable
private fun MonthSelectorPreview() {
    PiggyTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            MonthSelector(
                month = YearMonth.of(2026, 10),
                onMonthChange = {},
                modifier = Modifier.padding(PiggySpacing.margin)
            )
        }
    }
}

@Preview(
    name = "Month Selector - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun MonthSelectorDarkPreview() {
    PiggyTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            MonthSelector(
                month = YearMonth.of(2026, 10),
                onMonthChange = {},
                modifier = Modifier.padding(PiggySpacing.margin)
            )
        }
    }
}
