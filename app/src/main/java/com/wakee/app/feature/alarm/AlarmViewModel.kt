package com.wakee.app.feature.alarm

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.wakee.app.data.model.AppUser
import com.wakee.app.data.model.InboxEvent
import com.wakee.app.data.repository.AlarmRepository
import com.wakee.app.data.repository.AuthRepository
import com.wakee.app.data.repository.FriendRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val authRepository: AuthRepository,
    private val friendRepository: FriendRepository
) : ViewModel() {

    private val _friends = MutableStateFlow<List<AppUser>>(emptyList())
    val friends: StateFlow<List<AppUser>> = _friends.asStateFlow()

    private val _selectedFriend = MutableStateFlow<AppUser?>(null)
    val selectedFriend: StateFlow<AppUser?> = _selectedFriend.asStateFlow()

    private val _hour = MutableStateFlow(7)
    val hour: StateFlow<Int> = _hour.asStateFlow()

    private val _minute = MutableStateFlow(0)
    val minute: StateFlow<Int> = _minute.asStateFlow()

    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()

    private val _snoozeMin = MutableStateFlow(5)
    val snoozeMin: StateFlow<Int> = _snoozeMin.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _sendSuccess = MutableStateFlow(false)
    val sendSuccess: StateFlow<Boolean> = _sendSuccess.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _audioUri = MutableStateFlow<Uri?>(null)
    val audioUri: StateFlow<Uri?> = _audioUri.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    init {
        loadFriends()
    }

    private fun loadFriends() {
        viewModelScope.launch {
            try {
                val uid = authRepository.currentUid ?: return@launch
                _friends.value = friendRepository.getFriendUsers(uid)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun selectFriend(friend: AppUser) {
        _selectedFriend.value = friend
    }

    fun setTime(h: Int, m: Int) {
        _hour.value = h
        _minute.value = m
    }

    fun setMessage(msg: String) {
        _message.value = msg
    }

    fun setSnoozeMin(min: Int) {
        _snoozeMin.value = min
    }

    fun setAudioUri(uri: Uri?) {
        _audioUri.value = uri
    }

    fun setRecording(recording: Boolean) {
        _isRecording.value = recording
    }

    fun sendAlarm() {
        val friend = _selectedFriend.value ?: return
        viewModelScope.launch {
            _isSending.value = true
            try {
                val user = authRepository.getCurrentUser() ?: return@launch
                val time = String.format("%02d:%02d", _hour.value, _minute.value)
                alarmRepository.sendAlarm(
                    toUid = friend.uid,
                    senderUid = user.uid,
                    senderName = user.displayName,
                    time = time,
                    message = _message.value,
                    snoozeMin = _snoozeMin.value,
                    audioUri = _audioUri.value
                )
                _sendSuccess.value = true
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isSending.value = false
            }
        }
    }
}
