package com.wakee.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class FollowRequest(
    @DocumentId val requestId: String = "",
    val fromUid: String = "",
    val toUid: String = "",
    val fromDisplayName: String = "",
    val fromUsername: String = "",
    val fromPhotoURL: String? = null,
    val status: String = "pending",
    val createdAt: Timestamp? = null
) {
    companion object {
        const val STATUS_PENDING = "pending"
        const val STATUS_ACCEPTED = "accepted"
        const val STATUS_REJECTED = "rejected"
    }
}
