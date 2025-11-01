package com.goldhardt.feature.expenses.import

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldhardt.core.auth.repository.AuthRepository
import com.goldhardt.core.data.model.Category
import com.goldhardt.feature.expenses.domain.GetUserCategoriesUseCase
import com.goldhardt.feature.expenses.import.model.ImportExpenseItem
import com.goldhardt.feature.expenses.import.model.ParseResult
import com.goldhardt.feature.expenses.import.repository.ImportExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.InputStream
import javax.inject.Inject

/**
 * UI state for the import expenses screen.
 */
data class ImportUiState(
    val items: List<ImportExpenseItem> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val isParsing: Boolean = false,
    val isSaving: Boolean = false,
    val parseError: String? = null,
    val saveError: String? = null,
    val parseProgress: Int = 0,
    val confirmedCount: Int = 0,
    val saveSuccess: Pair<Int, Int>? = null, // (saved count, skipped count)
)

@HiltViewModel
class ImportExpensesViewModel @Inject constructor(
    private val importRepository: ImportExpenseRepository,
    private val getUserCategories: GetUserCategoriesUseCase,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportUiState())
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val categories = getUserCategories()
                _uiState.update { it.copy(categories = categories, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Parse CSV file from the given input stream.
     */
    fun parseFile(inputStream: InputStream) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isParsing = true,
                    parseError = null,
                    items = emptyList(),
                    parseProgress = 0
                )
            }

            try {
                val items = mutableListOf<ImportExpenseItem>()
                importRepository.parseStream(inputStream)
                    .catch { error ->
                        _uiState.update {
                            it.copy(
                                isParsing = false,
                                parseError = "Failed to parse CSV: ${error.message}"
                            )
                        }
                    }
                    .collect { result ->
                        when (result) {
                            is ParseResult.Success -> {
                                items.add(ImportExpenseItem(parsedExpense = result.expense))
                                _uiState.update {
                                    it.copy(
                                        items = items.toList(),
                                        parseProgress = items.size
                                    )
                                }
                            }
                            is ParseResult.Error -> {
                                if (result.rowNumber == 0) {
                                    // Critical error (header validation, etc.)
                                    _uiState.update {
                                        it.copy(
                                            isParsing = false,
                                            parseError = result.message
                                        )
                                    }
                                    return@collect
                                }
                                // Row-level error - could be handled individually
                                // For now, we skip invalid rows
                            }
                        }
                    }

                _uiState.update { it.copy(isParsing = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isParsing = false,
                        parseError = "Unexpected error: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Update the description of a specific item.
     */
    fun updateDescription(itemId: String, newDescription: String) {
        _uiState.update { state ->
            state.copy(
                items = state.items.map { item ->
                    if (item.parsedExpense.id == itemId) {
                        item.copy(editedDescription = newDescription)
                    } else {
                        item
                    }
                }
            )
        }
    }

    /**
     * Update the category selection for a specific item.
     */
    fun updateCategory(itemId: String, categoryId: String) {
        _uiState.update { state ->
            state.copy(
                items = state.items.map { item ->
                    if (item.parsedExpense.id == itemId) {
                        item.copy(selectedCategoryId = categoryId)
                    } else {
                        item
                    }
                }
            )
        }
    }

    /**
     * Confirm a single item for import.
     */
    fun confirmItem(itemId: String) {
        _uiState.update { state ->
            state.copy(
                items = state.items.map { item ->
                    if (item.parsedExpense.id == itemId) {
                        item.copy(isConfirmed = true, isSkipped = false)
                    } else {
                        item
                    }
                },
                confirmedCount = state.items.count { 
                    it.isConfirmed || it.parsedExpense.id == itemId 
                }
            )
        }
    }

    /**
     * Skip a single item.
     */
    fun skipItem(itemId: String) {
        _uiState.update { state ->
            state.copy(
                items = state.items.map { item ->
                    if (item.parsedExpense.id == itemId) {
                        item.copy(isSkipped = true, isConfirmed = false)
                    } else {
                        item
                    }
                },
                confirmedCount = state.items.count { 
                    it.isConfirmed && it.parsedExpense.id != itemId
                }
            )
        }
    }

    /**
     * Confirm all items at once.
     */
    fun confirmAll() {
        _uiState.update { state ->
            state.copy(
                items = state.items.map { item ->
                    item.copy(
                        isConfirmed = true,
                        isSkipped = false,
                        // Set default category if not already set
                        selectedCategoryId = item.selectedCategoryId 
                            ?: state.categories.firstOrNull()?.id
                    )
                },
                confirmedCount = state.items.size
            )
        }
    }

    /**
     * Save all confirmed expenses to Firestore.
     */
    fun saveConfirmed() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }

            try {
                val userId = authRepository.getCurrentUser()?.uid
                    ?: throw IllegalStateException("User not authenticated")

                val defaultCategoryId = _uiState.value.categories.firstOrNull()?.id
                    ?: throw IllegalStateException("No categories available")

                val (savedCount, skippedCount) = importRepository.saveExpenses(
                    userId = userId,
                    items = _uiState.value.items,
                    defaultCategoryId = defaultCategoryId
                )

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveSuccess = savedCount to skippedCount
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveError = "Failed to save expenses: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Clear the save success state (to dismiss success message).
     */
    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = null) }
    }

    /**
     * Reset the import flow (to start over or cancel).
     */
    fun reset() {
        _uiState.update {
            ImportUiState(categories = it.categories)
        }
    }
}
