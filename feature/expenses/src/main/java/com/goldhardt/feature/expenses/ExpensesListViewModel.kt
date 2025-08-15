package com.goldhardt.feature.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldhardt.core.data.model.Expense
import com.goldhardt.feature.expenses.domain.ObserveMonthExpensesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
) : ViewModel() {

    private val selectedMonth = MutableStateFlow(YearMonth.now())

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
