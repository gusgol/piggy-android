package com.goldhardt.feature.expenses.import.ui

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goldhardt.designsystem.components.ConfigureTopBar
import com.goldhardt.feature.expenses.import.ImportExpensesViewModel
import com.goldhardt.feature.expenses.import.model.ImportExpenseItem
import java.text.NumberFormat
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportExpensesScreen(
    viewModel: ImportExpensesViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.use { inputStream ->
                viewModel.parseFile(inputStream)
            }
        }
    }

    // Show success message
    LaunchedEffect(state.saveSuccess) {
        state.saveSuccess?.let { (saved, skipped) ->
            snackbarHostState.showSnackbar(
                message = "$saved imported, $skipped skipped",
                duration = SnackbarDuration.Short
            )
            viewModel.clearSaveSuccess()
            // Navigate back after successful import
            onNavigateBack()
        }
    }

    // Show error messages
    LaunchedEffect(state.parseError) {
        state.parseError?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Long
            )
        }
    }

    LaunchedEffect(state.saveError) {
        state.saveError?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Long
            )
        }
    }

    ConfigureTopBar(
        title = "Import Expenses",
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
            }
        }
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (state.items.isNotEmpty() && !state.isParsing && !state.isSaving) {
                FloatingActionButton(
                    onClick = { viewModel.saveConfirmed() },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Save")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                state.items.isEmpty() && !state.isParsing -> {
                    EmptyState(onSelectFile = { filePickerLauncher.launch("text/*") })
                }

                state.isParsing -> {
                    ParsingState(progress = state.parseProgress)
                }

                state.isSaving -> {
                    SavingState()
                }

                else -> {
                    ImportReviewContent(
                        items = state.items,
                        categories = state.categories,
                        confirmedCount = state.confirmedCount,
                        onConfirmItem = viewModel::confirmItem,
                        onSkipItem = viewModel::skipItem,
                        onUpdateCategory = viewModel::updateCategory,
                        onUpdateDescription = viewModel::updateDescription,
                        onConfirmAll = viewModel::confirmAll
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(onSelectFile: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Upload,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Import Expenses from CSV",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Select a CSV file with columns:\ndata, lançamento, valor",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onSelectFile) {
                Text("Select CSV File")
            }
        }
    }
}

@Composable
private fun ParsingState(progress: Int) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Parsing CSV...",
                style = MaterialTheme.typography.titleMedium
            )
            if (progress > 0) {
                Text(
                    text = "$progress rows parsed",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun SavingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Saving expenses...",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun ImportReviewContent(
    items: List<ImportExpenseItem>,
    categories: List<com.goldhardt.core.data.model.Category>,
    confirmedCount: Int,
    onConfirmItem: (String) -> Unit,
    onSkipItem: (String) -> Unit,
    onUpdateCategory: (String, String) -> Unit,
    onUpdateDescription: (String, String) -> Unit,
    onConfirmAll: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Progress header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$confirmedCount of ${items.size} confirmed",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    LinearProgressIndicator(
                        progress = { if (items.isEmpty()) 0f else confirmedCount.toFloat() / items.size },
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .padding(top = 8.dp),
                    )
                }
                TextButton(onClick = onConfirmAll) {
                    Text("Confirm All")
                }
            }
        }

        // List of items
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 80.dp) // Space for FAB
        ) {
            items(items, key = { it.parsedExpense.id }) { item ->
                ImportExpenseCard(
                    item = item,
                    categories = categories,
                    onConfirm = { onConfirmItem(item.parsedExpense.id) },
                    onSkip = { onSkipItem(item.parsedExpense.id) },
                    onCategoryChange = { categoryId ->
                        onUpdateCategory(item.parsedExpense.id, categoryId)
                    },
                    onDescriptionChange = { description ->
                        onUpdateDescription(item.parsedExpense.id, description)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImportExpenseCard(
    item: ImportExpenseItem,
    categories: List<com.goldhardt.core.data.model.Category>,
    onConfirm: () -> Unit,
    onSkip: () -> Unit,
    onCategoryChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit
) {
    val currency = NumberFormat.getCurrencyInstance()
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
    var expanded by remember { mutableStateOf(false) }
    var editingDescription by remember { mutableStateOf(false) }
    var descriptionText by remember { mutableStateOf(item.editedDescription) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                item.isConfirmed -> MaterialTheme.colorScheme.primaryContainer
                item.isSkipped -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Date and Amount row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.parsedExpense.date.format(dateFormatter),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = currency.format(item.parsedExpense.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            if (editingDescription) {
                OutlinedTextField(
                    value = descriptionText,
                    onValueChange = { descriptionText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Description") },
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = {
                            onDescriptionChange(descriptionText)
                            editingDescription = false
                        }) {
                            Icon(Icons.Default.Check, contentDescription = "Save")
                        }
                    }
                )
            } else {
                Text(
                    text = item.editedDescription,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Category selector
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = categories.find { it.id == item.selectedCategoryId }?.name ?: "Select Category",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text("${category.icon} ${category.name}") },
                            onClick = {
                                onCategoryChange(category.id)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!item.isConfirmed && !item.isSkipped) {
                    OutlinedButton(
                        onClick = onSkip,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Skip")
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Confirm")
                    }
                } else if (item.isConfirmed) {
                    AssistChip(
                        onClick = { onSkip() }, // Allow unconfirming by clicking
                        label = { Text("✓ Confirmed") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            labelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                } else {
                    AssistChip(
                        onClick = { onConfirm() }, // Allow re-confirming by clicking
                        label = { Text("Skipped") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }
    }
}
