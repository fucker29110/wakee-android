package com.wakee.app.data.repository

import android.net.Uri
import com.google.firebase.Timestamp
import com.wakee.app.data.model.InboxEvent
import com.wakee.app.data.remote.FirestoreService
import com.wakee.app.data.remote.StorageService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmRepository @Inject constructor(
    private val firestoreService: FirestoreService,
    private val storageService: StorageService
) {
    fun observeInbox(uid: String): Flow<List<InboxEvent>> {
        return firestoreService.observeInbox(uid)
    }

    suspend fun sendAlarm(
        toUid: String,
        senderUid: String,
        senderName: String,
        time: String,
        message: String,
        snoozeMin: Int,
        audioUri: Uri? = null
    ): String {
        var audioURL: String? = null
        if (audioUri != null) {
            audioURL = storageService.uploadAlarmAudio(senderUid, audioUri)
        }

        val event = mutableMapOf<String, Any>(
            "senderUid" to senderUid,
            "senderName" to senderName,
            "time" to time,
            "label" to "${senderName}からのアラーム",
            "message" to message,
            "repeat" to emptyList<String>(),
            "snoozeMin" to snoozeMin,
            "status" to InboxEvent.STATUS_PENDING,
            "createdAt" to Timestamp.now()
        )
        if (audioURL != null) {
            event["audioURL"] = audioURL
        }

        return firestoreService.sendAlarm(toUid, event)
    }

    suspend fun updateStatus(uid: String, eventId: String, status: String) {
        firestoreService.updateInboxStatus(uid, eventId, status)
    }

    suspend fun getInboxEvent(uid: String, eventId: String): InboxEvent? {
        return firestoreService.getInboxEvent(uid, eventId)
    }
}
