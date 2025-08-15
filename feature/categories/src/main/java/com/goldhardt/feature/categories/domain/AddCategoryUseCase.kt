package com.goldhardt.feature.categories.domain

import com.goldhardt.core.auth.repository.AuthRepository
import com.goldhardt.core.data.repository.CategoryRepository
import javax.inject.Inject

class AddCategoryUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val repository: CategoryRepository,
) {
    suspend operator fun invoke(name: String, icon: String, color: String) {
        val user = authRepository.currentUser
            ?: throw IllegalStateException("User must be signed in to add a category")
        repository.addCategory(user.id, name, icon, color)
    }
}