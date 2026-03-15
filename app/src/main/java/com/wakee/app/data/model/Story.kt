package com.wakee.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Story(
    @DocumentId val storyId: String = "",
    val uid: String = "",
    val displayName: String = "",
    val username: String = "",
    val photoURL: String? = null,
    val text: String = "",
    val imageURL: String? = null,
    val viewedBy: List<String> = emptyList(),
    val expiresAt: Timestamp? = null,
    val createdAt: Timestamp? = null
)
