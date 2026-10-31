package com.goldhardt.feature.expenses.domain

import com.goldhardt.core.domain.model.Category
import com.goldhardt.core.domain.model.Expense
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildMonthlyExpensesProjectionUseCaseTest {

    private val useCase = BuildMonthlyExpensesProjectionUseCase()

    private val categories = listOf(
        Category(id = "utilities", name = "Utilities", createdAt = Instant.EPOCH, userId = "u1", icon = "💡", color = "#0891B2"),
        Category(id = "housing", name = "Housing", createdAt = Instant.EPOCH, userId = "u1", icon = "🏠", color = "#F43F5E"),
        Category(id = "groceries", name = "Groceries", createdAt = Instant.EPOCH, userId = "u1", icon = "🛒", color = "#059669"),
        Category(id = "dining", name = "Dining Out", createdAt = Instant.EPOCH, userId = "u1", icon = "🍔", color = "#D97706"),
    )

    private val expenses = listOf(
        Expense(
            id = "e1",
            name = "Rent",
            amount = 1450.0,
            date = Instant.parse("2026-10-01T00:00:00Z"),
            categoryId = "housing",
            isFixed = true,
            createdAt = Instant.EPOCH,
            userId = "u1",
        ),
        Expense(
            id = "e2",
            name = "Market",
            amount = 184.50,
            date = Instant.parse("2026-10-12T00:00:00Z"),
            categoryId = "groceries",
            isFixed = false,
            createdAt = Instant.EPOCH,
            userId = "u1",
        ),
        Expense(
            id = "e3",
            name = "Dinner",
            amount = 65.50,
            date = Instant.parse("2026-10-15T00:00:00Z"),
            categoryId = "dining",
            isFixed = false,
            createdAt = Instant.EPOCH,
            userId = "u1",
        ),
    )

    @Test
    fun `computes full month totals and orders active category pills before unused ones`() {
        val result = useCase(
            expenses = expenses,
            categories = categories,
            selectedCategoryId = null,
        )

        assertEquals(1700.0, result.summary.totalAmount, 0.001)
        assertEquals(1450.0, result.summary.fixedAmount, 0.001)
        assertEquals(250.0, result.summary.variableAmount, 0.001)
        assertEquals(3, result.summary.totalCount)
        assertEquals(3, result.filteredExpenses.size)
        assertNull(result.effectiveSelectedCategoryId)

        // Active categories (housing, groceries, dining) come before unused (utilities)
        assertEquals(
            listOf("housing", "groceries", "dining", "utilities"),
            result.categoryFilters.map { it.id }
        )
        assertEquals(0, result.categoryFilters.last().expenseCount)
    }

    @Test
    fun `filtering by category keeps monthly summary totals fixed while filtering transactions`() {
        val result = useCase(
            expenses = expenses,
            categories = categories,
            selectedCategoryId = "dining",
        )

        assertEquals(1700.0, result.summary.totalAmount, 0.001)
        assertEquals(1450.0, result.summary.fixedAmount, 0.001)
        assertEquals(250.0, result.summary.variableAmount, 0.001)
        assertEquals(3, result.summary.totalCount)

        assertEquals("dining", result.effectiveSelectedCategoryId)
        assertEquals(1, result.filteredExpenses.size)
        assertEquals("Dinner", result.filteredExpenses.single().name)
        assertEquals("Dining Out", result.filteredExpenses.single().categoryName)
    }

    @Test
    fun `filtering by unused category returns empty filtered list while retaining month summary`() {
        val result = useCase(
            expenses = expenses,
            categories = categories,
            selectedCategoryId = "utilities",
        )

        assertEquals("utilities", result.effectiveSelectedCategoryId)
        assertTrue(result.filteredExpenses.isEmpty())
        assertEquals(3, result.summary.totalCount)
    }

    @Test
    fun `unknown selected category resets effectiveSelectedCategoryId to null`() {
        val result = useCase(
            expenses = expenses,
            categories = categories,
            selectedCategoryId = "deleted-cat",
        )

        assertNull(result.effectiveSelectedCategoryId)
        assertEquals(3, result.filteredExpenses.size)
    }

    @Test
    fun `computes ControlledVariable insight when fixed costs dominate`() {
        val result = useCase(
            expenses = expenses,
            categories = categories,
            selectedCategoryId = null,
        )

        val insight = result.insight
        assertTrue(insight is SpendingInsight.ControlledVariable)
        val controlled = insight as SpendingInsight.ControlledVariable
        assertEquals(250.0, controlled.variableAmount, 0.001)
        assertEquals(85, controlled.fixedPercentage)
    }

    @Test
    fun `computes TopCategory insight when variable spending dominates`() {
        val variableHeavy = listOf(
            Expense(
                id = "e1",
                name = "Gym",
                amount = 50.0,
                date = Instant.EPOCH,
                categoryId = "utilities",
                isFixed = true,
                createdAt = Instant.EPOCH,
                userId = "u1",
            ),
            Expense(
                id = "e2",
                name = "Steakhouse",
                amount = 150.0,
                date = Instant.EPOCH,
                categoryId = "dining",
                isFixed = false,
                createdAt = Instant.EPOCH,
                userId = "u1",
            ),
        )

        val result = useCase(
            expenses = variableHeavy,
            categories = categories,
            selectedCategoryId = null,
        )

        val insight = result.insight
        assertTrue(insight is SpendingInsight.TopCategory)
        val top = insight as SpendingInsight.TopCategory
        assertEquals("Dining Out", top.categoryName)
        assertEquals(75, top.sharePercentage)
        assertTrue(top.isVariableCategory)
    }
}

