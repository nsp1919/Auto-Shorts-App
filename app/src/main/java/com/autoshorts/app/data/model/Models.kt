package com.autoshorts.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * Response from video upload endpoint.
 */
data class UploadResponse(
    @SerializedName("video_id")
    val videoId: String,
    
    @SerializedName("filename")
    val filename: String? = null,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("status")
    val status: String? = null
)

/**
 * Request for YouTube URL upload.
 */
data class YouTubeUploadRequest(
    @SerializedName("url")
    val url: String
)

/**
 * Request to start video processing.
 */
data class ProcessRequest(
    @SerializedName("video_id")
    val videoId: String,
    
    @SerializedName("duration")
    val duration: Int,
    
    @SerializedName("quantity")
    val quantity: Int,
    
    @SerializedName("language")
    val language: String
)

/**
 * Response from process start endpoint.
 */
data class ProcessResponse(
    @SerializedName("job_id")
    val jobId: String,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("status")
    val status: String? = null
)

/**
 * Processing step information.
 */
data class ProcessingStep(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("status")
    val status: String, // "pending", "in_progress", "completed", "failed"
    
    @SerializedName("progress")
    val progress: Int? = null
)

/**
 * Generated video clip information.
 */
data class GeneratedClip(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("url")
    val url: String,
    
    @SerializedName("thumbnail_url")
    val thumbnailUrl: String? = null,
    
    @SerializedName("duration")
    val duration: Int? = null,
    
    @SerializedName("title")
    val title: String? = null
)

/**
 * Response from job status endpoint.
 */
data class JobStatusResponse(
    @SerializedName("job_id")
    val jobId: String,
    
    @SerializedName("status")
    val status: String, // "pending", "processing", "completed", "failed"
    
    @SerializedName("progress")
    val progress: Int = 0,
    
    @SerializedName("current_step")
    val currentStep: String? = null,
    
    @SerializedName("steps")
    val steps: List<ProcessingStep>? = null,
    
    @SerializedName("clips")
    val clips: List<GeneratedClip>? = null,
    
    @SerializedName("error")
    val error: String? = null,
    
    @SerializedName("estimated_time_remaining")
    val estimatedTimeRemaining: Int? = null
)

/**
 * Caption style configuration.
 */
data class CaptionStyle(
    @SerializedName("style")
    val style: String,
    
    @SerializedName("color")
    val color: String,
    
    @SerializedName("font_size")
    val fontSize: Int,
    
    @SerializedName("position")
    val position: String = "bottom"
)

/**
 * Request to regenerate video with new styles.
 */
data class RegenerateRequest(
    @SerializedName("job_id")
    val jobId: String,
    
    @SerializedName("clip_id")
    val clipId: String,
    
    @SerializedName("caption_style")
    val captionStyle: CaptionStyle
)

/**
 * Response from regenerate endpoint.
 */
data class RegenerateResponse(
    @SerializedName("job_id")
    val jobId: String,
    
    @SerializedName("clip_id")
    val clipId: String,
    
    @SerializedName("url")
    val url: String,
    
    @SerializedName("status")
    val status: String? = null
)

/**
 * Request to generate rocket share metadata.
 */
data class RocketRequest(
    @SerializedName("clip_id")
    val clipId: String,
    
    @SerializedName("platform")
    val platform: String? = null
)

/**
 * Response from rocket generate endpoint.
 */
data class RocketResponse(
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("hashtags")
    val hashtags: List<String>,
    
    @SerializedName("suggested_time")
    val suggestedTime: String? = null
)

/**
 * Request to share video to a platform.
 */
data class ShareRequest(
    @SerializedName("clip_id")
    val clipId: String,
    
    @SerializedName("video_url")
    val videoUrl: String,
    
    @SerializedName("title")
    val title: String? = null,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("hashtags")
    val hashtags: List<String>? = null
)

/**
 * Response from share endpoint.
 */
data class ShareResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("share_url")
    val shareUrl: String? = null
)

/**
 * Generic API error response.
 */
data class ApiError(
    @SerializedName("detail")
    val detail: String? = null,
    
    @SerializedName("message")
    val message: String? = null
)
