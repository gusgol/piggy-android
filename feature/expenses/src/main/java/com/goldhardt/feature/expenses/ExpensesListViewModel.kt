package com.goldhardt.feature.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldhardt.core.domain.model.Category
import com.goldhardt.core.domain.model.Expense
import com.goldhardt.core.domain.model.ExpenseFormData
import com.goldhardt.core.domain.model.ExpenseUpdate
import com.goldhardt.feature.expenses.domain.AddExpenseUseCase
import com.goldhardt.feature.expenses.domain.BuildMonthlyExpensesProjectionUseCase
import com.goldhardt.feature.expenses.domain.CategoryFilterOption
import com.goldhardt.feature.expenses.domain.DeleteExpenseUseCase
import com.goldhardt.feature.expenses.domain.ObserveCategoriesUseCase
import com.goldhardt.feature.expenses.domain.ObserveMonthExpensesUseCase
import com.goldhardt.feature.expenses.domain.SpendingInsight
import com.goldhardt.feature.expenses.domain.UpdateExpenseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UiState(
    val month: YearMonth = YearMonth.now(),
    val expenses: List<Expense> = emptyList(),
    val total: Double = 0.0,
    val fixedTotal: Double = 0.0,
    val variableTotal: Double = 0.0,
    val fixedFraction: Float = 0f,
    val variableFraction: Float = 0f,
    val totalExpenseCount: Int = 0,
    val categories: List<Category> = emptyList(),
    val categoryFilters: List<CategoryFilterOption> = emptyList(),
    val selectedCategoryId: String? = null,
    val insight: SpendingInsight? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
)

private sealed interface MonthExpensesLoadState {
    data class Loading(val month: YearMonth) : MonthExpensesLoadState
    data class Loaded(val month: YearMonth, val expenses: List<Expense>) : MonthExpensesLoadState
    data class Failed(val month: YearMonth, val message: String?) : MonthExpensesLoadState
}

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ExpensesListViewModel @Inject constructor(
    private val observeMonthExpenses: ObserveMonthExpensesUseCase,
    private val observeCategories: ObserveCategoriesUseCase,
    private val buildProjection: BuildMonthlyExpensesProjectionUseCase,
    private val addExpenseUseCase: AddExpenseUseCase,
    private val updateExpenseUseCase: UpdateExpenseUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
) : ViewModel() {

    private val selectedMonth = MutableStateFlow(YearMonth.now())
    private val selectedCategoryId = MutableStateFlow<String?>(null)
    private val mutationError = MutableStateFlow<String?>(null)

    private val monthExpensesState = selectedMonth.flatMapLatest { month ->
        observeMonthExpenses(month)
            .map<List<Expense>, MonthExpensesLoadState> { expenses ->
                MonthExpensesLoadState.Loaded(month, expenses)
            }
            .onStart { emit(MonthExpensesLoadState.Loading(month)) }
            .catch { e -> emit(MonthExpensesLoadState.Failed(month, e.message)) }
    }

    private val categoriesState = observeCategories()
        .catch { emit(emptyList()) }

    val uiState: StateFlow<UiState> = combine(
        monthExpensesState,
        categoriesState,
        selectedCategoryId,
        mutationError,
    ) { monthState, categories, filterCategoryId, mutErr ->
        when (monthState) {
            is MonthExpensesLoadState.Loading -> {
                val projection = buildProjection(emptyList(), categories, filterCategoryId)
                UiState(
                    month = monthState.month,
                    categories = categories,
                    categoryFilters = projection.categoryFilters,
                    selectedCategoryId = projection.effectiveSelectedCategoryId,
                    isLoading = true,
                    error = mutErr,
                )
            }
            is MonthExpensesLoadState.Failed -> {
                val projection = buildProjection(emptyList(), categories, filterCategoryId)
                UiState(
                    month = monthState.month,
                    categories = categories,
                    categoryFilters = projection.categoryFilters,
                    selectedCategoryId = projection.effectiveSelectedCategoryId,
                    isLoading = false,
                    error = mutErr ?: monthState.message,
                )
            }
            is MonthExpensesLoadState.Loaded -> {
                val projection = buildProjection(monthState.expenses, categories, filterCategoryId)
                UiState(
                    month = monthState.month,
                    expenses = projection.filteredExpenses,
                    total = projection.summary.totalAmount,
                    fixedTotal = projection.summary.fixedAmount,
                    variableTotal = projection.summary.variableAmount,
                    fixedFraction = projection.summary.fixedFraction,
                    variableFraction = projection.summary.variableFraction,
                    totalExpenseCount = projection.summary.totalCount,
                    categories = categories,
                    categoryFilters = projection.categoryFilters,
                    selectedCategoryId = projection.effectiveSelectedCategoryId,
                    insight = projection.insight,
                    isLoading = false,
                    error = mutErr,
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UiState(month = selectedMonth.value, isLoading = true),
    )

    fun setMonth(month: YearMonth) {
        selectedMonth.value = month
    }

    fun selectCategory(categoryId: String?) {
        selectedCategoryId.value = if (categoryId != null && selectedCategoryId.value == categoryId) {
            null
        } else {
            categoryId
        }
    }

    fun clearError() {
        mutationError.value = null
    }

    fun addExpense(
        name: String,
        amount: Double,
        date: Instant,
        categoryId: String,
        isFixed: Boolean,
    ) {
        viewModelScope.launch {
            try {
                mutationError.value = null
                addExpenseUseCase(
                    ExpenseFormData(
                        name = name.trim(),
                        amount = amount,
                        date = date,
                        categoryId = categoryId,
                        isFixed = isFixed,
                    )
                )
            } catch (t: Throwable) {
                mutationError.value = t.message ?: "Failed to add expense"
            }
        }
    }

    fun updateExpense(
        original: Expense,
        name: String,
        amount: Double,
        date: Instant,
        categoryId: String,
        isFixed: Boolean,
    ) {
        viewModelScope.launch {
            try {
                mutationError.value = null
                val trimmedName = name.trim()
                val update = ExpenseUpdate(
                    name = trimmedName.takeIf { it != original.name },
                    amount = amount.takeIf { it != original.amount },
                    date = date.takeIf { it != original.date },
                    categoryId = categoryId.takeIf { it != original.categoryId },
                    isFixed = isFixed.takeIf { it != original.isFixed },
                )
                updateExpenseUseCase(original.id, update)
            } catch (t: Throwable) {
                mutationError.value = t.message ?: "Failed to update expense"
            }
        }
    }

    fun deleteExpense(expenseId: String) {
        viewModelScope.launch {
            try {
                mutationError.value = null
                deleteExpenseUseCase(expenseId)
            } catch (t: Throwable) {
                mutationError.value = t.message ?: "Failed to delete expense"
            }
        }
    }
}
