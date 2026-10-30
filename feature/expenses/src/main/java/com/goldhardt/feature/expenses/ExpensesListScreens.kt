package com.goldhardt.feature.expenses

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goldhardt.core.data.model.Category
import com.goldhardt.core.data.model.Expense
import com.goldhardt.designsystem.components.ConfigureTopBar
import com.goldhardt.designsystem.components.MonthSelector
import com.goldhardt.designsystem.components.TotalAmountCard
import com.goldhardt.designsystem.theme.PiggyTheme
import com.goldhardt.feature.expenses.ui.ExpenseEditorBottomSheet
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesListScreen(
    viewModel: ExpensesListViewModel = hiltViewModel(),
    onNavigateToImport: () -> Unit = {}
) {
    val context = LocalContext.current
    val isAddingExpense = remember { mutableStateOf(false) }

    ConfigureTopBar(
        title = stringResource(R.string.title_expenses),
        actions = {
            IconButton(onClick = onNavigateToImport) {
                Icon(
                    painter = painterResource(R.drawable.ic_upload),
                    contentDescription = "Import expenses"
                )
            }
            IconButton(onClick = {
                viewModel.refreshCategories()
                isAddingExpense.value = true
            }) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = context.getString(R.string.title_expenses)
                )
            }
        }
    )

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    ExpensesListScreenContent(
        state = state,
        categories = categories,
        isAddingExpense = isAddingExpense.value,
        onDismissAddExpense = { isAddingExpense.value = false },
        onMonthChange = { viewModel.setMonth(it) },
        onRefreshCategories = { viewModel.refreshCategories() },
        onAddExpense = { name, amount, date, categoryId, isFixed ->
            viewModel.addExpense(
                name = name,
                amount = amount,
                date = date,
                categoryId = categoryId,
                isFixed = isFixed,
            )
            isAddingExpense.value = false
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
        onDeleteExpense = { expenseId ->
            viewModel.deleteExpense(expenseId)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExpensesListScreenContent(
    state: UiState,
    modifier: Modifier = Modifier,
    categories: List<Category> = emptyList(),
    isAddingExpense: Boolean = false,
    onDismissAddExpense: () -> Unit = {},
    onMonthChange: (YearMonth) -> Unit = {},
    onRefreshCategories: () -> Unit = {},
    onAddExpense: (name: String, amount: Double, date: Instant, categoryId: String, isFixed: Boolean) -> Unit = { _, _, _, _, _ -> },
    onUpdateExpense: (original: Expense, name: String, amount: Double, date: Instant, categoryId: String, isFixed: Boolean) -> Unit = { _, _, _, _, _, _ -> },
    onDeleteExpense: (expenseId: String) -> Unit = {},
) {
    val addSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var editingExpense by remember { mutableStateOf<Expense?>(null) }
    val editSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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

        Spacer(modifier = Modifier.height(16.dp))

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.expenses.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.no_expenses_for_month), style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            ExpensesList(
                total = state.total,
                items = state.expenses,
                onClick = { exp ->
                    onRefreshCategories()
                    editingExpense = exp
                }
            )
        }
    }

    if (isAddingExpense) {
        ExpenseEditorBottomSheet(
            title = stringResource(R.string.title_add_expense),
            sheetState = addSheetState,
            categories = categories,
            initialName = "",
            initialAmount = "",
            initialDate = Instant.now(),
            initialCategoryId = categories.firstOrNull()?.id,
            initialIsFixed = false,
            onDismiss = onDismissAddExpense,
            onSave = onAddExpense
        )
    }

    editingExpense?.let { current ->
        ExpenseEditorBottomSheet(
            title = stringResource(R.string.title_edit_expense),
            sheetState = editSheetState,
            categories = categories,
            initialName = current.name,
            initialAmount = current.amount.toString(),
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
            }
        )
    }
}

@Composable
private fun ExpensesList(
    total: Double,
    items: List<Expense>,
    onClick: (Expense) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            TotalAmountCard(total = total)
        }
        items(items, key = { it.id }) { expense ->
            ExpenseItem(expense, onClick = { onClick(expense) })
        }
    }
}

@Composable
private fun ExpenseItem(expense: Expense, onClick: () -> Unit = {}) {
    val currency = NumberFormat.getCurrencyInstance()
    val dateLabel = try {
        DateTimeFormatter.ofPattern("MMM d")
            .withZone(ZoneId.systemDefault())
            .format(expense.date)
    } catch (_: Throwable) {
        ""
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryAvatar(
                name = expense.categoryName,
                colorHex = expense.categoryColor,
                iconEmoji = expense.categoryIcon
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                if (dateLabel.isNotEmpty()) {
                    Text(
                        text = dateLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Text(
                    text = expense.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row {
                    expense.categoryName?.let {
                        Pill(text = it)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    if (expense.isFixed) {
                        Pill(text = "Fixed")
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = currency.format(expense.amount),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
}

@Composable
private fun Pill(text: String) {
    AssistChip(
        onClick = {},
        enabled = false,
        label = { Text(text) },
        colors = AssistChipDefaults.assistChipColors(
            disabledContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
            disabledLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
    )
}

@Composable
private fun CategoryAvatar(name: String?, colorHex: String?, iconEmoji: String?) {
    val bg = try {
        Color((colorHex ?: "#F1F1F1").toColorInt())
    } catch (_: Throwable) {
        MaterialTheme.colorScheme.tertiaryContainer
    }
    Surface(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape),
        shape = CircleShape,
        color = bg
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = iconEmoji ?: (name?.firstOrNull()?.uppercase() ?: "?"))
        }
    }
}


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
        categoryColor = "#FFADAD"
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
        categoryColor = "#CAFFBF"
    ),
    Expense(
        id = "3",
        name = "Dinner with Friends",
        amount = 68.0,
        date = LocalDate.of(2026, 10, 15).atStartOfDay(ZoneId.systemDefault()).toInstant(),
        categoryId = "food",
        isFixed = false,
        createdAt = Instant.EPOCH,
        userId = "user1",
        categoryName = "Dining Out",
        categoryIcon = "🍔",
        categoryColor = "#FFD6A5"
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
        categoryIcon = "🚗",
        categoryColor = "#A0C4FF"
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
        categoryColor = "#BDB2FF"
    )
)

@Preview(name = "Expenses List Screen - Light", showBackground = true)
@Composable
private fun ExpensesListScreenPreview() {
    PiggyTheme {
        Surface {
            ExpensesListScreenContent(
                state = UiState(
                    month = YearMonth.of(2026, 10),
                    expenses = previewExpenses,
                    total = previewExpenses.sumOf { it.amount },
                    isLoading = false
                )
            )
        }
    }
}

@Preview(
    name = "Expenses List Screen - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ExpensesListScreenDarkPreview() {
    PiggyTheme(darkTheme = true) {
        Surface {
            ExpensesListScreenContent(
                state = UiState(
                    month = YearMonth.of(2026, 10),
                    expenses = previewExpenses,
                    total = previewExpenses.sumOf { it.amount },
                    isLoading = false
                )
            )
        }
    }
}

@Preview(name = "Expenses List Screen - Empty", showBackground = true)
@Composable
private fun ExpensesListScreenEmptyPreview() {
    PiggyTheme {
        Surface {
            ExpensesListScreenContent(
                state = UiState(
                    month = YearMonth.of(2026, 10),
                    expenses = emptyList(),
                    total = 0.0,
                    isLoading = false
                )
            )
        }
    }
}

@Composable
@Preview(name = "Expense Item", showBackground = true)
private fun ExpenseItemPreview() {
    val expense = previewExpenses[2]
    PiggyTheme {
        Surface {
            Column(Modifier.padding(16.dp)) {
                ExpenseItem(expense = expense)
                Spacer(Modifier.height(16.dp))
                ExpenseItem(expense = expense.copy(isFixed = true, categoryName = null))
                Spacer(Modifier.height(16.dp))
                ExpenseItem(
                    expense = expense.copy(
                        name = "Very very very long name for an expense that should be truncated",
                        isFixed = true
                    )
                )
            }
        }
    }
}