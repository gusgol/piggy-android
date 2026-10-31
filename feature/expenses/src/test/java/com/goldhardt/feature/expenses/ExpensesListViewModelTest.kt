package com.goldhardt.feature.expenses

import com.goldhardt.core.auth.model.User
import com.goldhardt.core.auth.repository.AuthRepository
import com.goldhardt.core.auth.session.UserSession
import com.goldhardt.core.domain.model.Category
import com.goldhardt.core.domain.model.Expense
import com.goldhardt.core.domain.model.ExpenseFormData
import com.goldhardt.core.domain.model.ExpenseUpdate
import com.goldhardt.core.domain.repository.CategoryRepository
import com.goldhardt.core.domain.repository.ExpenseRepository
import com.goldhardt.feature.expenses.domain.AddExpenseUseCase
import com.goldhardt.feature.expenses.domain.BuildMonthlyExpensesProjectionUseCase
import com.goldhardt.feature.expenses.domain.DeleteExpenseUseCase
import com.goldhardt.feature.expenses.domain.ObserveCategoriesUseCase
import com.goldhardt.feature.expenses.domain.ObserveMonthExpensesUseCase
import com.goldhardt.feature.expenses.domain.UpdateExpenseUseCase
import java.time.Instant
import java.time.YearMonth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExpensesListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `toggling category filter pill filters transactions while keeping monthly summary totals`() = runTest {
        val oct = YearMonth.of(2026, 10)
        val categories = listOf(
            Category("housing", "Housing", Instant.EPOCH, "u1", "🏠", "#F43F5E"),
            Category("groceries", "Groceries", Instant.EPOCH, "u1", "🛒", "#059669"),
            Category("utilities", "Utilities", Instant.EPOCH, "u1", "💡", "#0891B2"),
        )
        val octExpenses = listOf(
            Expense("1", "Rent", 1000.0, Instant.EPOCH, "housing", true, Instant.EPOCH, "u1"),
            Expense("2", "Market", 200.0, Instant.EPOCH, "groceries", false, Instant.EPOCH, "u1"),
        )

        val expenseRepo = FakeExpenseRepository(mapOf(oct to octExpenses))
        val categoryRepo = FakeCategoryRepository(categories)
        val vm = buildViewModel(expenseRepo, categoryRepo)
        vm.setMonth(oct)

        val collectJob = backgroundScope.launch(testDispatcher) { vm.uiState.collect() }

        val initial = vm.uiState.value
        assertFalse(initial.isLoading)
        assertEquals(1200.0, initial.total, 0.001)
        assertEquals(1000.0, initial.fixedTotal, 0.001)
        assertEquals(200.0, initial.variableTotal, 0.001)
        assertEquals(2, initial.expenses.size)
        assertEquals(3, initial.categoryFilters.size)

        // Select "groceries" pill
        vm.selectCategory("groceries")
        val filtered = vm.uiState.value
        assertEquals("groceries", filtered.selectedCategoryId)
        assertEquals(1, filtered.expenses.size)
        assertEquals("Market", filtered.expenses.single().name)
        assertEquals(1200.0, filtered.total, 0.001)
        assertEquals(2, filtered.totalExpenseCount)

        // Tapping the same pill again toggles back to All (null)
        vm.selectCategory("groceries")
        val cleared = vm.uiState.value
        assertNull(cleared.selectedCategoryId)
        assertEquals(2, cleared.expenses.size)

        collectJob.cancel()
    }

    @Test
    fun `switching month observes new month expenses and surfaces mutation errors cleanly`() = runTest {
        val oct = YearMonth.of(2026, 10)
        val nov = YearMonth.of(2026, 11)
        val expenseRepo = FakeExpenseRepository(
            expensesByMonth = mapOf(
                oct to listOf(Expense("1", "Rent", 1000.0, Instant.EPOCH, "housing", true, Instant.EPOCH, "u1")),
                nov to emptyList(),
            ),
            failOnAdd = true,
        )
        val categoryRepo = FakeCategoryRepository(
            listOf(Category("housing", "Housing", Instant.EPOCH, "u1", "🏠", "#F43F5E"))
        )
        val vm = buildViewModel(expenseRepo, categoryRepo)
        vm.setMonth(oct)

        val collectJob = backgroundScope.launch(testDispatcher) { vm.uiState.collect() }

        assertEquals(1000.0, vm.uiState.value.total, 0.001)

        vm.setMonth(nov)
        assertEquals(nov, vm.uiState.value.month)
        assertEquals(0, vm.uiState.value.totalExpenseCount)

        vm.addExpense("Coffee", 5.0, Instant.EPOCH, "housing", false)
        assertEquals("Write failed", vm.uiState.value.error)

        vm.clearError()
        assertNull(vm.uiState.value.error)

        collectJob.cancel()
    }

    private fun buildViewModel(
        expenseRepository: ExpenseRepository,
        categoryRepository: CategoryRepository,
    ): ExpensesListViewModel {
        val user = User(id = "u1", email = "alex@piggy.app", displayName = "Alex", photoUrl = null)
        val session = UserSession(FakeAuthRepository(user))
        return ExpensesListViewModel(
            observeMonthExpenses = ObserveMonthExpensesUseCase(expenseRepository, session),
            observeCategories = ObserveCategoriesUseCase(session, categoryRepository),
            buildProjection = BuildMonthlyExpensesProjectionUseCase(),
            addExpenseUseCase = AddExpenseUseCase(session, expenseRepository),
            updateExpenseUseCase = UpdateExpenseUseCase(expenseRepository),
            deleteExpenseUseCase = DeleteExpenseUseCase(expenseRepository),
        )
    }

    private class FakeAuthRepository(override val currentUser: User?) : AuthRepository {
        override fun authState(): Flow<User?> = flowOf(currentUser)
        override suspend fun signInWithGoogle(idToken: String): Result<User> = throw UnsupportedOperationException()
        override suspend fun signOut() = Unit
    }

    private class FakeCategoryRepository(categories: List<Category>) : CategoryRepository {
        private val flow = MutableStateFlow(categories)
        override fun observeCategories(userId: String): Flow<List<Category>> = flow
        override suspend fun getCategories(userId: String): List<Category> = flow.value
        override suspend fun updateCategory(category: Category) = Unit
        override suspend fun addCategory(userId: String, name: String, icon: String, color: String): Category =
            throw UnsupportedOperationException()
        override suspend fun deleteCategory(categoryId: String) = Unit
    }

    private class FakeExpenseRepository(
        private val expensesByMonth: Map<YearMonth, List<Expense>>,
        private val failOnAdd: Boolean = false,
    ) : ExpenseRepository {
        override fun observeExpenses(userId: String, month: YearMonth, categoryId: String?): Flow<List<Expense>> =
            flowOf(expensesByMonth[month].orEmpty())

        override fun observeExpensesForRange(
            userId: String,
            startMonthInclusive: YearMonth,
            endMonthExclusive: YearMonth,
        ): Flow<List<Expense>> = flowOf(
            generateSequence(startMonthInclusive) { month -> month.plusMonths(1) }
                .takeWhile { it < endMonthExclusive }
                .flatMap { expensesByMonth[it].orEmpty() }
                .toList(),
        )

        override suspend fun getExpenses(userId: String, month: YearMonth, categoryId: String?): List<Expense> =
            expensesByMonth[month].orEmpty()

        override suspend fun addExpense(userId: String, form: ExpenseFormData): Expense {
            if (failOnAdd) throw IllegalStateException("Write failed")
            throw UnsupportedOperationException()
        }

        override suspend fun addExpenses(userId: String, forms: List<ExpenseFormData>): List<Expense> =
            throw UnsupportedOperationException()

        override suspend fun updateExpense(expenseId: String, update: ExpenseUpdate) = Unit
        override suspend fun deleteExpense(expenseId: String) = Unit
    }
}
