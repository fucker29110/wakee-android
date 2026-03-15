package com.wakee.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Comment(
    @DocumentId val commentId: String = "",
    val activityId: String = "",
    val uid: String = "",
    val displayName: String = "",
    val username: String = "",
    val photoURL: String? = null,
    val text: String = "",
    val createdAt: Timestamp? = null
)
