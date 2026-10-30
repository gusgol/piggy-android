package com.goldhardt.feature.categories.domain

import com.goldhardt.core.auth.session.UserSession
import com.goldhardt.core.data.repository.CategoryRepository
import javax.inject.Inject

class AddCategoryUseCase @Inject constructor(
    private val userSession: UserSession,
    private val repository: CategoryRepository,
) {
    suspend operator fun invoke(name: String, icon: String, color: String) {
        repository.addCategory(userSession.userId, name, icon, color)
    }
}