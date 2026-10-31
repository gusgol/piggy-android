package com.goldhardt.feature.expenses.importexpenses

import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.goldhardt.core.domain.model.Category
import com.goldhardt.designsystem.R as DesignSystemR
import com.goldhardt.designsystem.components.ConfigureTopBar
import com.goldhardt.designsystem.theme.CategoryColorPalette
import com.goldhardt.designsystem.theme.PiggyColors
import com.goldhardt.designsystem.theme.PiggyShapeTokens
import com.goldhardt.designsystem.theme.PiggySpacing
import com.goldhardt.designsystem.theme.PiggyTheme
import com.goldhardt.designsystem.theme.PiggyTypography
import com.goldhardt.designsystem.theme.isPiggyDarkTheme
import com.goldhardt.designsystem.theme.onPrimaryFixed
import com.goldhardt.designsystem.theme.onSecondaryFixed
import com.goldhardt.designsystem.theme.onTertiaryFixed
import com.goldhardt.designsystem.theme.piggyCardBorderColor
import com.goldhardt.designsystem.theme.piggyCardContainerColor
import com.goldhardt.designsystem.theme.piggySubtleContainerColor
import com.goldhardt.designsystem.theme.primaryFixed
import com.goldhardt.designsystem.theme.rememberCategoryColorPalette
import com.goldhardt.designsystem.theme.secondaryFixed
import com.goldhardt.designsystem.theme.tertiaryFixed
import com.goldhardt.feature.expenses.R
import com.goldhardt.feature.expenses.importexpenses.domain.ImportedExpense
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportExpensesScreen(
    viewModel: ImportViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToTrends: (() -> Unit)? = null,
) {
    val context = LocalContext.current.applicationContext
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val topBarTitle = when (state.importStep) {
        ImportStep.ASSIGN_CATEGORIES -> stringResource(R.string.title_import_review)
        ImportStep.SELECT_FILE,
        ImportStep.COMPLETED -> stringResource(R.string.title_import_expenses)
    }
    val backDescription = stringResource(R.string.action_navigate_back)

    ConfigureTopBar(
        title = topBarTitle,
        navigationIcon = {
            IconButton(
                onClick = {
                    if (state.importStep == ImportStep.ASSIGN_CATEGORIES) {
                        viewModel.reset()
                    } else {
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(piggySubtleContainerColor())
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = backDescription,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp),
                )
            }
        },
    )

    ImportExpensesScreenContent(
        state = state,
        onFileSelected = { uri ->
            viewModel.parseCsvFile {
                context.contentResolver.openInputStream(uri)
            }
        },
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
        onDismissError = viewModel::clearError,
        onDone = onNavigateBack,
        onImportMore = { viewModel.reset() },
        onNavigateToTrends = onNavigateToTrends,
    )
}

@Composable
internal fun ImportExpensesScreenContent(
    state: ImportUiState,
    modifier: Modifier = Modifier,
    onFileSelected: (Uri) -> Unit = {},
    onCategorySelected: (Int, String) -> Unit = { _, _ -> },
    onIsFixedChanged: (Int, Boolean) -> Unit = { _, _ -> },
    onBulkAssignCategory: (String) -> Unit = {},
    onDeleteExpense: (Int) -> Unit = {},
    onImport: () -> Unit = {},
    onDismissError: () -> Unit = {},
    onDone: () -> Unit = {},
    onImportMore: () -> Unit = {},
    onNavigateToTrends: (() -> Unit)? = null,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        when (state.importStep) {
            ImportStep.SELECT_FILE -> {
                SelectFileStep(
                    onFileSelected = onFileSelected,
                    isLoading = state.isLoading,
                    error = state.error,
                    onDismissError = onDismissError,
                )
            }
            ImportStep.ASSIGN_CATEGORIES -> {
                AssignCategoriesStep(
                    expenses = state.expenses,
                    categories = state.categories,
                    onCategorySelected = onCategorySelected,
                    onIsFixedChanged = onIsFixedChanged,
                    onBulkAssignCategory = onBulkAssignCategory,
                    onDeleteExpense = onDeleteExpense,
                    onImport = onImport,
                    isImporting = state.isImporting,
                    importProgress = state.importProgress,
                    error = state.error,
                    onDismissError = onDismissError,
                )
            }
            ImportStep.COMPLETED -> {
                CompletedStep(
                    importedCount = state.importedCount,
                    expenses = state.expenses,
                    categories = state.categories,
                    error = state.error,
                    onDismissError = onDismissError,
                    onDone = onDone,
                    onImportMore = onImportMore,
                    onNavigateToTrends = onNavigateToTrends,
                )
            }
        }
    }
}

// ============================================================================
// Step 1: Select File
// ============================================================================

@Composable
private fun SelectFileStep(
    onFileSelected: (Uri) -> Unit,
    isLoading: Boolean,
    error: String?,
    onDismissError: () -> Unit,
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onFileSelected(it) }
    }

    val isDark = isPiggyDarkTheme()
    val badgeBg = if (isDark) MaterialTheme.colorScheme.primaryContainer else primaryFixed
    val badgeContent = if (isDark) MaterialTheme.colorScheme.onPrimaryContainer else onPrimaryFixed

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(
                horizontal = PiggySpacing.margin,
                vertical = PiggySpacing.spaceSm,
            ),
        verticalArrangement = Arrangement.spacedBy(PiggySpacing.spaceXl),
    ) {
        // Friendly Welcoming Header Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = PiggySpacing.space2xs),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PiggySpacing.spaceXs),
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(badgeBg),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_savings),
                        contentDescription = null,
                        tint = badgeContent,
                        modifier = Modifier.size(15.dp),
                    )
                }
                Text(
                    text = stringResource(R.string.import_smart_ledger_sync_caps),
                    style = PiggyTypography.labelCaps,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(PiggySpacing.spaceXs))

            Text(
                text = stringResource(R.string.import_select_title),
                style = PiggyTypography.headlineLg,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(PiggySpacing.space2xs))

            Text(
                text = stringResource(R.string.import_select_subtitle),
                style = PiggyTypography.bodyMd,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (!error.isNullOrBlank()) {
            ImportErrorBanner(
                message = error,
                onDismiss = onDismissError,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Interactive Tap Upload Zone Card
        UploadDropzoneCard(
            isLoading = isLoading,
            onBrowseFiles = {
                if (!isLoading) {
                    launcher.launch("text/*")
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )

        // Expected Column Format Guidelines Card
        ExpectedColumnFormatCard(
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(PiggySpacing.spaceLg))
    }
}

@Composable
private fun UploadDropzoneCard(
    isLoading: Boolean,
    onBrowseFiles: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = isPiggyDarkTheme()
    val topRightGlow = if (isDark) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
    } else {
        primaryFixed.copy(alpha = 0.32f)
    }
    val bottomLeftGlow = if (isDark) {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.20f)
    } else {
        secondaryFixed.copy(alpha = 0.30f)
    }
    val plusBadgeBg = if (isDark) MaterialTheme.colorScheme.primaryContainer else primaryFixed
    val plusBadgeContent = if (isDark) MaterialTheme.colorScheme.onPrimaryContainer else onPrimaryFixed

    Surface(
        onClick = onBrowseFiles,
        enabled = !isLoading,
        modifier = modifier,
        shape = PiggyShapeTokens.Card,
        color = piggySubtleContainerColor(),
        border = BorderStroke(1.dp, piggyCardBorderColor()),
        shadowElevation = 1.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawWithCache {
                    val topGlow = Brush.radialGradient(
                        colors = listOf(topRightGlow, Color.Transparent),
                        center = Offset(size.width, 0f),
                        radius = size.minDimension * 0.75f,
                    )
                    val bottomGlow = Brush.radialGradient(
                        colors = listOf(bottomLeftGlow, Color.Transparent),
                        center = Offset(0f, size.height),
                        radius = size.minDimension * 0.75f,
                    )
                    onDrawBehind {
                        drawRect(brush = topGlow)
                        drawRect(brush = bottomGlow)
                    }
                }
                .padding(PiggySpacing.space2xl),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Piggy Emerald Icon Capsule with + badge
                Box(
                    modifier = Modifier.padding(bottom = PiggySpacing.spaceMd),
                    contentAlignment = Alignment.Center,
                ) {
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shadowElevation = 4.dp,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(R.drawable.ic_upload),
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 4.dp, y = 4.dp)
                            .size(24.dp),
                        shape = CircleShape,
                        color = plusBadgeBg,
                        contentColor = plusBadgeContent,
                        shadowElevation = 2.dp,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                }

                Text(
                    text = stringResource(R.string.import_dropzone_title),
                    style = PiggyTypography.titleSm,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(PiggySpacing.space2xs))

                Text(
                    text = stringResource(R.string.import_dropzone_subtitle),
                    style = PiggyTypography.bodySm,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = PiggySpacing.spaceLg),
                )

                Spacer(modifier = Modifier.height(PiggySpacing.spaceLg))

                if (isLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(PiggySpacing.spaceSm),
                        modifier = Modifier.padding(vertical = PiggySpacing.spaceXs),
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = stringResource(R.string.import_analyzing_csv),
                            style = PiggyTypography.titleSm,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                } else {
                    Button(
                        onClick = onBrowseFiles,
                        shape = PiggyShapeTokens.Pill,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        contentPadding = PaddingValues(
                            horizontal = PiggySpacing.space2xl,
                            vertical = PiggySpacing.spaceSm,
                        ),
                        modifier = Modifier.height(44.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_attach_file),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(PiggySpacing.spaceXs))
                        Text(
                            text = stringResource(R.string.import_btn_browse_files),
                            style = PiggyTypography.titleSm,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(PiggySpacing.spaceMd))

                Text(
                    text = stringResource(R.string.import_file_limit_hint),
                    style = PiggyTypography.badgeLabel,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun ExpectedColumnFormatCard(
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
                .padding(PiggySpacing.spaceXl),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(PiggySpacing.spaceXs),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_rule),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = stringResource(R.string.import_format_card_title),
                        style = PiggyTypography.titleSm,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Surface(
                    shape = PiggyShapeTokens.Pill,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ) {
                    Text(
                        text = stringResource(R.string.import_format_badge_required),
                        style = PiggyTypography.badgeLabel,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(PiggySpacing.spaceMd))

            Text(
                text = stringResource(R.string.import_format_card_subtitle),
                style = PiggyTypography.bodySm,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(PiggySpacing.spaceMd))

            Column(
                verticalArrangement = Arrangement.spacedBy(PiggySpacing.spaceSm),
            ) {
                FormatColumnSpecRow(
                    columnName = stringResource(R.string.import_col_date),
                    specLabel = stringResource(R.string.import_col_date_spec),
                )
                FormatColumnSpecRow(
                    columnName = stringResource(R.string.import_col_description),
                    specLabel = stringResource(R.string.import_col_description_spec),
                )
                FormatColumnSpecRow(
                    columnName = stringResource(R.string.import_col_amount),
                    specLabel = stringResource(R.string.import_col_amount_spec),
                )
            }
        }
    }
}

@Composable
private fun FormatColumnSpecRow(
    columnName: String,
    specLabel: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = PiggyShapeTokens.Md,
        color = piggySubtleContainerColor(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PiggySpacing.spaceMd, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PiggySpacing.spaceXs),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Text(
                    text = columnName,
                    style = PiggyTypography.titleSm,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Surface(
                shape = PiggyShapeTokens.Default,
                color = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ) {
                Text(
                    text = specLabel,
                    style = PiggyTypography.badgeLabel,
                    modifier = Modifier.padding(horizontal = PiggySpacing.spaceSm, vertical = 2.dp),
                )
            }
        }
    }
}

// ============================================================================
// Step 2: Review & Assign Categories
// ============================================================================

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
    error: String?,
    onDismissError: () -> Unit,
) {
    val unassignedCount = expenses.count { it.categoryId == null }
    val totalAmount = expenses.sumOf { it.amount }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(
                start = PiggySpacing.margin,
                end = PiggySpacing.margin,
                top = PiggySpacing.spaceXs,
                bottom = PiggySpacing.space2xl,
            ),
            verticalArrangement = Arrangement.spacedBy(PiggySpacing.spaceMd),
        ) {
            // Progress Flow Ribbon
            item(key = "progress_ribbon") {
                ReviewProgressRibbon(
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (!error.isNullOrBlank()) {
                item(key = "error_banner") {
                    ImportErrorBanner(
                        message = error,
                        onDismiss = onDismissError,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // Top Summary Hero Card ("Staged Batch" & "Queue")
            item(key = "summary_hero") {
                StagedBatchSummaryCard(
                    totalAmount = totalAmount,
                    itemCount = expenses.size,
                    unassignedCount = unassignedCount,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // Bulk Assign Category Quick Action
            if (unassignedCount > 0 && categories.isNotEmpty()) {
                item(key = "bulk_assign") {
                    BulkAssignSection(
                        categories = categories,
                        onBulkAssign = onBulkAssignCategory,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // Staged Expenses Section Header
            item(key = "staged_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = PiggySpacing.space2xs,
                            start = PiggySpacing.space2xs,
                            end = PiggySpacing.space2xs,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(PiggySpacing.spaceXs),
                    ) {
                        Text(
                            text = stringResource(R.string.import_label_staged_expenses_caps),
                            style = PiggyTypography.labelCaps,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Surface(
                            shape = PiggyShapeTokens.Sm,
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ) {
                            Text(
                                text = expenses.size.toString(),
                                style = PiggyTypography.badgeLabel.copy(fontFeatureSettings = "tnum"),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                }
            }

            // Expense List Cards
            itemsIndexed(
                items = expenses,
                key = { index, item -> "${index}_${item.originalLine}" },
            ) { index, expense ->
                ExpenseImportItem(
                    expense = expense,
                    categories = categories,
                    onCategorySelected = { categoryId ->
                        onCategorySelected(index, categoryId)
                    },
                    onIsFixedChanged = { isFixed ->
                        onIsFixedChanged(index, isFixed)
                    },
                    onDelete = { onDeleteExpense(index) },
                )
            }
        }

        // Sticky Bottom Floating Action Container
        ImportStickyBottomAction(
            expenseCount = expenses.size,
            enabled = unassignedCount == 0 && expenses.isNotEmpty(),
            isImporting = isImporting,
            importProgress = importProgress,
            onImport = onImport,
        )
    }
}

@Composable
private fun ReviewProgressRibbon(
    modifier: Modifier = Modifier,
) {
    val isDark = isPiggyDarkTheme()
    val pillBg = if (isDark) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
    } else {
        primaryFixed.copy(alpha = 0.45f)
    }

    Row(
        modifier = modifier.padding(vertical = PiggySpacing.space2xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(PiggySpacing.spaceXs),
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "2",
                    style = PiggyTypography.badgeLabel,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
            Text(
                text = stringResource(R.string.import_step_review_stage),
                style = PiggyTypography.titleSm,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Surface(
            shape = PiggyShapeTokens.Pill,
            color = pillBg,
            contentColor = MaterialTheme.colorScheme.primary,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PiggySpacing.spaceXs),
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Text(
                    text = stringResource(R.string.import_badge_pending_commit),
                    style = PiggyTypography.badgeLabel,
                )
            }
        }
    }
}

@Composable
private fun StagedBatchSummaryCard(
    totalAmount: Double,
    itemCount: Int,
    unassignedCount: Int,
    modifier: Modifier = Modifier,
) {
    val isDark = isPiggyDarkTheme()
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance() }
    val topRightGlow = if (isDark) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f)
    } else {
        primaryFixed.copy(alpha = 0.30f)
    }
    val bottomLeftGlow = if (isDark) {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.18f)
    } else {
        secondaryFixed.copy(alpha = 0.28f)
    }
    val borderColor = if (isDark) {
        piggyCardBorderColor()
    } else {
        PiggyColors.Emerald500.copy(alpha = 0.16f)
    }

    Surface(
        modifier = modifier,
        shape = PiggyShapeTokens.Card,
        color = piggyCardContainerColor(),
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = 2.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawWithCache {
                    val topGlow = Brush.radialGradient(
                        colors = listOf(topRightGlow, Color.Transparent),
                        center = Offset(size.width, 0f),
                        radius = size.minDimension * 0.85f,
                    )
                    val bottomGlow = Brush.radialGradient(
                        colors = listOf(bottomLeftGlow, Color.Transparent),
                        center = Offset(0f, size.height),
                        radius = size.minDimension * 0.75f,
                    )
                    onDrawBehind {
                        drawRect(brush = topGlow)
                        drawRect(brush = bottomGlow)
                    }
                }
                .padding(PiggySpacing.spaceXl)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(PiggySpacing.spaceMd),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.import_label_staged_batch_caps),
                            style = PiggyTypography.labelCaps,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(PiggySpacing.space2xs))
                        Text(
                            text = currencyFormatter.format(totalAmount),
                            style = PiggyTypography.displayCurrencyMobile,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(R.string.import_label_queue_caps),
                            style = PiggyTypography.labelCaps,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(PiggySpacing.space2xs))
                        Text(
                            text = pluralStringResource(
                                R.plurals.import_queue_items_count,
                                itemCount,
                                itemCount,
                            ),
                            style = PiggyTypography.headlineMd,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                // Alert or Ready Helper Banner
                if (unassignedCount > 0) {
                    val alertBg = if (isDark) {
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                    } else {
                        secondaryFixed.copy(alpha = 0.45f)
                    }
                    val alertContent = if (isDark) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        onSecondaryFixed
                    }

                    Surface(
                        shape = PiggyShapeTokens.Md,
                        color = alertBg,
                        contentColor = alertContent,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = PiggySpacing.spaceMd, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(PiggySpacing.spaceSm),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_notification_important),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = pluralStringResource(
                                    R.plurals.import_unassigned_warning,
                                    unassignedCount,
                                    unassignedCount,
                                ),
                                style = PiggyTypography.bodySm.copy(fontWeight = FontWeight.Medium),
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                } else if (itemCount > 0) {
                    val readyBg = if (isDark) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    } else {
                        primaryFixed.copy(alpha = 0.35f)
                    }
                    val readyContent = if (isDark) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        onPrimaryFixed
                    }

                    Surface(
                        shape = PiggyShapeTokens.Md,
                        color = readyBg,
                        contentColor = readyContent,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = PiggySpacing.spaceMd, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(PiggySpacing.spaceSm),
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = stringResource(R.string.import_all_assigned_ready),
                                style = PiggyTypography.bodySm.copy(fontWeight = FontWeight.Medium),
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BulkAssignSection(
    categories: List<Category>,
    onBulkAssign: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Surface(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = PiggyShapeTokens.Card,
            color = piggyCardContainerColor(),
            border = BorderStroke(1.dp, piggyCardBorderColor()),
            shadowElevation = 1.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(PiggySpacing.spaceMd),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PiggySpacing.spaceMd),
            ) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(R.drawable.ic_auto_fix_high),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.import_bulk_assign_title),
                        style = PiggyTypography.titleSm,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = stringResource(R.string.import_bulk_assign_subtitle),
                        style = PiggyTypography.bodySm,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }

        CategorySelectionDropdown(
            expanded = expanded,
            categories = categories,
            onDismiss = { expanded = false },
            onSelectCategory = { categoryId ->
                onBulkAssign(categoryId)
                expanded = false
            },
        )
    }
}

@Composable
private fun ExpenseImportItem(
    expense: ImportedExpense,
    categories: List<Category>,
    onCategorySelected: (String) -> Unit,
    onIsFixedChanged: (Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault()) }
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance() }
    val selectedCategory = categories.find { it.id == expense.categoryId }
    val isUnassigned = selectedCategory == null
    val isDark = isPiggyDarkTheme()

    val palette = rememberCategoryColorPalette(
        colorHex = selectedCategory?.color,
        categoryName = selectedCategory?.name ?: expense.name,
    )

    val cardColor = when {
        isUnassigned && isDark -> PiggyColors.DarkSurfacePrimary
        isUnassigned -> MaterialTheme.colorScheme.surfaceContainerLowest
        else -> piggyCardContainerColor()
    }
    val cardBorder = if (isUnassigned) {
        BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f))
    } else {
        BorderStroke(1.dp, piggyCardBorderColor())
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = PiggyShapeTokens.Card,
        color = cardColor,
        border = cardBorder,
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isUnassigned) {
                        Modifier.background(
                            if (isDark) {
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.10f)
                            } else {
                                secondaryFixed.copy(alpha = 0.14f)
                            }
                        )
                    } else {
                        Modifier
                    }
                )
                .padding(PiggySpacing.spaceLg),
            verticalArrangement = Arrangement.spacedBy(PiggySpacing.spaceSm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(PiggySpacing.spaceSm),
                ) {
                    ImportItemAvatar(
                        category = selectedCategory,
                        fallbackName = expense.name,
                        palette = palette,
                        isUnassigned = isUnassigned,
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(PiggySpacing.spaceXs),
                        ) {
                            Text(
                                text = expense.name,
                                style = PiggyTypography.titleSm,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false),
                            )
                            if (isUnassigned) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondary)
                                )
                            }
                        }

                        Text(
                            text = expense.date.atZone(ZoneId.systemDefault()).format(dateFormatter),
                            style = PiggyTypography.bodySm,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(PiggySpacing.spaceSm))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(PiggySpacing.space2xs),
                ) {
                    Text(
                        text = currencyFormatter.format(expense.amount),
                        style = PiggyTypography.currencyItem,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.action_remove_expense),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            HorizontalDivider(
                thickness = 1.dp,
                color = piggyCardBorderColor().copy(alpha = 0.6f),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Category Selector Pill / Badge
                Box {
                    if (selectedCategory != null) {
                        Surface(
                            onClick = { expanded = true },
                            shape = PiggyShapeTokens.Pill,
                            color = palette.badgeContainer,
                            contentColor = palette.badgeContent,
                        ) {
                            Row(
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 28.dp)
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(palette.dot)
                                )
                                Text(
                                    text = selectedCategory.name,
                                    style = PiggyTypography.badgeLabel,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    } else {
                        Surface(
                            onClick = { expanded = true },
                            shape = PiggyShapeTokens.Pill,
                            color = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary,
                            shadowElevation = 1.dp,
                        ) {
                            Row(
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 30.dp)
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                )
                                Text(
                                    text = stringResource(R.string.import_btn_select_category),
                                    style = PiggyTypography.badgeLabel,
                                )
                            }
                        }
                    }

                    CategorySelectionDropdown(
                        expanded = expanded,
                        categories = categories,
                        onDismiss = { expanded = false },
                        onSelectCategory = { categoryId ->
                            onCategorySelected(categoryId)
                            expanded = false
                        },
                    )
                }

                // Fixed expense toggle
                Row(
                    modifier = Modifier
                        .clip(PiggyShapeTokens.Default)
                        .clickable { onIsFixedChanged(!expense.isFixed) }
                        .padding(horizontal = PiggySpacing.space2xs, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = expense.isFixed,
                        onCheckedChange = onIsFixedChanged,
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            checkmarkColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(PiggySpacing.spaceXs))
                    Text(
                        text = stringResource(R.string.label_fixed_expense),
                        style = PiggyTypography.bodySm,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun ImportItemAvatar(
    category: Category?,
    fallbackName: String,
    palette: CategoryColorPalette,
    isUnassigned: Boolean,
) {
    val isDark = isPiggyDarkTheme()
    val containerColor = when {
        isUnassigned && isDark -> MaterialTheme.colorScheme.secondaryContainer
        isUnassigned -> secondaryFixed
        else -> palette.avatarContainer
    }
    val contentColor = when {
        isUnassigned && isDark -> MaterialTheme.colorScheme.onSecondaryContainer
        isUnassigned -> MaterialTheme.colorScheme.secondary
        else -> palette.avatarContent
    }
    val label = category?.icon?.takeIf { it.isNotBlank() }
        ?: (category?.name ?: fallbackName).firstOrNull()?.uppercase()
        ?: "?"

    Surface(
        modifier = Modifier.size(40.dp),
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = PiggyTypography.titleSm,
            )
        }
    }
}

@Composable
private fun CategorySelectionDropdown(
    expanded: Boolean,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSelectCategory: (String) -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
    ) {
        categories.forEach { category ->
            val palette = rememberCategoryColorPalette(
                colorHex = category.color,
                categoryName = category.name,
            )
            DropdownMenuItem(
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(PiggySpacing.spaceSm),
                    ) {
                        Surface(
                            modifier = Modifier.size(28.dp),
                            shape = CircleShape,
                            color = palette.avatarContainer,
                            contentColor = palette.avatarContent,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = category.icon.takeIf { it.isNotBlank() }
                                        ?: (category.name.firstOrNull()?.uppercase() ?: "?"),
                                    style = PiggyTypography.bodySm,
                                )
                            }
                        }
                        Text(
                            text = category.name,
                            style = PiggyTypography.bodyMd,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
                onClick = { onSelectCategory(category.id) },
            )
        }
    }
}

@Composable
private fun ImportStickyBottomAction(
    expenseCount: Int,
    enabled: Boolean,
    isImporting: Boolean,
    importProgress: Int,
    onImport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
        ) {
            HorizontalDivider(
                thickness = 1.dp,
                color = piggyCardBorderColor(),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(PiggySpacing.margin),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(PiggySpacing.space2xs),
            ) {
                if (isImporting) {
                    LinearProgressIndicator(
                        progress = { importProgress / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(PiggyShapeTokens.Pill),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    )
                    Spacer(modifier = Modifier.height(PiggySpacing.space2xs))
                    Text(
                        text = stringResource(R.string.import_progress_status, importProgress),
                        style = PiggyTypography.bodySm,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Button(
                        onClick = onImport,
                        enabled = enabled,
                        shape = PiggyShapeTokens.Pill,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(PiggySpacing.spaceXs))
                        Text(
                            text = pluralStringResource(
                                R.plurals.import_btn_submit_count,
                                expenseCount,
                                expenseCount,
                            ),
                            style = PiggyTypography.titleSm,
                        )
                    }

                    Text(
                        text = stringResource(R.string.import_footer_sync_hint),
                        style = PiggyTypography.bodySm,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

// ============================================================================
// Step 3: Completed / Success
// ============================================================================

@Composable
private fun CompletedStep(
    importedCount: Int,
    expenses: List<ImportedExpense>,
    categories: List<Category>,
    error: String?,
    onDismissError: () -> Unit,
    onDone: () -> Unit,
    onImportMore: () -> Unit,
    onNavigateToTrends: (() -> Unit)? = null,
) {
    val totalAmount = remember(expenses) { expenses.sumOf { it.amount } }
    val currencyParts = remember(totalAmount) { formatCurrencyParts(totalAmount) }

    val distinctCategoryIds = remember(expenses) {
        expenses.mapNotNull { it.categoryId }.distinct()
    }
    val categorizedCount = distinctCategoryIds.size
    val categoryNamesSummary = remember(distinctCategoryIds, categories) {
        distinctCategoryIds.mapNotNull { id ->
            categories.find { it.id == id }?.name
        }.joinToString(", ")
    }

    val (timeWindowLabel, daysCaptured) = remember(expenses) {
        computeTimeWindowSummary(expenses)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(
                horizontal = PiggySpacing.margin,
                vertical = PiggySpacing.spaceMd,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Hero Celebration Visual Unit
        SuccessCelebrationHero(
            importedCount = importedCount,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = PiggySpacing.spaceXs, bottom = PiggySpacing.space2xl),
        )

        if (!error.isNullOrBlank()) {
            ImportErrorBanner(
                message = error,
                onDismiss = onDismissError,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = PiggySpacing.spaceLg),
            )
        }

        // Summary Breakdown Card
        SuccessSummaryBreakdownCard(
            currencySymbol = currencyParts.symbol,
            amountNumber = currencyParts.amountNumber,
            categorizedCount = categorizedCount,
            categoryNamesSummary = categoryNamesSummary,
            timeWindowLabel = timeWindowLabel,
            daysCaptured = daysCaptured,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(PiggySpacing.space2xl))

        // Primary & Secondary Action CTAs
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(PiggySpacing.spaceSm),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(
                onClick = onDone,
                shape = PiggyShapeTokens.Pill,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                Text(
                    text = stringResource(R.string.import_btn_view_expenses),
                    style = PiggyTypography.titleSm,
                )
                Spacer(modifier = Modifier.width(PiggySpacing.spaceSm))
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_forward),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            }

            Button(
                onClick = onImportMore,
                shape = PiggyShapeTokens.Pill,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_upload),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(PiggySpacing.spaceSm))
                Text(
                    text = stringResource(R.string.import_btn_import_another),
                    style = PiggyTypography.titleSm,
                )
            }

            if (onNavigateToTrends != null) {
                TextButton(
                    onClick = onNavigateToTrends,
                ) {
                    Text(
                        text = stringResource(R.string.import_btn_go_to_trends),
                        style = PiggyTypography.bodySm.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(PiggySpacing.space2xs))
                    Icon(
                        painter = painterResource(DesignSystemR.drawable.ic_trending_up),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SuccessCelebrationHero(
    importedCount: Int,
    modifier: Modifier = Modifier,
) {
    val isDark = isPiggyDarkTheme()
    val countBadgeBg = if (isDark) MaterialTheme.colorScheme.secondaryContainer else secondaryFixed
    val countBadgeText = if (isDark) MaterialTheme.colorScheme.onSecondaryContainer else onSecondaryFixed
    val accentBadgeBg = if (isDark) MaterialTheme.colorScheme.tertiaryContainer else tertiaryFixed
    val accentBadgeIcon = if (isDark) MaterialTheme.colorScheme.onTertiaryContainer else onTertiaryFixed

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Concentric Success Ring Container
        Box(
            modifier = Modifier
                .padding(bottom = PiggySpacing.spaceXl)
                .size(104.dp),
            contentAlignment = Alignment.Center,
        ) {
            // Outer Decorative Concentric Halo
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainer),
                contentAlignment = Alignment.Center,
            ) {
                // Inner Glow Circle
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerLow),
                    contentAlignment = Alignment.Center,
                ) {
                    // Main Vibrant Emerald Badge with Checkmark
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shadowElevation = 6.dp,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                            )
                        }
                    }
                }
            }

            // Floating +N Piggy Badge
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-2).dp),
                shape = PiggyShapeTokens.Pill,
                color = countBadgeBg,
                contentColor = countBadgeText,
                shadowElevation = 2.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_savings),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(13.dp),
                    )
                    Text(
                        text = "+$importedCount",
                        style = PiggyTypography.labelCaps,
                    )
                }
            }

            // Bottom-left Emerald Accent Badge
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = 2.dp, y = (-2).dp)
                    .size(24.dp),
                shape = CircleShape,
                color = accentBadgeBg,
                contentColor = accentBadgeIcon,
                shadowElevation = 2.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                    )
                }
            }
        }

        Text(
            text = stringResource(R.string.import_success_title),
            style = PiggyTypography.headlineLg,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(PiggySpacing.spaceXs))

        Text(
            text = pluralStringResource(
                R.plurals.import_success_subtitle,
                importedCount,
                importedCount,
            ),
            style = PiggyTypography.bodyMd,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = PiggySpacing.spaceLg),
        )
    }
}

@Composable
private fun SuccessSummaryBreakdownCard(
    currencySymbol: String,
    amountNumber: String,
    categorizedCount: Int,
    categoryNamesSummary: String,
    timeWindowLabel: String,
    daysCaptured: Int,
    modifier: Modifier = Modifier,
) {
    val isDark = isPiggyDarkTheme()
    val walletBg = if (isDark) MaterialTheme.colorScheme.primaryContainer else primaryFixed
    val walletTint = if (isDark) MaterialTheme.colorScheme.onPrimaryContainer else onPrimaryFixed

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
                .padding(PiggySpacing.spaceXl),
            verticalArrangement = Arrangement.spacedBy(PiggySpacing.spaceLg),
        ) {
            // Card Top: Total Balance Added Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = PiggyShapeTokens.Md,
                color = piggySubtleContainerColor(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(PiggySpacing.spaceMd),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.import_label_total_balance_added_caps),
                            style = PiggyTypography.labelCaps,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = currencySymbol,
                                style = PiggyTypography.titleSm,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.alignByBaseline(),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = amountNumber,
                                style = PiggyTypography.displayCurrencyMobile,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.alignByBaseline(),
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = CircleShape,
                        color = walletBg,
                        contentColor = walletTint,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(R.drawable.ic_account_balance_wallet),
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                }
            }

            // Quick Metrics Grid (2 Columns)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PiggySpacing.spaceSm),
            ) {
                // Metric 1: Impacted Categories
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = PiggyShapeTokens.Md,
                    color = piggySubtleContainerColor().copy(alpha = 0.65f),
                ) {
                    Column(
                        modifier = Modifier.padding(PiggySpacing.spaceMd),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_category),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = stringResource(R.string.import_label_categories_caps),
                                style = PiggyTypography.labelCaps,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        Text(
                            text = stringResource(R.string.import_categorized_count, categorizedCount),
                            style = PiggyTypography.currencyItem,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        if (categoryNamesSummary.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                                Text(
                                    text = categoryNamesSummary,
                                    style = PiggyTypography.bodySm,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }

                // Metric 2: Time Window Covered
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = PiggyShapeTokens.Md,
                    color = piggySubtleContainerColor().copy(alpha = 0.65f),
                ) {
                    Column(
                        modifier = Modifier.padding(PiggySpacing.spaceMd),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_calendar_today),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = stringResource(R.string.import_label_time_window_caps),
                                style = PiggyTypography.labelCaps,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        Text(
                            text = timeWindowLabel,
                            style = PiggyTypography.currencyItem,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )

                        Text(
                            text = pluralStringResource(
                                R.plurals.import_days_captured,
                                daysCaptured,
                                daysCaptured,
                            ),
                            style = PiggyTypography.bodySm,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// Shared Components & Formatting Helpers
// ============================================================================

@Composable
private fun ImportErrorBanner(
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
                    contentDescription = stringResource(R.string.action_dismiss),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

private data class CurrencyParts(
    val symbol: String,
    val amountNumber: String,
)

private fun formatCurrencyParts(
    amount: Double,
    locale: Locale = Locale.getDefault(),
): CurrencyParts {
    val currencyFormat = NumberFormat.getCurrencyInstance(locale)
    val numberFormat = NumberFormat.getNumberInstance(locale).apply {
        minimumFractionDigits = currencyFormat.minimumFractionDigits
        maximumFractionDigits = currencyFormat.maximumFractionDigits
        isGroupingUsed = true
    }
    val symbol = try {
        currencyFormat.currency?.getSymbol(locale) ?: "$"
    } catch (_: Throwable) {
        "$"
    }
    return CurrencyParts(
        symbol = symbol,
        amountNumber = numberFormat.format(amount),
    )
}

private data class TimeWindowSummary(
    val label: String,
    val daysCaptured: Int,
)

private fun computeTimeWindowSummary(expenses: List<ImportedExpense>): TimeWindowSummary {
    val zone = ZoneId.systemDefault()
    val dates = expenses.map { it.date.atZone(zone).toLocalDate() }
    val minDate = dates.minOrNull() ?: LocalDate.now()
    val maxDate = dates.maxOrNull() ?: LocalDate.now()

    val monthDayFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
    val dayOnlyFormatter = DateTimeFormatter.ofPattern("d", Locale.getDefault())

    val label = when {
        minDate == maxDate -> minDate.format(monthDayFormatter)
        minDate.month == maxDate.month && minDate.year == maxDate.year ->
            "${minDate.format(monthDayFormatter)} – ${maxDate.format(dayOnlyFormatter)}"
        else -> "${minDate.format(monthDayFormatter)} – ${maxDate.format(monthDayFormatter)}"
    }
    val days = (ChronoUnit.DAYS.between(minDate, maxDate).toInt() + 1).coerceAtLeast(1)
    return TimeWindowSummary(
        label = label,
        daysCaptured = days,
    )
}

private fun Color.toHexToken(): String =
    String.format(Locale.US, "#%06X", 0xFFFFFF and toArgb())

// ============================================================================
// Previews
// ============================================================================

private val previewImportCategories = listOf(
    Category(
        id = "groceries",
        name = "Groceries",
        createdAt = Instant.EPOCH,
        userId = "user1",
        icon = "🛒",
        color = PiggyColors.CategoryGroceries.toHexToken(),
    ),
    Category(
        id = "dining",
        name = "Dining Out",
        createdAt = Instant.EPOCH,
        userId = "user1",
        icon = "🍔",
        color = PiggyColors.CategoryDiningOut.toHexToken(),
    ),
    Category(
        id = "utilities",
        name = "Utilities",
        createdAt = Instant.EPOCH,
        userId = "user1",
        icon = "⚡",
        color = PiggyColors.CategoryUtilities.toHexToken(),
    ),
)

private val previewImportedExpenses = listOf(
    ImportedExpense(
        name = "Trader Joe's",
        amount = 94.30,
        date = LocalDate.of(2026, 10, 14).atStartOfDay(ZoneId.systemDefault()).toInstant(),
        categoryId = "groceries",
        isFixed = true,
        originalLine = "2026-10-14,Trader Joe's,94.30",
    ),
    ImportedExpense(
        name = "Electric Bill",
        amount = 112.00,
        date = LocalDate.of(2026, 10, 16).atStartOfDay(ZoneId.systemDefault()).toInstant(),
        categoryId = "utilities",
        isFixed = true,
        originalLine = "2026-10-16,Electric Bill,112.00",
    ),
    ImportedExpense(
        name = "Blue Bottle Coffee",
        amount = 11.50,
        date = LocalDate.of(2026, 10, 19).atStartOfDay(ZoneId.systemDefault()).toInstant(),
        categoryId = null,
        isFixed = false,
        originalLine = "2026-10-19,Blue Bottle Coffee,11.50",
    ),
)

@Preview(name = "Import Expenses - Select File", showBackground = true, heightDp = 860)
@Composable
private fun ImportExpensesSelectFilePreview() {
    PiggyTheme {
        ImportExpensesScreenContent(
            state = ImportUiState(
                importStep = ImportStep.SELECT_FILE,
            )
        )
    }
}

@Preview(
    name = "Import Expenses - Select File (Dark)",
    showBackground = true,
    heightDp = 860,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ImportExpensesSelectFileDarkPreview() {
    PiggyTheme(darkTheme = true) {
        ImportExpensesScreenContent(
            state = ImportUiState(
                importStep = ImportStep.SELECT_FILE,
            )
        )
    }
}

@Preview(name = "Import Expenses - Assign Categories", showBackground = true, heightDp = 920)
@Composable
private fun ImportExpensesScreenPreview() {
    PiggyTheme {
        ImportExpensesScreenContent(
            state = ImportUiState(
                importStep = ImportStep.ASSIGN_CATEGORIES,
                expenses = previewImportedExpenses,
                categories = previewImportCategories,
            )
        )
    }
}

@Preview(
    name = "Import Expenses - Assign Categories (Dark)",
    showBackground = true,
    heightDp = 920,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ImportExpensesScreenDarkPreview() {
    PiggyTheme(darkTheme = true) {
        ImportExpensesScreenContent(
            state = ImportUiState(
                importStep = ImportStep.ASSIGN_CATEGORIES,
                expenses = previewImportedExpenses,
                categories = previewImportCategories,
            )
        )
    }
}

@Preview(name = "Import Expenses - Completed", showBackground = true, heightDp = 880)
@Composable
private fun ImportExpensesCompletedPreview() {
    PiggyTheme {
        ImportExpensesScreenContent(
            state = ImportUiState(
                importStep = ImportStep.COMPLETED,
                importedCount = 18,
                expenses = previewImportedExpenses,
                categories = previewImportCategories,
            ),
            onNavigateToTrends = {},
        )
    }
}

@Preview(
    name = "Import Expenses - Completed (Dark)",
    showBackground = true,
    heightDp = 880,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ImportExpensesCompletedDarkPreview() {
    PiggyTheme(darkTheme = true) {
        ImportExpensesScreenContent(
            state = ImportUiState(
                importStep = ImportStep.COMPLETED,
                importedCount = 18,
                expenses = previewImportedExpenses,
                categories = previewImportCategories,
            ),
            onNavigateToTrends = {},
        )
    }
}

