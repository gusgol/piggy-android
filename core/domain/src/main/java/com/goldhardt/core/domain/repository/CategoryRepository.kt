package com.goldhardt.core.domain.repository

import com.goldhardt.core.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeCategories(userId: String): Flow<List<Category>>
    suspend fun getCategories(userId: String): List<Category>
    suspend fun updateCategory(category: Category)
    suspend fun addCategory(userId: String, name: String, icon: String, color: String): Category
    suspend fun deleteCategory(categoryId: String)
}
