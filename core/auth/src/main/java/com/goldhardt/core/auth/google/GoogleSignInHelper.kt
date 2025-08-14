package com.goldhardt.core.auth.google

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleSignInHelper @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val credentialManager = CredentialManager.create(context)


    suspend fun signIn(webClientId: String, nonce: String): Result<String> {
        return try {
            val authorizedAccountsOption = GetGoogleIdOption.Builder()
                .setServerClientId(webClientId)
                .setFilterByAuthorizedAccounts(true)
                .setNonce(nonce)
                .setAutoSelectEnabled(true)
                .build()

            val authorizedRequest = GetCredentialRequest.Builder()
                .addCredentialOption(authorizedAccountsOption)
                .build()

            try {
                val result = credentialManager.getCredential(
                    request = authorizedRequest,
                    context = context
                )
                Log.d(TAG, "Sign in successful with authorized account")
                val idToken = handleSignInResult(result)
                Result.success(idToken)
            } catch (e: NoCredentialException) {
                Log.d(TAG, "No authorized accounts found, trying without filter ${e.message}")

                // If no authorized accounts, try without filter
                val allAccountsOption = GetGoogleIdOption.Builder()
                    .setServerClientId(webClientId)
                    .setFilterByAuthorizedAccounts(false)
                    .setNonce(nonce)
                    .setAutoSelectEnabled(false)
                    .build()

                val allAccountsRequest = GetCredentialRequest.Builder()
                    .addCredentialOption(allAccountsOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = allAccountsRequest,
                    context = context
                )
                Log.d(TAG, "Sign in successful with account selection")
                val idToken = handleSignInResult(result)
                Result.success(idToken)
            }
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Sign in failed", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during sign in", e)
            Result.failure(e)
        }
    }

    private fun handleSignInResult(result: GetCredentialResponse): String {
        val credential = result.credential

        if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            return googleIdTokenCredential.idToken
        } else {
            throw IllegalStateException("Unexpected credential type: ${credential.type}")
        }
    }

    suspend fun signOut() {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            Log.d(TAG, "Sign out successful")
        } catch (e: Exception) {
            Log.e(TAG, "Sign out failed", e)
        }
    }

    companion object {
        const val TAG = "GoogleSignInHelper"
    }
}
