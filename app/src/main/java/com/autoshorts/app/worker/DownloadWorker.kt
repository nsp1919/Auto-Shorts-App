package com.autoshorts.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

/**
 * WorkManager worker for downloading generated videos.
 * Saves videos to the device's Download folder.
 */
class DownloadWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val VIDEO_URL_KEY = "video_url"
        const val FILE_NAME_KEY = "file_name"
        const val CHANNEL_ID = "download_channel"
        const val NOTIFICATION_ID = 2001

        /**
         * Start a download task.
         */
        fun start(context: Context, videoUrl: String, fileName: String) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val inputData = workDataOf(
                VIDEO_URL_KEY to videoUrl,
                FILE_NAME_KEY to fileName
            )

            val workRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
                .setConstraints(constraints)
                .setInputData(inputData)
                .addTag("download")
                .build()

            WorkManager.getInstance(context)
                .enqueue(workRequest)
        }
    }

    private val client = OkHttpClient.Builder().build()

    override suspend fun doWork(): Result {
        val videoUrl = inputData.getString(VIDEO_URL_KEY) ?: return Result.failure()
        val fileName = inputData.getString(FILE_NAME_KEY) ?: "autoshorts_video.mp4"

        return withContext(Dispatchers.IO) {
            try {
                showNotification("Downloading...", "Starting download")

                val request = Request.Builder()
                    .url(videoUrl)
                    .build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val body = response.body ?: return@withContext Result.failure()
                    val contentLength = body.contentLength()

                    // Save to Downloads folder
                    saveToDownloads(fileName, body.bytes())

                    showNotification(
                        "Download Complete",
                        "$fileName saved to Downloads"
                    )
                    
                    Result.success()
                } else {
                    showNotification("Download Failed", "Could not download video")
                    Result.failure()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showNotification("Download Failed", e.message ?: "Unknown error")
                Result.failure()
            }
        }
    }

    /**
     * Save video bytes to the Downloads folder.
     * Uses MediaStore for Android Q+ for proper scoped storage.
     */
    private fun saveToDownloads(fileName: String, bytes: ByteArray) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Use MediaStore for Android 10+
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "video/mp4")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }

            val resolver = applicationContext.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let {
                resolver.openOutputStream(it)?.use { output ->
                    output.write(bytes)
                }

                contentValues.clear()
                contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
        } else {
            // Legacy storage for older versions
            val downloadsDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            )
            val file = File(downloadsDir, fileName)
            FileOutputStream(file).use { output ->
                output.write(bytes)
            }
        }
    }

    /**
     * Show download notification.
     */
    private fun showNotification(title: String, message: String) {
        createNotificationChannel()

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val notificationManager = applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * Create notification channel for downloads.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Downloads",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for video downloads"
            }

            val notificationManager = applicationContext
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
