package com.goldhardt.feature.expenses.domain

import com.goldhardt.core.auth.session.UserSession
import com.goldhardt.core.domain.model.Category
import com.goldhardt.core.domain.repository.CategoryRepository
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveCategoriesUseCase @Inject constructor(
    private val userSession: UserSession,
    private val categoryRepository: CategoryRepository,
) {
    operator fun invoke(): Flow<List<Category>> =
        userSession.authState.flatMapLatest { user ->
            val uid = user?.id
            if (uid == null) {
                flowOf(emptyList())
            } else {
                categoryRepository.observeCategories(uid)
            }
        }
}

