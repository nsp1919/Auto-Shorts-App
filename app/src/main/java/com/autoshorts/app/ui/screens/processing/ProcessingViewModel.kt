package com.autoshorts.app.ui.screens.processing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autoshorts.app.data.model.GeneratedClip
import com.autoshorts.app.data.model.ProcessingStep
import com.autoshorts.app.data.repository.AutoShortsRepository
import com.autoshorts.app.util.Constants
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * UI state for Processing Screen.
 */
data class ProcessingUiState(
    val jobId: String = "",
    val status: String = "pending",
    val progress: Int = 0,
    val currentStep: String = "Initializing...",
    val steps: List<ProcessingStep> = emptyList(),
    val clips: List<GeneratedClip> = emptyList(),
    val estimatedTimeRemaining: Int? = null,
    val error: String? = null,
    val isCompleted: Boolean = false,
    val isFailed: Boolean = false
)

/**
 * ViewModel for Processing Screen.
 * Handles polling for job status updates.
 */
class ProcessingViewModel(
    private val repository: AutoShortsRepository = AutoShortsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProcessingUiState())
    val uiState: StateFlow<ProcessingUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null

    /**
     * Start polling for job status.
     * Polls every 3 seconds until job is completed or failed.
     */
    fun startPolling(jobId: String) {
        _uiState.update { it.copy(jobId = jobId) }
        
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                pollJobStatus(jobId)
                
                // Stop polling if completed or failed
                val currentState = _uiState.value
                if (currentState.isCompleted || currentState.isFailed) {
                    break
                }
                
                delay(Constants.POLLING_INTERVAL_MS)
            }
        }
    }

    /**
     * Poll job status once.
     */
    private suspend fun pollJobStatus(jobId: String) {
        val result = repository.getJobStatus(jobId)
        
        result.fold(
            onSuccess = { response ->
                _uiState.update {
                    it.copy(
                        status = response.status,
                        progress = response.progress,
                        currentStep = response.currentStep ?: getStepName(response.status),
                        steps = response.steps ?: generateDefaultSteps(response.progress),
                        clips = response.clips ?: emptyList(),
                        estimatedTimeRemaining = response.estimatedTimeRemaining,
                        isCompleted = response.status == "completed",
                        isFailed = response.status == "failed",
                        error = if (response.status == "failed") response.error else null
                    )
                }
            },
            onFailure = { error ->
                _uiState.update {
                    it.copy(
                        error = error.message ?: "Failed to get status",
                        isFailed = true
                    )
                }
            }
        )
    }

    /**
     * Get human-readable step name from status.
     */
    private fun getStepName(status: String): String {
        return when (status.lowercase()) {
            "pending" -> "Waiting to start..."
            "processing" -> "Processing video..."
            "completed" -> "Complete!"
            "failed" -> "Processing failed"
            else -> "Processing..."
        }
    }

    /**
     * Generate default processing steps based on progress.
     */
    private fun generateDefaultSteps(progress: Int): List<ProcessingStep> {
        return listOf(
            ProcessingStep(
                name = "Analyzing Video",
                status = when {
                    progress >= 20 -> "completed"
                    progress > 0 -> "in_progress"
                    else -> "pending"
                }
            ),
            ProcessingStep(
                name = "Extracting Highlights",
                status = when {
                    progress >= 40 -> "completed"
                    progress >= 20 -> "in_progress"
                    else -> "pending"
                }
            ),
            ProcessingStep(
                name = "Generating Captions",
                status = when {
                    progress >= 60 -> "completed"
                    progress >= 40 -> "in_progress"
                    else -> "pending"
                }
            ),
            ProcessingStep(
                name = "Creating Shorts",
                status = when {
                    progress >= 80 -> "completed"
                    progress >= 60 -> "in_progress"
                    else -> "pending"
                }
            ),
            ProcessingStep(
                name = "Finalizing",
                status = when {
                    progress >= 100 -> "completed"
                    progress >= 80 -> "in_progress"
                    else -> "pending"
                }
            )
        )
    }

    /**
     * Retry failed job.
     */
    fun retry() {
        val jobId = _uiState.value.jobId
        if (jobId.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    error = null,
                    isFailed = false,
                    progress = 0,
                    status = "pending"
                )
            }
            startPolling(jobId)
        }
    }

    /**
     * Cancel polling when leaving screen.
     */
    fun stopPolling() {
        pollingJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }
}
