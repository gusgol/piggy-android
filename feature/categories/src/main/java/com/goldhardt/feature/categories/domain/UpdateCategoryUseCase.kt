package com.goldhardt.feature.categories.domain

import com.goldhardt.core.domain.model.Category
import com.goldhardt.core.domain.repository.CategoryRepository
import javax.inject.Inject

class UpdateCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository,
) {
    suspend operator fun invoke(category: Category) {
        repository.updateCategory(category)
    }
}

