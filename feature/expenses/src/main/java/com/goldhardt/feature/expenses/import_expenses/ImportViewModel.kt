package com.goldhardt.feature.expenses.import_expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldhardt.core.data.model.Category
import com.goldhardt.feature.expenses.domain.GetUserCategoriesUseCase
import com.goldhardt.feature.expenses.import_expenses.domain.CsvParseResult
import com.goldhardt.feature.expenses.import_expenses.domain.CsvParser
import com.goldhardt.feature.expenses.import_expenses.domain.ImportExpensesUseCase
import com.goldhardt.feature.expenses.import_expenses.domain.ImportResult
import com.goldhardt.feature.expenses.import_expenses.domain.ImportedExpense
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.InputStream
import javax.inject.Inject

@HiltViewModel
class ImportViewModel @Inject constructor(
    private val csvParser: CsvParser,
    private val importExpensesUseCase: ImportExpensesUseCase,
    private val getUserCategoriesUseCase: GetUserCategoriesUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportUiState())
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                val categories = getUserCategoriesUseCase()
                _uiState.update { it.copy(categories = categories) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to load categories: ${e.message}") }
            }
        }
    }

    fun parseCsvFile(inputStream: InputStream) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = csvParser.parse(inputStream)) {
                is CsvParseResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            expenses = result.expenses,
                            importStep = ImportStep.ASSIGN_CATEGORIES
                        )
                    }
                }
                is CsvParseResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    fun updateExpenseCategory(index: Int, categoryId: String) {
        _uiState.update { state ->
            val updatedExpenses = state.expenses.toMutableList()
            updatedExpenses[index] = updatedExpenses[index].copy(categoryId = categoryId)
            state.copy(expenses = updatedExpenses)
        }
    }

    fun updateExpenseIsFixed(index: Int, isFixed: Boolean) {
        _uiState.update { state ->
            val updatedExpenses = state.expenses.toMutableList()
            updatedExpenses[index] = updatedExpenses[index].copy(isFixed = isFixed)
            state.copy(expenses = updatedExpenses)
        }
    }

    fun bulkAssignCategory(categoryId: String) {
        _uiState.update { state ->
            val updatedExpenses = state.expenses.map { expense ->
                if (expense.categoryId == null) {
                    expense.copy(categoryId = categoryId)
                } else {
                    expense
                }
            }
            state.copy(expenses = updatedExpenses)
        }
    }

    fun deleteExpense(index: Int) {
        _uiState.update { state ->
            val updatedExpenses = state.expenses.toMutableList().apply {
                removeAt(index)
            }
            state.copy(expenses = updatedExpenses)
        }
    }

    fun importExpenses() {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, importProgress = 0) }

            val result = importExpensesUseCase(
                expenses = _uiState.value.expenses,
                onProgress = { current, total ->
                    _uiState.update { it.copy(importProgress = (current * 100) / total) }
                }
            )

            when (result) {
                is ImportResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isImporting = false,
                            importStep = ImportStep.COMPLETED,
                            importedCount = result.count
                        )
                    }
                }
                is ImportResult.PartialSuccess -> {
                    _uiState.update {
                        it.copy(
                            isImporting = false,
                            importStep = ImportStep.COMPLETED,
                            importedCount = result.successCount,
                            error = "Some expenses failed to import: ${result.failures.size} errors"
                        )
                    }
                }
                is ImportResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isImporting = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    fun reset() {
        _uiState.value = ImportUiState(categories = _uiState.value.categories)
    }
}

data class ImportUiState(
    val isLoading: Boolean = false,
    val isImporting: Boolean = false,
    val importProgress: Int = 0,
    val expenses: List<ImportedExpense> = emptyList(),
    val categories: List<Category> = emptyList(),
    val importStep: ImportStep = ImportStep.SELECT_FILE,
    val error: String? = null,
    val importedCount: Int = 0,
)

enum class ImportStep {
    SELECT_FILE,
    ASSIGN_CATEGORIES,
    COMPLETED
}

