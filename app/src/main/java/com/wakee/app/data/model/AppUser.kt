package com.wakee.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class AppUser(
    @DocumentId val uid: String = "",
    val displayName: String = "",
    val username: String = "",
    val photoURL: String? = null,
    val bio: String = "",
    val location: String = "",
    val streak: Int = 0,
    val settings: UserSettings = UserSettings(),
    val notificationSettings: NotificationSettings = NotificationSettings(),
    val fcmToken: String? = null,
    val onboardingCompleted: Boolean = false,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

data class UserSettings(
    val searchable: Boolean = true,
    val blocked: List<String> = emptyList()
)

data class NotificationSettings(
    val alarmReceived: Boolean = true,
    val messages: Boolean = true,
    val likes: Boolean = true,
    val reposts: Boolean = true,
    val friendRequests: Boolean = true,
    val reactions: Boolean = true,
    val liveActivity: Boolean = true
)
