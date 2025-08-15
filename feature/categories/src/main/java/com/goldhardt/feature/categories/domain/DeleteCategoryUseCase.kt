package com.goldhardt.feature.categories.domain

import com.goldhardt.core.data.repository.CategoryRepository
import javax.inject.Inject

class DeleteCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository,
) {
    suspend operator fun invoke(categoryId: String) {
        repository.deleteCategory(categoryId)
    }
}