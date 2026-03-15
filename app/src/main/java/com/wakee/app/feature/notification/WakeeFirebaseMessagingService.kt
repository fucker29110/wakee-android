package com.wakee.app.feature.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.wakee.app.MainActivity
import com.wakee.app.feature.alarm.AlarmService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WakeeFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val data = remoteMessage.data
        val type = data["type"] ?: ""

        when (type) {
            "alarm" -> handleAlarmNotification(data)
            "message" -> showNotification(
                title = data["senderName"] ?: "新しいメッセージ",
                body = data["body"] ?: "",
                channel = "message_channel",
                notificationId = data["chatId"]?.hashCode() ?: System.currentTimeMillis().toInt()
            )
            "like" -> showNotification(
                title = "${data["fromName"] ?: "誰か"}があなたの投稿にいいねしました",
                body = "",
                channel = "general_channel"
            )
            "comment" -> showNotification(
                title = "${data["fromName"] ?: "誰か"}がコメントしました",
                body = data["body"] ?: "",
                channel = "general_channel"
            )
            "friend_request" -> showNotification(
                title = "フレンドリクエスト",
                body = "${data["fromName"] ?: "誰か"}からフレンドリクエストが届きました",
                channel = "general_channel"
            )
            "friend_accept" -> showNotification(
                title = "フレンド承認",
                body = "${data["fromName"] ?: "誰か"}があなたのリクエストを承認しました",
                channel = "general_channel"
            )
            "repost" -> showNotification(
                title = "${data["fromName"] ?: "誰か"}があなたの投稿をリポストしました",
                body = "",
                channel = "general_channel"
            )
            else -> {
                val title = remoteMessage.notification?.title ?: data["title"] ?: "Wakee"
                val body = remoteMessage.notification?.body ?: data["body"] ?: ""
                showNotification(title, body)
            }
        }
    }

    private fun handleAlarmNotification(data: Map<String, String>) {
        val eventId = data["eventId"] ?: ""
        val senderName = data["senderName"] ?: "友達"
        val time = data["time"] ?: ""

        // Start alarm service for sound + vibration
        val serviceIntent = Intent(this, AlarmService::class.java).apply {
            putExtra("eventId", eventId)
            putExtra("senderName", senderName)
            putExtra("time", time)
        }
        startForegroundService(serviceIntent)

        // Also show notification with action buttons
        val openIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("eventId", eventId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Wake up action
        val wakeUpIntent = Intent(this, AlarmActionReceiver::class.java).apply {
            action = "ACTION_WAKE_UP"
            putExtra("eventId", eventId)
        }
        val wakeUpPendingIntent = PendingIntent.getBroadcast(
            this, 1, wakeUpIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze action
        val snoozeIntent = Intent(this, AlarmActionReceiver::class.java).apply {
            action = "ACTION_SNOOZE"
            putExtra("eventId", eventId)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            this, 2, snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Dismiss action
        val dismissIntent = Intent(this, AlarmActionReceiver::class.java).apply {
            action = "ACTION_DISMISS"
            putExtra("eventId", eventId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            this, 3, dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "alarm_channel")
            .setContentTitle("${senderName}からのアラーム")
            .setContentText("${time}のアラームが鳴っています")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(openPendingIntent, true)
            .setContentIntent(openPendingIntent)
            .setAutoCancel(false)
            .setOngoing(true)
            .addAction(0, "☀️ 起きた", wakeUpPendingIntent)
            .addAction(0, "😴 スヌーズ", snoozePendingIntent)
            .addAction(0, "❌ 拒否", dismissPendingIntent)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(ALARM_NOTIFICATION_ID, notification)
    }

    override fun onNewToken(token: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("users").document(uid)
                    .update("fcmToken", token)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showNotification(
        title: String,
        body: String,
        channel: String = "general_channel",
        notificationId: Int = System.currentTimeMillis().toInt()
    ) {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channel)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(notificationId, notification)
    }

    companion object {
        const val ALARM_NOTIFICATION_ID = 1001
    }
}
