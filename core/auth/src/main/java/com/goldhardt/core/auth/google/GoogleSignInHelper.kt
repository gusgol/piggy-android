package com.goldhardt.core.auth.google

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleSignInHelper @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val credentialManager = CredentialManager.create(context)

    suspend fun signOut() {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            Log.d(TAG, "Sign out successful")
        } catch (e: Exception) {
            Log.e(TAG, "Sign out failed", e)
        }
    }

    companion object {
        private const val TAG = "GoogleSignInHelper"
    }
}
