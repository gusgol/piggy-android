package com.goldhardt.core.data.repository

import com.goldhardt.core.data.datasource.ExpenseRemoteDataSource
import com.goldhardt.core.data.model.Expense
import com.goldhardt.core.data.model.ExpenseFormData
import com.goldhardt.core.data.model.ExpenseUpdate
import com.goldhardt.core.data.util.yearMonthToTimestampRange
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreExpenseRepository @Inject constructor(
    private val remote: ExpenseRemoteDataSource,
) : ExpenseRepository {

    override fun observeExpenses(userId: String, month: YearMonth, categoryId: String?): Flow<List<Expense>> {
        val (startTs, endTs) = yearMonthToTimestampRange(month)
        return remote.observeExpenses(userId, startTs, endTs, categoryId)
    }

    override suspend fun getExpenses(userId: String, month: YearMonth, categoryId: String?): List<Expense> {
        val (startTs, endTs) = yearMonthToTimestampRange(month)
        return remote.getExpenses(userId, startTs, endTs, categoryId)
    }

    override suspend fun addExpense(userId: String, form: ExpenseFormData): Expense {
        return remote.addExpense(userId, form)
    }

    override suspend fun updateExpense(expenseId: String, update: ExpenseUpdate) {
        remote.updateExpense(expenseId, update)
    }

    override suspend fun deleteExpense(expenseId: String) {
        remote.deleteExpense(expenseId)
    }
}
