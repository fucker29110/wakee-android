package com.wakee.app.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Settings
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
import com.wakee.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onEditProfile: () -> Unit,
    onSettings: () -> Unit,
    onUserClick: (String) -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsState()
    val friendCount by viewModel.friendCount.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        TopAppBar(
            title = { Text("プロフィール", color = PrimaryText, fontWeight = FontWeight.Bold, fontSize = 24.sp) },
            actions = {
                IconButton(onClick = onSettings) {
                    Icon(Icons.Outlined.Settings, contentDescription = "設定", tint = PrimaryText)
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

                Spacer(modifier = Modifier.height(20.dp))

                // Stats
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$friendCount", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
                        Text("フレンド", fontSize = 12.sp, color = SecondaryText)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${u.streak}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Accent)
                        Text("連続起床", fontSize = 12.sp, color = SecondaryText)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Edit button
                Button(
                    onClick = onEditProfile,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariant)
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null, tint = PrimaryText, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("プロフィールを編集", fontSize = 15.sp, color = PrimaryText)
                }
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Accent)
        }
    }
}
