package com.goldhardt.feature.trends

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goldhardt.designsystem.components.ConfigureTopBar
import com.goldhardt.designsystem.components.MonthSelector
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
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.min

@Composable
fun TrendsScreen(
    modifier: Modifier = Modifier,
    viewModel: TrendsViewModel = hiltViewModel(),
) {
    ConfigureTopBar(title = stringResource(R.string.title_trends))
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TrendsScreenContent(state, modifier, viewModel::setMonth)
}

@Composable
internal fun TrendsScreenContent(
    state: TrendsUiState,
    modifier: Modifier = Modifier,
    onMonthChange: (YearMonth) -> Unit = {},
) {
    var selectedTab by rememberSaveable { mutableStateOf(TrendsTab.Current) }
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val contentWidth = maxWidth.coerceAtMost(680.dp)
        val horizontalMargin = if (maxWidth < 400.dp) PiggySpacing.marginSm else PiggySpacing.margin
        LazyColumn(
            modifier = Modifier.widthIn(max = contentWidth).fillMaxWidth().align(Alignment.TopCenter),
            verticalArrangement = Arrangement.spacedBy(PiggySpacing.spaceXl),
            contentPadding = PaddingValues(
                start = horizontalMargin, end = horizontalMargin, top = PiggySpacing.spaceSm,
                bottom = PiggyNavigationBarDefaults.FloatingBottomClearance + PiggySpacing.space2xl,
            ),
        ) {
            item {
                PrimaryTabRow(selectedTabIndex = selectedTab.ordinal) {
                    TrendsTab.entries.forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = { Text(stringResource(tab.titleRes)) },
                        )
                    }
                }
            }
            if (selectedTab == TrendsTab.Current) {
                item { MonthSelector(state.month, onMonthChange, Modifier.fillMaxWidth()) }
                when {
                    state.isLoading -> item { LoadingState() }
                    state.total <= 0.0 -> item { EmptyTrendsState() }
                    else -> {
                        item { SpendingSummaryCard(state.total, state.categories.sumOf { it.count }) }
                        item { CategoryAnalyticsCard(state.slices) }
                        item { DailySpendingCard(state.dayStacks, state.month) }
                        item { CategoryDetailsHeading() }
                        items(state.categories, key = { it.id }) { CategoryDetailCard(it) }
                    }
                }
            }
        }
    }
}

private enum class TrendsTab(val titleRes: Int) {
    Current(R.string.tab_current),
    Year(R.string.tab_year),
}

@Composable
private fun LoadingState() {
    Box(Modifier.fillMaxWidth().padding(vertical = 80.dp), contentAlignment = Alignment.Center) {
        Text("Loading…", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun SpendingSummaryCard(total: Double, expenseCount: Int) {
    val currency = remember { NumberFormat.getCurrencyInstance() }
    val borderColor = if (isPiggyDarkTheme()) {
        piggyCardBorderColor()
    } else {
        PiggyColors.Emerald500.copy(alpha = 0.16f)
    }
    Surface(
        modifier = Modifier.fillMaxWidth(), shape = PiggyShapeTokens.Card,
        color = piggyCardContainerColor(),
        border = BorderStroke(1.dp, borderColor), shadowElevation = 2.dp,
    ) {
        Column(Modifier.fillMaxWidth().padding(PiggySpacing.spaceXl), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("TOTAL SPENT", style = PiggyTypography.labelCaps, color = PiggyColors.TextMuted)
            Spacer(Modifier.height(PiggySpacing.spaceXs))
            Text(currency.format(total), style = PiggyTypography.displayCurrencyMobile, color = PiggyColors.TextStrong)
            Spacer(Modifier.height(PiggySpacing.spaceMd))
            Surface(shape = PiggyShapeTokens.Pill, color = PiggyColors.Emerald50) {
                Text(
                    "$expenseCount ${if (expenseCount == 1) "expense" else "expenses"}",
                    style = PiggyTypography.badgeLabel, color = PiggyColors.Emerald700,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun CategoryAnalyticsCard(slices: List<CategorySliceUi>) {
    val dominant = slices.maxByOrNull { it.amount }
    val total = slices.sumOf { it.amount }.coerceAtLeast(0.0001)
    val colors = slices.map { categoryColor(it.categoryName, it.colorHex) }
    AnalyticsCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionTitle("By category", Modifier.weight(1f))
            Text("${slices.size} ${if (slices.size == 1) "Category" else "Categories"}", style = PiggyTypography.bodyMd, color = PiggyColors.TextMuted)
        }
        Spacer(Modifier.height(PiggySpacing.spaceLg))
        Box(Modifier.fillMaxWidth().height(238.dp), contentAlignment = Alignment.Center) {
            DonutChart(slices, colors, Modifier.size(206.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("DOMINANT", style = PiggyTypography.labelCaps, color = PiggyColors.TextMuted)
                Text(dominant?.categoryName.orEmpty(), style = MaterialTheme.typography.headlineMedium, color = PiggyColors.TextStrong)
                Text(
                    dominant?.let { String.format(Locale.getDefault(), "%.1f%%", it.amount / total * 100) }.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    color = dominant?.let { categoryColor(it.categoryName, it.colorHex) } ?: PiggyColors.TextMuted,
                )
            }
        }
        Spacer(Modifier.height(PiggySpacing.spaceLg))
        CategoryLegend(slices, colors, total)
    }
}

@Composable
private fun AnalyticsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(), shape = PiggyShapeTokens.Card,
        color = piggyCardContainerColor(),
        border = BorderStroke(1.dp, piggyCardBorderColor()), shadowElevation = 1.dp,
    ) { Column(Modifier.padding(PiggySpacing.spaceXl), content = content) }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Surface(Modifier.size(10.dp), PiggyShapeTokens.Pill, PiggyColors.Emerald700) {}
        Spacer(Modifier.width(PiggySpacing.spaceSm))
        Text(text, style = MaterialTheme.typography.headlineMedium, color = PiggyColors.TextStrong)
    }
}

@Composable
private fun DonutChart(slices: List<CategorySliceUi>, colors: List<Color>, modifier: Modifier = Modifier) {
    val total = slices.sumOf { it.amount }.coerceAtLeast(0.0001)
    Canvas(modifier) {
        val stroke = 24.dp.toPx()
        var startAngle = -90f
        slices.forEachIndexed { index, slice ->
            val sweep = (slice.amount / total * 360.0).toFloat()
            val gap = min(3.5f, sweep / 3f)
            drawArc(
                color = colors[index], startAngle = startAngle + gap / 2,
                sweepAngle = (sweep - gap).coerceAtLeast(0f), useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            startAngle += sweep
        }
    }
}

@Composable
private fun CategoryLegend(slices: List<CategorySliceUi>, colors: List<Color>, total: Double) {
    Column(verticalArrangement = Arrangement.spacedBy(PiggySpacing.spaceSm)) {
        slices.chunked(2).forEachIndexed { rowIndex, row ->
            Row(horizontalArrangement = Arrangement.spacedBy(PiggySpacing.spaceSm)) {
                row.forEachIndexed { columnIndex, slice ->
                    val colorIndex = rowIndex * 2 + columnIndex
                    Surface(Modifier.weight(1f), PiggyShapeTokens.Md, colors[colorIndex].copy(alpha = 0.10f)) {
                        Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(Modifier.size(10.dp), PiggyShapeTokens.Pill, colors[colorIndex]) {}
                            Spacer(Modifier.width(PiggySpacing.spaceSm))
                            Column {
                                Text(slice.categoryName, style = PiggyTypography.badgeLabel, color = PiggyColors.TextStrong, maxLines = 1)
                                Text(String.format(Locale.getDefault(), "%.1f%%", slice.amount / total * 100), style = PiggyTypography.bodySm, color = PiggyColors.TextMuted)
                            }
                        }
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun DailySpendingCard(stacks: List<DayStackUi>, month: YearMonth) {
    val lastDay = stacks.lastOrNull { it.pieces.isNotEmpty() }?.day ?: month.lengthOfMonth()
    val shortMonth = month.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    AnalyticsCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionTitle("By day", Modifier.weight(1f))
            Text("$shortMonth 1 – $shortMonth $lastDay", style = PiggyTypography.bodyMd, color = PiggyColors.TextMuted)
        }
        Spacer(Modifier.height(PiggySpacing.spaceXl))
        DailyStackedBarChart(stacks, month)
    }
}

@Composable
private fun DailyStackedBarChart(stacks: List<DayStackUi>, yearMonth: YearMonth) {
    val maxTotal = stacks.maxOfOrNull { it.pieces.sumOf(DayPieceUi::amount) }?.coerceAtLeast(0.0001) ?: 0.0001
    LazyRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(horizontal = 2.dp)) {
        items(stacks, key = { it.day }) { DailyBar(it, maxTotal, yearMonth) }
    }
}

@Composable
private fun DailyBar(stack: DayStackUi, maxTotal: Double, yearMonth: YearMonth) {
    val pieces = stack.pieces.map { it.amount to categoryColor(null, it.colorHex) }
    val weekday = remember(yearMonth, stack.day) {
        LocalDate.of(yearMonth.year, yearMonth.month, stack.day).dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    }
    val baselineColor = piggyCardBorderColor()
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(24.dp)) {
        Canvas(Modifier.height(160.dp).width(18.dp)) {
            val total = pieces.sumOf { it.first }
            if (total == 0.0) {
                drawLine(baselineColor, Offset(0f, size.height - 1.dp.toPx()), Offset(size.width, size.height - 1.dp.toPx()), 2.dp.toPx())
            } else {
                var bottom = size.height
                pieces.forEach { (amount, color) ->
                    val height = (amount / maxTotal * size.height).toFloat().coerceAtLeast(2.dp.toPx())
                    drawRoundRect(color, Offset(0f, bottom - height), Size(size.width, height), CornerRadius(6.dp.toPx()))
                    bottom -= height
                }
            }
        }
        Spacer(Modifier.height(PiggySpacing.spaceXs))
        Text(stack.day.toString(), style = PiggyTypography.badgeLabel, color = PiggyColors.TextStrong)
        Text(weekday, style = PiggyTypography.bodySm, color = PiggyColors.TextMuted)
    }
}

@Composable
private fun CategoryDetailsHeading() {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("Category Details", style = MaterialTheme.typography.headlineMedium, color = PiggyColors.TextStrong, modifier = Modifier.weight(1f))
        Text("Tap to view", style = PiggyTypography.bodyMd, color = PiggyColors.TextMuted)
    }
}

@Composable
private fun CategoryDetailCard(data: CategoryGroupUi) {
    val currency = remember { NumberFormat.getCurrencyInstance() }
    var expanded by rememberSaveable(data.id) { mutableStateOf(false) }
    val color = categoryColor(data.name, data.colorHex)
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "category_chevron_rotation",
    )
    Surface(
        modifier = Modifier.fillMaxWidth().animateContentSize(), shape = PiggyShapeTokens.Card,
        color = piggyCardContainerColor(),
        border = BorderStroke(1.dp, piggyCardBorderColor()), shadowElevation = 1.dp,
    ) {
        Column {
            Row(
                Modifier.fillMaxWidth().clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { expanded = !expanded }.padding(PiggySpacing.spaceLg),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(Modifier.size(48.dp), PiggyShapeTokens.Pill, color.copy(alpha = 0.16f)) {
                    Box(contentAlignment = Alignment.Center) { Text(data.icon, style = MaterialTheme.typography.titleLarge) }
                }
                Spacer(Modifier.width(PiggySpacing.spaceMd))
                Column(Modifier.weight(1f)) {
                    Text(data.name, style = MaterialTheme.typography.titleMedium, color = PiggyColors.TextStrong)
                    Text("${data.count} ${if (data.count == 1) "expense" else "expenses"}", style = PiggyTypography.bodyMd, color = PiggyColors.TextMuted)
                }
                Text(currency.format(data.total), style = PiggyTypography.currencyItem, color = PiggyColors.TextStrong)
                Spacer(Modifier.width(PiggySpacing.spaceSm))
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse ${data.name}" else "Expand ${data.name}",
                    tint = PiggyColors.TextSubtle,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(chevronRotation),
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(tween(150)) + fadeIn(tween(120)),
                exit = shrinkVertically(tween(150)) + fadeOut(tween(120)),
            ) {
                Column(Modifier.padding(start = 76.dp, end = PiggySpacing.spaceLg, bottom = PiggySpacing.spaceLg)) {
                    data.items.forEach { item ->
                        Row(Modifier.fillMaxWidth().padding(top = PiggySpacing.spaceSm), verticalAlignment = Alignment.CenterVertically) {
                            Text(item.dateLabel, style = PiggyTypography.bodySm, color = PiggyColors.TextMuted, modifier = Modifier.width(56.dp))
                            Text(item.name, style = PiggyTypography.bodyMd, color = PiggyColors.TextStrong, modifier = Modifier.weight(1f), maxLines = 1)
                            Text(currency.format(item.amount), style = PiggyTypography.badgeLabel, color = PiggyColors.TextStrong)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyTrendsState() {
    Surface(
        modifier = Modifier.fillMaxWidth(), shape = PiggyShapeTokens.Card,
        color = piggyCardContainerColor(),
        border = BorderStroke(1.dp, piggyCardBorderColor()), shadowElevation = 1.dp,
    ) {
        Column(Modifier.padding(vertical = 72.dp, horizontal = PiggySpacing.spaceXl), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No expenses yet", style = MaterialTheme.typography.headlineMedium, color = PiggyColors.TextStrong)
            Spacer(Modifier.height(PiggySpacing.spaceSm))
            Text("Your spending insights will appear here.", style = PiggyTypography.bodyMd, color = PiggyColors.TextMuted)
        }
    }
}

private fun categoryColor(categoryName: String?, colorHex: String?): Color = when (categoryName?.lowercase(Locale.getDefault())) {
    "housing" -> PiggyColors.CategoryHousing
    "groceries" -> PiggyColors.CategoryGroceries
    "dining out" -> PiggyColors.CategoryDiningOut
    "transport" -> PiggyColors.CategoryTransport
    "utilities" -> PiggyColors.CategoryUtilities
    "coffee & leisure" -> PiggyColors.CategoryCoffeeLeisure
    else -> colorHex?.let { runCatching { Color(it.toColorInt()) }.getOrNull() } ?: PiggyColors.Emerald600
}

private val previewTrendsState = TrendsUiState(
    month = YearMonth.of(2026, 10), isLoading = false, total = 2145.90,
    slices = listOf(
        CategorySliceUi("housing", "Housing", "#F43F5E", 1450.0), CategorySliceUi("groceries", "Groceries", "#059669", 385.50),
        CategorySliceUi("dining", "Dining Out", "#D97706", 185.0), CategorySliceUi("utilities", "Utilities", "#0891B2", 125.40),
    ),
    categories = listOf(
        CategoryGroupUi("housing", "Housing", "🏠", "#F43F5E", 1450.0, 1, listOf(CategoryExpenseItemUi("Apartment rent", 1450.0, "Oct 1"))),
        CategoryGroupUi("groceries", "Groceries", "🛒", "#059669", 385.50, 3, listOf(CategoryExpenseItemUi("Market", 184.50, "Oct 5"))),
        CategoryGroupUi("dining", "Dining Out", "🍴", "#D97706", 185.0, 5, listOf(CategoryExpenseItemUi("Dinner", 84.0, "Oct 8"))),
        CategoryGroupUi("utilities", "Utilities", "💡", "#0891B2", 125.40, 2, listOf(CategoryExpenseItemUi("Energy", 125.40, "Oct 12"))),
    ),
    dayStacks = (1..14).map { day ->
        DayStackUi(day, when (day) {
            1 -> listOf(DayPieceUi("housing", 1450.0, "#F43F5E")); 2 -> listOf(DayPieceUi("utilities", 45.0, "#0891B2"))
            5 -> listOf(DayPieceUi("groceries", 184.50, "#059669")); 8 -> listOf(DayPieceUi("dining", 84.0, "#D97706"), DayPieceUi("utilities", 24.0, "#0891B2"))
            10 -> listOf(DayPieceUi("dining", 42.0, "#D97706")); 12 -> listOf(DayPieceUi("groceries", 115.0, "#059669"), DayPieceUi("dining", 38.0, "#D97706"))
            else -> emptyList()
        })
    },
)

@Preview(name = "Trends screen", showBackground = true, heightDp = 900)
@Composable
private fun TrendsScreenPreview() { PiggyTheme { Surface { TrendsScreenContent(previewTrendsState) } } }

@Preview(name = "Trends screen dark", showBackground = true, heightDp = 900, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TrendsScreenDarkPreview() { PiggyTheme(darkTheme = true) { Surface { TrendsScreenContent(previewTrendsState) } } }
