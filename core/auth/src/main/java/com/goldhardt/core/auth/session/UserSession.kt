package com.goldhardt.core.auth.session

import com.goldhardt.core.auth.model.User
import com.goldhardt.core.auth.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides a centralized way to access the current user's information and session state.
 * This simplifies repositories and use cases by removing the need to manually check auth state.
 */
@Singleton
class UserSession @Inject constructor(
    private val authRepository: AuthRepository
) {
    /**
     * Returns the current user or throws if not authenticated.
     * Use this in repositories for operations that require an authenticated user.
     */
    val currentUser: User
        get() = authRepository.currentUser ?: throw IllegalStateException("User session required but not found")

    /**
     * The current user's ID. Throws if not authenticated.
     */
    val userId: String
        get() = currentUser.id

    /**
     * A flow of the current authentication state.
     */
    val authState: Flow<User?> = authRepository.authState()

    /**
     * Returns true if a user is currently signed in.
     */
    fun isSignedIn(): Boolean = authRepository.currentUser != null
}
