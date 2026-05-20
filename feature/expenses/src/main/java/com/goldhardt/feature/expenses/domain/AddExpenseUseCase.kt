package com.goldhardt.feature.expenses.domain

import com.goldhardt.core.auth.session.UserSession
import com.goldhardt.core.data.model.Expense
import com.goldhardt.core.data.model.ExpenseFormData
import com.goldhardt.core.data.repository.ExpenseRepository
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
