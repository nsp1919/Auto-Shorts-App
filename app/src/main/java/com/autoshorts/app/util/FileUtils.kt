package com.autoshorts.app.util

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Utility functions for file operations.
 */
object FileUtils {

    /**
     * Get the file name from a content URI.
     */
    fun getFileName(context: Context, uri: Uri): String? {
        var fileName: String? = null
        
        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            val cursor: Cursor? = context.contentResolver.query(
                uri, null, null, null, null
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        fileName = it.getString(nameIndex)
                    }
                }
            }
        }
        
        if (fileName == null) {
            fileName = uri.path?.substringAfterLast('/')
        }
        
        return fileName
    }

    /**
     * Get the file size from a content URI.
     */
    fun getFileSize(context: Context, uri: Uri): Long {
        var size: Long = 0
        
        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            val cursor: Cursor? = context.contentResolver.query(
                uri, null, null, null, null
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex >= 0) {
                        size = it.getLong(sizeIndex)
                    }
                }
            }
        }
        
        return size
    }

    /**
     * Get MIME type from a content URI.
     */
    fun getMimeType(context: Context, uri: Uri): String? {
        return if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            context.contentResolver.getType(uri)
        } else {
            val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
    }

    /**
     * Copy URI content to a temporary file.
     * Returns the temporary file or null if operation fails.
     */
    fun copyUriToTempFile(context: Context, uri: Uri): File? {
        return try {
            val fileName = getFileName(context, uri) ?: "temp_video.mp4"
            val tempFile = File(context.cacheDir, fileName)
            
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Clear the app's cache directory.
     */
    fun clearCache(context: Context) {
        try {
            context.cacheDir.listFiles()?.forEach { file ->
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Format file size to human-readable string.
     */
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
            else -> String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }

    /**
     * Validate if the URI points to a video file.
     */
    fun isVideoFile(context: Context, uri: Uri): Boolean {
        val mimeType = getMimeType(context, uri)
        return mimeType?.startsWith("video/") == true
    }
}
