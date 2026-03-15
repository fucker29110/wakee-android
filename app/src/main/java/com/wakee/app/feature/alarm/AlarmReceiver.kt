package com.wakee.app.feature.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val eventId = intent.getStringExtra("eventId") ?: return
        val senderName = intent.getStringExtra("senderName") ?: ""
        val time = intent.getStringExtra("time") ?: ""

        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra("eventId", eventId)
            putExtra("senderName", senderName)
            putExtra("time", time)
        }
        context.startForegroundService(serviceIntent)
    }
}
