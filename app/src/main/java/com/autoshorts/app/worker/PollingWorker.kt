package com.autoshorts.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.autoshorts.app.data.api.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker for polling job status in the background.
 * Useful when the app is backgrounded during processing.
 */
class PollingWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val JOB_ID_KEY = "job_id"
        const val CHANNEL_ID = "processing_channel"
        const val NOTIFICATION_ID = 1001

        /**
         * Schedule background polling for a job.
         */
        fun schedule(context: Context, jobId: String) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val inputData = workDataOf(JOB_ID_KEY to jobId)

            val workRequest = PeriodicWorkRequestBuilder<PollingWorker>(
                repeatInterval = 15,
                repeatIntervalTimeUnit = TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setInputData(inputData)
                .addTag("polling_$jobId")
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    "polling_$jobId",
                    ExistingPeriodicWorkPolicy.REPLACE,
                    workRequest
                )
        }

        /**
         * Cancel polling for a job.
         */
        fun cancel(context: Context, jobId: String) {
            WorkManager.getInstance(context)
                .cancelAllWorkByTag("polling_$jobId")
        }
    }

    override suspend fun doWork(): Result {
        val jobId = inputData.getString(JOB_ID_KEY) ?: return Result.failure()

        return withContext(Dispatchers.IO) {
            try {
                val response = NetworkClient.apiService.getJobStatus(jobId)

                if (response.isSuccessful) {
                    val status = response.body()

                    when (status?.status?.lowercase()) {
                        "completed" -> {
                            showNotification(
                                title = "Processing Complete! 🎉",
                                message = "Your shorts are ready to view"
                            )
                            cancel(applicationContext, jobId)
                            Result.success()
                        }
                        "failed" -> {
                            showNotification(
                                title = "Processing Failed",
                                message = status.error ?: "Something went wrong"
                            )
                            cancel(applicationContext, jobId)
                            Result.failure()
                        }
                        else -> {
                            // Still processing, continue polling
                            showNotification(
                                title = "Processing...",
                                message = "${status?.progress ?: 0}% complete"
                            )
                            Result.success()
                        }
                    }
                } else {
                    Result.retry()
                }
            } catch (e: Exception) {
                Result.retry()
            }
        }
    }

    /**
     * Show a notification with progress or completion status.
     */
    private fun showNotification(title: String, message: String) {
        createNotificationChannel()

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_menu_upload)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val notificationManager = applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * Create notification channel for Android O+.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Processing Status",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for video processing status"
            }

            val notificationManager = applicationContext
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
