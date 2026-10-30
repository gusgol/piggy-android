package com.goldhardt.feature.expenses.domain

import com.goldhardt.core.auth.session.UserSession
import com.goldhardt.core.domain.model.Expense
import com.goldhardt.core.domain.repository.ExpenseRepository
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveMonthExpensesUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val userSession: UserSession,
) {
    operator fun invoke(
        month: YearMonth,
        categoryId: String? = null,
    ): Flow<List<Expense>> {
        return userSession.authState.flatMapLatest { user ->
            val uid = user?.id
            if (uid == null) {
                flowOf(emptyList())
            } else {
                expenseRepository.observeExpenses(uid, month, categoryId)
            }
        }
    }
}