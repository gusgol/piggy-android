package com.goldhardt.feature.trends.domain

import com.goldhardt.core.auth.model.User
import com.goldhardt.core.auth.repository.AuthRepository
import com.goldhardt.core.auth.session.UserSession
import com.goldhardt.core.domain.model.Expense
import com.goldhardt.core.domain.model.ExpenseFormData
import com.goldhardt.core.domain.model.ExpenseUpdate
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
            Expense(
                id = "1",
                name = "Lunch",
                amount = 30.0,
                date = day5,
                categoryId = "food",
                isFixed = false,
                createdAt = day5,
                userId = "u1",
                categoryName = "Food",
                categoryIcon = "🍔",
                categoryColor = "#FFADAD"
            ),
            Expense(
                id = "2",
                name = "Dinner",
                amount = 70.0,
                date = day12,
                categoryId = "food",
                isFixed = false,
                createdAt = day12,
                userId = "u1",
                categoryName = "Food",
                categoryIcon = "🍔",
                categoryColor = "#FFADAD"
            ),
            Expense(
                id = "3",
                name = "Bus",
                amount = 25.0,
                date = day5,
                categoryId = "transport",
                isFixed = false,
                createdAt = day5,
                userId = "u1",
                categoryName = "Transport",
                categoryIcon = "🚌",
                categoryColor = "#9BF6FF"
            )
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

    private class FakeAuthRepository(override val currentUser: User?) : AuthRepository {
        override fun authState(): Flow<User?> = flowOf(currentUser)
        override suspend fun signInWithGoogle(idToken: String): Result<User> = throw UnsupportedOperationException()
        override suspend fun signOut() = Unit
    }

    private class FakeExpenseRepository(private val expenses: List<Expense>) : ExpenseRepository {
        override fun observeExpenses(userId: String, month: YearMonth, categoryId: String?): Flow<List<Expense>> =
            flowOf(expenses)

        override suspend fun getExpenses(userId: String, month: YearMonth, categoryId: String?): List<Expense> =
            expenses

        override suspend fun addExpense(userId: String, form: ExpenseFormData): Expense =
            throw UnsupportedOperationException()

        override suspend fun addExpenses(userId: String, forms: List<ExpenseFormData>): List<Expense> =
            throw UnsupportedOperationException()

        override suspend fun updateExpense(expenseId: String, update: ExpenseUpdate) = Unit
        override suspend fun deleteExpense(expenseId: String) = Unit
    }
}

