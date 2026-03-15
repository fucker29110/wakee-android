package com.wakee.app.feature.alarm

import android.Manifest
import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.wakee.app.data.model.AppUser
import com.wakee.app.ui.theme.*
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmSetScreen(
    onAlarmSent: () -> Unit,
    viewModel: AlarmViewModel = hiltViewModel()
) {
    val friends by viewModel.friends.collectAsState()
    val selectedFriend by viewModel.selectedFriend.collectAsState()
    val hour by viewModel.hour.collectAsState()
    val minute by viewModel.minute.collectAsState()
    val message by viewModel.message.collectAsState()
    val snoozeMin by viewModel.snoozeMin.collectAsState()
    val isSending by viewModel.isSending.collectAsState()
    val sendSuccess by viewModel.sendSuccess.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val audioUri by viewModel.audioUri.collectAsState()

    var showTimePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(sendSuccess) {
        if (sendSuccess) onAlarmSent()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Top Bar
        TopAppBar(
            title = { Text("アラーム送信", color = PrimaryText, fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Friend selector
            Text("送信先を選択", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
            Spacer(modifier = Modifier.height(12.dp))

            if (friends.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "フレンドがいません。\nフレンドタブで友達を追加しましょう！",
                        color = SecondaryText,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp).fillMaxWidth()
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    friends.forEach { friend ->
                        val isSelected = selectedFriend?.uid == friend.uid
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { viewModel.selectFriend(friend) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .then(
                                        if (isSelected) Modifier.border(
                                            3.dp,
                                            Brush.linearGradient(listOf(Accent, AccentEnd)),
                                            CircleShape
                                        ) else Modifier
                                    )
                                    .padding(if (isSelected) 3.dp else 0.dp)
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
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = friend.displayName,
                                fontSize = 11.sp,
                                color = if (isSelected) Accent else SecondaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 60.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Time picker
            Text("アラーム時刻", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showTimePicker = true }
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = String.format("%02d:%02d", hour, minute),
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold,
                        color = Accent
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Message input
            Text("メッセージ（任意）", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = message,
                onValueChange = { if (it.length <= 200) viewModel.setMessage(it) },
                placeholder = { Text("おはようメッセージ", color = SecondaryText) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
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
                supportingText = {
                    Text("${message.length}/200", color = SecondaryText, fontSize = 12.sp)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Snooze duration
            Text("スヌーズ時間", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(5, 10, 15, 30).forEach { min ->
                    FilterChip(
                        selected = snoozeMin == min,
                        onClick = { viewModel.setSnoozeMin(min) },
                        label = { Text("${min}分") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Accent,
                            selectedLabelColor = PrimaryText,
                            containerColor = SurfaceVariant,
                            labelColor = SecondaryText
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Voice recording
            Text("ボイスメッセージ（任意）", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (audioUri != null) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Success)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("録音済み", color = Success, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        TextButton(onClick = { viewModel.setAudioUri(null) }) {
                            Text("削除", color = Danger, fontSize = 14.sp)
                        }
                    } else {
                        IconButton(
                            onClick = { /* Recording handled by Activity with permission */ }
                        ) {
                            Icon(
                                Icons.Filled.Mic,
                                contentDescription = "録音",
                                tint = if (isRecording) Danger else Accent,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isRecording) "録音中..." else "タップして録音",
                            color = if (isRecording) Danger else SecondaryText,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Send button
        Button(
            onClick = { viewModel.sendAlarm() },
            enabled = selectedFriend != null && !isSending,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Accent,
                disabledContainerColor = Accent.copy(alpha = 0.3f)
            )
        ) {
            if (isSending) {
                CircularProgressIndicator(color = PrimaryText, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Filled.Alarm, contentDescription = null, tint = PrimaryText)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "アラームを送信",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )
            }
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = hour,
            initialMinute = minute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setTime(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) {
                    Text("OK", color = Accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("キャンセル", color = SecondaryText)
                }
            },
            text = {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = SurfaceVariant,
                        selectorColor = Accent,
                        periodSelectorSelectedContainerColor = Accent
                    )
                )
            },
            containerColor = Surface
        )
    }
}
