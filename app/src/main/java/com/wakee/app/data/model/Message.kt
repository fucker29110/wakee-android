package com.wakee.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Message(
    @DocumentId val messageId: String = "",
    val senderUid: String = "",
    val text: String = "",
    val imageURL: String? = null,
    val read: Boolean = false,
    val createdAt: Timestamp? = null
)
