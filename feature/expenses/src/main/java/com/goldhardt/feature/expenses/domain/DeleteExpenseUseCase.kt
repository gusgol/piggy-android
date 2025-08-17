package com.goldhardt.feature.expenses.domain

import com.goldhardt.core.data.repository.ExpenseRepository
import javax.inject.Inject

class DeleteExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
) {
    suspend operator fun invoke(expenseId: String) {
        expenseRepository.deleteExpense(expenseId)
    }
}

