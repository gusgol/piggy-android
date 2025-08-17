package com.goldhardt.feature.expenses.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.goldhardt.core.data.model.Category
import com.goldhardt.feature.expenses.R
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseEditorBottomSheet(
    title: String,
    sheetState: SheetState,
    categories: List<Category>,
    initialName: String,
    initialAmount: String,
    initialDate: Instant,
    initialCategoryId: String?,
    initialIsFixed: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, amount: Double, date: Instant, categoryId: String, isFixed: Boolean) -> Unit,
    showDelete: Boolean = false,
    onDelete: (() -> Unit)? = null,
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
                    val filtered = new.filter { it.isDigit() || it == '.' || it == ',' }
                    amountText = filtered
                },
                label = { Text(stringResource(R.string.label_amount)) },
                leadingIcon = { Text(currencySymbol) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

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
                            Icon(imageVector = Icons.Outlined.Add, contentDescription = null, modifier = Modifier.rotate(if (expanded) 45f else 0f))
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

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = stringResource(R.string.label_fixed), modifier = Modifier.weight(1f))
                Switch(checked = isFixed, onCheckedChange = { isFixed = it })
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                if (showDelete) {
                    Button(
                        onClick = { onDelete?.invoke() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) { Text(stringResource(R.string.btn_delete)) }
                }
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
                val millis = datePickerState.selectedDateMillis
                if (millis != null) {
                    selectedDate = Instant.ofEpochMilli(millis)
                }
            }
        }
    }
}
