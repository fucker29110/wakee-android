package com.wakee.app.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import com.wakee.app.data.model.Story
import com.wakee.app.data.repository.AuthRepository
import com.wakee.app.data.repository.StoryRepository
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

@Composable
fun StoryViewModal(
    storyId: String,
    onDismiss: () -> Unit,
    viewModel: StoryViewViewModel = hiltViewModel()
) {
    val story by viewModel.story.collectAsState()

    LaunchedEffect(storyId) {
        viewModel.loadStory(storyId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        story?.let { s ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SurfaceVariant)
                        ) {
                            AsyncImage(
                                model = s.photoURL,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(s.displayName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = PrimaryText)
                            Text(TimeUtils.timeAgo(s.createdAt), fontSize = 11.sp, color = SecondaryText)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "閉じる", tint = PrimaryText)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Story content
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (!s.imageURL.isNullOrEmpty()) {
                        AsyncImage(
                            model = s.imageURL,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                    if (s.text.isNotEmpty()) {
                        Text(
                            text = s.text,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                    }
                }

                // View count
                Text(
                    text = "${s.viewedBy.size}人が閲覧",
                    fontSize = 13.sp,
                    color = SecondaryText,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        } ?: Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Accent)
        }
    }
}

@HiltViewModel
class StoryViewViewModel @Inject constructor(
    private val storyRepository: StoryRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _story = MutableStateFlow<Story?>(null)
    val story: StateFlow<Story?> = _story.asStateFlow()

    fun loadStory(storyId: String) {
        viewModelScope.launch {
            try {
                _story.value = storyRepository.getStory(storyId)
                val uid = authRepository.currentUid ?: return@launch
                storyRepository.markViewed(storyId, uid)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
