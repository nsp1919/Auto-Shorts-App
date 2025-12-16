package com.autoshorts.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.autoshorts.app.navigation.AutoShortsNavGraph
import com.autoshorts.app.ui.theme.AutoShortsTheme

/**
 * Main activity for Auto Shorts app.
 * Single activity architecture with Compose navigation.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display for immersive UI
        enableEdgeToEdge()

        setContent {
            AutoShortsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AutoShortsNavGraph()
                }
            }
        }
    }
}
