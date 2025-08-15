package com.goldhardt.feature.expenses.domain

import com.goldhardt.core.auth.repository.AuthRepository
import com.goldhardt.core.data.model.Expense
import com.goldhardt.core.data.repository.CategoryRepository
import com.goldhardt.core.data.repository.ExpenseRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.time.YearMonth
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveMonthExpensesUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val authRepository: AuthRepository,
) {
    operator fun invoke(
        month: YearMonth,
        categoryId: String? = null,
    ): Flow<List<Expense>> {
        return authRepository.authState().flatMapLatest { user ->
            val uid = user?.id
            if (uid == null) {
                flowOf(emptyList())
            } else {
                val expensesFlow = expenseRepository.observeExpenses(uid, month, categoryId)
                val categoriesFlow = categoryRepository.observeCategories(uid)
                expensesFlow.combine(categoriesFlow) { expenses, categories ->
                    val catMap = categories.associateBy { it.id }
                    expenses.map { e ->
                        val c = catMap[e.categoryId]
                        e.copy(
                            categoryName = c?.name ?: e.categoryName,
                            categoryIcon = c?.icon ?: e.categoryIcon,
                            categoryColor = c?.color ?: e.categoryColor,
                        )
                    }
                }
            }
        }
    }
}