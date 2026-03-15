package com.wakee.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.wakee.app.data.model.Activity
import com.wakee.app.data.model.AppUser
import com.wakee.app.data.model.Story
import com.wakee.app.data.repository.ActivityRepository
import com.wakee.app.data.repository.FriendRepository
import com.wakee.app.data.repository.StoryRepository
import com.wakee.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val friendRepository: FriendRepository,
    private val storyRepository: StoryRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _activities = MutableStateFlow<List<Activity>>(emptyList())
    val activities: StateFlow<List<Activity>> = _activities.asStateFlow()

    private val _stories = MutableStateFlow<List<Story>>(emptyList())
    val stories: StateFlow<List<Story>> = _stories.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _currentUser = MutableStateFlow<AppUser?>(null)
    val currentUser: StateFlow<AppUser?> = _currentUser.asStateFlow()

    private var friendUids = listOf<String>()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val uid = authRepository.currentUid ?: return@launch
                _currentUser.value = authRepository.getCurrentUser()
                friendUids = friendRepository.getFriends(uid)
                val allUids = friendUids + uid
                _activities.value = activityRepository.getFeed(allUids)
                _stories.value = storyRepository.getStories(allUids)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun toggleLike(activityId: String) {
        viewModelScope.launch {
            try {
                val uid = authRepository.currentUid ?: return@launch
                activityRepository.toggleLike(activityId, uid)
                loadData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun repost(activityId: String, comment: String? = null) {
        viewModelScope.launch {
            try {
                val user = _currentUser.value ?: return@launch
                activityRepository.repost(
                    activityId, user.uid, user.displayName, user.username, user.photoURL, comment
                )
                loadData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
