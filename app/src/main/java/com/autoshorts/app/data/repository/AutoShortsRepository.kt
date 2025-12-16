package com.autoshorts.app.data.repository

import android.content.Context
import android.net.Uri
import com.autoshorts.app.data.api.ApiService
import com.autoshorts.app.data.api.NetworkClient
import com.autoshorts.app.data.model.*
import com.autoshorts.app.util.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File

/**
 * Repository class that handles all data operations.
 * Acts as a single source of truth for the app's data.
 */
class AutoShortsRepository(
    private val apiService: ApiService = NetworkClient.apiService
) {

    /**
     * Upload a video file from a content URI.
     * Copies the file to cache first, then uploads via multipart.
     */
    suspend fun uploadVideo(context: Context, uri: Uri): Result<UploadResponse> {
        return withContext(Dispatchers.IO) {
            try {
                // Copy URI content to a temporary file
                val tempFile = FileUtils.copyUriToTempFile(context, uri)
                    ?: return@withContext Result.failure(Exception("Failed to read video file"))

                val mimeType = FileUtils.getMimeType(context, uri) ?: "video/mp4"
                val requestBody = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
                val multipartBody = MultipartBody.Part.createFormData(
                    "file",
                    tempFile.name,
                    requestBody
                )

                val response = apiService.uploadVideo(multipartBody)
                
                // Clean up temp file
                tempFile.delete()

                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Upload a video via YouTube URL.
     */
    suspend fun uploadYouTubeUrl(url: String): Result<UploadResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = YouTubeUploadRequest(url)
                val response = apiService.uploadYouTubeUrl(request)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Start video processing with specified parameters.
     */
    suspend fun processVideo(
        videoId: String,
        duration: Int,
        quantity: Int,
        language: String
    ): Result<ProcessResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = ProcessRequest(
                    videoId = videoId,
                    duration = duration,
                    quantity = quantity,
                    language = language
                )
                val response = apiService.processVideo(request)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Get the current status of a processing job.
     */
    suspend fun getJobStatus(jobId: String): Result<JobStatusResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getJobStatus(jobId)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Regenerate a clip with new caption styles.
     */
    suspend fun regenerateClip(
        jobId: String,
        clipId: String,
        style: String,
        color: String,
        fontSize: Int
    ): Result<RegenerateResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val captionStyle = CaptionStyle(
                    style = style,
                    color = color,
                    fontSize = fontSize
                )
                val request = RegenerateRequest(
                    jobId = jobId,
                    clipId = clipId,
                    captionStyle = captionStyle
                )
                val response = apiService.regenerateClip(request)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Generate AI-powered metadata for sharing.
     */
    suspend fun generateRocketMetadata(
        clipId: String,
        platform: String? = null
    ): Result<RocketResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = RocketRequest(clipId = clipId, platform = platform)
                val response = apiService.generateRocketMetadata(request)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Share a video to a specific platform.
     */
    suspend fun shareVideo(
        platform: String,
        clipId: String,
        videoUrl: String,
        title: String?,
        description: String?,
        hashtags: List<String>?
    ): Result<ShareResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = ShareRequest(
                    clipId = clipId,
                    videoUrl = videoUrl,
                    title = title,
                    description = description,
                    hashtags = hashtags
                )
                val response = apiService.shareVideo(platform, request)
                handleResponse(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Handle Retrofit response and convert to Result.
     */
    private fun <T> handleResponse(response: Response<T>): Result<T> {
        return if (response.isSuccessful) {
            response.body()?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Empty response body"))
        } else {
            val errorBody = response.errorBody()?.string()
            Result.failure(Exception(errorBody ?: "API error: ${response.code()}"))
        }
    }
}
