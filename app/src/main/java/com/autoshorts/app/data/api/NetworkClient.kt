package com.autoshorts.app.data.api

import com.autoshorts.app.util.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton object for network client configuration.
 * Provides configured Retrofit instance with OkHttp client.
 */
object NetworkClient {

    /**
     * OkHttp logging interceptor for debugging network calls.
     * Shows request/response headers and body in logcat.
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * Configured OkHttp client with:
     * - 60 second timeouts for large video uploads
     * - Logging interceptor for debugging
     */
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS) // Longer timeout for video uploads
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            // Add common headers to all requests
            val request = chain.request().newBuilder()
                .addHeader("Accept", "application/json")
                .build()
            chain.proceed(request)
        }
        .build()

    /**
     * Retrofit instance configured with:
     * - Base URL from Constants
     * - Gson converter for JSON serialization
     * - Custom OkHttp client
     */
    private val retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    /**
     * API service instance for making network calls.
     * Use this throughout the app for all API interactions.
     */
    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
