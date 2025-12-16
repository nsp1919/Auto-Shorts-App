package com.autoshorts.app

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager

/**
 * Application class for Auto Shorts app.
 * Initializes WorkManager and other app-wide dependencies.
 */
class AutoShortsApp : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    /**
     * Configure WorkManager with default settings.
     * This enables background processing for polling and downloads.
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()

    companion object {
        lateinit var instance: AutoShortsApp
            private set
    }
}
