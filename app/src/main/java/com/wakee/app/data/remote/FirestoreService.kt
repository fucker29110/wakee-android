package com.wakee.app.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.Timestamp
import com.wakee.app.data.model.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    val currentUid: String? get() = auth.currentUser?.uid

    // ==================== Users ====================

    suspend fun getUser(uid: String): AppUser? {
        return firestore.collection("users").document(uid)
            .get().await().toObject(AppUser::class.java)
    }

    suspend fun createOrGetUser(uid: String, displayName: String, email: String?, photoURL: String?): AppUser {
        val docRef = firestore.collection("users").document(uid)
        val doc = docRef.get().await()
        if (doc.exists()) {
            return doc.toObject(AppUser::class.java) ?: AppUser(uid = uid)
        }
        val newUser = AppUser(
            uid = uid,
            displayName = displayName,
            username = "",
            photoURL = photoURL,
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now()
        )
        docRef.set(newUser).await()
        return newUser
    }

    suspend fun updateUser(uid: String, updates: Map<String, Any>) {
        val mutable = updates.toMutableMap()
        mutable["updatedAt"] = Timestamp.now()
        firestore.collection("users").document(uid)
            .update(mutable).await()
    }

    suspend fun searchUsers(query: String): List<AppUser> {
        val byUsername = firestore.collection("users")
            .whereGreaterThanOrEqualTo("username", query.lowercase())
            .whereLessThanOrEqualTo("username", query.lowercase() + "\uf8ff")
            .limit(20)
            .get().await()
            .toObjects(AppUser::class.java)

        val byDisplayName = firestore.collection("users")
            .whereGreaterThanOrEqualTo("displayName", query)
            .whereLessThanOrEqualTo("displayName", query + "\uf8ff")
            .limit(20)
            .get().await()
            .toObjects(AppUser::class.java)

        return (byUsername + byDisplayName).distinctBy { it.uid }
    }

    // ==================== Friends ====================

    suspend fun getFriends(uid: String): List<String> {
        return firestore.collection("users").document(uid)
            .collection("friends")
            .get().await()
            .documents.map { it.id }
    }

    suspend fun addFriend(uid: String, friendUid: String) {
        val batch = firestore.batch()
        batch.set(
            firestore.collection("users").document(uid).collection("friends").document(friendUid),
            mapOf("since" to Timestamp.now())
        )
        batch.set(
            firestore.collection("users").document(friendUid).collection("friends").document(uid),
            mapOf("since" to Timestamp.now())
        )
        batch.commit().await()
    }

    suspend fun removeFriend(uid: String, friendUid: String) {
        val batch = firestore.batch()
        batch.delete(firestore.collection("users").document(uid).collection("friends").document(friendUid))
        batch.delete(firestore.collection("users").document(friendUid).collection("friends").document(uid))
        batch.commit().await()
    }

    suspend fun isFriend(uid: String, friendUid: String): Boolean {
        return firestore.collection("users").document(uid)
            .collection("friends").document(friendUid)
            .get().await().exists()
    }

    // ==================== Follow Requests ====================

    suspend fun sendFollowRequest(from: AppUser, toUid: String) {
        val request = mapOf(
            "fromUid" to from.uid,
            "toUid" to toUid,
            "fromDisplayName" to from.displayName,
            "fromUsername" to from.username,
            "fromPhotoURL" to (from.photoURL ?: ""),
            "status" to FollowRequest.STATUS_PENDING,
            "createdAt" to Timestamp.now()
        )
        firestore.collection("follow_requests").add(request).await()
    }

    suspend fun getPendingRequests(uid: String): List<FollowRequest> {
        return firestore.collection("follow_requests")
            .whereEqualTo("toUid", uid)
            .whereEqualTo("status", FollowRequest.STATUS_PENDING)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get().await()
            .toObjects(FollowRequest::class.java)
    }

    suspend fun getSentRequests(uid: String): List<FollowRequest> {
        return firestore.collection("follow_requests")
            .whereEqualTo("fromUid", uid)
            .whereEqualTo("status", FollowRequest.STATUS_PENDING)
            .get().await()
            .toObjects(FollowRequest::class.java)
    }

    suspend fun acceptFollowRequest(requestId: String, fromUid: String, toUid: String) {
        firestore.collection("follow_requests").document(requestId)
            .update("status", FollowRequest.STATUS_ACCEPTED).await()
        addFriend(fromUid, toUid)
    }

    suspend fun rejectFollowRequest(requestId: String) {
        firestore.collection("follow_requests").document(requestId)
            .update("status", FollowRequest.STATUS_REJECTED).await()
    }

    // ==================== Inbox (Alarms) ====================

    fun observeInbox(uid: String): Flow<List<InboxEvent>> = callbackFlow {
        val listener = firestore.collection("users").document(uid)
            .collection("inbox")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val events = snapshot?.toObjects(InboxEvent::class.java) ?: emptyList()
                trySend(events)
            }
        awaitClose { listener.remove() }
    }

    suspend fun sendAlarm(toUid: String, event: Map<String, Any>): String {
        val docRef = firestore.collection("users").document(toUid)
            .collection("inbox").add(event).await()
        return docRef.id
    }

    suspend fun updateInboxStatus(uid: String, eventId: String, status: String) {
        firestore.collection("users").document(uid)
            .collection("inbox").document(eventId)
            .update("status", status).await()
    }

    suspend fun getInboxEvent(uid: String, eventId: String): InboxEvent? {
        return firestore.collection("users").document(uid)
            .collection("inbox").document(eventId)
            .get().await().toObject(InboxEvent::class.java)
    }

    // ==================== Activities (Feed) ====================

    suspend fun getActivities(friendUids: List<String>, limit: Long = 50): List<Activity> {
        if (friendUids.isEmpty()) return emptyList()
        val chunks = friendUids.chunked(10)
        val results = mutableListOf<Activity>()
        for (chunk in chunks) {
            val docs = firestore.collection("activities")
                .whereIn("uid", chunk)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit)
                .get().await()
                .toObjects(Activity::class.java)
            results.addAll(docs)
        }
        return results.sortedByDescending { it.createdAt }
    }

    suspend fun getActivity(activityId: String): Activity? {
        return firestore.collection("activities").document(activityId)
            .get().await().toObject(Activity::class.java)
    }

    suspend fun createActivity(activity: Map<String, Any>): String {
        val docRef = firestore.collection("activities").add(activity).await()
        return docRef.id
    }

    suspend fun toggleLike(activityId: String, uid: String) {
        val docRef = firestore.collection("activities").document(activityId)
        val doc = docRef.get().await()
        val likes = doc.get("likes") as? List<*> ?: emptyList<String>()
        if (likes.contains(uid)) {
            docRef.update("likes", FieldValue.arrayRemove(uid)).await()
        } else {
            docRef.update("likes", FieldValue.arrayUnion(uid)).await()
        }
    }

    suspend fun addRepost(activityId: String, uid: String) {
        firestore.collection("activities").document(activityId)
            .update("reposts", FieldValue.arrayUnion(uid)).await()
    }

    // ==================== Comments ====================

    suspend fun getComments(activityId: String): List<Comment> {
        return firestore.collection("activities").document(activityId)
            .collection("comments")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .get().await()
            .toObjects(Comment::class.java)
    }

    suspend fun addComment(activityId: String, comment: Map<String, Any>) {
        val batch = firestore.batch()
        batch.set(
            firestore.collection("activities").document(activityId)
                .collection("comments").document(),
            comment
        )
        batch.update(
            firestore.collection("activities").document(activityId),
            "commentCount", FieldValue.increment(1)
        )
        batch.commit().await()
    }

    // ==================== Stories ====================

    suspend fun getStories(friendUids: List<String>): List<Story> {
        if (friendUids.isEmpty()) return emptyList()
        val now = Timestamp.now()
        val chunks = friendUids.chunked(10)
        val results = mutableListOf<Story>()
        for (chunk in chunks) {
            val docs = firestore.collection("stories")
                .whereIn("uid", chunk)
                .whereGreaterThan("expiresAt", now)
                .orderBy("expiresAt")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().await()
                .toObjects(Story::class.java)
            results.addAll(docs)
        }
        return results
    }

    suspend fun createStory(story: Map<String, Any>): String {
        val docRef = firestore.collection("stories").add(story).await()
        return docRef.id
    }

    suspend fun markStoryViewed(storyId: String, uid: String) {
        firestore.collection("stories").document(storyId)
            .update("viewedBy", FieldValue.arrayUnion(uid)).await()
    }

    suspend fun getStory(storyId: String): Story? {
        return firestore.collection("stories").document(storyId)
            .get().await().toObject(Story::class.java)
    }

    // ==================== Chats ====================

    fun observeChats(uid: String): Flow<List<Chat>> = callbackFlow {
        val listener = firestore.collection("chats")
            .whereArrayContains("participants", uid)
            .orderBy("lastMessageAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val chats = snapshot?.toObjects(Chat::class.java) ?: emptyList()
                trySend(chats)
            }
        awaitClose { listener.remove() }
    }

    fun observeMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        val listener = firestore.collection("chats").document(chatId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val messages = snapshot?.toObjects(Message::class.java) ?: emptyList()
                trySend(messages)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getOrCreateChat(uid: String, otherUid: String): String {
        // Check if chat already exists
        val existing = firestore.collection("chats")
            .whereArrayContains("participants", uid)
            .get().await()
            .toObjects(Chat::class.java)
            .find { it.participants.contains(otherUid) }

        if (existing != null) return existing.chatId

        val chatData = mapOf(
            "participants" to listOf(uid, otherUid),
            "lastMessage" to "",
            "lastMessageAt" to Timestamp.now(),
            "lastSenderUid" to "",
            "unreadCount" to mapOf(uid to 0, otherUid to 0),
            "createdAt" to Timestamp.now()
        )
        return firestore.collection("chats").add(chatData).await().id
    }

    suspend fun sendMessage(chatId: String, senderUid: String, text: String, imageURL: String? = null) {
        val messageData = mapOf(
            "senderUid" to senderUid,
            "text" to text,
            "imageURL" to (imageURL ?: ""),
            "read" to false,
            "createdAt" to Timestamp.now()
        )
        val batch = firestore.batch()
        batch.set(
            firestore.collection("chats").document(chatId).collection("messages").document(),
            messageData
        )
        // Get chat to find other participant
        val chat = firestore.collection("chats").document(chatId).get().await()
            .toObject(Chat::class.java)
        val otherUid = chat?.participants?.find { it != senderUid } ?: ""

        batch.update(
            firestore.collection("chats").document(chatId),
            mapOf(
                "lastMessage" to text,
                "lastMessageAt" to Timestamp.now(),
                "lastSenderUid" to senderUid,
                "unreadCount.$otherUid" to FieldValue.increment(1)
            )
        )
        batch.commit().await()
    }

    suspend fun markChatRead(chatId: String, uid: String) {
        firestore.collection("chats").document(chatId)
            .update("unreadCount.$uid", 0).await()
    }

    // ==================== Notifications ====================

    fun observeNotifications(uid: String): Flow<List<AppNotification>> = callbackFlow {
        val listener = firestore.collection("notifications")
            .whereEqualTo("toUid", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val notifs = snapshot?.toObjects(AppNotification::class.java) ?: emptyList()
                trySend(notifs)
            }
        awaitClose { listener.remove() }
    }

    suspend fun createNotification(notification: Map<String, Any>) {
        firestore.collection("notifications").add(notification).await()
    }

    suspend fun markNotificationRead(notificationId: String) {
        firestore.collection("notifications").document(notificationId)
            .update("read", true).await()
    }

    suspend fun markAllNotificationsRead(uid: String) {
        val docs = firestore.collection("notifications")
            .whereEqualTo("toUid", uid)
            .whereEqualTo("read", false)
            .get().await()
        val batch = firestore.batch()
        docs.documents.forEach { doc ->
            batch.update(doc.reference, "read", true)
        }
        batch.commit().await()
    }

    // ==================== FCM Token ====================

    suspend fun updateFcmToken(uid: String, token: String) {
        firestore.collection("users").document(uid)
            .update("fcmToken", token).await()
    }
}
