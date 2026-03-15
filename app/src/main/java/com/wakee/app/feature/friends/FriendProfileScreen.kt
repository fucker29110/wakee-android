package com.wakee.app.feature.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.wakee.app.data.model.AppUser
import com.wakee.app.data.repository.AuthRepository
import com.wakee.app.data.repository.FriendRepository
import com.wakee.app.data.repository.UserRepository
import com.wakee.app.data.repository.ChatRepository
import com.wakee.app.ui.theme.*
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
fun FriendProfileScreen(
    userId: String,
    onBack: () -> Unit,
    onChatClick: (String, String) -> Unit,
    viewModel: FriendProfileViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsState()
    val isFriend by viewModel.isFriend.collectAsState()
    val friendCount by viewModel.friendCount.collectAsState()

    LaunchedEffect(userId) {
        viewModel.loadUser(userId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        TopAppBar(
            title = { Text("プロフィール", color = PrimaryText, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "戻る", tint = PrimaryText)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
        )

        user?.let { u ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(SurfaceVariant)
                ) {
                    AsyncImage(
                        model = u.photoURL,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(u.displayName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
                Text("@${u.username}", fontSize = 14.sp, color = SecondaryText)

                if (u.bio.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(u.bio, fontSize = 14.sp, color = PrimaryText, textAlign = TextAlign.Center)
                }

                if (u.location.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = SecondaryText, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(u.location, fontSize = 13.sp, color = SecondaryText)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("フレンド ${friendCount}人", fontSize = 14.sp, color = SecondaryText)

                Spacer(modifier = Modifier.height(20.dp))

                // Action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Friend button
                    Button(
                        onClick = {
                            if (isFriend) viewModel.removeFriend()
                            else viewModel.sendRequest()
                        },
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFriend) SurfaceVariant else Accent
                        )
                    ) {
                        Text(
                            if (isFriend) "フレンド解除" else "フレンド追加",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                    }

                    // Chat button
                    if (isFriend) {
                        OutlinedButton(
                            onClick = {
                                viewModel.openChat { chatId ->
                                    onChatClick(chatId, userId)
                                }
                            },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.Chat, contentDescription = null, tint = Accent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("チャット", fontSize = 14.sp, color = Accent)
                        }
                    }
                }
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Accent)
        }
    }
}

@HiltViewModel
class FriendProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val friendRepository: FriendRepository,
    private val authRepository: AuthRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _user = MutableStateFlow<AppUser?>(null)
    val user: StateFlow<AppUser?> = _user.asStateFlow()

    private val _isFriend = MutableStateFlow(false)
    val isFriend: StateFlow<Boolean> = _isFriend.asStateFlow()

    private val _friendCount = MutableStateFlow(0)
    val friendCount: StateFlow<Int> = _friendCount.asStateFlow()

    private var targetUid = ""

    fun loadUser(uid: String) {
        targetUid = uid
        viewModelScope.launch {
            try {
                _user.value = userRepository.getUser(uid)
                val myUid = authRepository.currentUid ?: return@launch
                _isFriend.value = friendRepository.isFriend(myUid, uid)
                _friendCount.value = friendRepository.getFriends(uid).size
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendRequest() {
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser() ?: return@launch
                friendRepository.sendFollowRequest(user, targetUid)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun removeFriend() {
        viewModelScope.launch {
            try {
                val myUid = authRepository.currentUid ?: return@launch
                friendRepository.removeFriend(myUid, targetUid)
                _isFriend.value = false
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun openChat(onChatReady: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val myUid = authRepository.currentUid ?: return@launch
                val chatId = chatRepository.getOrCreateChat(myUid, targetUid)
                onChatReady(chatId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
