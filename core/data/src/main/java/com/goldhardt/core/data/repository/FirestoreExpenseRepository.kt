package com.goldhardt.core.data.repository

import com.goldhardt.core.data.datasource.CategoryRemoteDataSource
import com.goldhardt.core.data.datasource.ExpenseRemoteDataSource
import com.goldhardt.core.domain.model.Expense
import com.goldhardt.core.domain.model.Category
import com.goldhardt.core.domain.model.ExpenseFormData
import com.goldhardt.core.domain.model.ExpenseUpdate
import com.goldhardt.core.domain.model.ExpenseWithCategory
import com.goldhardt.core.domain.repository.ExpenseRepository
import com.goldhardt.core.data.util.yearMonthToInstantRange
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@Singleton
class FirestoreExpenseRepository @Inject constructor(
    private val remote: ExpenseRemoteDataSource,
    private val categoryRemote: CategoryRemoteDataSource,
) : ExpenseRepository {

    override fun observeExpenses(userId: String, month: YearMonth, categoryId: String?): Flow<List<ExpenseWithCategory>> {
        val (startAt, endAt) = yearMonthToInstantRange(month)
        val expensesFlow = remote.observeExpenses(userId, startAt, endAt, categoryId)
        val categoriesFlow = categoryRemote.observeCategories(userId)
        return expensesFlow.combine(categoriesFlow, ::withCategories)
    }

    override suspend fun getExpenses(userId: String, month: YearMonth, categoryId: String?): List<ExpenseWithCategory> {
        val (startAt, endAt) = yearMonthToInstantRange(month)
        val expenses = remote.getExpenses(userId, startAt, endAt, categoryId)
        val categories = categoryRemote.getCategories(userId)
        return withCategories(expenses, categories)
    }

    override suspend fun addExpense(userId: String, form: ExpenseFormData): Expense {
        return remote.addExpense(userId, form)
    }

    override suspend fun addExpenses(userId: String, forms: List<ExpenseFormData>): List<Expense> {
        return remote.addExpenses(userId, forms)
    }

    override suspend fun updateExpense(expenseId: String, update: ExpenseUpdate) {
        remote.updateExpense(expenseId, update)
    }

    override suspend fun deleteExpense(expenseId: String) {
        remote.deleteExpense(expenseId)
    }

    private fun withCategories(expenses: List<Expense>, categories: List<Category>): List<ExpenseWithCategory> {
        val categoryById = categories.associateBy { it.id }
        return expenses.map { expense ->
            ExpenseWithCategory(expense = expense, category = categoryById[expense.categoryId])
        }
    }
}
