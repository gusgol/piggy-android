package com.goldhardt.feature.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldhardt.core.domain.model.Expense
import com.goldhardt.core.domain.model.Category
import com.goldhardt.core.domain.model.ExpenseFormData
import com.goldhardt.core.domain.model.ExpenseUpdate
import com.goldhardt.core.domain.model.ExpenseWithCategory
import com.goldhardt.feature.expenses.domain.ObserveMonthExpensesUseCase
import com.goldhardt.feature.expenses.domain.AddExpenseUseCase
import com.goldhardt.feature.expenses.domain.GetUserCategoriesUseCase
import com.goldhardt.feature.expenses.domain.UpdateExpenseUseCase
import com.goldhardt.feature.expenses.domain.DeleteExpenseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.YearMonth

data class UiState(
    val month: YearMonth = YearMonth.now(),
    val expenses: List<ExpenseWithCategory> = emptyList(),
    val total: Double = 0.0,
    val isLoading: Boolean = true,
    val error: String? = null,
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
    private val mutationError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<UiState> =
        selectedMonth
            .flatMapLatest { month ->
                observeMonthExpenses(month)
                    .map { expenses -> createUiState(month, expenses, isLoading = false) }
                    .catch { emit(createUiState(month, isLoading = false, error = "Couldn't load expenses. Please try again.")) }
            }
            .combine(mutationError) { state, error -> state.copy(error = error ?: state.error) }
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
                mutationError.value = null
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _categories.value = emptyList()
                mutationError.value = "Couldn't load categories. Please try again."
            }
        }
    }

    fun addExpense(name: String, amount: Double, date: Instant, categoryId: String, isFixed: Boolean) {
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
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                mutationError.value = "Couldn't add expense. Please try again."
            }
        }
    }

    fun updateExpense(original: Expense, name: String, amount: Double, date: Instant, categoryId: String, isFixed: Boolean) {
        viewModelScope.launch {
            try {
                mutationError.value = null
                val update = ExpenseUpdate(
                    name = original.name.takeIf { it != name }?.let { name.trim() },
                    amount = original.amount.takeIf { it != amount }?.let { amount },
                    date = original.date.takeIf { it != date }?.let { date },
                    categoryId = original.categoryId.takeIf { it != categoryId }?.let { categoryId },
                    isFixed = original.isFixed.takeIf { it != isFixed }?.let { isFixed },
                )
                updateExpenseUseCase(original.id, update)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                mutationError.value = "Couldn't update expense. Please try again."
            }
        }
    }

    fun deleteExpense(expenseId: String) {
        viewModelScope.launch {
            try {
                mutationError.value = null
                deleteExpenseUseCase(expenseId)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                mutationError.value = "Couldn't delete expense. Please try again."
            }
        }
    }

    private fun createUiState(
        month: YearMonth,
        expenses: List<ExpenseWithCategory> = emptyList(),
        isLoading: Boolean = false,
        error: String? = null,
    ): UiState {
        return UiState(
            month = month,
            expenses = expenses,
            total = expenses.sumOf { it.expense.amount },
            isLoading = isLoading,
            error = error,
        )
    }


}
