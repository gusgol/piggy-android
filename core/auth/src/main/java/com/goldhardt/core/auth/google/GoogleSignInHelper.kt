package com.goldhardt.core.auth.google

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleSignInHelper @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val credentialManager = CredentialManager.create(context)


    suspend fun signIn(activity: Activity, webClientId: String, nonce: String): Result<String> {
        return try {
            // 1. Try with authorized accounts first (Bottom Sheet flow)
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
                    context = activity
                )
                Log.d(TAG, "Sign in successful with authorized account")
                Result.success(handleSignInResult(result))
            } catch (_: NoCredentialException) {
                Log.d(TAG, "No authorized accounts found, showing full account picker")

                // 2. Fallback to full account picker (Button flow)
                val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(webClientId)
                    .setNonce(nonce)
                    .build()

                val allAccountsRequest = GetCredentialRequest.Builder()
                    .addCredentialOption(signInWithGoogleOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = allAccountsRequest,
                    context = activity
                )
                Log.d(TAG, "Sign in successful with account selection")
                Result.success(handleSignInResult(result))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "Sign in cancelled by user")
            Result.failure(e)
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Sign in failed: ${e.message}", e)
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
