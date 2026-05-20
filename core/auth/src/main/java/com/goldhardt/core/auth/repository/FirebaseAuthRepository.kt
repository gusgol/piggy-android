package com.goldhardt.core.auth.repository

import com.goldhardt.core.auth.google.GoogleSignInHelper
import com.goldhardt.core.auth.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val googleSignInHelper: GoogleSignInHelper,
) : AuthRepository {

    override val currentUser: User?
        get() = firebaseAuth.currentUser?.toUser()

    override fun authState(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toUser())
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()

            result.user?.toUser()?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Firebase authentication failed - no user returned"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        try {
            googleSignInHelper.signOut()
            firebaseAuth.signOut()
        } catch (e: Exception) {
            firebaseAuth.signOut()
            throw e
        }
    }

    private fun FirebaseUser.toUser(): User {
        return User(
            id = uid,
            email = email,
            displayName = displayName,
            photoUrl = photoUrl?.toString(),
            isEmailVerified = isEmailVerified
        )
    }
}
