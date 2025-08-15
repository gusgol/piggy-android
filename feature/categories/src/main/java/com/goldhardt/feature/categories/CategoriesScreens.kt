package com.goldhardt.feature.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goldhardt.core.data.model.Category
import com.goldhardt.designsystem.components.ConfigureTopBar
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    modifier: Modifier = Modifier,
    viewModel: CategoriesViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // Add Category state (moved up to be available in ConfigureTopBar actions)
    val isAddingCategory = remember { mutableStateOf(false) }
    val addSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ConfigureTopBar (
        title = context.getString(R.string.title_categories),
        actions = {
            IconButton(onClick = {
                // Open Add Category sheet
                isAddingCategory.value = true
            }) {
                Icon(imageVector = Icons.Outlined.Add, contentDescription = stringResource(R.string.action_add_category))
            }
        }
    )

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var editingCategory by remember { mutableStateOf<Category?>(null) }
    val editSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Surface(modifier = modifier.fillMaxSize()) {
        when {
            state.isLoading && state.categories.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.categories.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No categories yet")
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.categories, key = { it.id }) { category ->
                        CategoryCard(
                            category = category,
                            modifier = Modifier.clickable { editingCategory = category }
                        )
                    }
                }
            }
        }

        // Edit bottom sheet
        editingCategory?.let { current ->
            CategoryEditorBottomSheet(
                title = "Edit Category",
                sheetState = editSheetState,
                initialName = current.name,
                initialIcon = current.icon,
                initialColor = current.color,
                showDelete = true,
                onDismiss = { editingCategory = null },
                onSave = { name, icon, color ->
                    viewModel.updateCategory(current.copy(name = name.trim(), icon = icon, color = color))
                    editingCategory = null
                },
                onDelete = {
                    viewModel.deleteCategory(current.id)
                    editingCategory = null
                }
            )
        }

        // Add bottom sheet
        if (isAddingCategory.value) {
            CategoryEditorBottomSheet(
                title = "Add Category",
                sheetState = addSheetState,
                initialName = "",
                initialIcon = ICONS.firstOrNull() ?: "🏷️",
                initialColor = COLORS.firstOrNull() ?: "#FFADAD",
                showDelete = false,
                onDismiss = { isAddingCategory.value = false },
                onSave = { name, icon, color ->
                    viewModel.addCategory(name.trim(), icon, color)
                    isAddingCategory.value = false
                }
            )
        }
    }
}

@Composable
private fun CategoryCard(category: Category, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
    ) {
        CategoryRow(category = category, modifier = Modifier.padding(16.dp))
    }
}

@Composable
private fun CategoryRow(category: Category, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val bg = parseHexColorOrDefault(category.color, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(bg),
            contentAlignment = Alignment.Center
        ) {
            Text(text = category.icon, style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun CategoryEditorBottomSheet(
    title: String,
    sheetState: SheetState,
    initialName: String,
    initialIcon: String,
    initialColor: String,
    showDelete: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, icon: String, color: String) -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        var name by rememberSaveable(title) { mutableStateOf(initialName) }
        var selectedIcon by rememberSaveable(title) { mutableStateOf(initialIcon) }
        var selectedColor by rememberSaveable(title) { mutableStateOf(initialColor) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Preview
            Row(verticalAlignment = Alignment.CenterVertically) {
                val previewBg = parseHexColorOrDefault(selectedColor, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(previewBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = selectedIcon, style = MaterialTheme.typography.titleLarge)
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }

            // Name
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Icon Picker
            Text(text = "Icon", style = MaterialTheme.typography.titleMedium)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ICONS.forEach { emoji ->
                    val selected = emoji == selectedIcon
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .then(
                                if (selected) Modifier.border(
                                    2.dp,
                                    MaterialTheme.colorScheme.primary,
                                    CircleShape
                                )
                                else Modifier
                            )
                            .clickable { selectedIcon = emoji },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            // Color Picker
            Text(text = "Color", style = MaterialTheme.typography.titleMedium)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                COLORS.forEach { hex ->
                    val color = parseHexColorOrDefault(hex, MaterialTheme.colorScheme.primary)
                    val isSelected = hex.equals(selectedColor, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .then(
                                if (isSelected) Modifier.border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                ) else Modifier
                            )
                            .clickable { selectedColor = hex }
                    )
                }
            }

            // Actions (right-aligned)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                Button(
                    onClick = {
                        onSave(name.trim(), selectedIcon, selectedColor)
                    },
                    enabled = name.isNotBlank()
                ) { Text("Save") }
                if (showDelete) {
                    Button(
                        onClick = { onDelete?.invoke() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) { Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Delete") }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

private fun parseHexColorOrDefault(hex: String?, default: Color): Color {
    if (hex.isNullOrBlank()) return default
    return try {
        Color(hex.toColorInt())
    } catch (_: IllegalArgumentException) {
        default
    }
}

private val ICONS = listOf(
    "🏠", "🍔", "🛒", "🚗", "✈️", "🎬", "💊", "🎁", "👕", "💡", "📞", "🎓", "🐶", "🎉", "💼", "💸", "☕️", "🏋️‍♂️", "💅", "🚗", "🚌", "🧾", "💖", "✨", "🎉"
)

private val COLORS = listOf(
    "#FFADAD", "#FFD6A5", "#FDFFB6", "#CAFFBF", "#9BF6FF", "#A0C4FF", "#BDB2FF", "#FFC6FF",
    "#E7E7E7", "#FFB3BA", "#FFDFBA", "#FFFFBA", "#BAFFC9", "#BAE1FF", "#B5B9FF", "#FFB5E8",
    "#FF9AA2", "#FFB7B2", "#FFDAC1", "#E2F0CB", "#B5EAD7", "#C7CEEA", "#F3E4EA", "#F4C2C2",
    "#FFD1DC", "#FFABAB", "#FFC3A0", "#FFCCB6"
)
