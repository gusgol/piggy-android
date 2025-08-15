package com.goldhardt.core.data.datasource

import com.goldhardt.core.data.model.Category
import kotlinx.coroutines.flow.Flow

/**
 * Abstraction for reading Categories from a remote source (Firestore).
 */
interface CategoryRemoteDataSource {
    /** Observe categories for a given user in realtime. */
    fun observeCategories(userId: String): Flow<List<Category>>

    /** Fetch categories once for a given user. */
    suspend fun getCategories(userId: String): List<Category>

    /** Update a category in the remote source. */
    suspend fun updateCategory(category: Category)
}
