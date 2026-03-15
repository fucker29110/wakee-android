package com.wakee.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakee.app.data.repository.AuthRepository
import com.wakee.app.data.repository.StoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoryCreateViewModel @Inject constructor(
    private val storyRepository: StoryRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isPosting = MutableStateFlow(false)
    val isPosting: StateFlow<Boolean> = _isPosting.asStateFlow()

    fun createStory(text: String) {
        viewModelScope.launch {
            _isPosting.value = true
            try {
                val user = authRepository.getCurrentUser() ?: return@launch
                storyRepository.createStory(
                    uid = user.uid,
                    displayName = user.displayName,
                    username = user.username,
                    photoURL = user.photoURL,
                    text = text
                )
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isPosting.value = false
            }
        }
    }
}
