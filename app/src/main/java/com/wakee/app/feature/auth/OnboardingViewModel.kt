package com.wakee.app.feature.auth

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakee.app.data.model.AppUser
import com.wakee.app.data.repository.AuthRepository
import com.wakee.app.data.repository.FriendRepository
import com.wakee.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val friendRepository: FriendRepository
) : ViewModel() {

    // ==================== Step Management ====================

    private val _currentStep = MutableStateFlow(0)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    val totalSteps = 4

    fun nextStep() {
        if (_currentStep.value < totalSteps - 1) {
            _currentStep.value += 1
        }
    }

    // ==================== Step 1: Name & Avatar ====================

    private val _displayName = MutableStateFlow("")
    val displayName: StateFlow<String> = _displayName.asStateFlow()

    private val _avatarUri = MutableStateFlow<Uri?>(null)
    val avatarUri: StateFlow<Uri?> = _avatarUri.asStateFlow()

    private val _isUploadingAvatar = MutableStateFlow(false)
    val isUploadingAvatar: StateFlow<Boolean> = _isUploadingAvatar.asStateFlow()

    fun setDisplayName(name: String) {
        _displayName.value = name
    }

    fun setAvatarUri(uri: Uri?) {
        _avatarUri.value = uri
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    if (_displayName.value.isBlank()) {
                        _displayName.value = user.displayName
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val canContinueName: Boolean
        get() = _displayName.value.trim().isNotEmpty()

    fun saveNameAndAvatar(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isUploadingAvatar.value = true
            try {
                val updates = mutableMapOf<String, Any>("displayName" to _displayName.value.trim())

                val uri = _avatarUri.value
                if (uri != null) {
                    val url = authRepository.uploadAvatar(uri)
                    updates["photoURL"] = url
                }

                authRepository.updateProfile(updates)
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isUploadingAvatar.value = false
            }
        }
    }

    // ==================== Step 2: Username ====================

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _isUsernameAvailable = MutableStateFlow<Boolean?>(null)
    val isUsernameAvailable: StateFlow<Boolean?> = _isUsernameAvailable.asStateFlow()

    private val _isCheckingUsername = MutableStateFlow(false)
    val isCheckingUsername: StateFlow<Boolean> = _isCheckingUsername.asStateFlow()

    private val _isSavingUsername = MutableStateFlow(false)
    val isSavingUsername: StateFlow<Boolean> = _isSavingUsername.asStateFlow()

    private var usernameCheckJob: Job? = null

    fun setUsername(value: String) {
        _username.value = value
        _isUsernameAvailable.value = null
        debounceCheckUsername()
    }

    private val usernameRegex = Regex("^[a-zA-Z0-9._]{3,20}$")

    val isValidUsername: Boolean
        get() = usernameRegex.matches(_username.value)

    val canContinueUsername: Boolean
        get() = isValidUsername && _isUsernameAvailable.value == true

    private fun debounceCheckUsername() {
        usernameCheckJob?.cancel()
        if (!isValidUsername) return
        usernameCheckJob = viewModelScope.launch {
            delay(500)
            checkUsernameAvailability()
        }
    }

    fun checkUsernameNow() {
        if (!isValidUsername) return
        usernameCheckJob?.cancel()
        viewModelScope.launch { checkUsernameAvailability() }
    }

    private suspend fun checkUsernameAvailability() {
        val current = _username.value
        _isCheckingUsername.value = true
        try {
            val results = userRepository.searchUsers(current.lowercase())
            val myUid = authRepository.currentUid
            val taken = results.any { it.username.equals(current, ignoreCase = true) && it.uid != myUid }
            if (_username.value == current) {
                _isUsernameAvailable.value = !taken
            }
        } catch (e: Exception) {
            if (_username.value == current) {
                _isUsernameAvailable.value = false
            }
        } finally {
            _isCheckingUsername.value = false
        }
    }

    fun generateRandomUsername() {
        val name = _displayName.value.trim()
        val base = name.lowercase()
            .replace(" ", "")
            .filter { it.isLetterOrDigit() && it.code < 128 }
        val prefix = if (base.isEmpty()) "user" else base.take(12)
        val charset = "abcdefghijklmnopqrstuvwxyz0123456789"
        val suffix = (1..4).map { charset.random() }.joinToString("")
        _username.value = "${prefix}_$suffix"
        _isUsernameAvailable.value = null
        debounceCheckUsername()
    }

    fun saveUsername(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isSavingUsername.value = true
            try {
                authRepository.updateProfile(mapOf("username" to _username.value))
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSavingUsername.value = false
            }
        }
    }

    // ==================== Step 3: Find Friends ====================

    private val _friendSearchQuery = MutableStateFlow("")
    val friendSearchQuery: StateFlow<String> = _friendSearchQuery.asStateFlow()

    private val _friendSearchResults = MutableStateFlow<List<AppUser>>(emptyList())
    val friendSearchResults: StateFlow<List<AppUser>> = _friendSearchResults.asStateFlow()

    private val _friendSuggestions = MutableStateFlow<List<AppUser>>(emptyList())
    val friendSuggestions: StateFlow<List<AppUser>> = _friendSuggestions.asStateFlow()

    private val _sentRequests = MutableStateFlow<Set<String>>(emptySet())
    val sentRequests: StateFlow<Set<String>> = _sentRequests.asStateFlow()

    private val _isSearchingFriends = MutableStateFlow(false)
    val isSearchingFriends: StateFlow<Boolean> = _isSearchingFriends.asStateFlow()

    private val _isLoadingSuggestions = MutableStateFlow(false)
    val isLoadingSuggestions: StateFlow<Boolean> = _isLoadingSuggestions.asStateFlow()

    fun setFriendSearchQuery(query: String) {
        _friendSearchQuery.value = query
    }

    fun searchFriends() {
        val query = _friendSearchQuery.value.trim()
        if (query.isBlank()) {
            _friendSearchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isSearchingFriends.value = true
            try {
                val myUid = authRepository.currentUid ?: return@launch
                val results = userRepository.searchUsers(query)
                _friendSearchResults.value = results.filter { it.uid != myUid }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSearchingFriends.value = false
            }
        }
    }

    fun loadSuggestions() {
        viewModelScope.launch {
            _isLoadingSuggestions.value = true
            try {
                val myUid = authRepository.currentUid ?: return@launch
                // Load sent requests so we can show "requested" state
                val sent = friendRepository.getSentRequests(myUid)
                _sentRequests.value = sent.map { it.toUid }.toSet()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoadingSuggestions.value = false
            }
        }
    }

    fun sendFollowRequest(toUid: String) {
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser() ?: return@launch
                friendRepository.sendFollowRequest(user, toUid)
                _sentRequests.value = _sentRequests.value + toUid
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ==================== Step 4: Permissions ====================

    private val _notificationGranted = MutableStateFlow(false)
    val notificationGranted: StateFlow<Boolean> = _notificationGranted.asStateFlow()

    private val _microphoneGranted = MutableStateFlow(false)
    val microphoneGranted: StateFlow<Boolean> = _microphoneGranted.asStateFlow()

    fun updatePermissionStates(context: Context) {
        _notificationGranted.value = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        _microphoneGranted.value = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun setNotificationGranted(granted: Boolean) {
        _notificationGranted.value = granted
    }

    fun setMicrophoneGranted(granted: Boolean) {
        _microphoneGranted.value = granted
    }

    // ==================== Complete Onboarding ====================

    private val _isCompleting = MutableStateFlow(false)
    val isCompleting: StateFlow<Boolean> = _isCompleting.asStateFlow()

    fun completeOnboarding(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isCompleting.value = true
            try {
                authRepository.updateProfile(mapOf("onboardingCompleted" to true))
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isCompleting.value = false
            }
        }
    }
}
