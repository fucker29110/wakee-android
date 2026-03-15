package com.wakee.app.data.repository

import com.wakee.app.data.model.AppNotification
import com.wakee.app.data.remote.FirestoreService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val firestoreService: FirestoreService
) {
    fun observeNotifications(uid: String): Flow<List<AppNotification>> {
        return firestoreService.observeNotifications(uid)
    }

    suspend fun markRead(notificationId: String) {
        firestoreService.markNotificationRead(notificationId)
    }

    suspend fun markAllRead(uid: String) {
        firestoreService.markAllNotificationsRead(uid)
    }

    suspend fun createNotification(notification: Map<String, Any>) {
        firestoreService.createNotification(notification)
    }
}
