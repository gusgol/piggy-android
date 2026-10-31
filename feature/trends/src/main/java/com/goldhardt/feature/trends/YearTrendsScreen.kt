package com.goldhardt.feature.trends

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goldhardt.designsystem.components.PiggyNavigationBarDefaults
import com.goldhardt.designsystem.theme.PiggyColors
import com.goldhardt.designsystem.theme.PiggyShapeTokens
import com.goldhardt.designsystem.theme.PiggySpacing
import com.goldhardt.designsystem.theme.PiggyTheme
import com.goldhardt.designsystem.theme.PiggyTypography
import com.goldhardt.designsystem.theme.isPiggyDarkTheme
import com.goldhardt.designsystem.theme.piggyCardBorderColor
import com.goldhardt.designsystem.theme.piggyCardContainerColor
import java.text.NumberFormat
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
private fun textStrongColor(): Color = MaterialTheme.colorScheme.onSurface

@Composable
private fun textMutedColor(): Color = MaterialTheme.colorScheme.onSurfaceVariant

@Composable
private fun textSubtleColor(): Color =
    if (isPiggyDarkTheme()) MaterialTheme.colorScheme.outline else PiggyColors.TextSubtle

@Composable
internal fun YearTrendsScreen(
    modifier: Modifier = Modifier,
    viewModel: YearTrendsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    YearTrendsContent(
        state = state,
        modifier = modifier,
        onSelectCategory = viewModel::setCategory,
    )
}

@Composable
internal fun YearTrendsContent(
    state: YearTrendsUiState,
    modifier: Modifier = Modifier,
    onSelectCategory: (String?) -> Unit = {},
) {
    val categories = remember(state.months) {
        state.months
            .flatMap { it.categories }
            .distinctBy { it.id }
            .sortedBy { it.name.lowercase(Locale.getDefault()) }
    }
    val selectedCategoryId = state.selectedCategoryId
        ?.takeIf { id -> categories.any { it.id == id } }
    val maxAmount = state.months.maxOfOrNull { month ->
        if (selectedCategoryId == null) {
            month.total
        } else {
            month.categories.firstOrNull { it.id == selectedCategoryId }?.total ?: 0.0
        }
    } ?: 0.0

    val totalYearSpend = remember(state.months, selectedCategoryId) {
        state.months.sumOf { month ->
            if (selectedCategoryId == null) {
                month.total
            } else {
                month.categories.firstOrNull { it.id == selectedCategoryId }?.total ?: 0.0
            }
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val contentWidth = maxWidth.coerceAtMost(680.dp)
        val horizontalMargin = if (maxWidth < 400.dp) PiggySpacing.marginSm else PiggySpacing.margin
        LazyColumn(
            modifier = Modifier
                .widthIn(max = contentWidth)
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            verticalArrangement = Arrangement.spacedBy(PiggySpacing.spaceXl),
            contentPadding = PaddingValues(
                start = horizontalMargin,
                end = horizontalMargin,
                top = PiggySpacing.spaceLg,
                bottom = PiggyNavigationBarDefaults.FloatingBottomClearance + PiggySpacing.space2xl,
            ),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(PiggySpacing.spaceXs)) {
                    Text(
                        text = stringResource(R.string.year_comparison_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = textStrongColor(),
                    )
                    Text(
                        text = stringResource(R.string.year_comparison_subtitle),
                        style = PiggyTypography.bodyMd,
                        color = textMutedColor(),
                    )
                }
            }

            if (!state.isLoading && state.error == null && state.months.any { it.total > 0.0 }) {
                item {
                    YearlySummaryCard(
                        totalSpend = totalYearSpend,
                        monthCount = state.months.count { month ->
                            if (selectedCategoryId == null) month.total > 0.0
                            else month.categories.any { it.id == selectedCategoryId && it.total > 0.0 }
                        },
                        selectedCategoryName = categories.firstOrNull { it.id == selectedCategoryId }?.name,
                    )
                }
            }

            if (categories.isNotEmpty()) {
                item {
                    CategoryFilterRow(
                        categories = categories,
                        selectedCategoryId = selectedCategoryId,
                        onSelectCategory = onSelectCategory,
                    )
                }
            }

            when {
                state.isLoading -> item { YearLoadingState() }
                state.error != null -> item { YearErrorState() }
                state.months.none { it.total > 0.0 } -> item { EmptyYearTrendsState() }
                else -> {
                    item {
                        YearlyBreakdownCard(
                            months = state.months,
                            selectedCategoryId = selectedCategoryId,
                            maxAmount = maxAmount,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun YearlySummaryCard(
    totalSpend: Double,
    monthCount: Int,
    selectedCategoryName: String?,
) {
    val currency = remember { NumberFormat.getCurrencyInstance() }
    val borderColor = if (isPiggyDarkTheme()) {
        piggyCardBorderColor()
    } else {
        PiggyColors.Emerald500.copy(alpha = 0.16f)
    }
    val avgSpend = if (monthCount > 0) totalSpend / monthCount else totalSpend / 12.0

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = PiggyShapeTokens.Card,
        color = piggyCardContainerColor(),
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PiggySpacing.spaceXl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = if (selectedCategoryName != null) {
                    "${selectedCategoryName.uppercase(Locale.getDefault())} • 12-MONTH SPEND"
                } else {
                    "12-MONTH TOTAL SPEND"
                },
                style = PiggyTypography.labelCaps,
                color = textMutedColor(),
            )
            Spacer(Modifier.height(PiggySpacing.spaceXs))
            Text(
                text = currency.format(totalSpend),
                style = PiggyTypography.displayCurrencyMobile,
                color = textStrongColor(),
            )
            Spacer(Modifier.height(PiggySpacing.spaceMd))
            Surface(
                shape = PiggyShapeTokens.Pill,
                color = if (isPiggyDarkTheme()) PiggyColors.DarkSurfaceSecondary else PiggyColors.Emerald50,
            ) {
                Text(
                    text = "Avg. ${currency.format(avgSpend)} / month",
                    style = PiggyTypography.badgeLabel,
                    color = if (isPiggyDarkTheme()) PiggyColors.Emerald500 else PiggyColors.Emerald700,
                    modifier = Modifier.padding(horizontal = PiggySpacing.spaceLg, vertical = PiggySpacing.spaceXs),
                )
            }
        }
    }
}

@Composable
private fun CategoryFilterRow(
    categories: List<MonthlyCategorySpendUi>,
    selectedCategoryId: String?,
    onSelectCategory: (String?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(PiggySpacing.spaceSm)) {
        Text(
            text = "FILTER BY CATEGORY",
            style = PiggyTypography.labelCaps,
            color = textMutedColor(),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(PiggySpacing.spaceSm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryFilterPill(
                label = stringResource(R.string.filter_all_categories),
                isSelected = selectedCategoryId == null,
                colorDot = null,
                onClick = { onSelectCategory(null) },
            )
            categories.forEach { category ->
                CategoryFilterPill(
                    label = category.name,
                    isSelected = selectedCategoryId == category.id,
                    colorDot = categoryColor(category.name, category.colorHex),
                    onClick = {
                        onSelectCategory(if (selectedCategoryId == category.id) null else category.id)
                    },
                )
            }
        }
    }
}

@Composable
private fun CategoryFilterPill(
    label: String,
    isSelected: Boolean,
    colorDot: Color?,
    onClick: () -> Unit,
) {
    val isDark = isPiggyDarkTheme()
    val containerColor = when {
        isSelected -> if (isDark) PiggyColors.Emerald700 else PiggyColors.Emerald50
        else -> piggyCardContainerColor()
    }
    val contentColor = when {
        isSelected -> if (isDark) PiggyColors.SurfacePure else PiggyColors.Emerald700
        else -> textStrongColor()
    }
    val borderColor = when {
        isSelected -> PiggyColors.Emerald600
        else -> piggyCardBorderColor()
    }

    Surface(
        modifier = Modifier
            .clip(PiggyShapeTokens.Pill)
            .clickable(onClick = onClick),
        shape = PiggyShapeTokens.Pill,
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PiggySpacing.spaceXs),
        ) {
            if (colorDot != null) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(colorDot, CircleShape),
                )
            }
            Text(
                text = label,
                style = PiggyTypography.badgeLabel,
                color = contentColor,
            )
        }
    }
}

@Composable
private fun YearlyBreakdownCard(
    months: List<YearlyMonthUi>,
    selectedCategoryId: String?,
    maxAmount: Double,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = PiggyShapeTokens.Card,
        color = piggyCardContainerColor(),
        border = BorderStroke(1.dp, piggyCardBorderColor()),
        shadowElevation = 1.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "MONTHLY BREAKDOWN",
                style = PiggyTypography.labelCaps,
                color = textMutedColor(),
                modifier = Modifier.padding(
                    start = PiggySpacing.spaceLg,
                    end = PiggySpacing.spaceLg,
                    top = PiggySpacing.spaceLg,
                    bottom = PiggySpacing.spaceSm,
                ),
            )
            months.forEachIndexed { index, month ->
                YearMonthRow(
                    month = month,
                    selectedCategoryId = selectedCategoryId,
                    maxAmount = maxAmount,
                )
                if (index < months.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = PiggySpacing.spaceLg),
                        color = piggyCardBorderColor().copy(alpha = 0.5f),
                    )
                }
            }
        }
    }
}

@Composable
private fun YearMonthRow(
    month: YearlyMonthUi,
    selectedCategoryId: String?,
    maxAmount: Double,
) {
    val categories = if (selectedCategoryId == null) {
        month.categories
    } else {
        month.categories.filter { it.id == selectedCategoryId }
    }
    val amount = if (selectedCategoryId == null) month.total else categories.sumOf { it.total }
    val currency = remember { NumberFormat.getCurrencyInstance() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PiggySpacing.spaceLg, vertical = PiggySpacing.spaceMd),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PiggySpacing.spaceSm),
    ) {
        Column(modifier = Modifier.width(48.dp)) {
            Text(
                text = month.month.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase(Locale.getDefault()),
                style = PiggyTypography.badgeLabel,
                color = textStrongColor(),
            )
            Text(
                text = month.month.year.toString(),
                style = PiggyTypography.bodySm,
                color = textMutedColor(),
            )
        }
        YearCategoryBar(
            categories = categories,
            maxAmount = maxAmount,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = currency.format(amount),
            modifier = Modifier.widthIn(min = 72.dp),
            textAlign = TextAlign.End,
            style = PiggyTypography.currencyItem,
            color = if (amount > 0.0) textStrongColor() else textSubtleColor(),
            maxLines = 1,
        )
    }
}

@Composable
private fun YearCategoryBar(
    categories: List<MonthlyCategorySpendUi>,
    maxAmount: Double,
    modifier: Modifier = Modifier,
) {
    val segments = categories
        .filter { it.total > 0.0 }
        .map { it.total to categoryColor(it.name, it.colorHex) }
    val total = segments.sumOf { it.first }
    val trackColor = if (isPiggyDarkTheme()) PiggyColors.DarkSurfaceSecondary else PiggyColors.SurfaceSubtle
    Canvas(modifier.fillMaxWidth().height(12.dp)) {
        drawRoundRect(
            color = trackColor,
            cornerRadius = CornerRadius(size.height / 2),
        )
        if (total <= 0.0 || maxAmount <= 0.0) return@Canvas

        var left = 0f
        segments.forEachIndexed { index, (amount, color) ->
            val segmentWidth = (amount / maxAmount * size.width).toFloat().coerceAtLeast(0f)
            if (segmentWidth > 0f) {
                val gap = if (index > 0) 2.dp.toPx() else 0f
                drawRoundRect(
                    color = color,
                    topLeft = Offset(left + gap, 0f),
                    size = Size((segmentWidth - gap).coerceAtLeast(0f), size.height),
                    cornerRadius = CornerRadius(size.height / 2),
                )
                left += segmentWidth
            }
        }
    }
}

@Composable
private fun EmptyYearTrendsState() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = PiggyShapeTokens.Card,
        color = piggyCardContainerColor(),
        border = BorderStroke(1.dp, piggyCardBorderColor()),
    ) {
        Text(
            text = stringResource(R.string.year_comparison_empty),
            modifier = Modifier.fillMaxWidth().padding(vertical = PiggySpacing.space2xl, horizontal = PiggySpacing.spaceLg),
            style = PiggyTypography.bodyMd,
            color = textMutedColor(),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun YearLoadingState() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 80.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.trends_loading),
            style = MaterialTheme.typography.bodyMedium,
            color = textMutedColor(),
        )
    }
}

@Composable
private fun YearErrorState() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = PiggyShapeTokens.Card,
        color = piggyCardContainerColor(),
        border = BorderStroke(1.dp, piggyCardBorderColor()),
    ) {
        Text(
            text = stringResource(R.string.year_comparison_error),
            modifier = Modifier.fillMaxWidth().padding(vertical = PiggySpacing.space2xl, horizontal = PiggySpacing.spaceLg),
            style = PiggyTypography.bodyMd,
            color = textMutedColor(),
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(name = "Light Mode")
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun YearTrendsScreenPreview() {
    PiggyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            YearTrendsContent(
                state = YearTrendsUiState(
                    isLoading = false,
                    selectedCategoryId = null,
                    months = listOf(
                        YearlyMonthUi(
                            YearMonth.of(2026, 10),
                            2145.90,
                            listOf(
                                MonthlyCategorySpendUi("housing", "Housing", "#F43F5E", 1450.0),
                                MonthlyCategorySpendUi("groceries", "Groceries", "#059669", 385.50),
                                MonthlyCategorySpendUi("dining", "Dining Out", "#D97706", 185.0),
                                MonthlyCategorySpendUi("utilities", "Utilities", "#0891B2", 125.40),
                            ),
                        ),
                        YearlyMonthUi(
                            YearMonth.of(2026, 9),
                            1890.00,
                            listOf(
                                MonthlyCategorySpendUi("housing", "Housing", "#F43F5E", 1450.0),
                                MonthlyCategorySpendUi("groceries", "Groceries", "#059669", 315.00),
                                MonthlyCategorySpendUi("dining", "Dining Out", "#D97706", 125.0),
                            ),
                        ),
                        YearlyMonthUi(
                            YearMonth.of(2026, 8),
                            2410.50,
                            listOf(
                                MonthlyCategorySpendUi("housing", "Housing", "#F43F5E", 1450.0),
                                MonthlyCategorySpendUi("groceries", "Groceries", "#059669", 420.00),
                                MonthlyCategorySpendUi("dining", "Dining Out", "#D97706", 290.50),
                                MonthlyCategorySpendUi("transport", "Transport", "#0284C7", 250.00),
                            ),
                        ),
                        YearlyMonthUi(
                            YearMonth.of(2026, 7),
                            1750.00,
                            listOf(
                                MonthlyCategorySpendUi("housing", "Housing", "#F43F5E", 1450.0),
                                MonthlyCategorySpendUi("groceries", "Groceries", "#059669", 300.00),
                            ),
                        ),
                    ),
                ),
            )
        }
    }
}
