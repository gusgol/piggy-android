package com.goldhardt.core.data.repository

import com.goldhardt.core.data.datasource.CategoryRemoteDataSource
import com.goldhardt.core.data.datasource.ExpenseRemoteDataSource
import com.goldhardt.core.data.model.Expense
import com.goldhardt.core.data.model.ExpenseFormData
import com.goldhardt.core.data.model.ExpenseUpdate
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

    override fun observeExpenses(userId: String, month: YearMonth, categoryId: String?): Flow<List<Expense>> {
        val (startAt, endAt) = yearMonthToInstantRange(month)
        val expensesFlow = remote.observeExpenses(userId, startAt, endAt, categoryId)
        val categoriesFlow = categoryRemote.observeCategories(userId)
        return expensesFlow.combine(categoriesFlow) { expenses, categories ->
            val categoryById = categories.associateBy { it.id }
            expenses.map { expense ->
                val category = categoryById[expense.categoryId]
                expense.copy(
                    categoryName = category?.name ?: expense.categoryName,
                    categoryIcon = category?.icon ?: expense.categoryIcon,
                    categoryColor = category?.color ?: expense.categoryColor,
                )
            }
        }
    }

    override suspend fun getExpenses(userId: String, month: YearMonth, categoryId: String?): List<Expense> {
        val (startAt, endAt) = yearMonthToInstantRange(month)
        return remote.getExpenses(userId, startAt, endAt, categoryId)
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
}
