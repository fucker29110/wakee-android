package com.wakee.app.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeepLinkManager @Inject constructor() {

    sealed class DeepLink {
        data class Alarm(val eventId: String) : DeepLink()
        data class Chat(val chatId: String, val otherUserId: String) : DeepLink()
        data class Profile(val userId: String) : DeepLink()
        data class Post(val activityId: String) : DeepLink()
        data object Home : DeepLink()
    }

    private val _pendingDeepLink = MutableStateFlow<DeepLink?>(null)
    val pendingDeepLink: StateFlow<DeepLink?> = _pendingDeepLink.asStateFlow()

    fun setPendingDeepLink(deepLink: DeepLink) {
        _pendingDeepLink.value = deepLink
    }

    fun consumeDeepLink(): DeepLink? {
        val link = _pendingDeepLink.value
        _pendingDeepLink.value = null
        return link
    }

    fun handleIntent(extras: android.os.Bundle?) {
        extras ?: return
        val eventId = extras.getString("eventId")
        val chatId = extras.getString("chatId")
        val otherUserId = extras.getString("otherUserId")
        val activityId = extras.getString("activityId")
        val userId = extras.getString("userId")

        when {
            eventId != null -> setPendingDeepLink(DeepLink.Alarm(eventId))
            chatId != null && otherUserId != null -> setPendingDeepLink(DeepLink.Chat(chatId, otherUserId))
            activityId != null -> setPendingDeepLink(DeepLink.Post(activityId))
            userId != null -> setPendingDeepLink(DeepLink.Profile(userId))
        }
    }
}
