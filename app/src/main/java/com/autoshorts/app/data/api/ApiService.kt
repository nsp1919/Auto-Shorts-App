package com.autoshorts.app.data.api

import com.autoshorts.app.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API service interface for Auto Shorts backend.
 * All endpoints return Response wrapper for proper error handling.
 */
interface ApiService {

    /**
     * Upload a video file using multipart form data.
     * @param file The video file to upload
     * @return UploadResponse containing video_id
     */
    @Multipart
    @POST("api/upload")
    suspend fun uploadVideo(
        @Part file: MultipartBody.Part
    ): Response<UploadResponse>

    /**
     * Upload a video via YouTube URL.
     * @param request YouTubeUploadRequest containing the URL
     * @return UploadResponse containing video_id
     */
    @POST("api/upload")
    suspend fun uploadYouTubeUrl(
        @Body request: YouTubeUploadRequest
    ): Response<UploadResponse>

    /**
     * Start video processing with specified parameters.
     * @param request ProcessRequest with video_id, duration, quantity, language
     * @return ProcessResponse containing job_id
     */
    @POST("api/process")
    suspend fun processVideo(
        @Body request: ProcessRequest
    ): Response<ProcessResponse>

    /**
     * Get the status of a processing job.
     * @param jobId The job ID to check
     * @return JobStatusResponse with status, progress, and clips if completed
     */
    @GET("api/process/status/{job_id}")
    suspend fun getJobStatus(
        @Path("job_id") jobId: String
    ): Response<JobStatusResponse>

    /**
     * Regenerate a clip with new caption styles.
     * @param request RegenerateRequest with job_id, clip_id, and new style
     * @return RegenerateResponse with new video URL
     */
    @POST("api/process/regenerate")
    suspend fun regenerateClip(
        @Body request: RegenerateRequest
    ): Response<RegenerateResponse>

    /**
     * Generate AI-powered metadata for sharing (title, description, hashtags).
     * @param request RocketRequest with clip_id and optional platform
     * @return RocketResponse with AI-generated content
     */
    @POST("api/rocket/generate")
    suspend fun generateRocketMetadata(
        @Body request: RocketRequest
    ): Response<RocketResponse>

    /**
     * Share a video to a specific platform.
     * @param platform Target platform (instagram, youtube, tiktok)
     * @param request ShareRequest with video details
     * @return ShareResponse with success status
     */
    @POST("api/share/{platform}")
    suspend fun shareVideo(
        @Path("platform") platform: String,
        @Body request: ShareRequest
    ): Response<ShareResponse>
}
