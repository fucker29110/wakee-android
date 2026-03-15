package com.wakee.app.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakee.app.data.model.Activity
import com.wakee.app.data.repository.ActivityRepository
import com.wakee.app.data.repository.AuthRepository
import com.wakee.app.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepostSheet(
    activity: Activity,
    onDismiss: () -> Unit,
    onDone: () -> Unit,
    viewModel: RepostViewModel = hiltViewModel()
) {
    val comment by viewModel.comment.collectAsState()
    val isSending by viewModel.isSending.collectAsState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Surface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(SecondaryText, RoundedCornerShape(2.dp))
            )
        }
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Title
                Text(
                    "リポスト",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Original post preview
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceVariant, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        activity.displayName,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryText,
                        fontSize = 14.sp
                    )
                    if (!activity.message.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            activity.message,
                            color = SecondaryText,
                            fontSize = 14.sp,
                            maxLines = 3
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Comment text field
                OutlinedTextField(
                    value = comment,
                    onValueChange = { viewModel.updateComment(it) },
                    placeholder = {
                        Text("コメントを追加（任意）", color = SecondaryText)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PrimaryText,
                        unfocusedTextColor = PrimaryText,
                        cursorColor = Accent,
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = Border
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Repost button
                Button(
                    onClick = {
                        viewModel.repost(activity) {
                            onDone()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !isSending,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Accent,
                        disabledContainerColor = Accent.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        "リポスト",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Cancel button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        "キャンセル",
                        fontSize = 16.sp,
                        color = SecondaryText
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Loading overlay
            if (isSending) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Accent)
                }
            }
        }
    }
}

@HiltViewModel
class RepostViewModel @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _comment = MutableStateFlow("")
    val comment: StateFlow<String> = _comment.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    fun updateComment(value: String) {
        _comment.value = value
    }

    fun repost(activity: Activity, onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                _isSending.value = true
                val user = authRepository.getCurrentUser() ?: return@launch
                activityRepository.repost(
                    activityId = activity.activityId,
                    uid = user.uid,
                    displayName = user.displayName,
                    username = user.username,
                    photoURL = user.photoURL,
                    comment = _comment.value.ifEmpty { null }
                )
                onDone()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSending.value = false
            }
        }
    }
}
