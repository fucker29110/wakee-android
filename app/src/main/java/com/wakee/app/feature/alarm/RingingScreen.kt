package com.wakee.app.feature.alarm

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.wakee.app.data.model.InboxEvent
import com.wakee.app.ui.theme.*

@Composable
fun RingingScreen(
    eventId: String,
    onDismissed: () -> Unit,
    viewModel: RingingViewModel = hiltViewModel()
) {
    val event by viewModel.event.collectAsState()
    val showReactionPicker by viewModel.showReactionPicker.collectAsState()
    val isDismissed by viewModel.isDismissed.collectAsState()

    LaunchedEffect(eventId) {
        viewModel.loadEvent(eventId)
    }

    LaunchedEffect(isDismissed) {
        if (isDismissed) onDismissed()
    }

    // Pulsating animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        if (showReactionPicker) {
            // Reaction picker
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "リアクションを選択",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )
                Spacer(modifier = Modifier.height(24.dp))

                val emojis = listOf("😊", "😎", "🥱", "😤", "🔥", "💪", "😴", "☀️")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    emojis.take(4).forEach { emoji ->
                        TextButton(
                            onClick = { viewModel.selectReaction(emoji) },
                            modifier = Modifier.size(64.dp)
                        ) {
                            Text(emoji, fontSize = 32.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    emojis.drop(4).forEach { emoji ->
                        TextButton(
                            onClick = { viewModel.selectReaction(emoji) },
                            modifier = Modifier.size(64.dp)
                        ) {
                            Text(emoji, fontSize = 32.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                TextButton(onClick = { viewModel.skipReaction() }) {
                    Text("スキップ", color = SecondaryText, fontSize = 16.sp)
                }
            }
        } else {
            // Ringing UI
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.height(48.dp))

                // Alarm icon (pulsating)
                Icon(
                    Icons.Filled.Alarm,
                    contentDescription = null,
                    tint = Accent,
                    modifier = Modifier
                        .size(80.dp)
                        .scale(scale)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Sender info
                event?.let { ev ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = ev.senderName,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                        Text(
                            text = "からのアラーム",
                            fontSize = 16.sp,
                            color = SecondaryText
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Time
                        Text(
                            text = ev.time,
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Black,
                            color = Accent
                        )

                        if (ev.message.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = ev.message,
                                    fontSize = 16.sp,
                                    color = PrimaryText,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Action buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Wake up button
                    Button(
                        onClick = { viewModel.wokeUp() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Success)
                    ) {
                        Text("☀️ 起きた！", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
                    }

                    // Snooze button
                    Button(
                        onClick = { viewModel.snooze() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Warning)
                    ) {
                        Text(
                            "😴 スヌーズ（${event?.snoozeMin ?: 5}分）",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryText
                        )
                    }

                    // Dismiss button
                    OutlinedButton(
                        onClick = { viewModel.dismiss() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Danger)
                    ) {
                        Text("❌ 拒否", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Danger)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
