package com.goldhardt.core.data.model

import com.google.firebase.Timestamp

/** Firestore document shape for a category; its ID comes from the document path. */
internal data class CategoryDocument(
    val name: String? = null,
    val createdAt: Timestamp? = null,
    val userId: String? = null,
    val icon: String? = null,
    val color: String? = null,
)
