package com.wakee.app.feature.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.wakee.app.data.model.AppUser
import com.wakee.app.data.model.FollowRequest
import com.wakee.app.ui.theme.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsListScreen(
    onUserClick: (String) -> Unit,
    viewModel: FriendsViewModel = hiltViewModel()
) {
    val friends by viewModel.friends.collectAsState()
    val pendingRequests by viewModel.pendingRequests.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        TopAppBar(
            title = { Text("フレンド", color = PrimaryText, fontWeight = FontWeight.Bold, fontSize = 24.sp) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
        )

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Background,
            contentColor = Accent,
            indicator = { tabPositions ->
                if (selectedTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Accent
                    )
                }
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { viewModel.setTab(0) },
                text = { Text("フレンド", color = if (selectedTab == 0) PrimaryText else SecondaryText) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { viewModel.setTab(1) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("リクエスト", color = if (selectedTab == 1) PrimaryText else SecondaryText)
                        if (pendingRequests.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(Accent),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${pendingRequests.size}", fontSize = 10.sp, color = PrimaryText, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { viewModel.setTab(2) },
                text = { Text("検索", color = if (selectedTab == 2) PrimaryText else SecondaryText) }
            )
        }

        when (selectedTab) {
            0 -> FriendsTab(friends = friends, onUserClick = onUserClick)
            1 -> RequestsTab(
                requests = pendingRequests,
                onAccept = { viewModel.acceptRequest(it) },
                onReject = { viewModel.rejectRequest(it) },
                onUserClick = onUserClick
            )
            2 -> SearchTab(
                query = searchQuery,
                onQueryChange = { searchQuery = it; viewModel.searchUsers(it) },
                results = searchResults,
                onUserClick = onUserClick,
                onSendRequest = { viewModel.sendRequest(it) }
            )
        }
    }
}

@Composable
fun FriendsTab(friends: List<AppUser>, onUserClick: (String) -> Unit) {
    if (friends.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.People, contentDescription = null, tint = SecondaryText, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("フレンドがいません", color = SecondaryText, fontSize = 16.sp)
                Text("検索タブで友達を見つけましょう", color = SecondaryText, fontSize = 14.sp)
            }
        }
    } else {
        LazyColumn {
            items(friends, key = { it.uid }) { user ->
                UserListItem(user = user, onClick = { onUserClick(user.uid) })
            }
        }
    }
}

@Composable
fun RequestsTab(
    requests: List<FollowRequest>,
    onAccept: (FollowRequest) -> Unit,
    onReject: (FollowRequest) -> Unit,
    onUserClick: (String) -> Unit
) {
    if (requests.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("保留中のリクエストはありません", color = SecondaryText, fontSize = 16.sp)
        }
    } else {
        LazyColumn {
            items(requests, key = { it.requestId }) { request ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onUserClick(request.fromUid) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(SurfaceVariant)
                    ) {
                        AsyncImage(
                            model = request.fromPhotoURL,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(request.fromDisplayName, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = PrimaryText)
                        Text("@${request.fromUsername}", fontSize = 13.sp, color = SecondaryText)
                    }
                    IconButton(onClick = { onAccept(request) }) {
                        Icon(Icons.Filled.Check, contentDescription = "承認", tint = Success)
                    }
                    IconButton(onClick = { onReject(request) }) {
                        Icon(Icons.Filled.Close, contentDescription = "拒否", tint = Danger)
                    }
                }
                HorizontalDivider(color = Border, thickness = 0.5.dp)
            }
        }
    }
}

@Composable
fun SearchTab(
    query: String,
    onQueryChange: (String) -> Unit,
    results: List<AppUser>,
    onUserClick: (String) -> Unit,
    onSendRequest: (String) -> Unit
) {
    Column {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("ユーザー名で検索", color = SecondaryText) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = SecondaryText) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = PrimaryText,
                unfocusedTextColor = PrimaryText,
                focusedBorderColor = Accent,
                unfocusedBorderColor = Border,
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface,
                cursorColor = Accent
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        LazyColumn {
            items(results, key = { it.uid }) { user ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onUserClick(user.uid) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(SurfaceVariant)
                    ) {
                        AsyncImage(
                            model = user.photoURL,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(user.displayName, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = PrimaryText)
                        Text("@${user.username}", fontSize = 13.sp, color = SecondaryText)
                    }
                    Button(
                        onClick = { onSendRequest(user.uid) },
                        colors = ButtonDefaults.buttonColors(containerColor = Accent),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text("追加", fontSize = 13.sp, color = PrimaryText)
                    }
                }
                HorizontalDivider(color = Border, thickness = 0.5.dp)
            }
        }
    }
}

@Composable
fun UserListItem(user: AppUser, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(SurfaceVariant)
        ) {
            AsyncImage(
                model = user.photoURL,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(user.displayName, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = PrimaryText)
            Text("@${user.username}", fontSize = 13.sp, color = SecondaryText)
        }
    }
    HorizontalDivider(color = Border, thickness = 0.5.dp)
}
