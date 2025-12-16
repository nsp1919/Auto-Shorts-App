package com.autoshorts.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.autoshorts.app.R
import com.autoshorts.app.ui.theme.*

/**
 * Lottie animation loader component.
 * Uses Lottie for smooth, high-quality animations.
 */
@Composable
fun LottieLoader(
    animationResId: Int,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = true,
    iterations: Int = LottieConstants.IterateForever
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(animationResId))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = isPlaying,
        iterations = iterations,
        speed = 1f
    )

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier
    )
}

/**
 * Processing loader with step-based progress UI.
 */
@Composable
fun ProcessingLoader(
    currentStep: String,
    progress: Int,
    steps: List<Pair<String, String>>, // name to status
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Lottie animation placeholder (will use shimmer until Lottie JSON is added)
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(100.dp)),
            contentAlignment = Alignment.Center
        ) {
            ShimmerLoader()
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Current step text
        Text(
            text = currentStep,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Progress bar
        LinearProgressIndicator(
            progress = { progress / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = PrimaryBlue,
            trackColor = SurfaceDarkElevated
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Progress percentage
        Text(
            text = "$progress%",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = PrimaryBlue
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Steps list
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            steps.forEach { (name, status) ->
                ProcessingStepItem(name = name, status = status)
            }
        }
    }
}

@Composable
private fun ProcessingStepItem(
    name: String,
    status: String
) {
    val statusColor = when (status.lowercase()) {
        "completed" -> SuccessGreen
        "in_progress" -> PrimaryBlue
        "failed" -> ErrorRed
        else -> TextTertiary
    }

    val statusIcon = when (status.lowercase()) {
        "completed" -> "✓"
        "in_progress" -> "◉"
        "failed" -> "✗"
        else -> "○"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceDarkElevated)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = statusIcon,
                color = statusColor
            )
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
        }
        
        if (status.lowercase() == "in_progress") {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = PrimaryBlue,
                strokeWidth = 2.dp
            )
        }
    }
}

/**
 * Shimmer loading effect for placeholders.
 */
@Composable
fun ShimmerLoader(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerTranslate by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    Box(
        modifier = modifier
            .size(150.dp)
            .clip(RoundedCornerShape(75.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        SurfaceDarkElevated,
                        SurfaceDarkHighlight,
                        SurfaceDarkElevated
                    )
                )
            )
    ) {
        // Animated gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            PrimaryBlue.copy(alpha = 0.3f),
                            Color.Transparent
                        ),
                        startX = shimmerTranslate - 500f,
                        endX = shimmerTranslate
                    )
                )
        )
    }
}

/**
 * Full screen loading overlay.
 */
@Composable
fun LoadingOverlay(
    message: String = "Loading...",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = PrimaryBlue,
                strokeWidth = 4.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

/**
 * Error state with retry button.
 */
@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "😕",
            style = MaterialTheme.typography.displayLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        PrimaryButton(
            text = "Try Again",
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(0.6f)
        )
    }
}
