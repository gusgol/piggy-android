package com.goldhardt.feature.trends.domain

import com.goldhardt.core.auth.session.UserSession
import com.goldhardt.core.domain.model.Expense
import com.goldhardt.core.domain.repository.ExpenseRepository
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveYearlyTrendsUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val userSession: UserSession,
) {
    operator fun invoke(referenceMonth: YearMonth): Flow<YearlyTrendsData> {
        val startMonth = referenceMonth.minusMonths(11)
        val endMonthExclusive = referenceMonth.plusMonths(1)
        return userSession.authState.flatMapLatest { user ->
            val uid = user?.id ?: return@flatMapLatest flowOf(YearlyTrendsData.empty(referenceMonth))
            expenseRepository.observeExpensesForRange(uid, startMonth, endMonthExclusive).map { expenses ->
                buildYearlyTrends(referenceMonth, expenses)
            }
        }
    }

    private fun buildYearlyTrends(referenceMonth: YearMonth, expenses: List<Expense>): YearlyTrendsData {
        val expensesByMonth = expenses.groupBy { expense ->
            YearMonth.from(expense.date.atZone(ZoneId.systemDefault()))
        }
        val months = (0L..11L).map { monthsAgo ->
            val month = referenceMonth.minusMonths(monthsAgo)
            val categories = expensesByMonth[month].orEmpty()
                .groupBy { it.categoryId }
                .map { (categoryId, categoryExpenses) ->
                    MonthlyCategorySpend(
                        id = categoryId,
                        name = categoryExpenses.firstNotNullOfOrNull { it.categoryName } ?: "Other",
                        colorHex = categoryExpenses.firstNotNullOfOrNull { it.categoryColor },
                        total = categoryExpenses.sumOf { it.amount },
                    )
                }
                .sortedByDescending { it.total }
            YearlyMonthSpend(
                month = month,
                total = categories.sumOf { it.total },
                categories = categories,
            )
        }
        return YearlyTrendsData(months)
    }
}

data class YearlyTrendsData(
    val months: List<YearlyMonthSpend>,
) {
    companion object {
        fun empty(referenceMonth: YearMonth): YearlyTrendsData =
            YearlyTrendsData((0L..11L).map { offset ->
                YearlyMonthSpend(referenceMonth.minusMonths(offset), 0.0, emptyList())
            })
    }
}

data class YearlyMonthSpend(
    val month: YearMonth,
    val total: Double,
    val categories: List<MonthlyCategorySpend>,
)

data class MonthlyCategorySpend(
    val id: String,
    val name: String,
    val colorHex: String?,
    val total: Double,
)
