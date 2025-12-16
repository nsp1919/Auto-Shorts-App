package com.autoshorts.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.autoshorts.app.ui.screens.processing.ProcessingScreen
import com.autoshorts.app.ui.screens.result.ResultScreen
import com.autoshorts.app.ui.screens.rocket.RocketShareScreen
import com.autoshorts.app.ui.screens.splash.SplashScreen
import com.autoshorts.app.ui.screens.upload.UploadScreen
import java.net.URLDecoder
import java.net.URLEncoder

/**
 * Navigation routes for the app.
 */
object Routes {
    const val SPLASH = "splash"
    const val UPLOAD = "upload"
    const val PROCESSING = "processing/{jobId}"
    const val RESULT = "result/{jobId}"
    const val ROCKET = "rocket/{clipId}/{videoUrl}"
    
    fun processing(jobId: String) = "processing/$jobId"
    fun result(jobId: String) = "result/$jobId"
    fun rocket(clipId: String, videoUrl: String) = "rocket/$clipId/${URLEncoder.encode(videoUrl, "UTF-8")}"
}

/**
 * Main navigation graph for Auto Shorts app.
 * Handles navigation between all screens with proper arguments.
 */
@Composable
fun AutoShortsNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        // Splash Screen
        composable(Routes.SPLASH) {
            SplashScreen(
                onNavigateToUpload = {
                    navController.navigate(Routes.UPLOAD) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        // Upload Screen
        composable(Routes.UPLOAD) {
            UploadScreen(
                onNavigateToProcessing = { jobId ->
                    navController.navigate(Routes.processing(jobId))
                }
            )
        }

        // Processing Screen
        composable(
            route = Routes.PROCESSING,
            arguments = listOf(
                navArgument("jobId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
            ProcessingScreen(
                jobId = jobId,
                onNavigateToResult = { id ->
                    navController.navigate(Routes.result(id)) {
                        popUpTo(Routes.UPLOAD)
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Result Screen
        composable(
            route = Routes.RESULT,
            arguments = listOf(
                navArgument("jobId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
            ResultScreen(
                jobId = jobId,
                onNavigateToRocket = { clipId, videoUrl ->
                    navController.navigate(Routes.rocket(clipId, videoUrl))
                },
                onNavigateBack = {
                    navController.navigate(Routes.UPLOAD) {
                        popUpTo(Routes.UPLOAD) { inclusive = true }
                    }
                }
            )
        }

        // Rocket Share Screen
        composable(
            route = Routes.ROCKET,
            arguments = listOf(
                navArgument("clipId") { type = NavType.StringType },
                navArgument("videoUrl") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val clipId = backStackEntry.arguments?.getString("clipId") ?: ""
            val videoUrl = try {
                URLDecoder.decode(
                    backStackEntry.arguments?.getString("videoUrl") ?: "",
                    "UTF-8"
                )
            } catch (e: Exception) {
                ""
            }
            
            RocketShareScreen(
                clipId = clipId,
                videoUrl = videoUrl,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
