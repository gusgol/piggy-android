package com.goldhardt.core.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
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
    // Optional denormalized category fields for easier display
    val categoryName: String? = null,
    val categoryIcon: String? = null,
    val categoryColor: String? = null,
)

/**
 * Firestore document shape for Expense used with toObject/set operations.
 * The id is taken from the Firestore document id and is not included in the document body.
 */
internal data class ExpenseDocument(
    val name: String? = null,
    val amount: Double? = null,
    val date: Timestamp? = null,
    val categoryId: String? = null,
    @get:PropertyName("isFixed") val isFixed: Boolean? = null,
    val createdAt: Timestamp? = null,
    val userId: String? = null,
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
    val categoryName: String? = null, // allow updating denormalized field when category changes
)
