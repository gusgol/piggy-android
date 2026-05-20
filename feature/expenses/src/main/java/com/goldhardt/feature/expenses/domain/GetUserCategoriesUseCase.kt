package com.goldhardt.feature.expenses.domain

import com.goldhardt.core.auth.session.UserSession
import com.goldhardt.core.data.model.Category
import com.goldhardt.core.data.repository.CategoryRepository
import javax.inject.Inject

class GetUserCategoriesUseCase @Inject constructor(
    private val userSession: UserSession,
    private val categoryRepository: CategoryRepository,
) {
    suspend operator fun invoke(): List<Category> {
        if (!userSession.isSignedIn()) return emptyList()
        return categoryRepository.getCategories(userSession.userId)
    }
}

