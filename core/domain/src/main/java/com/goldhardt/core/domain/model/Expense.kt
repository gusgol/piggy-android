package com.goldhardt.core.domain.model

import java.time.Instant

/**
 * Domain model representing an expense. Firebase-agnostic.
 */
data class Expense(
    val id: String,
    val name: String,
    val amount: Double,
    val date: Instant,
    val categoryId: String,
    val isFixed: Boolean,
    val createdAt: Instant,
    val userId: String,
)

/** Expense read result with its current category metadata. */
data class ExpenseWithCategory(
    val expense: Expense,
    val category: Category?,
)

/** Shape for creating a new expense. */
data class ExpenseFormData(
    val name: String,
    val amount: Double,
    val date: Instant,
    val categoryId: String,
    val isFixed: Boolean,
)

/** Partial update for an expense. Only non-null fields will be updated. */
data class ExpenseUpdate(
    val name: String? = null,
    val amount: Double? = null,
    val date: Instant? = null,
    val categoryId: String? = null,
    val isFixed: Boolean? = null,
)
