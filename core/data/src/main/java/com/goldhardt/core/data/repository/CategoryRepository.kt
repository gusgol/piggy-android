package com.goldhardt.core.data.repository

import com.goldhardt.core.data.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeCategories(userId: String): Flow<List<Category>>
    suspend fun getCategories(userId: String): List<Category>
}

