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
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goldhardt.core.data.model.Category
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    modifier: Modifier = Modifier,
    viewModel: CategoriesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var editingCategory by remember { mutableStateOf<Category?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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

        editingCategory?.let { current ->
            EditCategoryBottomSheet(
                category = current,
                sheetState = sheetState,
                onDismiss = { editingCategory = null },
                onSave = { updated ->
                    viewModel.updateCategory(updated)
                    editingCategory = null
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
private fun EditCategoryBottomSheet(
    category: Category,
    sheetState: androidx.compose.material3.SheetState,
    onDismiss: () -> Unit,
    onSave: (Category) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        var name by rememberSaveable(category.id) { mutableStateOf(category.name) }
        var selectedIcon by rememberSaveable(category.id) { mutableStateOf(category.icon) }
        var selectedColor by rememberSaveable(category.id) { mutableStateOf(category.color) }

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
                    text = "Edit Category",
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
                                if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
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
                        val updated = category.copy(
                            name = name.trim(),
                            icon = selectedIcon,
                            color = selectedColor
                        )
                        onSave(updated)
                    },
                    enabled = name.isNotBlank()
                ) { Text("Save") }
                Button(
                    onClick = { /* TODO: hook up delete */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) { Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Delete") }
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
