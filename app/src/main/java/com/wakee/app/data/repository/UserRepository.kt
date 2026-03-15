package com.wakee.app.data.repository

import com.wakee.app.data.model.AppUser
import com.wakee.app.data.remote.FirestoreService
import com.wakee.app.data.remote.StorageService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firestoreService: FirestoreService,
    private val storageService: StorageService
) {
    suspend fun getUser(uid: String): AppUser? {
        return firestoreService.getUser(uid)
    }

    suspend fun searchUsers(query: String): List<AppUser> {
        return firestoreService.searchUsers(query)
    }

    suspend fun updateUser(uid: String, updates: Map<String, Any>) {
        firestoreService.updateUser(uid, updates)
    }

    suspend fun updateFcmToken(uid: String, token: String) {
        firestoreService.updateFcmToken(uid, token)
    }
}
