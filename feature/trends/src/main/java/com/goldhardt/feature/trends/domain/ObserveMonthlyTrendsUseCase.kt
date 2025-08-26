package com.goldhardt.feature.trends.domain

import com.goldhardt.core.auth.repository.AuthRepository
import com.goldhardt.core.data.model.Expense
import com.goldhardt.core.data.repository.CategoryRepository
import com.goldhardt.core.data.repository.ExpenseRepository
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.time.ZoneId
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveMonthlyTrendsUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val authRepository: AuthRepository,
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()).withZone(ZoneId.systemDefault())

    operator fun invoke(month: YearMonth): Flow<TrendsData> {
        return authRepository.authState().flatMapLatest { user ->
            val uid = user?.id ?: return@flatMapLatest flowOf(TrendsData.empty(month))
            val expensesFlow = expenseRepository.observeExpenses(uid, month, null)
            val categoriesFlow = categoryRepository.observeCategories(uid)
            expensesFlow.combine(categoriesFlow) { expenses, categories ->
                val catMap = categories.associateBy { it.id }
                // decorate expenses with latest category fields if missing
                val enriched = expenses.map { e ->
                    val c = catMap[e.categoryId]
                    e.copy(
                        categoryName = c?.name ?: e.categoryName,
                        categoryIcon = c?.icon ?: e.categoryIcon,
                        categoryColor = c?.color ?: e.categoryColor,
                    )
                }
                buildTrends(month, enriched)
            }
        }
    }

    private fun buildTrends(month: YearMonth, expenses: List<Expense>): TrendsData {
        if (expenses.isEmpty()) return TrendsData(total = 0.0, slices = emptyList(), categories = emptyList(), dayStacks = emptyList())

        val groups = expenses.groupBy { it.categoryId }
        val categories = groups.map { (categoryId, items) ->
            val name = items.firstNotNullOfOrNull { it.categoryName } ?: "Other"
            val icon = items.firstNotNullOfOrNull { it.categoryIcon } ?: "🏷️"
            val color = items.firstNotNullOfOrNull { it.categoryColor }
            val total = items.sumOf { it.amount }
            val uiItems = items.sortedByDescending { it.date }.map {
                CategoryExpenseItem(
                    name = it.name,
                    amount = it.amount,
                    dateLabel = try { dateFormatter.format(it.date) } catch (_: Throwable) { "" }
                )
            }
            CategoryAggregate(
                id = categoryId,
                name = name,
                icon = icon,
                colorHex = color,
                total = total,
                items = uiItems
            )
        }.sortedByDescending { it.total }

        val total = categories.sumOf { it.total }
        val slices = categories.map { Slice(it.id, it.name, it.colorHex, it.total) }

        // Build day-level stacks across the entire selected month
        val daysInMonth = month.lengthOfMonth()
        val byDay: Map<Int, List<Expense>> = expenses.groupBy { exp ->
            try { exp.date.atZone(ZoneId.systemDefault()).dayOfMonth } catch (_: Throwable) { 1 }
        }
        val dayStacks = (1..daysInMonth).map { day ->
            val items = byDay[day].orEmpty()
            val pieces = items
                .groupBy { it.categoryId }
                .map { (categoryId, list) ->
                    val amount = list.sumOf { it.amount }
                    val colorHex = list.firstNotNullOfOrNull { it.categoryColor }
                    DayPiece(categoryId = categoryId, amount = amount, colorHex = colorHex)
                }
                .sortedByDescending { it.amount }
            DayStack(day = day, pieces = pieces)
        }

        return TrendsData(total = total, slices = slices, categories = categories, dayStacks = dayStacks)
    }
}

// Domain models for trends

data class TrendsData(
    val total: Double,
    val slices: List<Slice>,
    val categories: List<CategoryAggregate>,
    val dayStacks: List<DayStack>
) {
    companion object {
        fun empty(month: YearMonth) = TrendsData(0.0, emptyList(), emptyList(), emptyList())
    }
}

data class Slice(
    val categoryId: String,
    val name: String,
    val colorHex: String?,
    val total: Double,
)

data class CategoryAggregate(
    val id: String,
    val name: String,
    val icon: String,
    val colorHex: String?,
    val total: Double,
    val items: List<CategoryExpenseItem>,
)

// New daily stacked bar domain models

data class DayStack(
    val day: Int,
    val pieces: List<DayPiece>,
)

data class DayPiece(
    val categoryId: String,
    val amount: Double,
    val colorHex: String?,
)

data class CategoryExpenseItem(
    val name: String,
    val amount: Double,
    val dateLabel: String,
)
