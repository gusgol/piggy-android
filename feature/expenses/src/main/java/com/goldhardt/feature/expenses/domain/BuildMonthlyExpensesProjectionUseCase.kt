package com.goldhardt.feature.expenses.domain

import com.goldhardt.core.domain.model.Category
import com.goldhardt.core.domain.model.Expense
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Pure domain read-model projection for the monthly Expenses screen.
 * Keeps monthly summary figures anchored to the full month while filtering the transaction feed
 * by the active category pill.
 */
data class MonthlyExpensesProjection(
    val summary: MonthlyExpensesSummary,
    val categoryFilters: List<CategoryFilterOption>,
    val effectiveSelectedCategoryId: String?,
    val filteredExpenses: List<Expense>,
    val insight: SpendingInsight?,
)

data class MonthlyExpensesSummary(
    val totalAmount: Double = 0.0,
    val fixedAmount: Double = 0.0,
    val variableAmount: Double = 0.0,
    val fixedFraction: Float = 0f,
    val variableFraction: Float = 0f,
    val totalCount: Int = 0,
)

data class CategoryFilterOption(
    val id: String,
    val name: String,
    val icon: String?,
    val colorHex: String?,
    val expenseCount: Int,
)

sealed interface SpendingInsight {
    data class AllFixed(val fixedAmount: Double) : SpendingInsight

    data class ControlledVariable(
        val variableAmount: Double,
        val fixedPercentage: Int,
    ) : SpendingInsight

    data class TopCategory(
        val categoryName: String,
        val amount: Double,
        val sharePercentage: Int,
        val isVariableCategory: Boolean,
    ) : SpendingInsight
}

class BuildMonthlyExpensesProjectionUseCase @Inject constructor() {

    operator fun invoke(
        expenses: List<Expense>,
        categories: List<Category>,
        selectedCategoryId: String?,
    ): MonthlyExpensesProjection {
        val fixedAmount = expenses.asSequence().filter { it.isFixed }.sumOf { it.amount }
        val variableAmount = expenses.asSequence().filterNot { it.isFixed }.sumOf { it.amount }
        val totalAmount = fixedAmount + variableAmount

        val fixedFraction = if (totalAmount > 0.0) {
            (fixedAmount / totalAmount).toFloat().coerceIn(0f, 1f)
        } else {
            0f
        }
        val variableFraction = if (totalAmount > 0.0) {
            (variableAmount / totalAmount).toFloat().coerceIn(0f, 1f)
        } else {
            0f
        }

        val summary = MonthlyExpensesSummary(
            totalAmount = totalAmount,
            fixedAmount = fixedAmount,
            variableAmount = variableAmount,
            fixedFraction = fixedFraction,
            variableFraction = variableFraction,
            totalCount = expenses.size,
        )

        val expensesByCategory = expenses.groupBy { it.categoryId }
        val knownCategoryIds = categories.asSequence().map { it.id }.toSet()

        val userCategoryOptions = categories.map { category ->
            CategoryFilterOption(
                id = category.id,
                name = category.name,
                icon = category.icon,
                colorHex = category.color,
                expenseCount = expensesByCategory[category.id]?.size ?: 0,
            )
        }

        // Defensive fallback if an expense points to a deleted or not-yet-synced category
        val orphanedCategoryOptions = expensesByCategory
            .filterKeys { it !in knownCategoryIds && it.isNotBlank() }
            .map { (catId, catExpenses) ->
                val sample = catExpenses.first()
                CategoryFilterOption(
                    id = catId,
                    name = sample.categoryName?.takeIf { it.isNotBlank() } ?: "Other",
                    icon = sample.categoryIcon,
                    colorHex = sample.categoryColor,
                    expenseCount = catExpenses.size,
                )
            }

        // Stable sort keeps active categories upfront without reshuffling when pills are tapped
        val categoryFilters = (userCategoryOptions + orphanedCategoryOptions)
            .sortedByDescending { it.expenseCount > 0 }

        val effectiveSelectedCategoryId = selectedCategoryId?.takeIf { targetId ->
            categoryFilters.any { it.id == targetId }
        }

        val categoryById = categories.associateBy { it.id }
        val enrichedExpenses = expenses.map { expense ->
            val cat = categoryById[expense.categoryId]
            if (cat != null) {
                expense.copy(
                    categoryName = cat.name,
                    categoryIcon = cat.icon,
                    categoryColor = cat.color,
                )
            } else {
                expense
            }
        }

        val filteredExpenses = if (effectiveSelectedCategoryId == null) {
            enrichedExpenses
        } else {
            enrichedExpenses.filter { it.categoryId == effectiveSelectedCategoryId }
        }

        val insight = computeInsight(
            expenses = enrichedExpenses,
            totalAmount = totalAmount,
            fixedAmount = fixedAmount,
            variableAmount = variableAmount,
            fixedFraction = fixedFraction,
        )

        return MonthlyExpensesProjection(
            summary = summary,
            categoryFilters = categoryFilters,
            effectiveSelectedCategoryId = effectiveSelectedCategoryId,
            filteredExpenses = filteredExpenses,
            insight = insight,
        )
    }

    private fun computeInsight(
        expenses: List<Expense>,
        totalAmount: Double,
        fixedAmount: Double,
        variableAmount: Double,
        fixedFraction: Float,
    ): SpendingInsight? {
        if (expenses.isEmpty() || totalAmount <= 0.0) return null

        if (variableAmount <= 0.0 && fixedAmount > 0.0) {
            return SpendingInsight.AllFixed(fixedAmount = fixedAmount)
        }

        if (fixedFraction >= 0.65f) {
            return SpendingInsight.ControlledVariable(
                variableAmount = variableAmount,
                fixedPercentage = (fixedFraction * 100f).roundToInt().coerceIn(0, 100),
            )
        }

        val variableByCategory = expenses
            .filterNot { it.isFixed }
            .groupBy { it.categoryId }
            .mapValues { (_, list) -> list.sumOf { it.amount } to list.first() }

        val topEntry = variableByCategory.maxByOrNull { it.value.first }
        if (topEntry != null && topEntry.value.first > 0.0) {
            val (catAmount, sample) = topEntry.value
            val catName = sample.categoryName?.takeIf { it.isNotBlank() } ?: "Other"
            val sharePct = ((catAmount / totalAmount) * 100.0).roundToInt().coerceIn(1, 100)
            return SpendingInsight.TopCategory(
                categoryName = catName,
                amount = catAmount,
                sharePercentage = sharePct,
                isVariableCategory = true,
            )
        }

        val overallTop = expenses
            .groupBy { it.categoryId }
            .mapValues { (_, list) -> list.sumOf { it.amount } to list.first() }
            .maxByOrNull { it.value.first }
            ?: return null

        val (catAmount, sample) = overallTop.value
        val catName = sample.categoryName?.takeIf { it.isNotBlank() } ?: "Other"
        val sharePct = ((catAmount / totalAmount) * 100.0).roundToInt().coerceIn(1, 100)
        return SpendingInsight.TopCategory(
            categoryName = catName,
            amount = catAmount,
            sharePercentage = sharePct,
            isVariableCategory = false,
        )
    }
}

