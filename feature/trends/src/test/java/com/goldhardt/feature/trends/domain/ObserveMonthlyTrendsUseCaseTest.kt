package com.goldhardt.feature.trends.domain

import com.goldhardt.core.auth.model.User
import com.goldhardt.core.auth.repository.AuthRepository
import com.goldhardt.core.auth.session.UserSession
import com.goldhardt.core.domain.model.Expense
import com.goldhardt.core.domain.model.Category
import com.goldhardt.core.domain.model.ExpenseFormData
import com.goldhardt.core.domain.model.ExpenseUpdate
import com.goldhardt.core.domain.model.ExpenseWithCategory
import com.goldhardt.core.domain.repository.ExpenseRepository
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveMonthlyTrendsUseCaseTest {

    @Test
    fun `invoke aggregates expenses by category and builds daily stacks`() = runTest {
        val month = YearMonth.of(2025, 10)
        val day5 = LocalDate.of(2025, 10, 5).atStartOfDay(ZoneId.systemDefault()).toInstant()
        val day12 = LocalDate.of(2025, 10, 12).atStartOfDay(ZoneId.systemDefault()).toInstant()

        val expenses = listOf(
            expense("1", "Lunch", 30.0, day5, "food", "Food", "🍔", "#FFADAD"),
            expense("2", "Dinner", 70.0, day12, "food", "Food", "🍔", "#FFADAD"),
            expense("3", "Bus", 25.0, day5, "transport", "Transport", "🚌", "#9BF6FF")
        )

        val user = User(id = "u1", email = "a@b.com", displayName = "Test", photoUrl = null)
        val useCase = ObserveMonthlyTrendsUseCase(
            expenseRepository = FakeExpenseRepository(expenses),
            userSession = UserSession(FakeAuthRepository(user))
        )

        val trends = useCase(month).first()

        assertEquals(125.0, trends.total, 0.001)
        assertEquals(2, trends.categories.size)
        assertEquals("Food", trends.categories[0].name)
        assertEquals(100.0, trends.categories[0].total, 0.001)
        assertEquals("Transport", trends.categories[1].name)
        assertEquals(25.0, trends.categories[1].total, 0.001)
        assertEquals(31, trends.dayStacks.size)
        assertEquals(2, trends.dayStacks[4].pieces.size) // Oct 5 has 2 categories
    }

    private fun expense(
        id: String,
        name: String,
        amount: Double,
        date: Instant,
        categoryId: String,
        categoryName: String,
        categoryIcon: String,
        categoryColor: String,
    ) = ExpenseWithCategory(
        expense = Expense(id, name, amount, date, categoryId, false, date, "u1"),
        category = Category(categoryId, categoryName, date, "u1", categoryIcon, categoryColor),
    )

    private class FakeAuthRepository(override val currentUser: User?) : AuthRepository {
        override fun authState(): Flow<User?> = flowOf(currentUser)
        override suspend fun signInWithGoogle(idToken: String): Result<User> = throw UnsupportedOperationException()
        override suspend fun signOut() = Unit
    }

    private class FakeExpenseRepository(private val expenses: List<ExpenseWithCategory>) : ExpenseRepository {
        override fun observeExpenses(userId: String, month: YearMonth, categoryId: String?): Flow<List<ExpenseWithCategory>> =
            flowOf(expenses)

        override suspend fun getExpenses(userId: String, month: YearMonth, categoryId: String?): List<ExpenseWithCategory> =
            expenses

        override suspend fun addExpense(userId: String, form: ExpenseFormData): Expense =
            throw UnsupportedOperationException()

        override suspend fun addExpenses(userId: String, forms: List<ExpenseFormData>): List<Expense> =
            throw UnsupportedOperationException()

        override suspend fun updateExpense(expenseId: String, update: ExpenseUpdate) = Unit
        override suspend fun deleteExpense(expenseId: String) = Unit
    }
}

