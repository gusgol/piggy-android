package com.goldhardt.feature.expenses.domain

import com.goldhardt.core.auth.repository.AuthRepository
import com.goldhardt.core.data.model.Expense
import com.goldhardt.core.data.model.ExpenseFormData
import com.goldhardt.core.data.repository.ExpenseRepository
import javax.inject.Inject

class AddExpenseUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val expenseRepository: ExpenseRepository,
) {
    /** Adds an expense for the current user; throws if not authenticated. */
    suspend operator fun invoke(form: ExpenseFormData): Expense {
        val user = authRepository.currentUser
            ?: throw IllegalStateException("User must be signed in")
        return expenseRepository.addExpense(user.id, form)
    }
}
