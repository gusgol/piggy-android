package com.goldhardt.feature.expenses

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goldhardt.core.data.model.Expense
import com.goldhardt.designsystem.components.ConfigureTopBar
import com.goldhardt.designsystem.components.MonthSelector
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ExpensesListScreen(
    viewModel: ExpensesListViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    ConfigureTopBar (
        title = stringResource(R.string.title_expenses),
        actions = {
            IconButton(onClick = {
            }) {
                Icon(imageVector = Icons.Outlined.Add, contentDescription = context.getString(R.string.title_expenses))
            }
        }
    )

    val state by viewModel.uiState.collectAsStateWithLifecycle()

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
                Text("No expenses for this month", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            ExpensesList(
                total = state.total,
                items = state.expenses,
            )
        }
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
