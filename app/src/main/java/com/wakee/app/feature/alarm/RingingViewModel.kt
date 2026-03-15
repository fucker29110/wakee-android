package com.wakee.app.feature.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakee.app.data.model.Activity
import com.wakee.app.data.model.InboxEvent
import com.wakee.app.data.repository.ActivityRepository
import com.wakee.app.data.repository.AlarmRepository
import com.wakee.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RingingViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val activityRepository: ActivityRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _event = MutableStateFlow<InboxEvent?>(null)
    val event: StateFlow<InboxEvent?> = _event.asStateFlow()

    private val _showReactionPicker = MutableStateFlow(false)
    val showReactionPicker: StateFlow<Boolean> = _showReactionPicker.asStateFlow()

    private val _isDismissed = MutableStateFlow(false)
    val isDismissed: StateFlow<Boolean> = _isDismissed.asStateFlow()

    private var currentResult: String = ""

    fun loadEvent(eventId: String) {
        viewModelScope.launch {
            try {
                val uid = authRepository.currentUid ?: return@launch
                _event.value = alarmRepository.getInboxEvent(uid, eventId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun wokeUp() {
        currentResult = Activity.RESULT_WOKE_UP
        updateStatus(InboxEvent.STATUS_DISMISSED)
        _showReactionPicker.value = true
    }

    fun snooze() {
        currentResult = Activity.RESULT_SNOOZED
        updateStatus(InboxEvent.STATUS_SNOOZED)
        _showReactionPicker.value = true
    }

    fun dismiss() {
        currentResult = Activity.RESULT_DISMISSED
        updateStatus(InboxEvent.STATUS_DISMISSED)
        _showReactionPicker.value = true
    }

    fun selectReaction(emoji: String) {
        postResult(emoji)
    }

    fun skipReaction() {
        postResult(null)
    }

    private fun updateStatus(status: String) {
        viewModelScope.launch {
            try {
                val uid = authRepository.currentUid ?: return@launch
                val ev = _event.value ?: return@launch
                alarmRepository.updateStatus(uid, ev.eventId, status)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun postResult(emoji: String?) {
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser() ?: return@launch
                val ev = _event.value ?: return@launch
                activityRepository.createAlarmResultActivity(
                    uid = user.uid,
                    displayName = user.displayName,
                    username = user.username,
                    photoURL = user.photoURL,
                    targetUid = ev.senderUid,
                    targetDisplayName = ev.senderName,
                    targetUsername = "",
                    alarmTime = ev.time,
                    result = currentResult,
                    reactionEmoji = emoji
                )
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isDismissed.value = true
            }
        }
    }
}
