package com.goldhardt.core.domain.model

import java.time.Instant

/**
 * Domain model representing a category. Firebase-agnostic.
 */
data class Category(
    val id: String,
    val name: String,
    val createdAt: Instant,
    val userId: String,
    val icon: String,
    val color: String,
)
