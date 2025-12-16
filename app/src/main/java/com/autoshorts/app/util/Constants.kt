package com.autoshorts.app.util

/**
 * Constants used throughout the app.
 * Update BASE_URL when deploying to production.
 */
object Constants {
    // Backend API base URL
    // For Android emulator, use 10.0.2.2 to access localhost
    // For physical device, use your computer's local IP or production URL
    const val BASE_URL = "http://10.0.2.2:8000/"
    
    // API endpoints
    const val ENDPOINT_UPLOAD = "api/upload"
    const val ENDPOINT_PROCESS = "api/process"
    const val ENDPOINT_STATUS = "api/process/status"
    const val ENDPOINT_REGENERATE = "api/process/regenerate"
    const val ENDPOINT_ROCKET = "api/rocket/generate"
    const val ENDPOINT_SHARE = "api/share"
    
    // Duration options in seconds
    val DURATION_OPTIONS = listOf(30, 60, 90, 120)
    
    // Quantity range
    const val MIN_QUANTITY = 1
    const val MAX_QUANTITY = 10
    
    // Supported languages
    val LANGUAGE_OPTIONS = listOf(
        "English" to "en",
        "Hindi" to "hi",
        "Telugu" to "te",
        "Tamil" to "ta",
        "Spanish" to "es",
        "Portuguese" to "pt",
        "French" to "fr",
        "German" to "de",
        "Japanese" to "ja",
        "Korean" to "ko"
    )
    
    // Caption styles
    val CAPTION_STYLES = listOf(
        "Modern",
        "Classic",
        "Bold",
        "Minimal",
        "Neon",
        "Gradient"
    )
    
    // Polling interval in milliseconds
    const val POLLING_INTERVAL_MS = 3000L
    
    // Share platforms
    val SHARE_PLATFORMS = listOf(
        "instagram",
        "youtube",
        "tiktok"
    )
}
