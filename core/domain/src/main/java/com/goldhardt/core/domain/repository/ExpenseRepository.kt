package com.goldhardt.core.domain.repository

import com.goldhardt.core.domain.model.Expense
import com.goldhardt.core.domain.model.ExpenseFormData
import com.goldhardt.core.domain.model.ExpenseUpdate
import com.goldhardt.core.domain.model.ExpenseWithCategory
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

interface ExpenseRepository {
    fun observeExpenses(
        userId: String,
        month: YearMonth,
        categoryId: String? = null,
    ): Flow<List<ExpenseWithCategory>>

    suspend fun getExpenses(
        userId: String,
        month: YearMonth,
        categoryId: String? = null,
    ): List<ExpenseWithCategory>

    suspend fun addExpense(userId: String, form: ExpenseFormData): Expense

    suspend fun addExpenses(userId: String, forms: List<ExpenseFormData>): List<Expense>

    suspend fun updateExpense(expenseId: String, update: ExpenseUpdate)

    suspend fun deleteExpense(expenseId: String)
}

