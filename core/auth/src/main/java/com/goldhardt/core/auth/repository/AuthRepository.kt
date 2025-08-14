package com.goldhardt.core.auth.repository

import com.goldhardt.core.auth.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: User?
    fun authState(): Flow<User?>
    suspend fun signIn(): Result<User>
    suspend fun signOut()
}