package com.wakee.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WakeeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val alarmChannel = NotificationChannel(
            "alarm_channel",
            "アラーム",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "アラーム通知"
            setSound(null, null)
        }

        val messageChannel = NotificationChannel(
            "message_channel",
            "メッセージ",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "チャットメッセージ通知"
        }

        val generalChannel = NotificationChannel(
            "general_channel",
            "一般",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "一般通知"
        }

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(alarmChannel)
        manager.createNotificationChannel(messageChannel)
        manager.createNotificationChannel(generalChannel)
    }
}
