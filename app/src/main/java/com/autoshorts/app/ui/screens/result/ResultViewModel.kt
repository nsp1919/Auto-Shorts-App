package com.autoshorts.app.ui.screens.result

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autoshorts.app.data.model.GeneratedClip
import com.autoshorts.app.data.repository.AutoShortsRepository
import com.autoshorts.app.ui.theme.CaptionColors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for Result Screen.
 */
data class ResultUiState(
    val jobId: String = "",
    val clips: List<GeneratedClip> = emptyList(),
    val selectedClipIndex: Int = 0,
    
    // Caption customization
    val captionStyle: String = "Modern",
    val captionColor: Color = Color.White,
    val fontSize: Int = 24,
    
    // Action states
    val isRegenerating: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadProgress: Float = 0f,
    
    // Dialogs
    val showColorPicker: Boolean = false,
    val showShareOptions: Boolean = false,
    
    // Error handling
    val error: String? = null,
    val successMessage: String? = null
)

/**
 * ViewModel for Result Screen.
 * Handles clip preview, caption customization, regeneration, and sharing.
 */
class ResultViewModel(
    private val repository: AutoShortsRepository = AutoShortsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultUiState())
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    /**
     * Initialize with job ID and load clips.
     */
    fun initialize(jobId: String) {
        _uiState.update { it.copy(jobId = jobId) }
        loadClips(jobId)
    }

    /**
     * Load clips for the job.
     */
    private fun loadClips(jobId: String) {
        viewModelScope.launch {
            val result = repository.getJobStatus(jobId)
            result.fold(
                onSuccess = { response ->
                    _uiState.update {
                        it.copy(
                            clips = response.clips ?: emptyList(),
                            error = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(error = error.message ?: "Failed to load clips")
                    }
                }
            )
        }
    }

    /**
     * Select a clip to preview.
     */
    fun selectClip(index: Int) {
        _uiState.update { it.copy(selectedClipIndex = index) }
    }

    /**
     * Update caption style.
     */
    fun onCaptionStyleChanged(style: String) {
        _uiState.update { it.copy(captionStyle = style) }
    }

    /**
     * Update caption color.
     */
    fun onCaptionColorChanged(color: Color) {
        _uiState.update { 
            it.copy(
                captionColor = color,
                showColorPicker = false
            )
        }
    }

    /**
     * Update font size.
     */
    fun onFontSizeChanged(size: Int) {
        _uiState.update { it.copy(fontSize = size) }
    }

    /**
     * Toggle color picker dialog.
     */
    fun toggleColorPicker(show: Boolean) {
        _uiState.update { it.copy(showColorPicker = show) }
    }

    /**
     * Toggle share options dialog.
     */
    fun toggleShareOptions(show: Boolean) {
        _uiState.update { it.copy(showShareOptions = show) }
    }

    /**
     * Regenerate current clip with new caption settings.
     */
    fun regenerateClip() {
        val state = _uiState.value
        val clip = state.clips.getOrNull(state.selectedClipIndex) ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isRegenerating = true, error = null) }

            val colorHex = state.captionColor.toHexString()
            
            val result = repository.regenerateClip(
                jobId = state.jobId,
                clipId = clip.id,
                style = state.captionStyle,
                color = colorHex,
                fontSize = state.fontSize
            )

            result.fold(
                onSuccess = { response ->
                    // Update clip URL in the list
                    val updatedClips = state.clips.toMutableList()
                    updatedClips[state.selectedClipIndex] = clip.copy(url = response.url)
                    
                    _uiState.update {
                        it.copy(
                            isRegenerating = false,
                            clips = updatedClips,
                            successMessage = "Clip regenerated successfully!"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isRegenerating = false,
                            error = error.message ?: "Regeneration failed"
                        )
                    }
                }
            )
        }
    }

    /**
     * Get current clip.
     */
    fun getCurrentClip(): GeneratedClip? {
        return _uiState.value.clips.getOrNull(_uiState.value.selectedClipIndex)
    }

    /**
     * Clear messages.
     */
    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }

    /**
     * Convert Color to hex string.
     */
    private fun Color.toHexString(): String {
        val red = (this.red * 255).toInt()
        val green = (this.green * 255).toInt()
        val blue = (this.blue * 255).toInt()
        return String.format("#%02X%02X%02X", red, green, blue)
    }
}
