package com.goldhardt.core.auth.google

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.goldhardt.core.auth.config.AuthConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import java.security.SecureRandom
import javax.inject.Inject

/**
 * Handles the UI-dependent part of Google Sign-In (showing the picker).
 * This class belongs to the UI layer and interacts with the Activity.
 */
class GoogleSignInLauncher @Inject constructor(
    private val authConfig: AuthConfig
) {

    suspend fun signIn(activity: Activity): Result<String> {
        val credentialManager = CredentialManager.create(activity)
        val webClientId = authConfig.getWebClientId()
        val nonce = generateNonce()

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
                Result.success(extractIdToken(result.credential))
            } catch (_: NoCredentialException) {
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
                Result.success(extractIdToken(result.credential))
            }
        } catch (e: GetCredentialCancellationException) {
            Result.failure(e)
        } catch (e: GetCredentialException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractIdToken(credential: androidx.credentials.Credential): String {
        if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            return googleIdTokenCredential.idToken
        } else {
            throw IllegalStateException("Unexpected credential type: ${credential.type}")
        }
    }

    private fun generateNonce(length: Int = 32): String {
        val random = SecureRandom()
        val bytes = ByteArray(length)
        random.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
