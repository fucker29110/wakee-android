package com.wakee.app.feature.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.wakee.app.feature.alarm.AlarmService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val eventId = intent.getStringExtra("eventId") ?: return
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Stop alarm service
        context.stopService(Intent(context, AlarmService::class.java))

        // Clear notification
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.cancel(WakeeFirebaseMessagingService.ALARM_NOTIFICATION_ID)

        val status = when (intent.action) {
            "ACTION_WAKE_UP" -> "dismissed"
            "ACTION_SNOOZE" -> "snoozed"
            "ACTION_DISMISS" -> "dismissed"
            else -> return
        }

        // Update Firestore
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FirebaseFirestore.getInstance()
                    .collection("users").document(uid)
                    .collection("inbox").document(eventId)
                    .update("status", status)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
