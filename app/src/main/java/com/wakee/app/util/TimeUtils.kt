package com.wakee.app.util

import com.google.firebase.Timestamp
import java.util.concurrent.TimeUnit

object TimeUtils {
    fun timeAgo(timestamp: Timestamp?): String {
        if (timestamp == null) return ""
        val now = System.currentTimeMillis()
        val then = timestamp.toDate().time
        val diff = now - then

        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)

        return when {
            minutes < 1 -> "たった今"
            minutes < 60 -> "${minutes}分前"
            hours < 24 -> "${hours}時間前"
            days < 7 -> "${days}日前"
            days < 30 -> "${days / 7}週間前"
            days < 365 -> "${days / 30}ヶ月前"
            else -> "${days / 365}年前"
        }
    }

    fun formatTime(hour: Int, minute: Int): String {
        return String.format("%02d:%02d", hour, minute)
    }
}
