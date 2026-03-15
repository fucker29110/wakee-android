package com.wakee.app.feature.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakee.app.data.model.AppUser
import com.wakee.app.data.model.FollowRequest
import com.wakee.app.data.repository.AuthRepository
import com.wakee.app.data.repository.FriendRepository
import com.wakee.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val friendRepository: FriendRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _friends = MutableStateFlow<List<AppUser>>(emptyList())
    val friends: StateFlow<List<AppUser>> = _friends.asStateFlow()

    private val _pendingRequests = MutableStateFlow<List<FollowRequest>>(emptyList())
    val pendingRequests: StateFlow<List<FollowRequest>> = _pendingRequests.asStateFlow()

    private val _searchResults = MutableStateFlow<List<AppUser>>(emptyList())
    val searchResults: StateFlow<List<AppUser>> = _searchResults.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val currentUid: String get() = authRepository.currentUid ?: ""

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val uid = authRepository.currentUid ?: return@launch
                _friends.value = friendRepository.getFriendUsers(uid)
                _pendingRequests.value = friendRepository.getPendingRequests(uid)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun searchUsers(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            try {
                val results = userRepository.searchUsers(query)
                _searchResults.value = results.filter { it.uid != currentUid }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendRequest(toUid: String) {
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser() ?: return@launch
                friendRepository.sendFollowRequest(user, toUid)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun acceptRequest(request: FollowRequest) {
        viewModelScope.launch {
            try {
                friendRepository.acceptRequest(request.requestId, request.fromUid, currentUid)
                loadData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun rejectRequest(request: FollowRequest) {
        viewModelScope.launch {
            try {
                friendRepository.rejectRequest(request.requestId)
                loadData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun removeFriend(friendUid: String) {
        viewModelScope.launch {
            try {
                friendRepository.removeFriend(currentUid, friendUid)
                loadData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
