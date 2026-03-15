package com.wakee.app.data.repository

import android.net.Uri
import com.google.firebase.Timestamp
import com.wakee.app.data.model.Story
import com.wakee.app.data.remote.FirestoreService
import com.wakee.app.data.remote.StorageService
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoryRepository @Inject constructor(
    private val firestoreService: FirestoreService,
    private val storageService: StorageService
) {
    suspend fun getStories(friendUids: List<String>): List<Story> {
        return firestoreService.getStories(friendUids)
    }

    suspend fun createStory(uid: String, displayName: String, username: String, photoURL: String?, text: String, imageUri: Uri? = null): String {
        var imageURL: String? = null
        if (imageUri != null) {
            imageURL = storageService.uploadStoryImage(uid, imageUri)
        }
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.HOUR_OF_DAY, 24)
        val expiresAt = Timestamp(calendar.time)

        val story = mapOf(
            "uid" to uid,
            "displayName" to displayName,
            "username" to username,
            "photoURL" to (photoURL ?: ""),
            "text" to text,
            "imageURL" to (imageURL ?: ""),
            "viewedBy" to emptyList<String>(),
            "expiresAt" to expiresAt,
            "createdAt" to Timestamp.now()
        )
        return firestoreService.createStory(story)
    }

    suspend fun getStory(storyId: String): Story? {
        return firestoreService.getStory(storyId)
    }

    suspend fun markViewed(storyId: String, uid: String) {
        firestoreService.markStoryViewed(storyId, uid)
    }
}
