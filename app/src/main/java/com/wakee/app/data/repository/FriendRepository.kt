package com.wakee.app.data.repository

import com.wakee.app.data.model.AppUser
import com.wakee.app.data.model.FollowRequest
import com.wakee.app.data.remote.FirestoreService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FriendRepository @Inject constructor(
    private val firestoreService: FirestoreService
) {
    suspend fun getFriends(uid: String): List<String> {
        return firestoreService.getFriends(uid)
    }

    suspend fun getFriendUsers(uid: String): List<AppUser> {
        val friendUids = getFriends(uid)
        return friendUids.mapNotNull { firestoreService.getUser(it) }
    }

    suspend fun isFriend(uid: String, friendUid: String): Boolean {
        return firestoreService.isFriend(uid, friendUid)
    }

    suspend fun removeFriend(uid: String, friendUid: String) {
        firestoreService.removeFriend(uid, friendUid)
    }

    suspend fun sendFollowRequest(from: AppUser, toUid: String) {
        firestoreService.sendFollowRequest(from, toUid)
    }

    suspend fun getPendingRequests(uid: String): List<FollowRequest> {
        return firestoreService.getPendingRequests(uid)
    }

    suspend fun getSentRequests(uid: String): List<FollowRequest> {
        return firestoreService.getSentRequests(uid)
    }

    suspend fun acceptRequest(requestId: String, fromUid: String, toUid: String) {
        firestoreService.acceptFollowRequest(requestId, fromUid, toUid)
    }

    suspend fun rejectRequest(requestId: String) {
        firestoreService.rejectFollowRequest(requestId)
    }
}
