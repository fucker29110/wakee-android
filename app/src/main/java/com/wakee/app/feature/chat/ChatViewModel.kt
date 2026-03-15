package com.wakee.app.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.wakee.app.data.model.AppUser
import com.wakee.app.data.model.Chat
import com.wakee.app.data.model.Message
import com.wakee.app.data.repository.AuthRepository
import com.wakee.app.data.repository.ChatRepository
import com.wakee.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    private val _chatUsers = MutableStateFlow<Map<String, AppUser>>(emptyMap())
    val chatUsers: StateFlow<Map<String, AppUser>> = _chatUsers.asStateFlow()

    val currentUid: String get() = authRepository.currentUid ?: ""

    init {
        observeChats()
    }

    private fun observeChats() {
        val uid = authRepository.currentUid ?: return
        viewModelScope.launch {
            chatRepository.observeChats(uid).collect { chatList ->
                _chats.value = chatList
                val userIds = chatList.flatMap { it.participants }.distinct().filter { it != uid }
                val users = mutableMapOf<String, AppUser>()
                userIds.forEach { userId ->
                    userRepository.getUser(userId)?.let { users[userId] = it }
                }
                _chatUsers.value = users
            }
        }
    }
}

@HiltViewModel
class ChatRoomViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _otherUser = MutableStateFlow<AppUser?>(null)
    val otherUser: StateFlow<AppUser?> = _otherUser.asStateFlow()

    val currentUid: String get() = authRepository.currentUid ?: ""

    private var chatId = ""

    fun init(chatId: String, otherUserId: String) {
        this.chatId = chatId
        viewModelScope.launch {
            _otherUser.value = userRepository.getUser(otherUserId)
            chatRepository.markChatRead(chatId, currentUid)
        }
        viewModelScope.launch {
            chatRepository.observeMessages(chatId).collect { msgs ->
                _messages.value = msgs
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            try {
                chatRepository.sendMessage(chatId, currentUid, text)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
