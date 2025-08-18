package com.goldhardt.feature.trends

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goldhardt.designsystem.components.MonthSelector
import com.goldhardt.designsystem.components.TotalAmountCard
import java.text.NumberFormat

@Composable
fun TrendsScreen(
    modifier: Modifier = Modifier,
    viewModel: TrendsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        MonthSelector(
            month = state.month,
            onMonthChange = { viewModel.setMonth(it) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading…")
            }
            return
        }

        if (state.total <= 0.0) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No expenses for this month")
            }
            return
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item { TotalAmountCard(total = state.total) }
            item { Spacer(Modifier.height(12.dp)) }

            // Pie chart section
            item {
                Surface(
                    tonalElevation = 1.dp,
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "By category",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(Modifier.height(12.dp))
                        SimplePieChart(
                            slices = state.slices.map {
                                PieSlice(
                                    label = it.categoryName,
                                    value = it.amount.toFloat(),
                                    color = parseHexColorOrDefault(it.colorHex, MaterialTheme.colorScheme.tertiaryContainer)
                                )
                            },
                            chartSize = 180.dp,
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(12.dp)) }

            // Grouped categories as a simple list with dividers
            itemsIndexed(state.categories, key = { _, item -> item.id }) { index, cat ->
                if (index > 0) HorizontalDivider(
                    color = MaterialTheme.colorScheme.surfaceContainer
                )
                CategoryGroupRow(data = cat)
            }
        }
    }
}

private fun parseHexColorOrDefault(hex: String?, fallback: Color): Color {
    if (hex.isNullOrBlank()) return fallback
    return try { Color(hex.toColorInt()) } catch (_: Throwable) { fallback }
}

private data class PieSlice(val label: String, val value: Float, val color: Color)

@Composable
private fun SimplePieChart(slices: List<PieSlice>, chartSize: Dp, stroke: Dp = 24.dp) {
    val total = slices.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(0.0001f)
    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(chartSize + 16.dp)
    ) {
        val diameter = chartSize.toPx()
        val left = (size.width - diameter) / 2f
        val top = 8.dp.toPx()
        var startAngle = -90f
        slices.forEach { slice ->
            val sweep = (slice.value / total) * 360f
            drawArc(
                color = slice.color,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(left, top),
                size = androidx.compose.ui.geometry.Size(diameter, diameter),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke.toPx())
            )
            startAngle += sweep
        }
    }
}

@Composable
private fun CategoryGroupRow(data: CategoryGroupUi) {
    val currency = NumberFormat.getCurrencyInstance()
    var expanded by rememberSaveable(data.id) { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { expanded = !expanded }
        ) {
            Surface(
                modifier = Modifier
                    .height(40.dp)
                    .clip(CircleShape)
                    .width(40.dp),
                shape = CircleShape,
                color = parseHexColorOrDefault(data.colorHex, MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = data.icon)
                }
            }
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(text = data.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${data.count} expenses",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Text(
                text = currency.format(data.total),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(Modifier.width(8.dp))
            Text(text = if (expanded) "▲" else "▼", style = MaterialTheme.typography.titleMedium)
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(animationSpec = tween(durationMillis = 150)) + fadeIn(animationSpec = tween(durationMillis = 120)),
            exit = shrinkVertically(animationSpec = tween(durationMillis = 150)) + fadeOut(animationSpec = tween(durationMillis = 120))
        ) {
            Column(Modifier.padding(top = 8.dp, bottom = 4.dp)) {
                data.items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = item.dateLabel, style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(60.dp))
                        Text(text = item.name, modifier = Modifier.weight(1f), maxLines = 1)
                        Text(text = currency.format(item.amount), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun CategoryGroupRowPreview() {
    val data = CategoryGroupUi(
        id = "1",
        name = "Groceries",
        icon = "🛒",
        colorHex = "#FF0000",
        total = 123.45,
        count = 3,
        items = listOf(
            CategoryExpenseItemUi(
                name = "Aldi",
                amount = 50.0,
                dateLabel = "01/01"
            ),
            CategoryExpenseItemUi(
                name = "Lidl",
                amount = 40.0,
                dateLabel = "02/01"
            )
        )
    )
    CategoryGroupRow(data = data)
}
