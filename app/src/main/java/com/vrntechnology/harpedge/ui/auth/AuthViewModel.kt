package com.vrntechnology.harpedge.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vrntechnology.harpedge.data.model.UserProfile
import com.vrntechnology.harpedge.data.model.UserRole
import com.vrntechnology.harpedge.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val currentUser: UserProfile? = null
)

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState(currentUser = authRepository.currentUser.value))
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.value = _uiState.value.copy(currentUser = user)
            }
        }
    }

    fun signIn(email: String, pass: String, onSuccess: (UserRole) -> Unit) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val result = authRepository.signIn(email, pass)
            result.onSuccess { user ->
                _uiState.value = _uiState.value.copy(isLoading = false, currentUser = user)
                onSuccess(user.role)
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.localizedMessage ?: "Unable to sign in. Please verify your credentials."
                )
            }
        }
    }

    fun quickRoleSignIn(role: UserRole, onSuccess: (UserRole) -> Unit) {
        val user = authRepository.signInWithRole(role)
        _uiState.value = _uiState.value.copy(currentUser = user, errorMessage = null)
        onSuccess(user.role)
    }

    fun signOut() {
        authRepository.signOut()
        _uiState.value = AuthUiState(currentUser = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
