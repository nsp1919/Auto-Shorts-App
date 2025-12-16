package com.autoshorts.app.ui.screens.rocket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autoshorts.app.data.repository.AutoShortsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for Rocket Share Screen.
 */
data class RocketUiState(
    val clipId: String = "",
    val videoUrl: String = "",
    
    // AI-generated metadata
    val title: String = "",
    val description: String = "",
    val hashtags: List<String> = emptyList(),
    val suggestedTime: String? = null,
    
    // Editing states
    val isEditingTitle: Boolean = false,
    val isEditingDescription: Boolean = false,
    
    // Loading states
    val isLoading: Boolean = false,
    val isSharing: Boolean = false,
    
    // Error handling
    val error: String? = null,
    val successMessage: String? = null
)

/**
 * ViewModel for Rocket Share Screen.
 * Generates AI-powered metadata and handles sharing to platforms.
 */
class RocketShareViewModel(
    private val repository: AutoShortsRepository = AutoShortsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RocketUiState())
    val uiState: StateFlow<RocketUiState> = _uiState.asStateFlow()

    /**
     * Initialize with clip info and generate metadata.
     */
    fun initialize(clipId: String, videoUrl: String) {
        _uiState.update { 
            it.copy(
                clipId = clipId,
                videoUrl = videoUrl
            )
        }
        generateMetadata()
    }

    /**
     * Generate AI-powered metadata for the clip.
     */
    private fun generateMetadata() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = repository.generateRocketMetadata(_uiState.value.clipId)

            result.fold(
                onSuccess = { response ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            title = response.title,
                            description = response.description,
                            hashtags = response.hashtags,
                            suggestedTime = response.suggestedTime
                        )
                    }
                },
                onFailure = { error ->
                    // Use default metadata on error
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            title = "Check out this amazing short! 🔥",
                            description = "Created with Auto Shorts - Transform your videos into viral content!",
                            hashtags = listOf("shorts", "viral", "trending", "fyp", "reels"),
                            error = null // Don't show error, use defaults
                        )
                    }
                }
            )
        }
    }

    /**
     * Regenerate metadata with fresh AI suggestions.
     */
    fun regenerateMetadata() {
        generateMetadata()
    }

    /**
     * Update title.
     */
    fun onTitleChanged(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    /**
     * Update description.
     */
    fun onDescriptionChanged(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    /**
     * Toggle title editing mode.
     */
    fun toggleTitleEditing(editing: Boolean) {
        _uiState.update { it.copy(isEditingTitle = editing) }
    }

    /**
     * Toggle description editing mode.
     */
    fun toggleDescriptionEditing(editing: Boolean) {
        _uiState.update { it.copy(isEditingDescription = editing) }
    }

    /**
     * Add a custom hashtag.
     */
    fun addHashtag(hashtag: String) {
        val cleanTag = hashtag.trim().removePrefix("#")
        if (cleanTag.isNotEmpty() && cleanTag !in _uiState.value.hashtags) {
            _uiState.update {
                it.copy(hashtags = it.hashtags + cleanTag)
            }
        }
    }

    /**
     * Remove a hashtag.
     */
    fun removeHashtag(hashtag: String) {
        _uiState.update {
            it.copy(hashtags = it.hashtags.filter { it != hashtag })
        }
    }

    /**
     * Get formatted hashtags string for copying.
     */
    fun getFormattedHashtags(): String {
        return _uiState.value.hashtags.joinToString(" ") { "#$it" }
    }

    /**
     * Get full share text (title + description + hashtags).
     */
    fun getFullShareText(): String {
        val state = _uiState.value
        return buildString {
            append(state.title)
            append("\n\n")
            append(state.description)
            append("\n\n")
            append(getFormattedHashtags())
        }
    }

    /**
     * Share to a specific platform.
     */
    fun shareToplatform(platform: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSharing = true, error = null) }

            val state = _uiState.value
            val result = repository.shareVideo(
                platform = platform,
                clipId = state.clipId,
                videoUrl = state.videoUrl,
                title = state.title,
                description = state.description,
                hashtags = state.hashtags
            )

            result.fold(
                onSuccess = { response ->
                    _uiState.update {
                        it.copy(
                            isSharing = false,
                            successMessage = "Shared to ${platform.replaceFirstChar { it.uppercase() }} successfully!"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSharing = false,
                            error = error.message ?: "Failed to share"
                        )
                    }
                }
            )
        }
    }

    /**
     * Clear messages.
     */
    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
