package com.wakee.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Chat(
    @DocumentId val chatId: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageAt: Timestamp? = null,
    val lastSenderUid: String = "",
    val unreadCount: Map<String, Int> = emptyMap(),
    val createdAt: Timestamp? = null
)
