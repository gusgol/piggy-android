package com.goldhardt.feature.trends

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goldhardt.designsystem.components.PiggyNavigationBarDefaults
import com.goldhardt.designsystem.theme.PiggyColors
import com.goldhardt.designsystem.theme.PiggyShapeTokens
import com.goldhardt.designsystem.theme.PiggySpacing
import com.goldhardt.designsystem.theme.PiggyTypography
import com.goldhardt.designsystem.theme.piggyCardBorderColor
import java.text.NumberFormat
import java.time.format.TextStyle
import java.util.Locale

@Composable
internal fun YearTrendsScreen(
    modifier: Modifier = Modifier,
    viewModel: YearTrendsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
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

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val contentWidth = maxWidth.coerceAtMost(680.dp)
        val horizontalMargin = if (maxWidth < 400.dp) PiggySpacing.marginSm else PiggySpacing.margin
        LazyColumn(
            modifier = Modifier.widthIn(max = contentWidth).fillMaxWidth().align(Alignment.TopCenter),
            verticalArrangement = Arrangement.spacedBy(PiggySpacing.spaceLg),
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
                        stringResource(R.string.year_comparison_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = PiggyColors.TextStrong,
                    )
                    Text(
                        stringResource(R.string.year_comparison_subtitle),
                        style = PiggyTypography.bodyMd,
                        color = PiggyColors.TextMuted,
                    )
                }
            }
            if (categories.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(PiggySpacing.spaceSm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FilterChip(
                            selected = selectedCategoryId == null,
                            onClick = { viewModel.setCategory(null) },
                            label = { Text(stringResource(R.string.filter_all_categories)) },
                        )
                        categories.forEach { category ->
                            FilterChip(
                                selected = selectedCategoryId == category.id,
                                onClick = {
                                    viewModel.setCategory(
                                        if (selectedCategoryId == category.id) null else category.id,
                                    )
                                },
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            modifier = Modifier.size(8.dp),
                                            shape = PiggyShapeTokens.Pill,
                                            color = categoryColor(category.name, category.colorHex),
                                        ) {}
                                        Spacer(Modifier.width(PiggySpacing.spaceXs))
                                        Text(category.name)
                                    }
                                },
                            )
                        }
                    }
                }
            }
            when {
                state.isLoading -> item { YearLoadingState() }
                state.error != null -> item { YearErrorState() }
                state.months.none { it.total > 0.0 } -> item { EmptyYearTrendsState() }
            }
            if (!state.isLoading && state.error == null) {
                items(state.months, key = { it.month.toString() }) { month ->
                    YearMonthRow(
                        month = month,
                        selectedCategoryId = selectedCategoryId,
                        maxAmount = maxAmount,
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
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PiggySpacing.spaceSm),
    ) {
        Text(
            month.month.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
            modifier = Modifier.width(38.dp),
            style = PiggyTypography.badgeLabel,
            color = PiggyColors.TextStrong,
        )
        Text(
            month.month.year.toString(),
            modifier = Modifier.width(36.dp),
            style = PiggyTypography.bodySm,
            color = PiggyColors.TextMuted,
        )
        YearCategoryBar(
            categories = categories,
            maxAmount = maxAmount,
            modifier = Modifier.weight(1f),
        )
        Text(
            currency.format(amount),
            modifier = Modifier.widthIn(min = 70.dp),
            textAlign = TextAlign.End,
            style = PiggyTypography.badgeLabel,
            color = PiggyColors.TextStrong,
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
    val trackColor = piggyCardBorderColor().copy(alpha = 0.55f)
    Canvas(modifier.fillMaxWidth().height(14.dp)) {
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
    Text(
        stringResource(R.string.year_comparison_empty),
        modifier = Modifier.fillMaxWidth().padding(vertical = PiggySpacing.space2xl),
        style = PiggyTypography.bodyMd,
        color = PiggyColors.TextMuted,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun YearLoadingState() {
    Box(Modifier.fillMaxWidth().padding(vertical = 80.dp), contentAlignment = Alignment.Center) {
        Text(stringResource(R.string.trends_loading), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun YearErrorState() {
    Text(
        stringResource(R.string.year_comparison_error),
        modifier = Modifier.fillMaxWidth().padding(vertical = PiggySpacing.space2xl),
        style = PiggyTypography.bodyMd,
        color = PiggyColors.TextMuted,
        textAlign = TextAlign.Center,
    )
}
