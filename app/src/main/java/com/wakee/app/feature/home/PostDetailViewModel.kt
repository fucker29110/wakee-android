package com.wakee.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.wakee.app.data.model.Activity
import com.wakee.app.data.model.AppUser
import com.wakee.app.data.model.Comment
import com.wakee.app.data.repository.ActivityRepository
import com.wakee.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _activity = MutableStateFlow<Activity?>(null)
    val activity: StateFlow<Activity?> = _activity.asStateFlow()

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    val currentUid: String get() = authRepository.currentUid ?: ""

    private var activityId = ""

    fun loadActivity(id: String) {
        activityId = id
        viewModelScope.launch {
            try {
                _activity.value = activityRepository.getActivity(id)
                _comments.value = activityRepository.getComments(id)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleLike() {
        viewModelScope.launch {
            try {
                activityRepository.toggleLike(activityId, currentUid)
                _activity.value = activityRepository.getActivity(activityId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun repost() {
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser() ?: return@launch
                activityRepository.repost(
                    activityId, user.uid, user.displayName, user.username, user.photoURL, null
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addComment(text: String) {
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser() ?: return@launch
                activityRepository.addComment(
                    activityId, user.uid, user.displayName, user.username, user.photoURL, text
                )
                _comments.value = activityRepository.getComments(activityId)
                _activity.value = activityRepository.getActivity(activityId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
