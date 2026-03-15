package com.wakee.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.wakee.app.data.model.AppUser
import com.wakee.app.data.remote.FirestoreService
import com.wakee.app.data.remote.StorageService
import android.net.Uri
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firestoreService: FirestoreService,
    private val storageService: StorageService
) {
    private val auth = FirebaseAuth.getInstance()

    val currentUid: String? get() = auth.currentUser?.uid
    val isLoggedIn: Boolean get() = auth.currentUser != null

    suspend fun signInWithGoogle(idToken: String): AppUser {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        val user = result.user ?: throw Exception("Sign in failed")
        return firestoreService.createOrGetUser(
            uid = user.uid,
            displayName = user.displayName ?: "",
            email = user.email,
            photoURL = user.photoURL?.toString()
        )
    }

    suspend fun signInWithEmail(email: String, password: String): AppUser {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val user = result.user ?: throw Exception("Sign in failed")
        return firestoreService.createOrGetUser(
            uid = user.uid,
            displayName = user.displayName ?: email.substringBefore("@"),
            email = user.email,
            photoURL = user.photoURL?.toString()
        )
    }

    suspend fun signUpWithEmail(email: String, password: String, displayName: String): AppUser {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user ?: throw Exception("Sign up failed")
        return firestoreService.createOrGetUser(
            uid = user.uid,
            displayName = displayName,
            email = user.email,
            photoURL = null
        )
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun getCurrentUser(): AppUser? {
        val uid = currentUid ?: return null
        return firestoreService.getUser(uid)
    }

    suspend fun updateProfile(updates: Map<String, Any>) {
        val uid = currentUid ?: return
        firestoreService.updateUser(uid, updates)
    }

    suspend fun uploadAvatar(uri: Uri): String {
        val uid = currentUid ?: throw Exception("Not logged in")
        val url = storageService.uploadAvatar(uid, uri)
        firestoreService.updateUser(uid, mapOf("photoURL" to url))
        return url
    }
}
