package com.goldhardt.feature.expenses.import_expenses

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goldhardt.core.data.model.Category
import com.goldhardt.designsystem.components.ConfigureTopBar
import com.goldhardt.feature.expenses.R
import com.goldhardt.feature.expenses.import_expenses.domain.ImportedExpense
import java.text.NumberFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportExpensesScreen(
    viewModel: ImportViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ConfigureTopBar(
        title = "Import Expenses",
        actions = {
            if (state.importStep != ImportStep.SELECT_FILE) {
                IconButton(onClick = {
                    viewModel.reset()
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel")
                }
            }
        }
    )

    when (state.importStep) {
        ImportStep.SELECT_FILE -> {
            SelectFileStep(
                onFileSelected = { uri ->
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        viewModel.parseCsvFile(inputStream)
                    }
                },
                isLoading = state.isLoading,
                error = state.error
            )
        }
        ImportStep.ASSIGN_CATEGORIES -> {
            AssignCategoriesStep(
                expenses = state.expenses,
                categories = state.categories,
                onCategorySelected = { index, categoryId ->
                    viewModel.updateExpenseCategory(index, categoryId)
                },
                onIsFixedChanged = { index, isFixed ->
                    viewModel.updateExpenseIsFixed(index, isFixed)
                },
                onBulkAssignCategory = { categoryId ->
                    viewModel.bulkAssignCategory(categoryId)
                },
                onDeleteExpense = { index ->
                    viewModel.deleteExpense(index)
                },
                onImport = { viewModel.importExpenses() },
                isImporting = state.isImporting,
                importProgress = state.importProgress,
                error = state.error
            )
        }
        ImportStep.COMPLETED -> {
            CompletedStep(
                importedCount = state.importedCount,
                onDone = onNavigateBack,
                onImportMore = { viewModel.reset() }
            )
        }
    }
}

@Composable
private fun SelectFileStep(
    onFileSelected: (Uri) -> Unit,
    isLoading: Boolean,
    error: String?
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onFileSelected(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_upload),
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Import Expenses from CSV",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Select a CSV file with columns: date, name, amount",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { launcher.launch("text/*") },
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Icon(painter = painterResource(R.drawable.ic_attach_file), contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select CSV File")
            }
        }

        error?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AssignCategoriesStep(
    expenses: List<ImportedExpense>,
    categories: List<Category>,
    onCategorySelected: (Int, String) -> Unit,
    onIsFixedChanged: (Int, Boolean) -> Unit,
    onBulkAssignCategory: (String) -> Unit,
    onDeleteExpense: (Int) -> Unit,
    onImport: () -> Unit,
    isImporting: Boolean,
    importProgress: Int,
    error: String?
) {
    val unassignedCount = expenses.count { it.categoryId == null }
    val totalAmount = expenses.sumOf { it.amount }
    val currencyFormatter = NumberFormat.getCurrencyInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Summary card
        OutlinedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Total Expenses",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${expenses.size}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Total Amount",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = currencyFormatter.format(totalAmount),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (unassignedCount > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "$unassignedCount expenses need category assignment",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bulk assign option
        if (unassignedCount > 0 && categories.isNotEmpty()) {
            BulkAssignSection(
                categories = categories,
                onBulkAssign = onBulkAssignCategory
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Expense list
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(expenses) { index, expense ->
                ExpenseImportItem(
                    expense = expense,
                    categories = categories,
                    onCategorySelected = { categoryId ->
                        onCategorySelected(index, categoryId)
                    },
                    onIsFixedChanged = { isFixed ->
                        onIsFixedChanged(index, isFixed)
                    },
                    onDelete = { onDeleteExpense(index) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Import button
        if (isImporting) {
            Column {
                LinearProgressIndicator(
                    progress = { importProgress / 100f },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Importing... $importProgress%",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        } else {
            Button(
                onClick = onImport,
                modifier = Modifier.fillMaxWidth(),
                enabled = unassignedCount == 0
            ) {
                Text("Import ${expenses.size} Expenses")
            }
        }

        error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun BulkAssignSection(
    categories: List<Category>,
    onBulkAssign: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Bulk Assign Category",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Assign the same category to all unassigned expenses",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(category.color.toColorInt()))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(category.name)
                        }
                    },
                    onClick = {
                        onBulkAssign(category.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ExpenseImportItem(
    expense: ImportedExpense,
    categories: List<Category>,
    onCategorySelected: (String) -> Unit,
    onIsFixedChanged: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    val currencyFormatter = NumberFormat.getCurrencyInstance()
    val selectedCategory = categories.find { it.id == expense.categoryId }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (expense.categoryId == null) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = expense.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = expense.date.atZone(ZoneId.systemDefault()).format(dateFormatter),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = currencyFormatter.format(expense.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete expense",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Category selector
            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (selectedCategory != null) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(Color(selectedCategory.color.toColorInt()))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(selectedCategory.name)
                    } else {
                        Icon(Icons.Outlined.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select Category")
                    }
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(category.color.toColorInt()))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(category.name)
                                }
                            },
                            onClick = {
                                onCategorySelected(category.id)
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Fixed expense checkbox
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onIsFixedChanged(!expense.isFixed) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = expense.isFixed,
                    onCheckedChange = onIsFixedChanged
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Fixed expense",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun CompletedStep(
    importedCount: Int,
    onDone: () -> Unit,
    onImportMore: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Import Successful!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "$importedCount expenses imported successfully",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text("View Expenses")
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = onImportMore,
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text("Import Another File")
        }
    }
}
