package com.wakee.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class InboxEvent(
    @DocumentId val eventId: String = "",
    val senderUid: String = "",
    val senderName: String = "",
    val time: String = "",
    val label: String = "",
    val message: String = "",
    val repeat: List<String> = emptyList(),
    val snoozeMin: Int = 5,
    val status: String = "pending",
    val audioURL: String? = null,
    val createdAt: Timestamp? = null
) {
    companion object {
        const val STATUS_PENDING = "pending"
        const val STATUS_SCHEDULED = "scheduled"
        const val STATUS_RUNG = "rung"
        const val STATUS_DISMISSED = "dismissed"
        const val STATUS_SNOOZED = "snoozed"
    }
}
