package com.wakee.app.data.repository

import com.wakee.app.data.model.Chat
import com.wakee.app.data.model.Message
import com.wakee.app.data.remote.FirestoreService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val firestoreService: FirestoreService
) {
    fun observeChats(uid: String): Flow<List<Chat>> {
        return firestoreService.observeChats(uid)
    }

    fun observeMessages(chatId: String): Flow<List<Message>> {
        return firestoreService.observeMessages(chatId)
    }

    suspend fun getOrCreateChat(uid: String, otherUid: String): String {
        return firestoreService.getOrCreateChat(uid, otherUid)
    }

    suspend fun sendMessage(chatId: String, senderUid: String, text: String, imageURL: String? = null) {
        firestoreService.sendMessage(chatId, senderUid, text, imageURL)
    }

    suspend fun markChatRead(chatId: String, uid: String) {
        firestoreService.markChatRead(chatId, uid)
    }
}
