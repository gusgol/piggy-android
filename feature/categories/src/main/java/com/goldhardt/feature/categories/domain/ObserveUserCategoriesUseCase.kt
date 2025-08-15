package com.goldhardt.feature.categories.domain

import com.goldhardt.core.auth.repository.AuthRepository
import com.goldhardt.core.data.model.Category
import com.goldhardt.core.data.repository.CategoryRepository
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class ObserveUserCategoriesUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val categoryRepository: CategoryRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<Category>> =
        authRepository
            .authState()
            .flatMapLatest { user ->
                if (user != null) categoryRepository.observeCategories(user.id)
                else flowOf(emptyList())
            }
}

