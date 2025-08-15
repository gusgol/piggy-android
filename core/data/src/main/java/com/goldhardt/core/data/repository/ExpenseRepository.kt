package com.goldhardt.core.data.repository

import com.goldhardt.core.data.model.Expense
import com.goldhardt.core.data.model.ExpenseFormData
import com.goldhardt.core.data.model.ExpenseUpdate
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

interface ExpenseRepository {
    fun observeExpenses(
        userId: String,
        month: YearMonth,
        categoryId: String? = null,
    ): Flow<List<Expense>>

    suspend fun getExpenses(
        userId: String,
        month: YearMonth,
        categoryId: String? = null,
    ): List<Expense>

    suspend fun addExpense(userId: String, form: ExpenseFormData): Expense

    suspend fun updateExpense(expenseId: String, update: ExpenseUpdate)

    suspend fun deleteExpense(expenseId: String)
}

