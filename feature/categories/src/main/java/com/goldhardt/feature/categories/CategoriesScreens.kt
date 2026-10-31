package com.goldhardt.feature.categories

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goldhardt.core.domain.model.Category
import com.goldhardt.designsystem.components.ConfigureTopBar
import com.goldhardt.designsystem.components.PiggyNavigationBarDefaults
import com.goldhardt.designsystem.theme.CategoryColorPalette
import com.goldhardt.designsystem.theme.PiggyColors
import com.goldhardt.designsystem.theme.PiggyShapeTokens
import com.goldhardt.designsystem.theme.PiggySpacing
import com.goldhardt.designsystem.theme.PiggyTheme
import com.goldhardt.designsystem.theme.PiggyTypography
import com.goldhardt.designsystem.theme.piggyCardBorderColor
import com.goldhardt.designsystem.theme.piggyCardContainerColor
import com.goldhardt.designsystem.theme.rememberCategoryColorPalette
import java.time.Instant
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    modifier: Modifier = Modifier,
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    var isAddingCategory by rememberSaveable { mutableStateOf(false) }

    ConfigureTopBar(
        title = stringResource(R.string.title_categories),
    )

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CategoriesScreenContent(
        state = state,
        modifier = modifier,
        isAddingCategory = isAddingCategory,
        onOpenAddCategory = { isAddingCategory = true },
        onDismissAddCategory = { isAddingCategory = false },
        onDismissError = viewModel::clearError,
        onAddCategory = { name, icon, color ->
            viewModel.addCategory(name.trim(), icon, color)
            isAddingCategory = false
        },
        onUpdateCategory = viewModel::updateCategory,
        onDeleteCategory = viewModel::deleteCategory,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CategoriesScreenContent(
    state: UiState,
    modifier: Modifier = Modifier,
    isAddingCategory: Boolean = false,
    onOpenAddCategory: () -> Unit = {},
    onDismissAddCategory: () -> Unit = {},
    onDismissError: () -> Unit = {},
    onAddCategory: (name: String, icon: String, color: String) -> Unit = { _, _, _ -> },
    onUpdateCategory: (Category) -> Unit = {},
    onDeleteCategory: (String) -> Unit = {},
) {
    val addSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    val editSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                state.isLoading && state.categories.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = PiggySpacing.margin,
                            end = PiggySpacing.margin,
                            top = PiggySpacing.spaceSm,
                            bottom = PiggyNavigationBarDefaults.FloatingBottomClearance + 88.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(PiggySpacing.spaceSm),
                    ) {
                        if (!state.error.isNullOrBlank()) {
                            item(key = "error_banner") {
                                ErrorBanner(
                                    message = state.error,
                                    onDismiss = onDismissError,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }

                        if (state.categories.isEmpty()) {
                            item(key = "empty_categories_state") {
                                CategoriesEmptyStateCard(
                                    title = stringResource(R.string.empty_categories_title),
                                    subtitle = stringResource(R.string.empty_categories_subtitle),
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        } else {
                            items(
                                items = state.categories,
                                key = { it.id },
                            ) { category ->
                                CategoryCard(
                                    category = category,
                                    onClick = { editingCategory = category },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = onOpenAddCategory,
                shape = PiggyShapeTokens.Pill,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 8.dp,
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(
                        end = PiggySpacing.margin,
                        bottom = PiggyNavigationBarDefaults.FloatingBottomClearance + PiggySpacing.spaceSm,
                    )
                    .size(56.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.action_add_category),
                    modifier = Modifier.size(28.dp),
                )
            }
        }

        // Edit bottom sheet
        editingCategory?.let { current ->
            CategoryEditorBottomSheet(
                title = stringResource(R.string.title_edit_category),
                sheetState = editSheetState,
                initialName = current.name,
                initialIcon = current.icon,
                initialColor = current.color,
                showDelete = true,
                onDismiss = { editingCategory = null },
                onSave = { name, icon, color ->
                    onUpdateCategory(current.copy(name = name.trim(), icon = icon, color = color))
                    editingCategory = null
                },
                onDelete = {
                    onDeleteCategory(current.id)
                    editingCategory = null
                }
            )
        }

        // Add bottom sheet
        if (isAddingCategory) {
            CategoryEditorBottomSheet(
                title = stringResource(R.string.title_add_category),
                sheetState = addSheetState,
                initialName = "",
                initialIcon = ICONS.firstOrNull() ?: "🏷️",
                initialColor = COLORS.firstOrNull() ?: "#FFADAD",
                showDelete = false,
                onDismiss = onDismissAddCategory,
                onSave = onAddCategory
            )
        }
    }
}

@Composable
private fun CategoryCard(
    category: Category,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val palette = rememberCategoryColorPalette(
        colorHex = category.color,
        categoryName = category.name,
    )

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = PiggyShapeTokens.Lg,
        color = piggyCardContainerColor(),
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, piggyCardBorderColor()),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PiggySpacing.spaceMd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryAvatar(
                name = category.name,
                iconEmoji = category.icon,
                palette = palette,
            )

            Spacer(modifier = Modifier.width(PiggySpacing.spaceMd))

            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = category.name,
                    style = PiggyTypography.titleSm.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Spacer(modifier = Modifier.width(PiggySpacing.spaceSm))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(PiggyShapeTokens.Pill)
                        .background(palette.dot)
                )
            }

            Spacer(modifier = Modifier.width(PiggySpacing.spaceSm))

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun CategoryAvatar(
    name: String,
    iconEmoji: String,
    palette: CategoryColorPalette,
) {
    Surface(
        modifier = Modifier.size(44.dp),
        shape = PiggyShapeTokens.Pill,
        color = palette.avatarContainer,
        contentColor = palette.avatarContent,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = iconEmoji.takeIf { it.isNotBlank() }
                    ?: (name.firstOrNull()?.uppercase() ?: "?"),
                style = PiggyTypography.headlineMd,
            )
        }
    }
}

@Composable
private fun CategoriesEmptyStateCard(
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
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.outline,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "🏷️",
                        style = PiggyTypography.headlineMd,
                    )
                }
            }
            Spacer(modifier = Modifier.height(PiggySpacing.spaceSm))
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
                .padding(
                    start = PiggySpacing.spaceLg,
                    end = PiggySpacing.spaceXs,
                    top = PiggySpacing.space2xs,
                    bottom = PiggySpacing.space2xs,
                ),
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
                    contentDescription = stringResource(R.string.action_dismiss_error),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
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

private fun Color.toHexToken(): String =
    String.format(Locale.US, "#%06X", 0xFFFFFF and toArgb())

private val previewCategories = listOf(
    Category(
        id = "1",
        name = "Housing",
        createdAt = Instant.EPOCH,
        userId = "u1",
        icon = "🏠",
        color = PiggyColors.CategoryHousing.toHexToken(),
    ),
    Category(
        id = "2",
        name = "Groceries",
        createdAt = Instant.EPOCH,
        userId = "u1",
        icon = "🛒",
        color = PiggyColors.CategoryGroceries.toHexToken(),
    ),
    Category(
        id = "3",
        name = "Dining Out",
        createdAt = Instant.EPOCH,
        userId = "u1",
        icon = "🍔",
        color = PiggyColors.CategoryDiningOut.toHexToken(),
    ),
    Category(
        id = "4",
        name = "Transportation",
        createdAt = Instant.EPOCH,
        userId = "u1",
        icon = "🚗",
        color = PiggyColors.CategoryTransport.toHexToken(),
    ),
    Category(
        id = "5",
        name = "Entertainment",
        createdAt = Instant.EPOCH,
        userId = "u1",
        icon = "🎬",
        color = "#6D7A72",
    ),
    Category(
        id = "6",
        name = "Healthcare",
        createdAt = Instant.EPOCH,
        userId = "u1",
        icon = "💊",
        color = PiggyColors.CategoryHousing.toHexToken(),
    ),
    Category(
        id = "7",
        name = "Utilities",
        createdAt = Instant.EPOCH,
        userId = "u1",
        icon = "💡",
        color = PiggyColors.CategoryUtilities.toHexToken(),
    ),
    Category(
        id = "8",
        name = "Coffee & Snacks",
        createdAt = Instant.EPOCH,
        userId = "u1",
        icon = "☕️",
        color = PiggyColors.CategoryCoffeeLeisure.toHexToken(),
    ),
)

@Preview(name = "Categories Screen - Light", showBackground = true, heightDp = 820)
@Composable
private fun CategoriesScreenPreview() {
    PiggyTheme {
        CategoriesScreenContent(
            state = UiState(
                isLoading = false,
                categories = previewCategories
            )
        )
    }
}

@Preview(
    name = "Categories Screen - Dark",
    showBackground = true,
    heightDp = 820,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun CategoriesScreenDarkPreview() {
    PiggyTheme(darkTheme = true) {
        CategoriesScreenContent(
            state = UiState(
                isLoading = false,
                categories = previewCategories
            )
        )
    }
}

@Preview(name = "Categories Screen - Empty", showBackground = true, heightDp = 640)
@Composable
private fun CategoriesScreenEmptyPreview() {
    PiggyTheme {
        CategoriesScreenContent(
            state = UiState(
                isLoading = false,
                categories = emptyList()
            )
        )
    }
}
