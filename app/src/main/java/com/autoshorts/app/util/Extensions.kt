package com.autoshorts.app.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.ui.graphics.Color

/**
 * Kotlin extension functions for common operations.
 */

/**
 * Copy text to clipboard and show a toast.
 */
fun Context.copyToClipboard(text: String, label: String = "Copied") {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(this, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
}

/**
 * Share text via Android share sheet.
 */
fun Context.shareText(text: String, title: String = "Share") {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    startActivity(Intent.createChooser(intent, title))
}

/**
 * Convert hex color string to Compose Color.
 */
fun String.toColor(): Color {
    val colorString = this.removePrefix("#")
    return try {
        Color(android.graphics.Color.parseColor("#$colorString"))
    } catch (e: Exception) {
        Color.White
    }
}

/**
 * Format duration in seconds to MM:SS format.
 */
fun Int.formatDuration(): String {
    val minutes = this / 60
    val seconds = this % 60
    return String.format("%d:%02d", minutes, seconds)
}

/**
 * Format milliseconds to MM:SS format.
 */
fun Long.formatMillisToTime(): String {
    val totalSeconds = this / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

/**
 * Validate YouTube URL.
 */
fun String.isValidYouTubeUrl(): Boolean {
    val patterns = listOf(
        "^(https?://)?(www\\.)?youtube\\.com/watch\\?v=.+$",
        "^(https?://)?(www\\.)?youtu\\.be/.+$",
        "^(https?://)?(www\\.)?youtube\\.com/shorts/.+$"
    )
    return patterns.any { pattern ->
        Regex(pattern, RegexOption.IGNORE_CASE).matches(this)
    }
}

/**
 * Extract video ID from YouTube URL.
 */
fun String.extractYouTubeVideoId(): String? {
    val patterns = listOf(
        "(?:youtube\\.com/watch\\?v=|youtu\\.be/|youtube\\.com/shorts/)([a-zA-Z0-9_-]{11})"
    )
    for (pattern in patterns) {
        val match = Regex(pattern).find(this)
        if (match != null && match.groupValues.size > 1) {
            return match.groupValues[1]
        }
    }
    return null
}

/**
 * Truncate string with ellipsis if it exceeds max length.
 */
fun String.truncate(maxLength: Int): String {
    return if (this.length > maxLength) {
        this.take(maxLength - 3) + "..."
    } else {
        this
    }
}
