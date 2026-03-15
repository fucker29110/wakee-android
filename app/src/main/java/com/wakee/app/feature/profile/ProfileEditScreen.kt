package com.wakee.app.feature.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.wakee.app.data.repository.AuthRepository
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
fun ProfileEditScreen(
    onBack: () -> Unit,
    viewModel: ProfileEditViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()

    var displayName by remember(user) { mutableStateOf(user?.displayName ?: "") }
    var username by remember(user) { mutableStateOf(user?.username ?: "") }
    var bio by remember(user) { mutableStateOf(user?.bio ?: "") }
    var location by remember(user) { mutableStateOf(user?.location ?: "") }
    var avatarUri by remember { mutableStateOf<Uri?>(null) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { avatarUri = it }
    }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) onBack()
    }

    LaunchedEffect(Unit) {
        viewModel.loadUser()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        TopAppBar(
            title = { Text("プロフィール編集", color = PrimaryText, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "戻る", tint = PrimaryText)
                }
            },
            actions = {
                TextButton(
                    onClick = {
                        viewModel.save(displayName, username, bio, location, avatarUri)
                    },
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Accent, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("保存", color = Accent, fontWeight = FontWeight.Bold)
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
        )

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
                    .clickable { imagePicker.launch("image/*") }
            ) {
                AsyncImage(
                    model = avatarUri ?: user?.photoURL,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Background.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = "写真を変更", tint = PrimaryText)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Fields
            ProfileField("表示名", displayName) { displayName = it }
            Spacer(modifier = Modifier.height(12.dp))
            ProfileField("ユーザー名", username) { username = it }
            Spacer(modifier = Modifier.height(12.dp))
            ProfileField("自己紹介", bio, maxLines = 4) { bio = it }
            Spacer(modifier = Modifier.height(12.dp))
            ProfileField("場所", location) { location = it }
        }
    }
}

@Composable
fun ProfileField(label: String, value: String, maxLines: Int = 1, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 13.sp, color = SecondaryText, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            maxLines = maxLines,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = PrimaryText,
                unfocusedTextColor = PrimaryText,
                focusedBorderColor = Accent,
                unfocusedBorderColor = Border,
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface,
                cursorColor = Accent
            ),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@HiltViewModel
class ProfileEditViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _user = MutableStateFlow<com.wakee.app.data.model.AppUser?>(null)
    val user: StateFlow<com.wakee.app.data.model.AppUser?> = _user.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    fun loadUser() {
        viewModelScope.launch {
            _user.value = authRepository.getCurrentUser()
        }
    }

    fun save(displayName: String, username: String, bio: String, location: String, avatarUri: Uri?) {
        viewModelScope.launch {
            _isSaving.value = true
            try {
                if (avatarUri != null) {
                    authRepository.uploadAvatar(avatarUri)
                }
                authRepository.updateProfile(mapOf(
                    "displayName" to displayName,
                    "username" to username,
                    "bio" to bio,
                    "location" to location
                ))
                _saveSuccess.value = true
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSaving.value = false
            }
        }
    }
}
