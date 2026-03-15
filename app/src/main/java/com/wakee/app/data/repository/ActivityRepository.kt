package com.wakee.app.data.repository

import com.google.firebase.Timestamp
import com.wakee.app.data.model.Activity
import com.wakee.app.data.model.Comment
import com.wakee.app.data.remote.FirestoreService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityRepository @Inject constructor(
    private val firestoreService: FirestoreService
) {
    suspend fun getFeed(friendUids: List<String>): List<Activity> {
        return firestoreService.getActivities(friendUids)
    }

    suspend fun getActivity(activityId: String): Activity? {
        return firestoreService.getActivity(activityId)
    }

    suspend fun createAlarmResultActivity(
        uid: String,
        displayName: String,
        username: String,
        photoURL: String?,
        targetUid: String,
        targetDisplayName: String,
        targetUsername: String,
        alarmTime: String,
        result: String,
        reactionEmoji: String? = null
    ): String {
        val activity = mapOf(
            "type" to Activity.TYPE_ALARM_RESULT,
            "uid" to uid,
            "displayName" to displayName,
            "username" to username,
            "photoURL" to (photoURL ?: ""),
            "targetUid" to targetUid,
            "targetDisplayName" to targetDisplayName,
            "targetUsername" to targetUsername,
            "alarmTime" to alarmTime,
            "result" to result,
            "reactionEmoji" to (reactionEmoji ?: ""),
            "likes" to emptyList<String>(),
            "reposts" to emptyList<String>(),
            "commentCount" to 0,
            "createdAt" to Timestamp.now()
        )
        return firestoreService.createActivity(activity)
    }

    suspend fun toggleLike(activityId: String, uid: String) {
        firestoreService.toggleLike(activityId, uid)
    }

    suspend fun repost(activityId: String, uid: String, displayName: String, username: String, photoURL: String?, comment: String?) {
        val original = firestoreService.getActivity(activityId) ?: return
        firestoreService.addRepost(activityId, uid)
        val repostData = mapOf(
            "type" to Activity.TYPE_REPOST,
            "uid" to uid,
            "displayName" to displayName,
            "username" to username,
            "photoURL" to (photoURL ?: ""),
            "originalActivityId" to activityId,
            "originalUid" to original.uid,
            "originalDisplayName" to original.displayName,
            "repostComment" to (comment ?: ""),
            "likes" to emptyList<String>(),
            "reposts" to emptyList<String>(),
            "commentCount" to 0,
            "createdAt" to Timestamp.now()
        )
        firestoreService.createActivity(repostData)
    }

    suspend fun getComments(activityId: String): List<Comment> {
        return firestoreService.getComments(activityId)
    }

    suspend fun addComment(activityId: String, uid: String, displayName: String, username: String, photoURL: String?, text: String) {
        val comment = mapOf(
            "activityId" to activityId,
            "uid" to uid,
            "displayName" to displayName,
            "username" to username,
            "photoURL" to (photoURL ?: ""),
            "text" to text,
            "createdAt" to Timestamp.now()
        )
        firestoreService.addComment(activityId, comment)
    }
}
