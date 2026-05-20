package com.goldhardt.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.goldhardt.core.auth.google.GoogleSignInLauncher
import com.goldhardt.core.auth.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val googleSignInLauncher: GoogleSignInLauncher,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    val authState = authRepository.authState()

    fun signIn(activity: Activity) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val googleResult = googleSignInLauncher.signIn(activity)

            if (googleResult.isSuccess) {
                val idToken = googleResult.getOrThrow()
                val firebaseResult = authRepository.signInWithGoogle(idToken)

                _uiState.value = if (firebaseResult.isSuccess) {
                    _uiState.value.copy(isLoading = false, error = null)
                } else {
                    _uiState.value.copy(
                        isLoading = false,
                        error = firebaseResult.exceptionOrNull()?.message ?: "Firebase sign in failed"
                    )
                }
            } else {
                val exception = googleResult.exceptionOrNull()
                // Don't show error if user just cancelled
                val errorMessage = if (exception is GetCredentialCancellationException) {
                    null
                } else {
                    exception?.message ?: "Google sign in failed"
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = errorMessage
                )
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                authRepository.signOut()
                _uiState.value = _uiState.value.copy(isLoading = false, error = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Sign out failed"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)
