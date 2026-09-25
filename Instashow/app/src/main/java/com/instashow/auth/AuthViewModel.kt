package com.instashow.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    val session: StateFlow<UserSession?> = repository.session.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        null,
    )

    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready.asStateFlow()

    private val _error = MutableStateFlow<AuthError?>(null)
    val error: StateFlow<AuthError?> = _error.asStateFlow()

    private val _signingIn = MutableStateFlow(false)
    val signingIn: StateFlow<Boolean> = _signingIn.asStateFlow()

    private val _signedInMessage = MutableStateFlow(false)
    val signedInMessage: StateFlow<Boolean> = _signedInMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.session.first()
            _ready.value = true
        }
    }

    fun signIn(activityContext: Context) {
        if (_signingIn.value) return
        viewModelScope.launch {
            _signingIn.value = true
            _error.value = null
            when (val outcome = repository.signIn(activityContext)) {
                is SignInOutcome.Success -> _signedInMessage.value = true
                SignInOutcome.Cancelled -> Unit
                is SignInOutcome.Error -> _error.value = outcome.kind
            }
            _signingIn.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun consumeSignedInMessage() {
        _signedInMessage.value = false
    }

    fun signOut() {
        viewModelScope.launch { repository.signOut() }
    }

    companion object {
        fun factory(repository: AuthRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AuthViewModel(repository) as T
                }
            }
    }
}
