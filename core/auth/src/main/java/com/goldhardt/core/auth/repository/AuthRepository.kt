package com.goldhardt.core.auth.repository

import com.goldhardt.core.auth.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: User?
    fun authState(): Flow<User?>
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun signOut()
}
