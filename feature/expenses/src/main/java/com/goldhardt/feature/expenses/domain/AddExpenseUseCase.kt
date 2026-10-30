package com.goldhardt.feature.expenses.domain

import com.goldhardt.core.auth.session.UserSession
import com.goldhardt.core.domain.model.Expense
import com.goldhardt.core.domain.model.ExpenseFormData
import com.goldhardt.core.domain.repository.ExpenseRepository
import javax.inject.Inject

class AddExpenseUseCase @Inject constructor(
    private val userSession: UserSession,
    private val expenseRepository: ExpenseRepository,
) {
    /** Adds an expense for the current user; throws if not authenticated. */
    suspend operator fun invoke(form: ExpenseFormData): Expense {
        return expenseRepository.addExpense(userSession.userId, form)
    }
}
