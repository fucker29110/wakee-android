package com.wakee.app.feature.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.wakee.app.data.model.AppNotification
import com.wakee.app.data.repository.AuthRepository
import com.wakee.app.data.repository.NotificationRepository
import com.wakee.app.ui.theme.*
import com.wakee.app.util.TimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onBack: () -> Unit,
    onUserClick: (String) -> Unit,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        TopAppBar(
            title = { Text("通知", color = PrimaryText, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "戻る", tint = PrimaryText)
                }
            },
            actions = {
                TextButton(onClick = { viewModel.markAllRead() }) {
                    Text("全て既読", color = Accent, fontSize = 14.sp)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
        )

        if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Notifications, contentDescription = null, tint = SecondaryText, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("通知はありません", color = SecondaryText, fontSize = 16.sp)
                }
            }
        } else {
            LazyColumn {
                items(notifications, key = { it.notificationId }) { notification ->
                    NotificationItem(
                        notification = notification,
                        onClick = {
                            viewModel.markRead(notification.notificationId)
                            onUserClick(notification.fromUid)
                        }
                    )
                    HorizontalDivider(color = Border, thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: AppNotification, onClick: () -> Unit) {
    val icon = when (notification.type) {
        AppNotification.TYPE_ALARM -> Icons.Filled.Alarm
        AppNotification.TYPE_FRIEND_REQUEST -> Icons.Filled.PersonAdd
        AppNotification.TYPE_FRIEND_ACCEPT -> Icons.Filled.People
        AppNotification.TYPE_LIKE -> Icons.Filled.Favorite
        AppNotification.TYPE_COMMENT -> Icons.Filled.ChatBubble
        AppNotification.TYPE_REPOST -> Icons.Filled.Repeat
        else -> Icons.Filled.Notifications
    }
    val iconColor = when (notification.type) {
        AppNotification.TYPE_ALARM -> Accent
        AppNotification.TYPE_LIKE -> Danger
        AppNotification.TYPE_FRIEND_REQUEST, AppNotification.TYPE_FRIEND_ACCEPT -> Success
        else -> SecondaryText
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (!notification.read) SurfaceVariant.copy(alpha = 0.3f) else Background)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(SurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (!notification.fromPhotoURL.isNullOrEmpty()) {
                AsyncImage(
                    model = notification.fromPhotoURL,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = notification.fromDisplayName,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryText
            )
            Text(
                text = notification.message,
                fontSize = 13.sp,
                color = SecondaryText
            )
        }

        Text(
            text = TimeUtils.timeAgo(notification.createdAt),
            fontSize = 11.sp,
            color = SecondaryText
        )

        if (!notification.read) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Accent)
            )
        }
    }
}

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    init {
        observeNotifications()
    }

    private fun observeNotifications() {
        val uid = authRepository.currentUid ?: return
        viewModelScope.launch {
            notificationRepository.observeNotifications(uid).collect {
                _notifications.value = it
            }
        }
    }

    fun markRead(notificationId: String) {
        viewModelScope.launch {
            try {
                notificationRepository.markRead(notificationId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            try {
                val uid = authRepository.currentUid ?: return@launch
                notificationRepository.markAllRead(uid)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
