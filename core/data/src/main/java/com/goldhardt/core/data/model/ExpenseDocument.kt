package com.goldhardt.core.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

/** Firestore document shape for an expense; its ID comes from the document path. */
internal data class ExpenseDocument(
    val name: String? = null,
    val amount: Double? = null,
    val date: Timestamp? = null,
    val categoryId: String? = null,
    @get:PropertyName("isFixed") val isFixed: Boolean? = null,
    val createdAt: Timestamp? = null,
    val userId: String? = null,
)
