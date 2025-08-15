package com.goldhardt.core.data.repository

import com.goldhardt.core.data.datasource.ExpenseRemoteDataSource
import com.goldhardt.core.data.model.Expense
import com.goldhardt.core.data.model.ExpenseFormData
import com.goldhardt.core.data.model.ExpenseUpdate
import com.goldhardt.core.data.util.yearMonthToTimestampRange
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreExpenseRepository @Inject constructor(
    private val remote: ExpenseRemoteDataSource,
    private val categoryRepository: CategoryRepository,
) : ExpenseRepository {

    override fun observeExpenses(userId: String, month: YearMonth, categoryId: String?): Flow<List<Expense>> {
        val (startTs, endTs) = yearMonthToTimestampRange(month)
        val expensesFlow = remote.observeExpenses(userId, startTs, endTs, categoryId)
        val categoriesFlow = categoryRepository.observeCategories(userId)
        return expensesFlow.combine(categoriesFlow) { expenses, categories ->
            val map = categories.associateBy { it.id }
            expenses.map { e ->
                val c = map[e.categoryId]
                e.copy(
                    categoryName = c?.name ?: e.categoryName,
                    categoryIcon = c?.icon ?: e.categoryIcon,
                    categoryColor = c?.color ?: e.categoryColor,
                )
            }
        }
    }

    override suspend fun getExpenses(userId: String, month: YearMonth, categoryId: String?): List<Expense> {
        val (startTs, endTs) = yearMonthToTimestampRange(month)
        val expenses = remote.getExpenses(userId, startTs, endTs, categoryId)
        val categories = categoryRepository.observeCategories(userId).first()
        val map = categories.associateBy { it.id }
        return expenses.map { e ->
            val c = map[e.categoryId]
            e.copy(
                categoryName = c?.name ?: e.categoryName,
                categoryIcon = c?.icon ?: e.categoryIcon,
                categoryColor = c?.color ?: e.categoryColor,
            )
        }
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
