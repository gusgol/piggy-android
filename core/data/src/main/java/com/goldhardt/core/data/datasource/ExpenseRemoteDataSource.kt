package com.goldhardt.core.data.datasource

import com.goldhardt.core.data.model.Expense
import com.goldhardt.core.data.model.ExpenseFormData
import com.goldhardt.core.data.model.ExpenseUpdate
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow

/**
 * Abstraction for reading/writing Expenses from a remote source (Firestore).
 */
interface ExpenseRemoteDataSource {
    /** Observe expenses in realtime for a given user and date range, optionally filtered by category. */
    fun observeExpenses(
        userId: String,
        startAt: Timestamp,
        endAt: Timestamp,
        categoryId: String? = null,
    ): Flow<List<Expense>>

    /** Fetch expenses once for a given user and date range, optionally filtered by category. */
    suspend fun getExpenses(
        userId: String,
        startAt: Timestamp,
        endAt: Timestamp,
        categoryId: String? = null,
    ): List<Expense>

    /** Create a new expense for the given user and return the created model. */
    suspend fun addExpense(userId: String, form: ExpenseFormData): Expense

    /** Partially update an expense by its id. */
    suspend fun updateExpense(expenseId: String, update: ExpenseUpdate)

    /** Delete an expense by its id. */
    suspend fun deleteExpense(expenseId: String)
}

