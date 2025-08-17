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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goldhardt.core.data.model.Category
import com.goldhardt.core.data.model.Expense
import com.goldhardt.designsystem.components.ConfigureTopBar
import com.goldhardt.designsystem.components.MonthSelector
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesListScreen(
    viewModel: ExpensesListViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    // Add expense state
    val isAddingExpense = remember { mutableStateOf(false) }
    val addSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ConfigureTopBar (
        title = stringResource(R.string.title_expenses),
        actions = {
            IconButton(onClick = {
                viewModel.refreshCategories()
                isAddingExpense.value = true
            }) {
                Icon(imageVector = Icons.Outlined.Add, contentDescription = context.getString(R.string.title_expenses))
            }
        }
    )

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        MonthSelector(
            month = state.month,
            onMonthChange = { viewModel.setMonth(it) },
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
            )
        }
    }

    if (isAddingExpense.value) {
        AddEditExpenseBottomSheet(
            title = stringResource(R.string.title_add_expense),
            sheetState = addSheetState,
            categories = categories,
            onDismiss = { isAddingExpense.value = false },
            onSave = { name, amount, date, categoryId, isFixed ->
                viewModel.addExpense(
                    name = name,
                    amount = amount,
                    date = date,
                    categoryId = categoryId,
                    isFixed = isFixed,
                )
                isAddingExpense.value = false
            }
        )
    }
}

@Composable
private fun TotalCard(total: Double) {
    val currency = NumberFormat.getCurrencyInstance()
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            containerColor = MaterialTheme.colorScheme.background
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text(
                text = "Total",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = currency.format(total),
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
private fun ExpensesList(
    total: Double,
    items: List<Expense>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            TotalCard(total = total)
        }
        items(items, key = { it.id }) { expense ->
            ExpenseItem(expense)
        }
    }
}

@Composable
private fun ExpenseItem(expense: Expense) {
    val currency = NumberFormat.getCurrencyInstance()
    val dateLabel = try {
        DateTimeFormatter.ofPattern("MMM d")
            .withZone(ZoneId.systemDefault())
            .format(expense.date)
    } catch (_: Throwable) {
        ""
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditExpenseBottomSheet(
    title: String,
    sheetState: SheetState,
    categories: List<Category>,
    initialName: String = "",
    initialAmount: String = "",
    initialDate: Instant = Instant.now(),
    initialCategoryId: String? = null,
    initialIsFixed: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (name: String, amount: Double, date: Instant, categoryId: String, isFixed: Boolean) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        var name by rememberSaveable(title) { mutableStateOf(initialName) }
        var amountText by rememberSaveable(title) { mutableStateOf(initialAmount) }
        var isFixed by rememberSaveable(title) { mutableStateOf(initialIsFixed) }
        var selectedCategoryId by rememberSaveable(title) { mutableStateOf(initialCategoryId ?: categories.firstOrNull()?.id) }
        var selectedDate by rememberSaveable(title) { mutableStateOf(initialDate) }
        var showDatePicker by remember { mutableStateOf(false) }

        val currencySymbol = try {
            NumberFormat.getCurrencyInstance().currency?.symbol ?: "$"
        } catch (_: Throwable) { "$" }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold))

            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.label_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            TextField(
                value = amountText,
                onValueChange = { new ->
                    // allow digits, comma and dot
                    val filtered = new.filter { it.isDigit() || it == '.' || it == ',' }
                    amountText = filtered
                },
                label = { Text(stringResource(R.string.label_amount)) },
                leadingIcon = { Text(currencySymbol) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            // Date selector row
            Row(
                modifier = Modifier

                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.label_date),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                val dateLabel = try {
                    DateTimeFormatter.ofPattern("MMM d, yyyy").withZone(ZoneId.systemDefault()).format(selectedDate)
                } catch (_: Throwable) { "" }
                Text(text = dateLabel, style = MaterialTheme.typography.bodyLarge)
            }

            // Category selector (dropdown)
            Text(text = stringResource(R.string.label_category), style = MaterialTheme.typography.titleMedium)
            val selectedCategoryName = categories.firstOrNull { it.id == selectedCategoryId }?.name ?: ""
            var expanded by remember { mutableStateOf(false) }
            var textFieldWidth by remember { mutableStateOf(0) }
            val density = LocalDensity.current
            Box {
                TextField(
                    value = if (selectedCategoryName.isNotEmpty()) selectedCategoryName else if (categories.isEmpty()) "No categories" else "",
                    onValueChange = {},
                    readOnly = true,
                    enabled = categories.isNotEmpty(),
                    trailingIcon = {
                        IconButton(onClick = { if (categories.isNotEmpty()) expanded = !expanded }) {
                            Icon(imageVector = Icons.Outlined.KeyboardArrowDown, contentDescription = null, modifier = Modifier.rotate(if (expanded) 45f else 0f))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coords -> textFieldWidth = coords.size.width }
                        .clickable(enabled = categories.isNotEmpty()) { expanded = !expanded }
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.width(with(density) { textFieldWidth.toDp() })
                ) {
                    categories.forEach { c ->
                        DropdownMenuItem(
                            text = { Text(c.name) },
                            onClick = {
                                selectedCategoryId = c.id
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Fixed switch
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = stringResource(R.string.label_fixed), modifier = Modifier.weight(1f))
                Switch(checked = isFixed, onCheckedChange = { isFixed = it })
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                Button(
                    onClick = {
                        val parsed = amountText.replace(',', '.').toDoubleOrNull() ?: 0.0
                        val catId = selectedCategoryId
                        if (name.isNotBlank() && parsed > 0.0 && !catId.isNullOrBlank()) {
                            onSave(name.trim(), parsed, selectedDate, catId, isFixed)
                        }
                    },
                    enabled = name.isNotBlank() && (amountText.replace(',', '.').toDoubleOrNull() ?: 0.0) > 0.0 && !selectedCategoryId.isNullOrBlank()
                ) {
                    Text(stringResource(R.string.btn_save))
                }
            }

            Spacer(Modifier.height(8.dp))
        }

        if (showDatePicker) {
            androidx.compose.material3.DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    Button(onClick = { showDatePicker = false }) { Text(stringResource(android.R.string.ok)) }
                },
                dismissButton = {
                    Button(onClick = { showDatePicker = false }) { Text(stringResource(android.R.string.cancel)) }
                }
            ) {
                val datePickerState = androidx.compose.material3.rememberDatePickerState(
                    initialSelectedDateMillis = try { selectedDate.toEpochMilli() } catch (_: Throwable) { null }
                )
                androidx.compose.material3.DatePicker(state = datePickerState)
                // When confirmed, update selectedDate
                val millis = datePickerState.selectedDateMillis
                if (millis != null) {
                    selectedDate = Instant.ofEpochMilli(millis)
                }
            }
        }
    }
}




@Preview
@Composable
private fun TotalCardPreview() {
    Column(Modifier.padding(16.dp)) {
        TotalCard(total = 1290.0)
    }
}

@Composable
@Preview
private fun ExpenseItemPreview() {
    val expense = Expense(
        id = "1",
        name = "Dinner with friends",
        amount = 50.0,
        date = Instant.now(),
        categoryId = "food",
        isFixed = false,
        createdAt = Instant.now(),
        userId = "user1",
        categoryName = "Food",
        categoryIcon = "🍔",
        categoryColor = "#FFC107"
    )
    Column(Modifier.padding(16.dp)) {
        ExpenseItem(expense = expense)
        Spacer(Modifier.height(16.dp))
        ExpenseItem(expense = expense.copy(isFixed = true, categoryName = null))
        Spacer(Modifier.height(16.dp))
        ExpenseItem(expense = expense.copy(name = "Very very very long name for an expense that should be truncated", isFixed = true))
    }
}