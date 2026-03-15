package com.wakee.app.feature.auth

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.wakee.app.data.model.AppUser
import com.wakee.app.ui.theme.*

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val currentStep by viewModel.currentStep.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadCurrentUser()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
            },
            label = "onboarding_step"
        ) { step ->
            when (step) {
                0 -> OnboardingNameStep(viewModel = viewModel)
                1 -> OnboardingUsernameStep(viewModel = viewModel)
                2 -> OnboardingFriendsStep(viewModel = viewModel)
                3 -> OnboardingPermissionsStep(
                    viewModel = viewModel,
                    onComplete = {
                        viewModel.completeOnboarding { onComplete() }
                    }
                )
            }
        }
    }
}

// ==================== Step Indicator ====================

@Composable
private fun StepIndicator(current: Int, total: Int = 4) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.width(160.dp)
    ) {
        repeat(total) { i ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (i <= current) Accent else SurfaceVariant)
            )
        }
    }
}

// ==================== Step 1: Name & Avatar ====================

@Composable
private fun OnboardingNameStep(viewModel: OnboardingViewModel) {
    val displayName by viewModel.displayName.collectAsState()
    val avatarUri by viewModel.avatarUri.collectAsState()
    val isUploading by viewModel.isUploadingAvatar.collectAsState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.setAvatarUri(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // Header
        Text(
            text = "Create a new account",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryText
        )
        Spacer(modifier = Modifier.height(8.dp))
        StepIndicator(current = 0)

        Spacer(modifier = Modifier.height(40.dp))

        // Avatar Picker
        Box(
            modifier = Modifier
                .size(100.dp)
                .clickable { imagePickerLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (avatarUri != null) {
                AsyncImage(
                    model = avatarUri,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(SurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Default avatar",
                        tint = SecondaryText,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            // Camera overlay
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = "Pick photo",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Name field
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Name",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = SecondaryText
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = displayName,
                onValueChange = { viewModel.setDisplayName(it) },
                placeholder = {
                    Text("名前を入力", color = SecondaryText.copy(alpha = 0.5f))
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = PrimaryText,
                    unfocusedTextColor = PrimaryText,
                    cursorColor = Accent,
                    focusedBorderColor = Accent,
                    unfocusedBorderColor = Border,
                    focusedContainerColor = SurfaceVariant,
                    unfocusedContainerColor = SurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done
                )
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Continue button
        Button(
            onClick = {
                viewModel.saveNameAndAvatar { viewModel.nextStep() }
            },
            enabled = viewModel.canContinueName && !isUploading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = if (viewModel.canContinueName && !isUploading) {
                            Brush.linearGradient(listOf(Accent, AccentEnd))
                        } else {
                            Brush.linearGradient(listOf(SurfaceVariant, SurfaceVariant))
                        },
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isUploading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "次へ",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

// ==================== Step 2: Username ====================

@Composable
private fun OnboardingUsernameStep(viewModel: OnboardingViewModel) {
    val username by viewModel.username.collectAsState()
    val isAvailable by viewModel.isUsernameAvailable.collectAsState()
    val isChecking by viewModel.isCheckingUsername.collectAsState()
    val isSaving by viewModel.isSavingUsername.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // Header
        Text(
            text = "Create a new account",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryText
        )
        Spacer(modifier = Modifier.height(8.dp))
        StepIndicator(current = 1)

        Spacer(modifier = Modifier.height(40.dp))

        // Username field
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Username",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = SecondaryText
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "英数字・アンダースコア・ピリオドのみ",
                fontSize = 12.sp,
                color = SecondaryText
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { viewModel.setUsername(it) },
                    placeholder = {
                        Text("username", color = SecondaryText.copy(alpha = 0.5f))
                    },
                    prefix = {
                        Text("@", color = SecondaryText)
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PrimaryText,
                        unfocusedTextColor = PrimaryText,
                        cursorColor = Accent,
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = Border,
                        focusedContainerColor = SurfaceVariant,
                        unfocusedContainerColor = SurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        imeAction = ImeAction.Done,
                        capitalization = KeyboardCapitalization.None
                    )
                )

                // Random username generator button (dice)
                IconButton(
                    onClick = { viewModel.generateRandomUsername() },
                    modifier = Modifier
                        .size(48.dp)
                        .background(SurfaceVariant, RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Casino,
                        contentDescription = "Generate random username",
                        tint = Accent,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Availability indicator
            if (isChecking) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = SecondaryText
                    )
                    Text(
                        text = "確認中...",
                        fontSize = 12.sp,
                        color = SecondaryText
                    )
                }
            } else if (isAvailable != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isAvailable == true) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                        contentDescription = null,
                        tint = if (isAvailable == true) Success else Danger,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isAvailable == true) "利用可能です" else "既に使われています",
                        fontSize = 12.sp,
                        color = if (isAvailable == true) Success else Danger
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Continue button
        Button(
            onClick = {
                viewModel.saveUsername { viewModel.nextStep() }
            },
            enabled = viewModel.canContinueUsername && !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = if (viewModel.canContinueUsername && !isSaving) {
                            Brush.linearGradient(listOf(Accent, AccentEnd))
                        } else {
                            Brush.linearGradient(listOf(SurfaceVariant, SurfaceVariant))
                        },
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "次へ",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

// ==================== Step 3: Find Friends ====================

@Composable
private fun OnboardingFriendsStep(viewModel: OnboardingViewModel) {
    val searchQuery by viewModel.friendSearchQuery.collectAsState()
    val searchResults by viewModel.friendSearchResults.collectAsState()
    val suggestions by viewModel.friendSuggestions.collectAsState()
    val sentRequests by viewModel.sentRequests.collectAsState()
    val isSearching by viewModel.isSearchingFriends.collectAsState()
    val isLoadingSuggestions by viewModel.isLoadingSuggestions.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadSuggestions()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 32.dp)
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // Header
        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "友達を見つけよう",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            StepIndicator(current = 2)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Search bar
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setFriendSearchQuery(it) },
                placeholder = {
                    Text("ユーザー名で検索", color = SecondaryText.copy(alpha = 0.5f))
                },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = PrimaryText,
                    unfocusedTextColor = PrimaryText,
                    cursorColor = Accent,
                    focusedBorderColor = Accent,
                    unfocusedBorderColor = Border,
                    focusedContainerColor = SurfaceVariant,
                    unfocusedContainerColor = SurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search,
                    capitalization = KeyboardCapitalization.None
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { viewModel.searchFriends() }
                )
            )

            IconButton(
                onClick = { viewModel.searchFriends() },
                modifier = Modifier
                    .size(48.dp)
                    .background(Accent, RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Results list
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            // Search results
            if (searchResults.isNotEmpty()) {
                item {
                    SectionHeader("検索結果")
                }
                items(searchResults, key = { "search_${it.uid}" }) { user ->
                    FriendUserRow(
                        user = user,
                        isSent = sentRequests.contains(user.uid),
                        onAdd = { viewModel.sendFollowRequest(user.uid) }
                    )
                }
            }

            // Suggestions
            if (suggestions.isNotEmpty()) {
                item {
                    SectionHeader("おすすめ")
                }
                items(suggestions, key = { "suggest_${it.uid}" }) { user ->
                    FriendUserRow(
                        user = user,
                        isSent = sentRequests.contains(user.uid),
                        onAdd = { viewModel.sendFollowRequest(user.uid) }
                    )
                }
            }

            if (isSearching || isLoadingSuggestions) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Accent)
                    }
                }
            }
        }

        // Bottom buttons
        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { viewModel.nextStep() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(listOf(Accent, AccentEnd)),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "次へ",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }

            TextButton(onClick = { viewModel.nextStep() }) {
                Text(
                    text = "スキップ",
                    fontSize = 14.sp,
                    color = SecondaryText
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = SecondaryText,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 16.dp, bottom = 4.dp)
    )
}

@Composable
private fun FriendUserRow(
    user: AppUser,
    isSent: Boolean,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(SurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (user.photoURL != null) {
                AsyncImage(
                    model = user.photoURL,
                    contentDescription = user.displayName,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = user.displayName.take(1).uppercase(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )
            }
        }

        // Name and username
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.displayName,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryText,
                fontSize = 15.sp
            )
            Text(
                text = "@${user.username}",
                fontSize = 12.sp,
                color = SecondaryText
            )
        }

        // Add / Sent button
        if (isSent) {
            Text(
                text = "申請済み",
                fontSize = 12.sp,
                color = SecondaryText
            )
        } else {
            Button(
                onClick = onAdd,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(
                    text = "追加",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

// ==================== Step 4: Permissions ====================

@Composable
private fun OnboardingPermissionsStep(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val notificationGranted by viewModel.notificationGranted.collectAsState()
    val microphoneGranted by viewModel.microphoneGranted.collectAsState()
    val isCompleting by viewModel.isCompleting.collectAsState()

    // Check current permission states on appear
    LaunchedEffect(Unit) {
        viewModel.updatePermissionStates(context)
    }

    // Notification permission launcher
    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.setNotificationGranted(granted)
    }

    // Microphone permission launcher
    val microphoneLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.setMicrophoneGranted(granted)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // Header
        Text(
            text = "アクセスを許可する",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryText
        )
        Spacer(modifier = Modifier.height(8.dp))
        StepIndicator(current = 3)

        Spacer(modifier = Modifier.height(30.dp))

        // Permissions list
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface, RoundedCornerShape(12.dp))
        ) {
            // Notification permission
            PermissionRow(
                icon = Icons.Filled.Notifications,
                title = "通知",
                description = "アラームやメッセージを受け取るために必要です",
                isGranted = notificationGranted,
                onRequest = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        viewModel.setNotificationGranted(true)
                    }
                }
            )

            HorizontalDivider(color = Border, thickness = 0.5.dp)

            // Microphone permission
            PermissionRow(
                icon = Icons.Filled.Mic,
                title = "マイク",
                description = "ボイスメッセージの録音に使います",
                isGranted = microphoneGranted,
                onRequest = {
                    microphoneLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Continue button
        Button(
            onClick = onComplete,
            enabled = !isCompleting,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(listOf(Accent, AccentEnd)),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "次へ",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onComplete) {
            Text(
                text = "スキップ",
                fontSize = 14.sp,
                color = SecondaryText
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "設定はいつでも変更できます",
            fontSize = 12.sp,
            color = SecondaryText
        )
    }
}

@Composable
private fun PermissionRow(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (!isGranted) onRequest() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = Accent,
            modifier = Modifier.size(28.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryText
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = SecondaryText
            )
        }

        if (isGranted) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Granted",
                tint = Success,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Request",
                tint = SecondaryText,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
