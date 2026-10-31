package com.goldhardt.feature.categories

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.goldhardt.designsystem.theme.isPiggyDarkTheme
import com.goldhardt.designsystem.theme.piggyCardBorderColor
import com.goldhardt.designsystem.theme.piggyCardContainerColor
import com.goldhardt.designsystem.theme.piggySubtleContainerColor
import com.goldhardt.designsystem.theme.primaryFixed
import com.goldhardt.designsystem.theme.rememberCategoryColorPalette
import java.time.Instant
import java.util.Locale

private val SheetTopShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

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
                subtitle = stringResource(R.string.subtitle_edit_category),
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
                subtitle = stringResource(R.string.subtitle_add_category),
                sheetState = addSheetState,
                initialName = "",
                initialIcon = "",
                initialColor = "",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryEditorBottomSheet(
    title: String,
    subtitle: String,
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
        shape = SheetTopShape,
        containerColor = piggyCardContainerColor(),
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
        dragHandle = null,
    ) {
        CategoryEditorSheetContent(
            title = title,
            subtitle = subtitle,
            initialName = initialName,
            initialIcon = initialIcon,
            initialColor = initialColor,
            showDelete = showDelete,
            onDismiss = onDismiss,
            onSave = onSave,
            onDelete = onDelete,
        )
    }
}

@Composable
internal fun CategoryEditorSheetContent(
    title: String,
    subtitle: String,
    initialName: String,
    initialIcon: String,
    initialColor: String,
    showDelete: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, icon: String, color: String) -> Unit,
    modifier: Modifier = Modifier,
    onDelete: (() -> Unit)? = null,
) {
    var name by rememberSaveable(title, initialName) { mutableStateOf(initialName) }
    var selectedIcon by rememberSaveable(title, initialIcon) { mutableStateOf(initialIcon) }
    var selectedColor by rememberSaveable(title, initialColor) { mutableStateOf(initialColor) }

    val effectiveIcon = selectedIcon.ifBlank { ICONS.firstOrNull() ?: "🏷️" }
    val effectiveColor = selectedColor.ifBlank { COLORS.firstOrNull() ?: "#FFADAD" }
    val isSaveEnabled = name.isNotBlank()
    val maxSheetHeight = (LocalConfiguration.current.screenHeightDp * 0.88f).dp

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = maxSheetHeight),
        shape = SheetTopShape,
        color = piggyCardContainerColor(),
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(top = PiggySpacing.spaceMd),
        ) {
            // Drag Indicator Handle
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(48.dp)
                    .height(6.dp)
                    .clip(PiggyShapeTokens.Pill)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            )

            Spacer(modifier = Modifier.height(PiggySpacing.spaceXs))

            // Sheet Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PiggySpacing.spaceXl)
                    .padding(top = PiggySpacing.space2xs, bottom = PiggySpacing.spaceMd),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = PiggyTypography.headlineMd.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = PiggyTypography.bodySm,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }

                Spacer(modifier = Modifier.width(PiggySpacing.spaceSm))

                Surface(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp),
                    shape = PiggyShapeTokens.Pill,
                    color = piggySubtleContainerColor(),
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = stringResource(R.string.action_dismiss_sheet),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }

            // Scrollable Form Body
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = PiggySpacing.spaceXl)
                    .padding(bottom = PiggySpacing.space2xl),
                verticalArrangement = Arrangement.spacedBy(PiggySpacing.spaceXl),
            ) {
                // 1. Live Interactive Preview Card
                CategoryLivePreviewCard(
                    name = name,
                    selectedIcon = selectedIcon,
                    selectedColor = selectedColor,
                )

                // 2. Category Name Input
                CategoryNameField(
                    value = name,
                    onValueChange = { if (it.length <= 30) name = it },
                    onClear = { name = "" },
                )

                // 3. Curated Palette Swatches
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(PiggySpacing.spaceXs),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.label_theme_tone_caps),
                            style = PiggyTypography.labelCaps,
                            color = MaterialTheme.colorScheme.outline,
                        )
                        Text(
                            text = if (selectedColor.isNotBlank()) {
                                selectedColor.uppercase(Locale.US)
                            } else {
                                stringResource(R.string.label_select_tone)
                            },
                            style = PiggyTypography.bodySm.copy(fontWeight = FontWeight.Medium),
                            color = if (selectedColor.isNotBlank()) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = PiggySpacing.space2xs),
                        verticalArrangement = Arrangement.spacedBy(PiggySpacing.spaceSm),
                    ) {
                        COLORS.chunked(7).forEach { rowColors ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(PiggySpacing.spaceSm),
                            ) {
                                rowColors.forEach { hex ->
                                    val swatchColor = parseHexColorOrDefault(hex, MaterialTheme.colorScheme.primary)
                                    val isSelected = hex.equals(selectedColor, ignoreCase = true)
                                    val checkTint = if (swatchColor.luminance() > 0.5f) {
                                        PiggyColors.TextStrong
                                    } else {
                                        Color.White
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(40.dp)
                                            .clip(PiggyShapeTokens.Md)
                                            .background(swatchColor)
                                            .then(
                                                if (isSelected) {
                                                    Modifier.border(
                                                        width = 2.dp,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        shape = PiggyShapeTokens.Md,
                                                    )
                                                } else {
                                                    Modifier
                                                }
                                            )
                                            .clickable { selectedColor = hex },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Outlined.Check,
                                                contentDescription = null,
                                                tint = checkTint,
                                                modifier = Modifier.size(18.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Symbol / Icon Picker Grid
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(PiggySpacing.spaceXs),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.label_symbol_caps),
                            style = PiggyTypography.labelCaps,
                            color = MaterialTheme.colorScheme.outline,
                        )
                        Text(
                            text = stringResource(R.string.label_tap_to_choose),
                            style = PiggyTypography.bodySm,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = PiggySpacing.space2xs),
                        verticalArrangement = Arrangement.spacedBy(PiggySpacing.spaceSm),
                    ) {
                        val columns = 7
                        ICONS.chunked(columns).forEach { rowIcons ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                rowIcons.forEach { emoji ->
                                    val isSelected = emoji == selectedIcon
                                    val isDark = isPiggyDarkTheme()
                                    val containerBg = when {
                                        isSelected && isDark -> MaterialTheme.colorScheme.primaryContainer
                                        isSelected -> primaryFixed
                                        else -> piggySubtleContainerColor()
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(containerBg)
                                            .then(
                                                if (isSelected) {
                                                    Modifier.border(
                                                        width = 2.dp,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        shape = CircleShape,
                                                    )
                                                } else {
                                                    Modifier
                                                }
                                            )
                                            .clickable { selectedIcon = emoji },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = emoji,
                                            style = PiggyTypography.headlineMd,
                                        )
                                    }
                                }
                                repeat(columns - rowIcons.size) {
                                    Spacer(modifier = Modifier.size(44.dp))
                                }
                            }
                        }
                    }
                }

                // 5. Modal Footer Action Stack
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = PiggySpacing.spaceSm),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(PiggySpacing.spaceMd),
                ) {
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(name.trim(), effectiveIcon, effectiveColor)
                            }
                        },
                        enabled = isSaveEnabled,
                        shape = PiggyShapeTokens.Md,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 2.dp,
                            pressedElevation = 4.dp,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                    ) {
                        Icon(
                            imageVector = if (showDelete) Icons.Outlined.Check else Icons.Outlined.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(PiggySpacing.spaceXs))
                        Text(
                            text = if (showDelete) {
                                stringResource(R.string.btn_save_changes)
                            } else {
                                stringResource(R.string.btn_create_category)
                            },
                            style = PiggyTypography.titleSm,
                        )
                    }

                    if (showDelete && onDelete != null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            TextButton(
                                onClick = onDelete,
                                shape = PiggyShapeTokens.Default,
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error,
                                ),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(PiggySpacing.spaceXs))
                                Text(
                                    text = stringResource(R.string.btn_delete_category),
                                    style = PiggyTypography.titleSm.copy(fontSize = 15.sp),
                                )
                            }
                            Text(
                                text = stringResource(R.string.delete_category_helper),
                                style = PiggyTypography.bodySm.copy(fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.outline,
                                textAlign = TextAlign.Center,
                            )
                        }
                    } else {
                        TextButton(
                            onClick = onDismiss,
                            shape = PiggyShapeTokens.Default,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.outline,
                            ),
                        ) {
                            Text(
                                text = stringResource(R.string.btn_cancel_category),
                                style = PiggyTypography.titleSm.copy(fontSize = 15.sp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryLivePreviewCard(
    name: String,
    selectedIcon: String,
    selectedColor: String,
    modifier: Modifier = Modifier,
) {
    val hasColor = selectedColor.isNotBlank()
    val hasName = name.isNotBlank()
    val hasIcon = selectedIcon.isNotBlank()

    val palette = rememberCategoryColorPalette(
        colorHex = selectedColor.ifBlank { null },
        categoryName = name.ifBlank { null },
    )

    val avatarBg = if (hasColor) {
        palette.avatarContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val avatarContentColor = if (hasColor) {
        palette.avatarContent
    } else {
        MaterialTheme.colorScheme.outline
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = PiggyShapeTokens.Md,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PiggySpacing.spaceLg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PiggySpacing.spaceMd),
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = PiggyShapeTokens.Pill,
                color = avatarBg,
                contentColor = avatarContentColor,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (hasIcon) selectedIcon else "🏷️",
                        style = PiggyTypography.headlineMd,
                    )
                }
            }

            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (hasName) {
                        name.trim()
                    } else {
                        stringResource(R.string.placeholder_preview_category_name)
                    },
                    style = PiggyTypography.titleSm.copy(
                        fontWeight = if (hasName) FontWeight.Bold else FontWeight.SemiBold,
                    ),
                    color = if (hasName) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (hasColor || hasName) {
                    Spacer(modifier = Modifier.width(PiggySpacing.spaceSm))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(PiggyShapeTokens.Pill)
                            .background(palette.dot)
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryNameField(
    value: String,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val containerColor = if (isFocused) {
        piggyCardContainerColor()
    } else {
        piggySubtleContainerColor()
    }
    val borderColor = if (isFocused) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.Transparent
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(PiggySpacing.spaceXs),
    ) {
        Text(
            text = stringResource(R.string.label_category_name_caps),
            style = PiggyTypography.labelCaps,
            color = MaterialTheme.colorScheme.outline,
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    focusRequester.requestFocus()
                },
            shape = PiggyShapeTokens.Md,
            color = containerColor,
            border = BorderStroke(1.dp, borderColor),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 50.dp)
                    .padding(horizontal = PiggySpacing.spaceMd, vertical = PiggySpacing.spaceMd),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = PiggyTypography.bodyMd.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done,
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                        .onFocusChanged { isFocused = it.isFocused },
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (value.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.placeholder_category_name),
                                    style = PiggyTypography.bodyMd,
                                    color = MaterialTheme.colorScheme.outline,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            innerTextField()
                        }
                    },
                )

                if (value.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(PiggySpacing.spaceSm))
                    Surface(
                        onClick = onClear,
                        modifier = Modifier.size(24.dp),
                        shape = PiggyShapeTokens.Pill,
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = stringResource(R.string.action_clear_name),
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                }
            }
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

@Preview(name = "Edit Category Sheet - Light", showBackground = true)
@Composable
private fun EditCategorySheetPreview() {
    PiggyTheme {
        CategoryEditorSheetContent(
            title = "Edit Category",
            subtitle = "Customize your category badge, icon, and tone",
            initialName = "Coffee & Snacks",
            initialIcon = "☕️",
            initialColor = "#FFADAD",
            showDelete = true,
            onDismiss = {},
            onSave = { _, _, _ -> },
            onDelete = {},
        )
    }
}

@Preview(name = "New Category Sheet (Blank State) - Light", showBackground = true)
@Composable
private fun NewCategorySheetPreview() {
    PiggyTheme {
        CategoryEditorSheetContent(
            title = "New Category",
            subtitle = "Create a new custom category for your expenses",
            initialName = "",
            initialIcon = "",
            initialColor = "",
            showDelete = false,
            onDismiss = {},
            onSave = { _, _, _ -> },
        )
    }
}
