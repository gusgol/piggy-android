package com.goldhardt.feature.expenses.ui

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.goldhardt.core.domain.model.Category
import com.goldhardt.designsystem.theme.PiggyColors
import com.goldhardt.designsystem.theme.PiggyShapeTokens
import com.goldhardt.designsystem.theme.PiggySpacing
import com.goldhardt.designsystem.theme.PiggyTheme
import com.goldhardt.designsystem.theme.PiggyTypography
import com.goldhardt.designsystem.theme.onTertiaryFixed
import com.goldhardt.designsystem.theme.piggyCardBorderColor
import com.goldhardt.designsystem.theme.piggyCardContainerColor
import com.goldhardt.designsystem.theme.piggySubtleContainerColor
import com.goldhardt.designsystem.theme.primaryFixed
import com.goldhardt.designsystem.theme.primaryLight
import com.goldhardt.designsystem.theme.rememberCategoryColorPalette
import com.goldhardt.feature.expenses.R
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val SheetTopShape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)

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
        shape = SheetTopShape,
        containerColor = piggyCardContainerColor(),
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
        dragHandle = null,
    ) {
        ExpenseEditorSheetContent(
            title = title,
            categories = categories,
            initialName = initialName,
            initialAmount = initialAmount,
            initialDate = initialDate,
            initialCategoryId = initialCategoryId,
            initialIsFixed = initialIsFixed,
            onDismiss = onDismiss,
            onSave = onSave,
            showDelete = showDelete,
            onDelete = onDelete,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun ExpenseEditorSheetContent(
    title: String,
    categories: List<Category>,
    initialName: String,
    initialAmount: String,
    initialDate: Instant,
    initialCategoryId: String?,
    initialIsFixed: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, amount: Double, date: Instant, categoryId: String, isFixed: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    showDelete: Boolean = false,
    onDelete: (() -> Unit)? = null,
) {
    var name by rememberSaveable(title, initialName) { mutableStateOf(initialName) }
    var amountText by rememberSaveable(title, initialAmount) { mutableStateOf(initialAmount) }
    var isFixed by rememberSaveable(title, initialIsFixed) { mutableStateOf(initialIsFixed) }
    var selectedCategoryId by rememberSaveable(title, initialCategoryId) {
        mutableStateOf(initialCategoryId ?: categories.firstOrNull()?.id)
    }
    var selectedDate by rememberSaveable(title, initialDate) { mutableStateOf(initialDate) }
    var showDatePicker by remember { mutableStateOf(false) }

    val effectiveSelectedCategoryId = selectedCategoryId ?: categories.firstOrNull()?.id
    val selectedCategory = categories.firstOrNull { it.id == effectiveSelectedCategoryId }

    val currencySymbol = remember {
        try {
            NumberFormat.getCurrencyInstance().currency?.symbol ?: "$"
        } catch (_: Throwable) {
            "$"
        }
    }
    val amountPlaceholder = remember {
        val separator = try {
            DecimalFormatSymbols.getInstance().decimalSeparator
        } catch (_: Throwable) {
            ','
        }
        "0${separator}00"
    }

    val parsedAmount = amountText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val isSaveEnabled = name.isNotBlank() && parsedAmount > 0.0 && !effectiveSelectedCategoryId.isNullOrBlank()
    val maxSheetHeight = (LocalConfiguration.current.screenHeightDp * 0.86f).dp

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
                .padding(horizontal = PiggySpacing.margin)
                .padding(top = PiggySpacing.spaceMd),
        ) {
            // Custom Sheet Drag Handle
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(48.dp)
                    .height(6.dp)
                    .clip(PiggyShapeTokens.Pill)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            )

            Spacer(modifier = Modifier.height(PiggySpacing.spaceMd))

            // Sheet Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(PiggySpacing.space2xs),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AddCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = title,
                            style = PiggyTypography.headlineMd.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.subtitle_add_expense),
                        style = PiggyTypography.bodySm,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                            contentDescription = stringResource(R.string.action_dismiss),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(PiggySpacing.spaceLg))

            // Scrollable Form Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = PiggySpacing.space2xl),
                verticalArrangement = Arrangement.spacedBy(PiggySpacing.spaceLg),
            ) {
                // 1. Hero Amount Card
                AmountHeroInputCard(
                    currencySymbol = currencySymbol,
                    amountText = amountText,
                    placeholder = amountPlaceholder,
                    onAmountChange = { new ->
                        amountText = new.filter { it.isDigit() || it == '.' || it == ',' }
                    },
                )

                // 2. Expense Name Input
                ExpenseNameField(
                    value = name,
                    onValueChange = { name = it },
                )

                // 3. Date Selector Field
                ExpenseDateField(
                    selectedDate = selectedDate,
                    onClick = { showDatePicker = true },
                )

                // 4. Category Chips Selector
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(PiggySpacing.spaceSm),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.label_category),
                            style = PiggyTypography.badgeLabel,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        if (selectedCategory != null) {
                            Text(
                                text = selectedCategory.name.uppercase(Locale.getDefault()),
                                style = PiggyTypography.labelCaps,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    if (categories.isEmpty()) {
                        Text(
                            text = stringResource(R.string.label_no_categories),
                            style = PiggyTypography.bodySm,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = PiggySpacing.space2xs),
                            horizontalArrangement = Arrangement.spacedBy(PiggySpacing.spaceSm),
                            verticalArrangement = Arrangement.spacedBy(PiggySpacing.spaceSm),
                        ) {
                            categories.forEach { category ->
                                val isSelected = category.id == effectiveSelectedCategoryId
                                CategoryChip(
                                    category = category,
                                    selected = isSelected,
                                    onClick = { selectedCategoryId = category.id },
                                )
                            }
                        }
                    }
                }

                // 5. Fixed Expense Toggle Card
                FixedExpenseToggleCard(
                    isFixed = isFixed,
                    onToggle = { isFixed = !isFixed },
                )

                // 6. Action Buttons
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = PiggySpacing.spaceSm),
                    verticalArrangement = Arrangement.spacedBy(PiggySpacing.spaceSm),
                ) {
                    Button(
                        onClick = {
                            val catId = effectiveSelectedCategoryId
                            if (name.isNotBlank() && parsedAmount > 0.0 && !catId.isNullOrBlank()) {
                                onSave(name.trim(), parsedAmount, selectedDate, catId, isFixed)
                            }
                        },
                        enabled = isSaveEnabled,
                        shape = PiggyShapeTokens.Pill,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 6.dp,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(PiggySpacing.spaceSm))
                        Text(
                            text = stringResource(R.string.btn_save_expense),
                            style = PiggyTypography.titleSm,
                        )
                    }

                    if (showDelete && onDelete != null) {
                        TextButton(
                            onClick = onDelete,
                            shape = PiggyShapeTokens.Pill,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(PiggySpacing.spaceXs))
                            Text(
                                text = stringResource(R.string.btn_delete),
                                style = PiggyTypography.badgeLabel,
                            )
                        }
                    }

                    TextButton(
                        onClick = onDismiss,
                        shape = PiggyShapeTokens.Pill,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.btn_cancel),
                            style = PiggyTypography.badgeLabel,
                        )
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = try {
                selectedDate.toEpochMilli()
            } catch (_: Throwable) {
                null
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            selectedDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                                .atStartOfDay(ZoneId.systemDefault())
                                .toInstant()
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun AmountHeroInputCard(
    currencySymbol: String,
    amountText: String,
    placeholder: String,
    onAmountChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) {
                focusRequester.requestFocus()
            },
        shape = PiggyShapeTokens.Lg,
        color = piggySubtleContainerColor(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PiggySpacing.spaceLg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.label_amount_caps),
                style = PiggyTypography.labelCaps,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(PiggySpacing.space2xs))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = currencySymbol,
                    style = PiggyTypography.headlineLg,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.alignByBaseline(),
                )
                Spacer(modifier = Modifier.width(PiggySpacing.spaceXs))
                BasicTextField(
                    value = amountText,
                    onValueChange = onAmountChange,
                    singleLine = true,
                    textStyle = PiggyTypography.displayCurrencyMobile.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next,
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .width(192.dp)
                        .focusRequester(focusRequester)
                        .alignByBaseline(),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (amountText.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    style = PiggyTypography.displayCurrencyMobile,
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                            innerTextField()
                        }
                    },
                )
            }

            Spacer(modifier = Modifier.height(PiggySpacing.space2xs))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PiggySpacing.space2xs),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_drive_file_rename),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = stringResource(R.string.label_instant_vault_sync),
                    style = PiggyTypography.labelCaps,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ExpenseNameField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val borderColor = if (isFocused) {
        MaterialTheme.colorScheme.primary
    } else {
        piggyCardBorderColor()
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(PiggySpacing.spaceXs),
    ) {
        Text(
            text = stringResource(R.string.label_expense_name),
            style = PiggyTypography.badgeLabel,
            color = MaterialTheme.colorScheme.onSurface,
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
            color = piggyCardContainerColor(),
            border = BorderStroke(1.dp, borderColor),
            shadowElevation = 1.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 52.dp)
                    .padding(horizontal = 14.dp, vertical = PiggySpacing.spaceMd),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_storefront),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
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
                                    text = stringResource(R.string.placeholder_expense_name),
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
            }
        }
    }
}

@Composable
private fun ExpenseDateField(
    selectedDate: Instant,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dateLabel = remember(selectedDate) {
        try {
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
                .withZone(ZoneId.systemDefault())
                .format(selectedDate)
        } catch (_: Throwable) {
            ""
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(PiggySpacing.spaceXs),
    ) {
        Text(
            text = stringResource(R.string.label_date),
            style = PiggyTypography.badgeLabel,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Surface(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = PiggyShapeTokens.Md,
            color = piggyCardContainerColor(),
            border = BorderStroke(1.dp, piggyCardBorderColor()),
            shadowElevation = 1.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 52.dp)
                    .padding(horizontal = 14.dp, vertical = PiggySpacing.spaceMd),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_calendar_today),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = dateLabel,
                    style = PiggyTypography.bodyMd,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    painter = painterResource(R.drawable.ic_calendar_today),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun CategoryChip(
    category: Category,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = rememberCategoryColorPalette(
        colorHex = category.color,
        categoryName = category.name,
    )
    val containerColor = if (selected) {
        primaryFixed
    } else {
        piggySubtleContainerColor()
    }
    val contentColor = if (selected) {
        onTertiaryFixed
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val dotColor = if (selected) {
        primaryLight
    } else {
        palette.dot
    }

    Surface(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        shape = PiggyShapeTokens.Pill,
        color = containerColor,
        contentColor = contentColor,
        shadowElevation = if (selected) 1.dp else 0.dp,
    ) {
        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = 36.dp)
                .padding(horizontal = PiggySpacing.spaceMd, vertical = PiggySpacing.spaceXs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PiggySpacing.spaceXs),
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(PiggyShapeTokens.Pill)
                    .background(dotColor)
            )
            if (category.icon.isNotBlank()) {
                Text(
                    text = category.icon,
                    style = PiggyTypography.bodySm,
                )
            }
            Text(
                text = category.name,
                style = PiggyTypography.badgeLabel.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun FixedExpenseToggleCard(
    isFixed: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val trackColor by animateColorAsState(
        targetValue = if (isFixed) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest
        },
        animationSpec = tween(durationMillis = 200),
        label = "fixed_toggle_track_color",
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (isFixed) 24.dp else 4.dp,
        animationSpec = tween(durationMillis = 200),
        label = "fixed_toggle_thumb_offset",
    )

    Surface(
        onClick = onToggle,
        modifier = modifier.fillMaxWidth(),
        shape = PiggyShapeTokens.Lg,
        color = piggySubtleContainerColor(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PiggySpacing.spaceMd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = PiggySpacing.spaceSm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PiggySpacing.spaceSm),
            ) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = PiggyShapeTokens.Pill,
                    color = piggyCardContainerColor(),
                    contentColor = MaterialTheme.colorScheme.primary,
                    shadowElevation = 1.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(R.drawable.ic_event_repeat),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                Column {
                    Text(
                        text = stringResource(R.string.label_fixed_expense),
                        style = PiggyTypography.titleSm,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.subtitle_fixed_expense),
                        style = PiggyTypography.bodySm,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Custom tactile pill switch matching Stitch specification
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(28.dp)
                    .clip(PiggyShapeTokens.Pill)
                    .background(trackColor)
                    .clickable(
                        role = Role.Switch,
                        onClick = onToggle,
                    ),
                contentAlignment = Alignment.CenterStart,
            ) {
                Surface(
                    modifier = Modifier
                        .offset(x = thumbOffset)
                        .size(20.dp),
                    shape = PiggyShapeTokens.Pill,
                    color = Color.White,
                    shadowElevation = 1.dp,
                ) {}
            }
        }
    }
}

private fun Color.toHexToken(): String =
    String.format(Locale.US, "#%06X", 0xFFFFFF and toArgb())

private val previewSheetCategories = listOf(
    Category("housing", "Housing", Instant.EPOCH, "u1", "🏠", PiggyColors.CategoryHousing.toHexToken()),
    Category("groceries", "Groceries", Instant.EPOCH, "u1", "🛒", PiggyColors.CategoryGroceries.toHexToken()),
    Category("dining", "Dining Out", Instant.EPOCH, "u1", "🍔", PiggyColors.CategoryDiningOut.toHexToken()),
    Category("transport", "Transport", Instant.EPOCH, "u1", "🚗", PiggyColors.CategoryTransport.toHexToken()),
    Category("entertainment", "Entertainment", Instant.EPOCH, "u1", "🎬", "#10B981"),
    Category("healthcare", "Healthcare", Instant.EPOCH, "u1", "💊", "#FB7185"),
    Category("utilities", "Utilities", Instant.EPOCH, "u1", "💡", PiggyColors.CategoryUtilities.toHexToken()),
    Category("coffee", "Coffee & Snacks", Instant.EPOCH, "u1", "☕", PiggyColors.CategoryCoffeeLeisure.toHexToken()),
)

@Preview(name = "Create Expense Form - Light", showBackground = true, heightDp = 860)
@Composable
private fun CreateExpenseFormLightPreview() {
    PiggyTheme {
        ExpenseEditorSheetContent(
            title = "New Expense",
            categories = previewSheetCategories,
            initialName = "Whole Foods Market",
            initialAmount = "45,00",
            initialDate = LocalDate.of(2026, 10, 24).atStartOfDay(ZoneId.systemDefault()).toInstant(),
            initialCategoryId = "groceries",
            initialIsFixed = false,
            onDismiss = {},
            onSave = { _, _, _, _, _ -> },
        )
    }
}

@Preview(
    name = "Create Expense Form - Dark",
    showBackground = true,
    heightDp = 860,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun CreateExpenseFormDarkPreview() {
    PiggyTheme(darkTheme = true) {
        ExpenseEditorSheetContent(
            title = "New Expense",
            categories = previewSheetCategories,
            initialName = "Whole Foods Market",
            initialAmount = "45,00",
            initialDate = LocalDate.of(2026, 10, 24).atStartOfDay(ZoneId.systemDefault()).toInstant(),
            initialCategoryId = "groceries",
            initialIsFixed = true,
            onDismiss = {},
            onSave = { _, _, _, _, _ -> },
        )
    }
}

@Preview(name = "Edit Expense Form - Light", showBackground = true, heightDp = 910)
@Composable
private fun EditExpenseFormLightPreview() {
    PiggyTheme {
        ExpenseEditorSheetContent(
            title = "Edit Expense",
            categories = previewSheetCategories,
            initialName = "Apartment Rent",
            initialAmount = "1450.00",
            initialDate = LocalDate.of(2026, 10, 1).atStartOfDay(ZoneId.systemDefault()).toInstant(),
            initialCategoryId = "housing",
            initialIsFixed = true,
            showDelete = true,
            onDelete = {},
            onDismiss = {},
            onSave = { _, _, _, _, _ -> },
        )
    }
}
