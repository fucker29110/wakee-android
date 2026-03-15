package com.wakee.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Activity(
    @DocumentId val activityId: String = "",
    val type: String = "",
    val uid: String = "",
    val displayName: String = "",
    val username: String = "",
    val photoURL: String? = null,
    val targetUid: String? = null,
    val targetDisplayName: String? = null,
    val targetUsername: String? = null,
    val alarmTime: String? = null,
    val result: String? = null,
    val reactionEmoji: String? = null,
    val message: String? = null,
    val likes: List<String> = emptyList(),
    val reposts: List<String> = emptyList(),
    val commentCount: Int = 0,
    val originalActivityId: String? = null,
    val originalUid: String? = null,
    val originalDisplayName: String? = null,
    val repostComment: String? = null,
    val createdAt: Timestamp? = null
) {
    companion object {
        const val TYPE_ALARM_RESULT = "alarm_result"
        const val TYPE_REPOST = "repost"
        const val TYPE_FRIEND = "friend"

        const val RESULT_WOKE_UP = "woke_up"
        const val RESULT_SNOOZED = "snoozed"
        const val RESULT_DISMISSED = "dismissed"
        const val RESULT_MISSED = "missed"
    }
}
