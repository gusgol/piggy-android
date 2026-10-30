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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goldhardt.designsystem.components.ConfigureTopBar
import com.goldhardt.designsystem.components.MonthSelector
import com.goldhardt.designsystem.components.TotalAmountCard
import com.goldhardt.designsystem.theme.PiggyTheme
import com.goldhardt.feature.trends.domain.DayPiece
import com.goldhardt.feature.trends.domain.DayStack
import com.goldhardt.feature.trends.domain.Slice
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.foundation.gestures.detectTapGestures
import kotlin.math.atan2
import kotlin.math.sqrt
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle

@Composable
fun TrendsScreen(
    modifier: Modifier = Modifier,
    viewModel: TrendsViewModel = hiltViewModel()
) {
    ConfigureTopBar(
        title = stringResource(R.string.title_trends)
    )

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    TrendsScreenContent(
        state = state,
        modifier = modifier,
        onMonthChange = { viewModel.setMonth(it) }
    )
}

@Composable
internal fun TrendsScreenContent(
    state: TrendsUiState,
    modifier: Modifier = Modifier,
    onMonthChange: (YearMonth) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        MonthSelector(
            month = state.month,
            onMonthChange = onMonthChange,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading…")
            }
            return
        }

        state.error?.let { error ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(error, color = MaterialTheme.colorScheme.error)
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

            // Pie chart section
            item {
                Surface(
                    modifier = Modifier.padding(top = 12.dp, bottom = 12.dp),
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
                        var selected by remember { mutableStateOf<PieSlice?>(null) }
                        val slicesUi = state.slices.map {
                            PieSlice(
                                label = it.name,
                                value = it.total.toFloat(),
                                color = parseHexColorOrDefault(it.colorHex, MaterialTheme.colorScheme.tertiaryContainer)
                            )
                        }
                        SimplePieChart(
                            slices = slicesUi,
                            chartSize = 180.dp,
                            onSliceClick = { selected = it }
                        )
                        AnimatedVisibility(
                            modifier = Modifier.padding(top = 16.dp),
                            visible = selected != null,
                            enter = expandVertically(animationSpec = tween(durationMillis = 150)) + fadeIn(animationSpec = tween(durationMillis = 120)),
                            exit = shrinkVertically(animationSpec = tween(durationMillis = 150)) + fadeOut(animationSpec = tween(durationMillis = 120))
                        ) {
                            selected?.let { s ->
                                SelectedSliceInfo(
                                    slice = s,
                                    totalAmount = slicesUi.sumOf { it.value.toDouble() }
                                )
                            }
                        }
                    }
                }
            }

            // Daily stacked bar chart section
            item {
                Surface(
                    modifier = Modifier.padding(top = 0.dp, bottom = 12.dp),
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
                            text = "By day",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(Modifier.height(12.dp))
                        DailyStackedBarChart(
                            stacks = state.dayStacks,
                            chartHeight = 180.dp,
                            barWidth = 18.dp,
                            barSpacing = 6.dp,
                            yearMonth = state.month
                        )
                    }
                }
            }

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
private fun SimplePieChart(
    slices: List<PieSlice>,
    chartSize: Dp,
    stroke: Dp = 24.dp,
    onSliceClick: (PieSlice) -> Unit = {}
) {
    val total = slices.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(0.0001f)
    val density = LocalDensity.current
    val diameterPx = with(density) { chartSize.toPx() }
    val topPaddingPx = with(density) { 8.dp.toPx() }
    val strokePx = with(density) { stroke.toPx() }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(chartSize + 16.dp)
            .pointerInput(slices, diameterPx, topPaddingPx, strokePx, total) {
                detectTapGestures(onTap = { offset ->
                    val left = (size.width.toFloat() - diameterPx) / 2f
                    val centerX = left + diameterPx / 2f
                    val centerY = topPaddingPx + diameterPx / 2f
                    val dx = offset.x - centerX
                    val dy = offset.y - centerY
                    val r = sqrt(dx * dx + dy * dy)
                    val radius = diameterPx / 2f
                    if (r < radius - strokePx / 2f || r > radius + strokePx / 2f) return@detectTapGestures
                    val deg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                    val fromTop = (deg + 450f) % 360f
                    var start = 0f
                    for (slice in slices) {
                        val sweep = (slice.value / total) * 360f
                        if (fromTop >= start && fromTop < start + sweep) {
                            onSliceClick(slice)
                            break
                        }
                        start += sweep
                    }
                })
            }
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
                    text = "${data.items.size} expenses",
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


@Composable
private fun SelectedSliceInfo(
    slice: PieSlice,
    totalAmount: Double,
    modifier: Modifier = Modifier
) {
    val currency = remember { NumberFormat.getCurrencyInstance() }
    val safeTotal = totalAmount.coerceAtLeast(0.0001)
    val pct = ((slice.value.toDouble() / safeTotal) * 100.0)

    Spacer(Modifier.height(16.dp))
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .width(10.dp)
                    .height(10.dp),
                shape = CircleShape,
                color = slice.color,
                content = {}
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = slice.label,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${String.format(Locale.getDefault(), "%.1f", pct)}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = currency.format(slice.value.toDouble()),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
}

// Daily stacked bar chart composables

@Composable
private fun DailyStackedBarChart(
    stacks: List<DayStack>,
    chartHeight: Dp,
    barWidth: Dp,
    barSpacing: Dp,
    yearMonth: java.time.YearMonth,
) {
    val maxTotal = stacks.maxOfOrNull { day -> day.pieces.sumOf { it.amount } }?.coerceAtLeast(0.0001) ?: 0.0001
    Column(modifier = Modifier.fillMaxWidth()) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(barSpacing),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            items(items = stacks, key = { it.day }) { day ->
                DayBar(yearMonth = yearMonth, stack = day, maxTotal = maxTotal, chartHeight = chartHeight, barWidth = barWidth)
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun DayBar(
    yearMonth: java.time.YearMonth,
    stack: DayStack,
    maxTotal: Double,
    chartHeight: Dp,
    barWidth: Dp,
) {
    val emptyDayColor = MaterialTheme.colorScheme.surfaceVariant
    val defaultPieceColor = MaterialTheme.colorScheme.tertiaryContainer
    val segments: List<Pair<Double, Color>> = stack.pieces.map { it.amount to parseHexColorOrDefault(it.colorHex, defaultPieceColor) }
    val weekday = try {
        LocalDate.of(yearMonth.year, yearMonth.month, stack.day)
            .dayOfWeek
            .getDisplayName(TextStyle.SHORT, Locale.getDefault())
    } catch (_: Throwable) { "" }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(
            modifier = Modifier
                .height(chartHeight)
                .width(barWidth)
        ) {
            val totalForDay = stack.pieces.sumOf { it.amount }
            if (totalForDay <= 0.0) {
                // draw faint baseline to indicate empty day
                drawRect(
                    color = emptyDayColor,
                    size = androidx.compose.ui.geometry.Size(size.width, 2f),
                    topLeft = androidx.compose.ui.geometry.Offset(0f, size.height - 2f)
                )
                return@Canvas
            }
            var yBottom = size.height
            segments.forEach { (amount, color) ->
                if (amount <= 0.0) return@forEach
                val h = ((amount / maxTotal) * size.height).toFloat().coerceAtLeast(1f)
                drawRect(
                    color = color,
                    topLeft = androidx.compose.ui.geometry.Offset(0f, yBottom - h),
                    size = androidx.compose.ui.geometry.Size(width = size.width, height = h)
                )
                yBottom -= h
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = stack.day.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
        Text(
            text = weekday,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

private val previewTrendsState = TrendsUiState(
    month = YearMonth.of(2026, 10),
    isLoading = false,
    total = 2145.90,
    slices = listOf(
        Slice(categoryId = "housing", name = "Housing", colorHex = "#FFADAD", total = 1450.0),
        Slice(categoryId = "groceries", name = "Groceries", colorHex = "#CAFFBF", total = 385.50),
        Slice(categoryId = "dining", name = "Dining Out", colorHex = "#FFD6A5", total = 198.40),
        Slice(categoryId = "transport", name = "Transport", colorHex = "#A0C4FF", total = 112.00),
    ),
    categories = listOf(
        CategoryGroupUi(
            id = "housing",
            name = "Housing",
            icon = "🏠",
            colorHex = "#FFADAD",
            total = 1450.0,
            items = listOf(
                CategoryExpenseItemUi(name = "Apartment Rent", amount = 1450.0, dateLabel = "Oct 1")
            )
        ),
        CategoryGroupUi(
            id = "groceries",
            name = "Groceries",
            icon = "🛒",
            colorHex = "#CAFFBF",
            total = 385.50,
            items = listOf(
                CategoryExpenseItemUi(name = "Whole Foods", amount = 184.50, dateLabel = "Oct 5"),
                CategoryExpenseItemUi(name = "Trader Joe's", amount = 115.00, dateLabel = "Oct 12"),
                CategoryExpenseItemUi(name = "Farmers Market", amount = 86.00, dateLabel = "Oct 19")
            )
        ),
        CategoryGroupUi(
            id = "dining",
            name = "Dining Out",
            icon = "🍔",
            colorHex = "#FFD6A5",
            total = 198.40,
            items = listOf(
                CategoryExpenseItemUi(name = "Sushi Night", amount = 84.00, dateLabel = "Oct 8"),
                CategoryExpenseItemUi(name = "Brunch Cafe", amount = 52.40, dateLabel = "Oct 14"),
                CategoryExpenseItemUi(name = "Pizza Place", amount = 62.00, dateLabel = "Oct 21")
            )
        ),
        CategoryGroupUi(
            id = "transport",
            name = "Transport",
            icon = "🚗",
            colorHex = "#A0C4FF",
            total = 112.00,
            items = listOf(
                CategoryExpenseItemUi(name = "City Metro Pass", amount = 45.00, dateLabel = "Oct 2"),
                CategoryExpenseItemUi(name = "Airport Ride", amount = 67.00, dateLabel = "Oct 17")
            )
        )
    ),
    dayStacks = (1..14).map { day ->
        val pieces = when (day) {
            1 -> listOf(DayPiece("housing", 450.0, "#FFADAD"))
            2 -> listOf(DayPiece("transport", 45.0, "#A0C4FF"))
            5 -> listOf(DayPiece("groceries", 184.50, "#CAFFBF"))
            8 -> listOf(DayPiece("dining", 84.0, "#FFD6A5"), DayPiece("transport", 24.0, "#A0C4FF"))
            10 -> listOf(DayPiece("dining", 42.0, "#FFD6A5"))
            12 -> listOf(DayPiece("groceries", 115.0, "#CAFFBF"), DayPiece("dining", 38.0, "#FFD6A5"))
            14 -> listOf(DayPiece("dining", 52.40, "#FFD6A5"))
            else -> emptyList()
        }
        DayStack(day = day, pieces = pieces)
    }
)

@Preview(name = "Trends Screen - Light", showBackground = true, heightDp = 900)
@Composable
private fun TrendsScreenPreview() {
    PiggyTheme {
        Surface {
            TrendsScreenContent(state = previewTrendsState)
        }
    }
}

@Preview(
    name = "Trends Screen - Dark",
    showBackground = true,
    heightDp = 900,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun TrendsScreenDarkPreview() {
    PiggyTheme(darkTheme = true) {
        Surface {
            TrendsScreenContent(state = previewTrendsState)
        }
    }
}

@Preview(name = "Category Group Row", showBackground = true)
@Composable
private fun CategoryGroupRowPreview() {
    PiggyTheme {
        Surface(Modifier.padding(16.dp)) {
            CategoryGroupRow(data = previewTrendsState.categories[1])
        }
    }
}
