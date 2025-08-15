package com.goldhardt.core.data.repository

import com.goldhardt.core.data.datasource.CategoryRemoteDataSource
import com.goldhardt.core.data.model.Category
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreCategoryRepository @Inject constructor(
    private val remote: CategoryRemoteDataSource
) : CategoryRepository {
    override fun observeCategories(userId: String): Flow<List<Category>> =
        remote.observeCategories(userId)

    override suspend fun getCategories(userId: String): List<Category> =
        remote.getCategories(userId)

    override suspend fun updateCategory(category: Category) =
        remote.updateCategory(category)
}
