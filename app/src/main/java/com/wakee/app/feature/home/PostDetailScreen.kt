package com.wakee.app.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
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
import com.wakee.app.data.model.Comment
import com.wakee.app.ui.theme.*
import com.wakee.app.util.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    activityId: String,
    onBack: () -> Unit,
    onUserClick: (String) -> Unit,
    viewModel: PostDetailViewModel = hiltViewModel()
) {
    val activity by viewModel.activity.collectAsState()
    val comments by viewModel.comments.collectAsState()
    var commentText by remember { mutableStateOf("") }

    LaunchedEffect(activityId) {
        viewModel.loadActivity(activityId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Top Bar
        TopAppBar(
            title = { Text("投稿", color = PrimaryText, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "戻る", tint = PrimaryText)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
        )

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            // Activity content
            activity?.let { act ->
                item {
                    FeedItemView(
                        activity = act,
                        currentUid = viewModel.currentUid,
                        onPostClick = {},
                        onUserClick = { onUserClick(act.uid) },
                        onLike = { viewModel.toggleLike() },
                        onRepost = { viewModel.repost() }
                    )
                    HorizontalDivider(color = Border, thickness = 0.5.dp)
                    Text(
                        text = "コメント",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Comments
            items(comments) { comment ->
                CommentItem(
                    comment = comment,
                    onUserClick = { onUserClick(comment.uid) }
                )
            }
        }

        // Comment input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = commentText,
                onValueChange = { commentText = it },
                placeholder = { Text("コメントを入力", color = SecondaryText, fontSize = 14.sp) },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = PrimaryText,
                    unfocusedTextColor = PrimaryText,
                    focusedBorderColor = Accent,
                    unfocusedBorderColor = Border,
                    focusedContainerColor = SurfaceVariant,
                    unfocusedContainerColor = SurfaceVariant,
                    cursorColor = Accent
                ),
                shape = RoundedCornerShape(20.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (commentText.isNotBlank()) {
                        viewModel.addComment(commentText)
                        commentText = ""
                    }
                }
            ) {
                Icon(Icons.Filled.Send, contentDescription = "送信", tint = Accent)
            }
        }
    }
}

@Composable
fun CommentItem(comment: Comment, onUserClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(SurfaceVariant)
                .clickable { onUserClick() }
        ) {
            AsyncImage(
                model = comment.photoURL,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comment.displayName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryText
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = TimeUtils.timeAgo(comment.createdAt),
                    fontSize = 11.sp,
                    color = SecondaryText
                )
            }
            Text(
                text = comment.text,
                fontSize = 14.sp,
                color = PrimaryText
            )
        }
    }
}
