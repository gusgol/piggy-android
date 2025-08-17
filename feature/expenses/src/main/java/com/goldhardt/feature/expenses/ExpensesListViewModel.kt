package com.goldhardt.feature.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldhardt.core.data.model.Expense
import com.goldhardt.core.data.model.Category
import com.goldhardt.core.data.model.ExpenseFormData
import com.goldhardt.core.data.model.ExpenseUpdate
import com.goldhardt.feature.expenses.domain.ObserveMonthExpensesUseCase
import com.goldhardt.feature.expenses.domain.AddExpenseUseCase
import com.goldhardt.feature.expenses.domain.GetUserCategoriesUseCase
import com.goldhardt.feature.expenses.domain.UpdateExpenseUseCase
import com.goldhardt.feature.expenses.domain.DeleteExpenseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.YearMonth

data class UiState(
    val month: YearMonth = YearMonth.now(),
    val expenses: List<Expense> = emptyList(),
    val total: Double = 0.0,
    val isLoading: Boolean = true,
)

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ExpensesListViewModel @Inject constructor(
    private val observeMonthExpenses: ObserveMonthExpensesUseCase,
    private val addExpenseUseCase: AddExpenseUseCase,
    private val getUserCategoriesUseCase: GetUserCategoriesUseCase,
    private val updateExpenseUseCase: UpdateExpenseUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
) : ViewModel() {

    private val selectedMonth = MutableStateFlow(YearMonth.now())

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    val uiState: StateFlow<UiState> =
        selectedMonth
            .flatMapLatest { month ->
                observeMonthExpenses(month)
                    .map { expenses -> createUiState(month, expenses, isLoading = false) }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = createUiState(selectedMonth.value, isLoading = true)
            )

    fun setMonth(month: YearMonth) {
        selectedMonth.value = month
    }

    fun refreshCategories() {
        viewModelScope.launch {
            try {
                _categories.value = getUserCategoriesUseCase()
            } catch (_: Throwable) {
                _categories.value = emptyList()
            }
        }
    }

    fun addExpense(name: String, amount: Double, date: Instant, categoryId: String, isFixed: Boolean, onError: (Throwable) -> Unit = {}) {
        viewModelScope.launch {
            try {
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
                onError(t)
            }
        }
    }

    fun updateExpense(original: Expense, name: String, amount: Double, date: Instant, categoryId: String, isFixed: Boolean, onError: (Throwable) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val update = ExpenseUpdate(
                    name = original.name.takeIf { it != name }?.let { name.trim() },
                    amount = original.amount.takeIf { it != amount }?.let { amount },
                    date = original.date.takeIf { it != date }?.let { date },
                    categoryId = original.categoryId.takeIf { it != categoryId }?.let { categoryId },
                    isFixed = original.isFixed.takeIf { it != isFixed }?.let { isFixed },
                )
                updateExpenseUseCase(original.id, update)
            } catch (t: Throwable) {
                onError(t)
            }
        }
    }

    fun deleteExpense(expenseId: String, onError: (Throwable) -> Unit = {}) {
        viewModelScope.launch {
            try {
                deleteExpenseUseCase(expenseId)
            } catch (t: Throwable) {
                onError(t)
            }
        }
    }

    private fun createUiState(
        month: YearMonth,
        expenses: List<Expense> = emptyList(),
        isLoading: Boolean = false
    ): UiState {
        return UiState(
            month = month,
            expenses = expenses,
            total = expenses.sumOf { it.amount },
            isLoading = isLoading,
        )
    }


}
