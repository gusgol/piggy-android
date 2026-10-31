package com.goldhardt.feature.expenses

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goldhardt.core.domain.model.Category
import com.goldhardt.core.domain.model.Expense
import com.goldhardt.designsystem.components.ConfigureTopBar
import com.goldhardt.designsystem.components.MonthSelector
import com.goldhardt.designsystem.theme.CategoryColorPalette
import com.goldhardt.designsystem.theme.PiggyColors
import com.goldhardt.designsystem.theme.PiggyShapeTokens
import com.goldhardt.designsystem.theme.PiggySpacing
import com.goldhardt.designsystem.theme.PiggyTheme
import com.goldhardt.designsystem.theme.PiggyTypography
import com.goldhardt.designsystem.theme.isPiggyDarkTheme
import com.goldhardt.designsystem.theme.piggyCardBorderColor
import com.goldhardt.designsystem.theme.piggyCardContainerColor
import com.goldhardt.designsystem.theme.piggySubtleContainerColor
import com.goldhardt.designsystem.theme.primaryFixed
import com.goldhardt.designsystem.theme.rememberCategoryColorPalette
import com.goldhardt.designsystem.theme.secondaryFixed
import com.goldhardt.feature.expenses.domain.BuildMonthlyExpensesProjectionUseCase
import com.goldhardt.feature.expenses.domain.CategoryFilterOption
import com.goldhardt.feature.expenses.domain.SpendingInsight
import com.goldhardt.feature.expenses.ui.ExpenseEditorBottomSheet
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesListScreen(
    viewModel: ExpensesListViewModel = hiltViewModel(),
    onNavigateToImport: () -> Unit = {},
) {
    var isAddingExpense by rememberSaveable { mutableStateOf(false) }

    ConfigureTopBar(
        title = stringResource(R.string.title_expenses),
        actions = {
            var menuExpanded by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = stringResource(R.string.action_more_options),
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_import_expenses)) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_upload),
                                contentDescription = null,
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onNavigateToImport()
                        },
                    )
                }
            }
        }
    )

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ExpensesListScreenContent(
        state = state,
        isAddingExpense = isAddingExpense,
        onOpenAddExpense = { isAddingExpense = true },
        onDismissAddExpense = { isAddingExpense = false },
        onMonthChange = viewModel::setMonth,
        onSelectCategory = viewModel::selectCategory,
        onDismissError = viewModel::clearError,
        onAddExpense = { name, amount, date, categoryId, isFixed ->
            viewModel.addExpense(
                name = name,
                amount = amount,
                date = date,
                categoryId = categoryId,
                isFixed = isFixed,
            )
            isAddingExpense = false
        },
        onUpdateExpense = { original, name, amount, date, categoryId, isFixed ->
            viewModel.updateExpense(
                original = original,
                name = name,
                amount = amount,
                date = date,
                categoryId = categoryId,
                isFixed = isFixed,
            )
        },
        onDeleteExpense = viewModel::deleteExpense,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExpensesListScreenContent(
    state: UiState,
    modifier: Modifier = Modifier,
    isAddingExpense: Boolean = false,
    onOpenAddExpense: () -> Unit = {},
    onDismissAddExpense: () -> Unit = {},
    onMonthChange: (YearMonth) -> Unit = {},
    onSelectCategory: (String?) -> Unit = {},
    onDismissError: () -> Unit = {},
    onAddExpense: (name: String, amount: Double, date: Instant, categoryId: String, isFixed: Boolean) -> Unit = { _, _, _, _, _ -> },
    onUpdateExpense: (original: Expense, name: String, amount: Double, date: Instant, categoryId: String, isFixed: Boolean) -> Unit = { _, _, _, _, _, _ -> },
    onDeleteExpense: (expenseId: String) -> Unit = {},
) {
    val addSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var editingExpense by remember { mutableStateOf<Expense?>(null) }
    val editSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = PiggySpacing.spaceSm,
                    bottom = 96.dp,
                ),
            ) {
                item(key = "month_selector") {
                    MonthSelector(
                        month = state.month,
                        onMonthChange = onMonthChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = PiggySpacing.margin),
                    )
                    Spacer(modifier = Modifier.height(PiggySpacing.spaceLg))
                }

                if (!state.error.isNullOrBlank()) {
                    item(key = "error_banner") {
                        ErrorBanner(
                            message = state.error,
                            onDismiss = onDismissError,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = PiggySpacing.margin),
                        )
                        Spacer(modifier = Modifier.height(PiggySpacing.spaceMd))
                    }
                }

                item(key = "summary_hero") {
                    ExpensesSummaryCard(
                        total = state.total,
                        fixedTotal = state.fixedTotal,
                        variableTotal = state.variableTotal,
                        fixedFraction = state.fixedFraction,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = PiggySpacing.margin),
                    )
                    Spacer(modifier = Modifier.height(PiggySpacing.spaceXl))
                }

                item(key = "transactions_header_and_filters") {
                    TransactionsFilterSection(
                        totalExpenseCount = state.totalExpenseCount,
                        categoryFilters = state.categoryFilters,
                        selectedCategoryId = state.selectedCategoryId,
                        onSelectCategory = onSelectCategory,
                    )
                    Spacer(modifier = Modifier.height(PiggySpacing.spaceLg))
                }

                when {
                    state.isLoading -> {
                        item(key = "loading_state") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = PiggySpacing.space3xl),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    state.totalExpenseCount == 0 -> {
                        item(key = "empty_month_state") {
                            ExpensesEmptyStateCard(
                                title = stringResource(R.string.no_expenses_for_month),
                                subtitle = stringResource(R.string.no_expenses_for_month_subtitle),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = PiggySpacing.margin),
                            )
                        }
                    }

                    state.expenses.isEmpty() -> {
                        item(key = "empty_filter_state") {
                            ExpensesEmptyStateCard(
                                title = stringResource(R.string.no_expenses_in_filter),
                                subtitle = stringResource(R.string.no_expenses_in_filter_subtitle),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = PiggySpacing.margin),
                            )
                        }
                    }

                    else -> {
                        items(
                            items = state.expenses,
                            key = { it.id },
                        ) { expense ->
                            ExpenseTransactionCard(
                                expense = expense,
                                onClick = { editingExpense = expense },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = PiggySpacing.margin),
                            )
                            Spacer(modifier = Modifier.height(PiggySpacing.spaceMd))
                        }
                    }
                }

                if (!state.isLoading && state.insight != null) {
                    item(key = "smart_piggy_insight") {
                        Spacer(modifier = Modifier.height(PiggySpacing.spaceSm))
                        SmartPiggyInsightCard(
                            insight = state.insight,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = PiggySpacing.margin),
                        )
                    }
                }
            }

            ExtendedFloatingActionButton(
                onClick = onOpenAddExpense,
                shape = PiggyShapeTokens.Pill,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 8.dp,
                ),
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.action_add_expense),
                        style = PiggyTypography.titleSm,
                    )
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = PiggySpacing.margin,
                        bottom = PiggySpacing.spaceLg,
                    ),
            )
        }
    }

    if (isAddingExpense) {
        val defaultDate = remember(state.month) {
            val today = LocalDate.now()
            if (YearMonth.from(today) == state.month) {
                Instant.now()
            } else {
                state.month.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
            }
        }
        ExpenseEditorBottomSheet(
            title = stringResource(R.string.title_add_expense),
            sheetState = addSheetState,
            categories = state.categories,
            initialName = "",
            initialAmount = "",
            initialDate = defaultDate,
            initialCategoryId = state.selectedCategoryId ?: state.categories.firstOrNull()?.id,
            initialIsFixed = false,
            onDismiss = onDismissAddExpense,
            onSave = onAddExpense,
        )
    }

    editingExpense?.let { current ->
        ExpenseEditorBottomSheet(
            title = stringResource(R.string.title_edit_expense),
            sheetState = editSheetState,
            categories = state.categories,
            initialName = current.name,
            initialAmount = String.format(Locale.getDefault(), "%.2f", current.amount),
            initialDate = current.date,
            initialCategoryId = current.categoryId,
            initialIsFixed = current.isFixed,
            onDismiss = { editingExpense = null },
            onSave = { name, amount, date, categoryId, isFixed ->
                onUpdateExpense(current, name, amount, date, categoryId, isFixed)
                editingExpense = null
            },
            showDelete = true,
            onDelete = {
                onDeleteExpense(current.id)
                editingExpense = null
            },
        )
    }
}

@Composable
private fun ExpensesSummaryCard(
    total: Double,
    fixedTotal: Double,
    variableTotal: Double,
    fixedFraction: Float,
    modifier: Modifier = Modifier,
) {
    val isDark = isPiggyDarkTheme()
    val currencyParts = remember(total) { formatCurrencyParts(total) }
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance() }
    val hasSpending = total > 0.0
    val animatedFixedFraction by animateFloatAsState(
        targetValue = if (hasSpending) fixedFraction.coerceIn(0f, 1f) else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "fixed_spending_fraction",
    )

    val borderColor = if (isDark) {
        piggyCardBorderColor()
    } else {
        PiggyColors.Emerald500.copy(alpha = 0.16f)
    }
    val topLeftBloom = if (isDark) {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.18f)
    } else {
        secondaryFixed.copy(alpha = 0.40f)
    }
    val bottomRightBloom = if (isDark) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f)
    } else {
        primaryFixed.copy(alpha = 0.35f)
    }

    Surface(
        modifier = modifier,
        shape = PiggyShapeTokens.Card,
        color = piggyCardContainerColor(),
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 2.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawWithCache {
                    val topGlow = Brush.radialGradient(
                        colors = listOf(topLeftBloom, Color.Transparent),
                        center = Offset(0f, 0f),
                        radius = size.minDimension * 0.9f,
                    )
                    val bottomGlow = Brush.radialGradient(
                        colors = listOf(bottomRightBloom, Color.Transparent),
                        center = Offset(size.width, size.height),
                        radius = size.maxDimension * 0.75f,
                    )
                    onDrawBehind {
                        drawRect(brush = topGlow)
                        drawRect(brush = bottomGlow)
                    }
                }
                .padding(PiggySpacing.spaceXl)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(PiggySpacing.spaceSm),
            ) {
                Text(
                    text = stringResource(R.string.label_total_expenses),
                    style = PiggyTypography.labelCaps,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.padding(vertical = PiggySpacing.space2xs),
                ) {
                    Text(
                        text = currencyParts.symbol,
                        style = PiggyTypography.headlineLg,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.alignByBaseline(),
                    )
                    Spacer(modifier = Modifier.width(PiggySpacing.spaceXs))
                    Text(
                        text = currencyParts.amountNumber,
                        style = PiggyTypography.displayCurrencyMobile,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.alignByBaseline(),
                    )
                }

                // Segmented Breakdown Progress Meter
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = PiggySpacing.space2xs)
                        .height(8.dp)
                        .clip(PiggyShapeTokens.Pill)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(2.dp)
                ) {
                    if (hasSpending) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            when {
                                animatedFixedFraction >= 0.995f -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(PiggyShapeTokens.Pill)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                }
                                animatedFixedFraction <= 0.005f -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(PiggyShapeTokens.Pill)
                                            .background(MaterialTheme.colorScheme.secondaryContainer)
                                    )
                                }
                                else -> {
                                    Box(
                                        modifier = Modifier
                                            .weight(animatedFixedFraction)
                                            .fillMaxHeight()
                                            .clip(PiggyShapeTokens.Pill)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                    Spacer(modifier = Modifier.width(PiggySpacing.space2xs))
                                    Box(
                                        modifier = Modifier
                                            .weight(1f - animatedFixedFraction)
                                            .fillMaxHeight()
                                            .clip(PiggyShapeTokens.Pill)
                                            .background(MaterialTheme.colorScheme.secondaryContainer)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(PiggySpacing.space2xs))

                // Two-column Fixed vs Variable Breakdown
                Surface(
                    shape = PiggyShapeTokens.Md,
                    color = piggySubtleContainerColor().copy(alpha = if (isDark) 0.72f else 0.65f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(PiggySpacing.spaceMd),
                        horizontalArrangement = Arrangement.spacedBy(PiggySpacing.spaceMd),
                    ) {
                        SummaryMetricColumn(
                            label = stringResource(R.string.label_fixed_costs),
                            amountText = currencyFormatter.format(fixedTotal),
                            dotColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                        )
                        SummaryMetricColumn(
                            label = stringResource(R.string.label_variable_costs),
                            amountText = currencyFormatter.format(variableTotal),
                            dotColor = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryMetricColumn(
    label: String,
    amountText: String,
    dotColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PiggySpacing.spaceXs),
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(PiggyShapeTokens.Pill)
                    .background(dotColor)
            )
            Text(
                text = label,
                style = PiggyTypography.labelCaps,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.height(PiggySpacing.space2xs))
        Text(
            text = amountText,
            style = PiggyTypography.currencyItem,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun TransactionsFilterSection(
    totalExpenseCount: Int,
    categoryFilters: List<CategoryFilterOption>,
    selectedCategoryId: String?,
    onSelectCategory: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(PiggySpacing.spaceSm),
    ) {
        Text(
            text = stringResource(R.string.section_transactions),
            style = PiggyTypography.headlineMd,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = PiggySpacing.margin),
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(
                horizontal = PiggySpacing.margin,
                vertical = PiggySpacing.space2xs,
            ),
            horizontalArrangement = Arrangement.spacedBy(PiggySpacing.spaceSm),
        ) {
            item(key = "filter_all") {
                AllCategoryFilterPill(
                    count = totalExpenseCount,
                    selected = selectedCategoryId == null,
                    onClick = { onSelectCategory(null) },
                )
            }
            items(
                items = categoryFilters,
                key = { it.id },
            ) { option ->
                val palette = rememberCategoryColorPalette(
                    colorHex = option.colorHex,
                    categoryName = option.name,
                )
                CategoryFilterPill(
                    name = option.name,
                    dotColor = palette.dot,
                    selected = selectedCategoryId == option.id,
                    onClick = { onSelectCategory(option.id) },
                )
            }
        }
    }
}

@Composable
private fun AllCategoryFilterPill(
    count: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        piggyCardContainerColor()
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val border = if (selected) null else BorderStroke(1.dp, piggyCardBorderColor())
    val badgeBg = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val badgeFg = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        selected = selected,
        onClick = onClick,
        shape = PiggyShapeTokens.Pill,
        color = containerColor,
        contentColor = contentColor,
        border = border,
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = 34.dp)
                .padding(horizontal = PiggySpacing.spaceMd, vertical = PiggySpacing.spaceXs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PiggySpacing.spaceXs),
        ) {
            Text(
                text = stringResource(R.string.filter_all),
                style = PiggyTypography.badgeLabel,
            )
            Surface(
                shape = PiggyShapeTokens.Pill,
                color = badgeBg,
                contentColor = badgeFg,
            ) {
                Text(
                    text = count.toString(),
                    style = PiggyTypography.labelCaps.copy(fontFeatureSettings = "tnum"),
                    modifier = Modifier.padding(horizontal = PiggySpacing.spaceXs, vertical = 1.dp),
                )
            }
        }
    }
}

@Composable
private fun CategoryFilterPill(
    name: String,
    dotColor: Color,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        piggyCardContainerColor()
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val effectiveDotColor = if (selected) {
        primaryFixed
    } else {
        dotColor
    }
    val border = if (selected) null else BorderStroke(1.dp, piggyCardBorderColor())

    Surface(
        selected = selected,
        onClick = onClick,
        shape = PiggyShapeTokens.Pill,
        color = containerColor,
        contentColor = contentColor,
        border = border,
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = 34.dp)
                .padding(horizontal = PiggySpacing.spaceMd, vertical = PiggySpacing.spaceXs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PiggySpacing.spaceXs),
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(PiggyShapeTokens.Pill)
                    .background(effectiveDotColor)
            )
            Text(
                text = name,
                style = PiggyTypography.badgeLabel,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ExpenseTransactionCard(
    expense: Expense,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currency = remember { NumberFormat.getCurrencyInstance() }
    val dateLabel = remember(expense.date) {
        try {
            DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
                .withZone(ZoneId.systemDefault())
                .format(expense.date)
        } catch (_: Throwable) {
            ""
        }
    }
    val palette = rememberCategoryColorPalette(
        colorHex = expense.categoryColor,
        categoryName = expense.categoryName,
    )

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = PiggyShapeTokens.Card,
        color = piggyCardContainerColor(),
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, piggyCardBorderColor()),
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PiggySpacing.spaceLg),
            verticalArrangement = Arrangement.spacedBy(PiggySpacing.spaceSm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CategoryAvatar(
                    name = expense.categoryName,
                    iconEmoji = expense.categoryIcon,
                    palette = palette,
                )

                Spacer(modifier = Modifier.width(PiggySpacing.spaceMd))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = expense.name,
                        style = PiggyTypography.titleSm.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(PiggySpacing.spaceXs),
                    ) {
                        if (dateLabel.isNotEmpty()) {
                            Text(
                                text = dateLabel,
                                style = PiggyTypography.bodySm,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "•",
                                style = PiggyTypography.bodySm,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = if (expense.isFixed) {
                                stringResource(R.string.badge_fixed)
                            } else {
                                stringResource(R.string.badge_variable)
                            },
                            style = PiggyTypography.bodySm.copy(fontWeight = FontWeight.SemiBold),
                            color = if (expense.isFixed) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.secondary
                            },
                        )
                    }
                }

                Spacer(modifier = Modifier.width(PiggySpacing.spaceSm))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = currency.format(expense.amount),
                        style = PiggyTypography.currencyItem.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.width(PiggySpacing.space2xs))
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            if (!expense.categoryName.isNullOrBlank()) {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = piggyCardBorderColor().copy(alpha = 0.6f),
                )
                CategoryBadge(
                    name = expense.categoryName!!,
                    palette = palette,
                )
            }
        }
    }
}

@Composable
private fun CategoryAvatar(
    name: String?,
    iconEmoji: String?,
    palette: CategoryColorPalette,
) {
    Surface(
        modifier = Modifier.size(46.dp),
        shape = PiggyShapeTokens.Pill,
        color = palette.avatarContainer,
        contentColor = palette.avatarContent,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = iconEmoji?.takeIf { it.isNotBlank() }
                    ?: (name?.firstOrNull()?.uppercase() ?: "?"),
                style = PiggyTypography.headlineMd,
            )
        }
    }
}

@Composable
private fun CategoryBadge(
    name: String,
    palette: CategoryColorPalette,
) {
    Surface(
        shape = PiggyShapeTokens.Pill,
        color = palette.badgeContainer,
        contentColor = palette.badgeContent,
    ) {
        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = 26.dp)
                .padding(horizontal = 10.dp, vertical = PiggySpacing.space2xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PiggySpacing.spaceXs),
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(PiggyShapeTokens.Pill)
                    .background(palette.dot)
            )
            Text(
                text = name,
                style = PiggyTypography.badgeLabel,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ExpensesEmptyStateCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = PiggyShapeTokens.Card,
        color = piggyCardContainerColor(),
        border = BorderStroke(1.dp, piggyCardBorderColor()),
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PiggySpacing.space2xl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = PiggyShapeTokens.Pill,
                color = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.ic_receipt_long),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(PiggySpacing.spaceMd))
            Text(
                text = title,
                style = PiggyTypography.titleSm,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(PiggySpacing.space2xs))
            Text(
                text = subtitle,
                style = PiggyTypography.bodySm,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SmartPiggyInsightCard(
    insight: SpendingInsight,
    modifier: Modifier = Modifier,
) {
    val currency = remember { NumberFormat.getCurrencyInstance() }
    val isDark = isPiggyDarkTheme()
    val bodyText = when (insight) {
        is SpendingInsight.AllFixed -> stringResource(
            R.string.insight_all_fixed,
            currency.format(insight.fixedAmount),
        )
        is SpendingInsight.ControlledVariable -> stringResource(
            R.string.insight_controlled_variable,
            insight.fixedPercentage,
            currency.format(insight.variableAmount),
        )
        is SpendingInsight.TopCategory -> if (insight.isVariableCategory) {
            stringResource(
                R.string.insight_top_variable_category,
                insight.categoryName,
                currency.format(insight.amount),
                insight.sharePercentage,
            )
        } else {
            stringResource(
                R.string.insight_top_category,
                insight.categoryName,
                currency.format(insight.amount),
                insight.sharePercentage,
            )
        }
    }

    val iconBg = if (isDark) MaterialTheme.colorScheme.primaryContainer else primaryFixed
    val iconTint = if (isDark) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary

    Surface(
        modifier = modifier,
        shape = PiggyShapeTokens.Card,
        color = piggySubtleContainerColor(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PiggySpacing.spaceLg),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(PiggySpacing.spaceMd),
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = PiggyShapeTokens.Pill,
                color = iconBg,
                contentColor = iconTint,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.ic_lightbulb),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.insight_title),
                    style = PiggyTypography.titleSm,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = bodyText,
                    style = PiggyTypography.bodySm,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ErrorBanner(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = PiggyShapeTokens.Md,
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = PiggySpacing.spaceLg, end = PiggySpacing.spaceXs, top = PiggySpacing.space2xs, bottom = PiggySpacing.space2xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = message,
                style = PiggyTypography.bodySm,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Dismiss error",
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

private data class FormattedCurrencyParts(
    val symbol: String,
    val amountNumber: String,
)

private fun formatCurrencyParts(
    amount: Double,
    locale: Locale = Locale.getDefault(),
): FormattedCurrencyParts {
    val currencyFormat = NumberFormat.getCurrencyInstance(locale)
    val numberFormat = NumberFormat.getNumberInstance(locale).apply {
        minimumFractionDigits = currencyFormat.minimumFractionDigits
        maximumFractionDigits = currencyFormat.maximumFractionDigits
        isGroupingUsed = true
    }
    val symbol = try {
        currencyFormat.currency?.getSymbol(locale) ?: "$"
    } catch (_: Throwable) {
        "$"
    }
    return FormattedCurrencyParts(
        symbol = symbol,
        amountNumber = numberFormat.format(amount),
    )
}

private fun Color.toHexToken(): String =
    String.format(Locale.US, "#%06X", 0xFFFFFF and toArgb())

private val previewCategories = listOf(
    Category(
        id = "housing",
        name = "Housing",
        createdAt = Instant.EPOCH,
        userId = "user1",
        icon = "🏠",
        color = PiggyColors.CategoryHousing.toHexToken(),
    ),
    Category(
        id = "groceries",
        name = "Groceries",
        createdAt = Instant.EPOCH,
        userId = "user1",
        icon = "🛒",
        color = PiggyColors.CategoryGroceries.toHexToken(),
    ),
    Category(
        id = "dining",
        name = "Dining Out",
        createdAt = Instant.EPOCH,
        userId = "user1",
        icon = "🍔",
        color = PiggyColors.CategoryDiningOut.toHexToken(),
    ),
    Category(
        id = "transport",
        name = "Transport",
        createdAt = Instant.EPOCH,
        userId = "user1",
        icon = "🚆",
        color = PiggyColors.CategoryTransport.toHexToken(),
    ),
    Category(
        id = "coffee",
        name = "Coffee",
        createdAt = Instant.EPOCH,
        userId = "user1",
        icon = "☕️",
        color = PiggyColors.CategoryCoffeeLeisure.toHexToken(),
    ),
    Category(
        id = "utilities",
        name = "Utilities",
        createdAt = Instant.EPOCH,
        userId = "user1",
        icon = "💡",
        color = PiggyColors.CategoryUtilities.toHexToken(),
    ),
)

private val previewExpenses = listOf(
    Expense(
        id = "1",
        name = "Apartment Rent",
        amount = 1450.0,
        date = LocalDate.of(2026, 10, 1).atStartOfDay(ZoneId.systemDefault()).toInstant(),
        categoryId = "housing",
        isFixed = true,
        createdAt = Instant.EPOCH,
        userId = "user1",
        categoryName = "Housing",
        categoryIcon = "🏠",
        categoryColor = PiggyColors.CategoryHousing.toHexToken(),
    ),
    Expense(
        id = "2",
        name = "Whole Foods Market",
        amount = 184.50,
        date = LocalDate.of(2026, 10, 12).atStartOfDay(ZoneId.systemDefault()).toInstant(),
        categoryId = "groceries",
        isFixed = false,
        createdAt = Instant.EPOCH,
        userId = "user1",
        categoryName = "Groceries",
        categoryIcon = "🛒",
        categoryColor = PiggyColors.CategoryGroceries.toHexToken(),
    ),
    Expense(
        id = "3",
        name = "Dinner with Friends",
        amount = 68.0,
        date = LocalDate.of(2026, 10, 15).atStartOfDay(ZoneId.systemDefault()).toInstant(),
        categoryId = "dining",
        isFixed = false,
        createdAt = Instant.EPOCH,
        userId = "user1",
        categoryName = "Dining Out",
        categoryIcon = "🍔",
        categoryColor = PiggyColors.CategoryDiningOut.toHexToken(),
    ),
    Expense(
        id = "4",
        name = "City Metro Pass",
        amount = 45.0,
        date = LocalDate.of(2026, 10, 18).atStartOfDay(ZoneId.systemDefault()).toInstant(),
        categoryId = "transport",
        isFixed = true,
        createdAt = Instant.EPOCH,
        userId = "user1",
        categoryName = "Transport",
        categoryIcon = "🚆",
        categoryColor = PiggyColors.CategoryTransport.toHexToken(),
    ),
    Expense(
        id = "5",
        name = "Specialty Coffee Beans",
        amount = 24.90,
        date = LocalDate.of(2026, 10, 22).atStartOfDay(ZoneId.systemDefault()).toInstant(),
        categoryId = "coffee",
        isFixed = false,
        createdAt = Instant.EPOCH,
        userId = "user1",
        categoryName = "Coffee",
        categoryIcon = "☕️",
        categoryColor = PiggyColors.CategoryCoffeeLeisure.toHexToken(),
    ),
)

private fun buildPreviewUiState(
    expenses: List<Expense> = previewExpenses,
    categories: List<Category> = previewCategories,
    selectedCategoryId: String? = null,
): UiState {
    val projection = BuildMonthlyExpensesProjectionUseCase()(
        expenses = expenses,
        categories = categories,
        selectedCategoryId = selectedCategoryId,
    )
    return UiState(
        month = YearMonth.of(2026, 10),
        expenses = projection.filteredExpenses,
        total = projection.summary.totalAmount,
        fixedTotal = projection.summary.fixedAmount,
        variableTotal = projection.summary.variableAmount,
        fixedFraction = projection.summary.fixedFraction,
        variableFraction = projection.summary.variableFraction,
        totalExpenseCount = projection.summary.totalCount,
        categories = categories,
        categoryFilters = projection.categoryFilters,
        selectedCategoryId = projection.effectiveSelectedCategoryId,
        insight = projection.insight,
        isLoading = false,
    )
}

@Preview(name = "Expenses List Screen - Light", showBackground = true, heightDp = 1000)
@Composable
private fun ExpensesListScreenPreview() {
    PiggyTheme {
        ExpensesListScreenContent(state = buildPreviewUiState())
    }
}

@Preview(
    name = "Expenses List Screen - Dark",
    showBackground = true,
    heightDp = 1000,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ExpensesListScreenDarkPreview() {
    PiggyTheme(darkTheme = true) {
        ExpensesListScreenContent(state = buildPreviewUiState())
    }
}

@Preview(name = "Expenses List Screen - Filtered (Empty Category)", showBackground = true, heightDp = 820)
@Composable
private fun ExpensesListScreenFilteredEmptyPreview() {
    PiggyTheme {
        ExpensesListScreenContent(
            state = buildPreviewUiState(selectedCategoryId = "utilities")
        )
    }
}

@Preview(name = "Expenses List Screen - Empty Month", showBackground = true, heightDp = 820)
@Composable
private fun ExpensesListScreenEmptyPreview() {
    PiggyTheme {
        ExpensesListScreenContent(
            state = buildPreviewUiState(expenses = emptyList())
        )
    }
}

@Preview(name = "Expense Transaction Card", showBackground = true)
@Composable
private fun ExpenseItemPreview() {
    val expense = previewExpenses[2]
    PiggyTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Column(
                modifier = Modifier.padding(PiggySpacing.margin),
                verticalArrangement = Arrangement.spacedBy(PiggySpacing.spaceMd),
            ) {
                ExpenseTransactionCard(expense = expense, onClick = {})
                ExpenseTransactionCard(
                    expense = expense.copy(isFixed = true, categoryName = null),
                    onClick = {},
                )
                ExpenseTransactionCard(
                    expense = expense.copy(
                        name = "Very very very long name for an expense that should wrap cleanly to two lines and truncate",
                        isFixed = true,
                    ),
                    onClick = {},
                )
            }
        }
    }
}
