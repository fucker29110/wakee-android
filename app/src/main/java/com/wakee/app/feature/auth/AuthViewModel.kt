package com.wakee.app.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.wakee.app.data.model.AppUser
import com.wakee.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(authRepository.isLoggedIn)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentUser = MutableStateFlow<AppUser?>(null)
    val currentUser: StateFlow<AppUser?> = _currentUser.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _loginLoading = MutableStateFlow(false)
    val loginLoading: StateFlow<Boolean> = _loginLoading.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            try {
                if (authRepository.isLoggedIn) {
                    _currentUser.value = authRepository.getCurrentUser()
                    _isLoggedIn.value = true
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _loginLoading.value = true
            try {
                val user = authRepository.signInWithGoogle(idToken)
                _currentUser.value = user
                _isLoggedIn.value = true
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loginLoading.value = false
            }
        }
    }

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _loginLoading.value = true
            try {
                val user = authRepository.signInWithEmail(email, password)
                _currentUser.value = user
                _isLoggedIn.value = true
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loginLoading.value = false
            }
        }
    }

    fun signUpWithEmail(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _loginLoading.value = true
            try {
                val user = authRepository.signUpWithEmail(email, password, displayName)
                _currentUser.value = user
                _isLoggedIn.value = true
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loginLoading.value = false
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
        _currentUser.value = null
        _isLoggedIn.value = false
    }

    fun deleteAccount() {
        viewModelScope.launch {
            try {
                FirebaseAuth.getInstance().currentUser?.delete()?.await()
                _currentUser.value = null
                _isLoggedIn.value = false
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
