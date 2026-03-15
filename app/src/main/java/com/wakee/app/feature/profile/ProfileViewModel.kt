package com.wakee.app.feature.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakee.app.data.model.AppUser
import com.wakee.app.data.repository.AuthRepository
import com.wakee.app.data.repository.FriendRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val friendRepository: FriendRepository
) : ViewModel() {

    private val _user = MutableStateFlow<AppUser?>(null)
    val user: StateFlow<AppUser?> = _user.asStateFlow()

    private val _friendCount = MutableStateFlow(0)
    val friendCount: StateFlow<Int> = _friendCount.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            try {
                _user.value = authRepository.getCurrentUser()
                val uid = authRepository.currentUid ?: return@launch
                _friendCount.value = friendRepository.getFriends(uid).size
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
