package com.wakee.app.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.wakee.app.ui.theme.*
import com.wakee.app.util.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    onChatClick: (String, String) -> Unit,
    onUserClick: (String) -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val chats by viewModel.chats.collectAsState()
    val chatUsers by viewModel.chatUsers.collectAsState()
    val currentUid = viewModel.currentUid

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        TopAppBar(
            title = { Text("チャット", color = PrimaryText, fontWeight = FontWeight.Bold, fontSize = 24.sp) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
        )

        if (chats.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.ChatBubble,
                        contentDescription = null,
                        tint = SecondaryText,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("チャットがありません", color = SecondaryText, fontSize = 16.sp)
                }
            }
        } else {
            LazyColumn {
                items(chats, key = { it.chatId }) { chat ->
                    val otherUid = chat.participants.find { it != currentUid } ?: ""
                    val otherUser = chatUsers[otherUid]
                    val unread = chat.unreadCount[currentUid] ?: 0

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onChatClick(chat.chatId, otherUid) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(SurfaceVariant)
                        ) {
                            AsyncImage(
                                model = otherUser?.photoURL,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = otherUser?.displayName ?: "",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PrimaryText,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = TimeUtils.timeAgo(chat.lastMessageAt),
                                    fontSize = 12.sp,
                                    color = SecondaryText
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = chat.lastMessage,
                                    fontSize = 14.sp,
                                    color = SecondaryText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                if (unread > 0) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(Accent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (unread > 99) "99+" else "$unread",
                                            fontSize = 11.sp,
                                            color = PrimaryText,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                    HorizontalDivider(color = Border, thickness = 0.5.dp, modifier = Modifier.padding(start = 80.dp))
                }
            }
        }
    }
}
