package com.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.AuthRepository
import com.example.domain.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    val currentUser = authRepository.currentUser

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.signUp(email, password)
            result.onSuccess { user ->
                _authState.value = AuthState.Success(user)
            }.onFailure { error ->
                _authState.value = AuthState.Error(error.message ?: "An unknown error occurred")
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.signIn(email, password)
            result.onSuccess { user ->
                _authState.value = AuthState.Success(user)
            }.onFailure { error ->
                _authState.value = AuthState.Error(error.message ?: "An unknown error occurred")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.signOut()
            result.onSuccess {
                _authState.value = AuthState.Idle
            }.onFailure { error ->
                _authState.value = AuthState.Error(error.message ?: "Sign out failed")
            }
        }
    }
}
