package com.goldhardt.core.data.model

import com.google.firebase.Timestamp

/**
 * Data model representing a category as stored/fetched from Firestore.
 */
data class Category(
    val id: String,
    val name: String,
    val createdAt: Timestamp, // TODO leaking firebase details.
    val userId: String,
    val icon: String,
    val color: String,
)

/**
 * Firestore document shape for Category used with toObject/set operations.
 * The id is taken from the Firestore document id and is not included in the document body.
 */
internal data class CategoryDocument(
    val name: String? = null,
    val createdAt: Timestamp? = null,
    val userId: String? = null,
    val icon: String? = null,
    val color: String? = null,
)

