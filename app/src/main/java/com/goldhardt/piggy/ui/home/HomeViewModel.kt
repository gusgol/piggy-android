package com.goldhardt.piggy.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldhardt.core.auth.model.User
import com.goldhardt.core.auth.session.UserSession
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class HomeViewModel @Inject constructor(
    userSession: UserSession,
) : ViewModel() {

    val state: StateFlow<HomeState> = userSession
        .authState
        .map { user -> if (user == null) HomeState.SignedOut else HomeState.SignedIn(user) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeState.Loading
        )
}

sealed interface HomeState {
    data object Loading : HomeState
    data object SignedOut : HomeState
    data class SignedIn(val user: User) : HomeState
}

