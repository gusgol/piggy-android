package com.goldhardt.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.goldhardt.core.auth.google.GoogleSignInLauncher
import com.goldhardt.core.auth.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
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

    fun signIn(activity: Activity) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val googleResult = googleSignInLauncher.signIn(activity)
                val idToken = googleResult.getOrNull()
                if (idToken == null) {
                    val exception = googleResult.exceptionOrNull()
                    if (exception is CancellationException) throw exception
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = if (exception is GetCredentialCancellationException) null
                        else "Couldn't sign in with Google. Please try again."
                    )
                    return@launch
                }

                val firebaseResult = authRepository.signInWithGoogle(idToken)
                val firebaseException = firebaseResult.exceptionOrNull()
                if (firebaseException is CancellationException) throw firebaseException
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = if (firebaseResult.isSuccess) null
                    else "Couldn't sign in. Please try again."
                )
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Couldn't sign in. Please try again."
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
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Couldn't sign out. Please try again."
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
