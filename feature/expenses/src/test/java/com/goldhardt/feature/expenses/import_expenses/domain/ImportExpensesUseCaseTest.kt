package com.goldhardt.feature.expenses.import_expenses.domain

import com.goldhardt.core.auth.model.User
import com.goldhardt.core.auth.repository.AuthRepository
import com.goldhardt.core.auth.session.UserSession
import com.goldhardt.core.domain.model.Expense
import com.goldhardt.core.domain.model.ExpenseFormData
import com.goldhardt.core.domain.model.ExpenseUpdate
import com.goldhardt.core.domain.model.ExpenseWithCategory
import com.goldhardt.core.domain.repository.ExpenseRepository
import java.time.Instant
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ImportExpensesUseCaseTest {

    @Test
    fun `invoke batch-imports assigned expenses and reports partial failure for unassigned ones`() = runTest {
        val fakeAuth = FakeAuthRepository(User(id = "u1", email = "a@b.com", displayName = "Test", photoUrl = null))
        val fakeExpenseRepo = FakeExpenseRepository()
        val useCase = ImportExpensesUseCase(UserSession(fakeAuth), fakeExpenseRepo)

        val items = listOf(
            ImportedExpense(
                name = "Assigned",
                amount = 20.0,
                date = Instant.parse("2025-10-10T00:00:00Z"),
                categoryId = "cat-1",
                isFixed = false,
                originalLine = "2025-10-10,Assigned,20.0"
            ),
            ImportedExpense(
                name = "Unassigned",
                amount = 15.0,
                date = Instant.parse("2025-10-11T00:00:00Z"),
                categoryId = null,
                isFixed = false,
                originalLine = "2025-10-11,Unassigned,15.0"
            )
        )

        val result = useCase(items)

        assertTrue(result is ImportResult.PartialSuccess)
        val partial = result as ImportResult.PartialSuccess
        assertEquals(1, partial.successCount)
        assertEquals(1, partial.failures.size)
        assertEquals(1, fakeExpenseRepo.batchAdded.size)
        assertEquals("Assigned", fakeExpenseRepo.batchAdded.first().name)
    }

    @Test
    fun `invoke returns Error when user is not signed in`() = runTest {
        val fakeAuth = FakeAuthRepository(currentUser = null)
        val fakeExpenseRepo = FakeExpenseRepository()
        val useCase = ImportExpensesUseCase(UserSession(fakeAuth), fakeExpenseRepo)

        val result = useCase(emptyList())

        assertTrue(result is ImportResult.Error)
        assertEquals("User must be signed in", (result as ImportResult.Error).message)
    }

    private class FakeAuthRepository(override val currentUser: User?) : AuthRepository {
        override fun authState(): Flow<User?> = flowOf(currentUser)
        override suspend fun signInWithGoogle(idToken: String): Result<User> = throw UnsupportedOperationException()
        override suspend fun signOut() = Unit
    }

    private class FakeExpenseRepository : ExpenseRepository {
        val batchAdded = mutableListOf<ExpenseFormData>()

        override fun observeExpenses(userId: String, month: YearMonth, categoryId: String?): Flow<List<ExpenseWithCategory>> =
            flowOf(emptyList())

        override suspend fun getExpenses(userId: String, month: YearMonth, categoryId: String?): List<ExpenseWithCategory> =
            emptyList()

        override suspend fun addExpense(userId: String, form: ExpenseFormData): Expense =
            throw UnsupportedOperationException()

        override suspend fun addExpenses(userId: String, forms: List<ExpenseFormData>): List<Expense> {
            batchAdded.addAll(forms)
            return forms.mapIndexed { idx, f ->
                Expense(
                    id = "id-$idx",
                    name = f.name,
                    amount = f.amount,
                    date = f.date,
                    categoryId = f.categoryId,
                    isFixed = f.isFixed,
                    createdAt = Instant.now(),
                    userId = userId,
                )
            }
        }

        override suspend fun updateExpense(expenseId: String, update: ExpenseUpdate) = Unit
        override suspend fun deleteExpense(expenseId: String) = Unit
    }
}

