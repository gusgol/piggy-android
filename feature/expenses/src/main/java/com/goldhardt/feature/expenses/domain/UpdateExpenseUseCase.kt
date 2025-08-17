package com.goldhardt.feature.expenses.domain

import com.goldhardt.core.data.model.ExpenseUpdate
import com.goldhardt.core.data.repository.ExpenseRepository
import javax.inject.Inject

class UpdateExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
) {
    suspend operator fun invoke(expenseId: String, update: ExpenseUpdate) {
        expenseRepository.updateExpense(expenseId, update)
    }
}

