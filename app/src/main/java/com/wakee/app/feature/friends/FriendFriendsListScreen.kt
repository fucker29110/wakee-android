package com.wakee.app.feature.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.wakee.app.data.model.AppUser
import com.wakee.app.data.repository.FriendRepository
import com.wakee.app.data.repository.UserRepository
import com.wakee.app.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendFriendsListScreen(
    userId: String,
    onBack: () -> Unit,
    onUserClick: (String) -> Unit,
    viewModel: FriendFriendsListViewModel = hiltViewModel()
) {
    val friends by viewModel.friends.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val userName by viewModel.userName.collectAsState()

    LaunchedEffect(userId) {
        viewModel.load(userId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        TopAppBar(
            title = {
                Text(
                    "${userName}のフレンド",
                    color = PrimaryText,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "戻る", tint = PrimaryText)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
        )

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Accent)
                }
            }
            friends.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.People,
                            contentDescription = null,
                            tint = SecondaryText,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "フレンドがいません",
                            color = PrimaryText,
                            fontSize = 16.sp
                        )
                    }
                }
            }
            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(friends, key = { it.uid }) { friend ->
                        FriendRow(
                            friend = friend,
                            onClick = { onUserClick(friend.uid) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendRow(friend: AppUser, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(SurfaceVariant)
        ) {
            AsyncImage(
                model = friend.photoURL,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                friend.displayName,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryText,
                fontSize = 15.sp
            )
            Text(
                "@${friend.username}",
                color = SecondaryText,
                fontSize = 13.sp
            )
        }

        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = SecondaryText,
            modifier = Modifier.size(16.dp)
        )
    }

    Divider(color = Color(0xFF1F1F1F), thickness = 1.dp)
}

@HiltViewModel
class FriendFriendsListViewModel @Inject constructor(
    private val friendRepository: FriendRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _friends = MutableStateFlow<List<AppUser>>(emptyList())
    val friends: StateFlow<List<AppUser>> = _friends.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    fun load(uid: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val user = userRepository.getUser(uid)
                _userName.value = user?.displayName ?: ""
                _friends.value = friendRepository.getFriendUsers(uid)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
