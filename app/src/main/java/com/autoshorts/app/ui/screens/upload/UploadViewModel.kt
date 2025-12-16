package com.autoshorts.app.ui.screens.upload

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autoshorts.app.data.repository.AutoShortsRepository
import com.autoshorts.app.util.isValidYouTubeUrl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for Upload Screen.
 */
data class UploadUiState(
    // Input states
    val selectedVideoUri: Uri? = null,
    val selectedFileName: String? = null,
    val youtubeUrl: String = "",
    val selectedDuration: Int = 60,
    val selectedQuantity: Int = 3,
    val selectedLanguage: String = "en",
    
    // Upload states
    val isUploading: Boolean = false,
    val uploadProgress: Float = 0f,
    val videoId: String? = null,
    
    // Processing states
    val isProcessing: Boolean = false,
    val jobId: String? = null,
    
    // Error handling
    val error: String? = null,
    val isSuccess: Boolean = false
)

/**
 * ViewModel for Upload Screen.
 * Handles video upload (local file or YouTube URL) and processing initiation.
 */
class UploadViewModel(
    private val repository: AutoShortsRepository = AutoShortsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(UploadUiState())
    val uiState: StateFlow<UploadUiState> = _uiState.asStateFlow()

    /**
     * Update selected video file from gallery/file picker.
     */
    fun onVideoSelected(uri: Uri, fileName: String?) {
        _uiState.update {
            it.copy(
                selectedVideoUri = uri,
                selectedFileName = fileName,
                youtubeUrl = "", // Clear YouTube URL when file is selected
                error = null
            )
        }
    }

    /**
     * Update YouTube URL input.
     */
    fun onYouTubeUrlChanged(url: String) {
        _uiState.update {
            it.copy(
                youtubeUrl = url,
                selectedVideoUri = null, // Clear file when URL is entered
                selectedFileName = null,
                error = null
            )
        }
    }

    /**
     * Update selected duration.
     */
    fun onDurationSelected(duration: Int) {
        _uiState.update { it.copy(selectedDuration = duration) }
    }

    /**
     * Update selected quantity.
     */
    fun onQuantityChanged(quantity: Int) {
        _uiState.update { it.copy(selectedQuantity = quantity) }
    }

    /**
     * Update selected language.
     */
    fun onLanguageSelected(language: String) {
        _uiState.update { it.copy(selectedLanguage = language) }
    }

    /**
     * Clear any displayed error.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Validate input and check if ready to upload.
     */
    fun isReadyToUpload(): Boolean {
        val state = _uiState.value
        return state.selectedVideoUri != null || 
               (state.youtubeUrl.isNotEmpty() && state.youtubeUrl.isValidYouTubeUrl())
    }

    /**
     * Upload video using Retrofit (called from screen with context).
     * Returns true if upload started successfully.
     */
    suspend fun uploadVideoFile(context: android.content.Context): Boolean {
        val uri = _uiState.value.selectedVideoUri ?: return false
        
        _uiState.update { it.copy(isUploading = true, error = null) }
        
        val result = repository.uploadVideo(context, uri)
        
        result.fold(
            onSuccess = { response ->
                _uiState.update {
                    it.copy(
                        isUploading = false,
                        videoId = response.videoId,
                        uploadProgress = 1f
                    )
                }
                return true
            },
            onFailure = { error ->
                _uiState.update {
                    it.copy(
                        isUploading = false,
                        error = error.message ?: "Upload failed"
                    )
                }
                return false
            }
        )
    }

    /**
     * Upload video via YouTube URL.
     * Returns true if upload started successfully.
     */
    suspend fun uploadYouTubeUrl(): Boolean {
        val url = _uiState.value.youtubeUrl
        if (url.isEmpty() || !url.isValidYouTubeUrl()) {
            _uiState.update { it.copy(error = "Please enter a valid YouTube URL") }
            return false
        }
        
        _uiState.update { it.copy(isUploading = true, error = null) }
        
        val result = repository.uploadYouTubeUrl(url)
        
        result.fold(
            onSuccess = { response ->
                _uiState.update {
                    it.copy(
                        isUploading = false,
                        videoId = response.videoId,
                        uploadProgress = 1f
                    )
                }
                return true
            },
            onFailure = { error ->
                _uiState.update {
                    it.copy(
                        isUploading = false,
                        error = error.message ?: "Failed to process YouTube URL"
                    )
                }
                return false
            }
        )
    }

    /**
     * Start video processing after successful upload.
     * Returns the job ID if successful.
     */
    suspend fun startProcessing(): String? {
        val videoId = _uiState.value.videoId ?: return null
        val state = _uiState.value
        
        _uiState.update { it.copy(isProcessing = true, error = null) }
        
        val result = repository.processVideo(
            videoId = videoId,
            duration = state.selectedDuration,
            quantity = state.selectedQuantity,
            language = state.selectedLanguage
        )
        
        result.fold(
            onSuccess = { response ->
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        jobId = response.jobId,
                        isSuccess = true
                    )
                }
                return response.jobId
            },
            onFailure = { error ->
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        error = error.message ?: "Failed to start processing"
                    )
                }
                return null
            }
        )
    }

    /**
     * Reset state for a new upload.
     */
    fun reset() {
        _uiState.value = UploadUiState()
    }
}
