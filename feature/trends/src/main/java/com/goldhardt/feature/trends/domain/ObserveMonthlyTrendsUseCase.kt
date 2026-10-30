package com.goldhardt.feature.trends.domain

import com.goldhardt.core.auth.session.UserSession
import com.goldhardt.core.domain.model.ExpenseWithCategory
import com.goldhardt.core.domain.repository.ExpenseRepository
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveMonthlyTrendsUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val userSession: UserSession,
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()).withZone(ZoneId.systemDefault())

    operator fun invoke(month: YearMonth): Flow<TrendsData> {
        return userSession.authState.flatMapLatest { user ->
            val uid = user?.id ?: return@flatMapLatest flowOf(TrendsData.empty(month))
            expenseRepository.observeExpenses(uid, month, null).map { expenses ->
                buildTrends(month, expenses)
            }
        }
    }

    private fun buildTrends(month: YearMonth, expenses: List<ExpenseWithCategory>): TrendsData {
        if (expenses.isEmpty()) return TrendsData(total = 0.0, slices = emptyList(), categories = emptyList(), dayStacks = emptyList())

        val groups = expenses.groupBy { it.expense.categoryId }
        val categories = groups.map { (categoryId, items) ->
            val name = items.firstNotNullOfOrNull { it.category?.name } ?: "Other"
            val icon = items.firstNotNullOfOrNull { it.category?.icon } ?: "🏷️"
            val color = items.firstNotNullOfOrNull { it.category?.color }
            val total = items.sumOf { it.expense.amount }
            val uiItems = items.sortedByDescending { it.expense.date }.map { item ->
                CategoryExpenseItem(
                    name = item.expense.name,
                    amount = item.expense.amount,
                    dateLabel = try { dateFormatter.format(item.expense.date) } catch (_: Throwable) { "" }
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
        val byDay: Map<Int, List<ExpenseWithCategory>> = expenses.groupBy { item ->
            try { item.expense.date.atZone(ZoneId.systemDefault()).dayOfMonth } catch (_: Throwable) { 1 }
        }
        val dayStacks = (1..daysInMonth).map { day ->
            val items = byDay[day].orEmpty()
            val pieces = items
                .groupBy { it.expense.categoryId }
                .map { (categoryId, list) ->
                    val amount = list.sumOf { it.expense.amount }
                    val colorHex = list.firstNotNullOfOrNull { it.category?.color }
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
