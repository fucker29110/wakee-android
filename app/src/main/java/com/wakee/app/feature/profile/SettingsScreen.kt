package com.wakee.app.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wakee.app.feature.auth.AuthViewModel
import com.wakee.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Notification settings state
    var alarmReceived by remember { mutableStateOf(true) }
    var messages by remember { mutableStateOf(true) }
    var likes by remember { mutableStateOf(true) }
    var friendRequests by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        TopAppBar(
            title = { Text("設定", color = PrimaryText, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "戻る", tint = PrimaryText)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Notification settings
            Text("通知設定", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column {
                    SettingsToggle("アラーム受信", alarmReceived) { alarmReceived = it }
                    HorizontalDivider(color = Border, thickness = 0.5.dp)
                    SettingsToggle("メッセージ", messages) { messages = it }
                    HorizontalDivider(color = Border, thickness = 0.5.dp)
                    SettingsToggle("いいね", likes) { likes = it }
                    HorizontalDivider(color = Border, thickness = 0.5.dp)
                    SettingsToggle("フレンドリクエスト", friendRequests) { friendRequests = it }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Logout button
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Danger)
            ) {
                Icon(Icons.Filled.Logout, contentDescription = null, tint = PrimaryText)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ログアウト", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // App version
            Text(
                text = "Wakee v1.0.0",
                fontSize = 13.sp,
                color = SecondaryText,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }

    // Logout dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("ログアウト", color = PrimaryText) },
            text = { Text("本当にログアウトしますか？", color = SecondaryText) },
            confirmButton = {
                TextButton(onClick = {
                    authViewModel.signOut()
                    showLogoutDialog = false
                    onLogout()
                }) {
                    Text("ログアウト", color = Danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("キャンセル", color = SecondaryText)
                }
            },
            containerColor = Surface
        )
    }
}

@Composable
fun SettingsToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 15.sp, color = PrimaryText)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PrimaryText,
                checkedTrackColor = Accent,
                uncheckedThumbColor = SecondaryText,
                uncheckedTrackColor = SurfaceVariant
            )
        )
    }
}
