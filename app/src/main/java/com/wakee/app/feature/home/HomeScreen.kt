package com.wakee.app.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.wakee.app.data.model.Activity
import com.wakee.app.data.model.Story
import com.wakee.app.ui.theme.*
import com.wakee.app.util.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onPostClick: (String) -> Unit,
    onUserClick: (String) -> Unit,
    onNotificationClick: () -> Unit,
    onStoryCreate: () -> Unit,
    onStoryClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val activities by viewModel.activities.collectAsState()
    val stories by viewModel.stories.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Wakee",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = PrimaryText
            )
            IconButton(onClick = onNotificationClick) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "通知",
                    tint = PrimaryText
                )
            }
        }

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.loadData() },
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                // Story Row
                item {
                    StoryRow(
                        stories = stories,
                        currentUserPhotoURL = currentUser?.photoURL,
                        currentUid = currentUser?.uid ?: "",
                        onStoryCreate = onStoryCreate,
                        onStoryClick = onStoryClick
                    )
                    HorizontalDivider(color = Border, thickness = 0.5.dp)
                }

                // Feed
                items(activities, key = { it.activityId }) { activity ->
                    FeedItemView(
                        activity = activity,
                        currentUid = currentUser?.uid ?: "",
                        onPostClick = { onPostClick(activity.activityId) },
                        onUserClick = { onUserClick(activity.uid) },
                        onLike = { viewModel.toggleLike(activity.activityId) },
                        onRepost = { viewModel.repost(activity.activityId) }
                    )
                    HorizontalDivider(color = Border, thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
fun StoryRow(
    stories: List<Story>,
    currentUserPhotoURL: String?,
    currentUid: String,
    onStoryCreate: () -> Unit,
    onStoryClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Add story button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable { onStoryCreate() }
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(SurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (currentUserPhotoURL != null) {
                    AsyncImage(
                        model = currentUserPhotoURL,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Accent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = null,
                        tint = PrimaryText,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("ストーリー", fontSize = 10.sp, color = SecondaryText)
        }

        // Group stories by user
        val grouped = stories.groupBy { it.uid }
        grouped.forEach { (uid, userStories) ->
            val story = userStories.first()
            val hasUnread = userStories.any { !it.viewedBy.contains(currentUid) }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onStoryClick(story.storyId) }
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .then(
                            if (hasUnread) Modifier.border(
                                2.dp,
                                Brush.linearGradient(listOf(Accent, AccentEnd)),
                                CircleShape
                            ) else Modifier.border(2.dp, Border, CircleShape)
                        )
                        .padding(3.dp)
                        .clip(CircleShape)
                        .background(SurfaceVariant)
                ) {
                    AsyncImage(
                        model = story.photoURL,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = story.displayName,
                    fontSize = 10.sp,
                    color = if (hasUnread) PrimaryText else SecondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 64.dp)
                )
            }
        }
    }
}

@Composable
fun FeedItemView(
    activity: Activity,
    currentUid: String,
    onPostClick: () -> Unit,
    onUserClick: () -> Unit,
    onLike: () -> Unit,
    onRepost: () -> Unit
) {
    val isLiked = activity.likes.contains(currentUid)
    val resultEmoji = when (activity.result) {
        Activity.RESULT_WOKE_UP -> "☀️"
        Activity.RESULT_SNOOZED -> "😴"
        Activity.RESULT_DISMISSED -> "❌"
        Activity.RESULT_MISSED -> "💤"
        else -> "⏰"
    }
    val resultText = when (activity.result) {
        Activity.RESULT_WOKE_UP -> "即起き！"
        Activity.RESULT_SNOOZED -> "スヌーズした"
        Activity.RESULT_DISMISSED -> "拒否した"
        Activity.RESULT_MISSED -> "寝過ごした"
        else -> ""
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPostClick() }
            .padding(16.dp)
    ) {
        // Header: avatar + name + time
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(SurfaceVariant)
                    .clickable { onUserClick() }
            ) {
                AsyncImage(
                    model = activity.photoURL,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.displayName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryText
                )
                Text(
                    text = "@${activity.username} · ${TimeUtils.timeAgo(activity.createdAt)}",
                    fontSize = 12.sp,
                    color = SecondaryText
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content
        when (activity.type) {
            Activity.TYPE_ALARM_RESULT -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "$resultEmoji $resultText",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${activity.targetDisplayName}からの${activity.alarmTime ?: ""}のアラーム",
                            fontSize = 14.sp,
                            color = SecondaryText
                        )
                        if (!activity.reactionEmoji.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = activity.reactionEmoji,
                                fontSize = 32.sp
                            )
                        }
                    }
                }
            }
            Activity.TYPE_REPOST -> {
                if (!activity.repostComment.isNullOrEmpty()) {
                    Text(
                        text = activity.repostComment,
                        fontSize = 15.sp,
                        color = PrimaryText
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Repeat,
                                contentDescription = null,
                                tint = SecondaryText,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${activity.originalDisplayName}の投稿をリポスト",
                                fontSize = 12.sp,
                                color = SecondaryText
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Like
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onLike() }
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "いいね",
                    tint = if (isLiked) Danger else SecondaryText,
                    modifier = Modifier.size(20.dp)
                )
                if (activity.likes.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${activity.likes.size}",
                        fontSize = 13.sp,
                        color = if (isLiked) Danger else SecondaryText
                    )
                }
            }

            // Comment
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = "コメント",
                    tint = SecondaryText,
                    modifier = Modifier.size(20.dp)
                )
                if (activity.commentCount > 0) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${activity.commentCount}",
                        fontSize = 13.sp,
                        color = SecondaryText
                    )
                }
            }

            // Repost
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onRepost() }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Repeat,
                    contentDescription = "リポスト",
                    tint = if (activity.reposts.contains(currentUid)) Success else SecondaryText,
                    modifier = Modifier.size(20.dp)
                )
                if (activity.reposts.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${activity.reposts.size}",
                        fontSize = 13.sp,
                        color = SecondaryText
                    )
                }
            }
        }
    }
}
