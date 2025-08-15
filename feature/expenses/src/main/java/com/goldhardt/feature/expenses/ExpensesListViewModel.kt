package com.goldhardt.feature.expenses

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldhardt.core.auth.repository.AuthRepository
import com.goldhardt.core.data.model.Expense
import com.goldhardt.core.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ExpensesListViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    data class UiState(
        val month: YearMonth = YearMonth.now(),
        val expenses: List<Expense> = emptyList(),
        val total: Double = 0.0,
        val isLoading: Boolean = true,
        val userLoggedIn: Boolean = false,
    )

    private val selectedMonth = MutableStateFlow(YearMonth.now())

    val uiState: StateFlow<UiState> = combine(
        authRepository.authState(),
        selectedMonth,
    ) { user, month -> user to month }
        .flatMapLatest { (user, month) ->
            if (user == null) {
                flowOf(
                    UiState(
                        month = month,
                        expenses = emptyList(),
                        total = 0.0,
                        isLoading = false,
                        userLoggedIn = false
                    )
                )
            } else {
                expenseRepository.observeExpenses(user.id, month)
                    .map { expenses ->
                        UiState(
                            month = month,
                            expenses = expenses,
                            total = expenses.sumOf { it.amount },
                            isLoading = false,
                            userLoggedIn = true,
                        )
                    }
                    .onStart {
                        emit(
                            UiState(
                                month = month,
                                expenses = emptyList(),
                                total = 0.0,
                                isLoading = true,
                                userLoggedIn = true,
                            )
                        )
                    }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState(
                month = selectedMonth.value,
                isLoading = true,
                userLoggedIn = authRepository.currentUser != null
            )
        )

    fun setMonth(month: YearMonth) {
        selectedMonth.value = month
    }
}
