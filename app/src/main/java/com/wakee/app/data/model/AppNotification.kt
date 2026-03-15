package com.wakee.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class AppNotification(
    @DocumentId val notificationId: String = "",
    val type: String = "",
    val fromUid: String = "",
    val fromDisplayName: String = "",
    val fromPhotoURL: String? = null,
    val message: String = "",
    val relatedId: String? = null,
    val read: Boolean = false,
    val createdAt: Timestamp? = null
) {
    companion object {
        const val TYPE_ALARM = "alarm"
        const val TYPE_FRIEND_REQUEST = "friend_request"
        const val TYPE_FRIEND_ACCEPT = "friend_accept"
        const val TYPE_LIKE = "like"
        const val TYPE_COMMENT = "comment"
        const val TYPE_REPOST = "repost"
    }
}
