package com.wakee.app.data.remote

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageService @Inject constructor(
    private val storage: FirebaseStorage
) {
    suspend fun uploadAvatar(uid: String, uri: Uri): String {
        val ref = storage.reference.child("avatars/$uid")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun uploadAlarmAudio(uid: String, uri: Uri): String {
        val timestamp = System.currentTimeMillis()
        val ref = storage.reference.child("alarm_audio/${uid}_${timestamp}.m4a")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun uploadStoryImage(uid: String, uri: Uri): String {
        val timestamp = System.currentTimeMillis()
        val ref = storage.reference.child("stories/${uid}_${timestamp}.jpg")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun uploadChatImage(chatId: String, uri: Uri): String {
        val timestamp = System.currentTimeMillis()
        val ref = storage.reference.child("chat_images/${chatId}_${timestamp}.jpg")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }
}
