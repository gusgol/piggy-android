package com.goldhardt.core.data.repository

import com.goldhardt.core.data.datasource.CategoryRemoteDataSource
import com.goldhardt.core.data.datasource.ExpenseRemoteDataSource
import com.goldhardt.core.domain.model.Category
import com.goldhardt.core.domain.model.Expense
import com.goldhardt.core.domain.model.ExpenseFormData
import com.goldhardt.core.domain.model.ExpenseUpdate
import java.time.Instant
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FirestoreExpenseRepositoryTest {

    @Test
    fun `observeExpenses enriches expenses with category metadata`() = runTest {
        val now = Instant.parse("2025-10-15T12:00:00Z")
        val rawExpenses = listOf(
            Expense(
                id = "e1",
                name = "Supermarket",
                amount = 85.0,
                date = now,
                categoryId = "c1",
                isFixed = false,
                createdAt = now,
                userId = "u1"
            )
        )
        val categories = listOf(
            Category(
                id = "c1",
                name = "Groceries",
                createdAt = now,
                userId = "u1",
                icon = "🛒",
                color = "#CAFFBF"
            )
        )

        val repository = FirestoreExpenseRepository(
            remote = FakeExpenseRemoteDataSource(rawExpenses),
            categoryRemote = FakeCategoryRemoteDataSource(categories)
        )

        val enriched = repository.observeExpenses("u1", YearMonth.of(2025, 10)).first()

        assertEquals(1, enriched.size)
        assertEquals("Groceries", enriched[0].category?.name)
        assertEquals("🛒", enriched[0].category?.icon)
        assertEquals("#CAFFBF", enriched[0].category?.color)
    }

    private class FakeExpenseRemoteDataSource(private val items: List<Expense>) : ExpenseRemoteDataSource {
        override fun observeExpenses(
            userId: String,
            startAt: Instant,
            endAt: Instant,
            categoryId: String?
        ): Flow<List<Expense>> = flowOf(items)

        override suspend fun getExpenses(
            userId: String,
            startAt: Instant,
            endAt: Instant,
            categoryId: String?
        ): List<Expense> = items

        override suspend fun addExpense(userId: String, form: ExpenseFormData): Expense =
            throw UnsupportedOperationException()

        override suspend fun addExpenses(userId: String, forms: List<ExpenseFormData>): List<Expense> =
            throw UnsupportedOperationException()

        override suspend fun updateExpense(expenseId: String, update: ExpenseUpdate) = Unit
        override suspend fun deleteExpense(expenseId: String) = Unit
    }

    private class FakeCategoryRemoteDataSource(private val categories: List<Category>) : CategoryRemoteDataSource {
        override fun observeCategories(userId: String): Flow<List<Category>> = flowOf(categories)
        override suspend fun getCategories(userId: String): List<Category> = categories
        override suspend fun updateCategory(category: Category) = Unit
        override suspend fun addCategory(userId: String, name: String, icon: String, color: String): Category =
            throw UnsupportedOperationException()
        override suspend fun deleteCategory(categoryId: String) = Unit
    }
}

