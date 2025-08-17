package com.goldhardt.feature.expenses.domain

import com.goldhardt.core.auth.repository.AuthRepository
import com.goldhardt.core.data.model.Category
import com.goldhardt.core.data.repository.CategoryRepository
import javax.inject.Inject

class GetUserCategoriesUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val categoryRepository: CategoryRepository,
) {
    suspend operator fun invoke(): List<Category> {
        val user = authRepository.currentUser ?: return emptyList()
        return categoryRepository.getCategories(user.id)
    }
}

