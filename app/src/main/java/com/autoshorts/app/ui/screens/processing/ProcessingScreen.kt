package com.autoshorts.app.ui.screens.processing

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.autoshorts.app.ui.components.*
import com.autoshorts.app.ui.theme.*

/**
 * Processing Screen showing real-time progress of video processing.
 * Polls backend for status updates and displays step-by-step progress.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcessingScreen(
    jobId: String,
    onNavigateToResult: (jobId: String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ProcessingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Start polling when screen is displayed
    LaunchedEffect(jobId) {
        viewModel.startPolling(jobId)
    }

    // Stop polling when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopPolling()
        }
    }

    // Navigate to result when completed
    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            onNavigateToResult(jobId)
        }
    }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = { Text("Processing") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isFailed -> {
                    // Error state
                    ErrorState(
                        message = uiState.error ?: "Something went wrong",
                        onRetry = viewModel::retry
                    )
                }
                else -> {
                    // Processing state
                    ProcessingContent(
                        currentStep = uiState.currentStep,
                        progress = uiState.progress,
                        steps = uiState.steps.map { it.name to it.status },
                        estimatedTime = uiState.estimatedTimeRemaining
                    )
                }
            }
        }
    }
}

/**
 * Main content showing processing progress.
 */
@Composable
private fun ProcessingContent(
    currentStep: String,
    progress: Int,
    steps: List<Pair<String, String>>,
    estimatedTime: Int?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Animated loader
        AnimatedLoader()

        Spacer(modifier = Modifier.height(32.dp))

        // Current step text with animation
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith
                        fadeOut(animationSpec = tween(300))
            },
            label = "step_text"
        ) { step ->
            Text(
                text = step,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Estimated time
        estimatedTime?.let {
            Text(
                text = "About ${it / 60}:${String.format("%02d", it % 60)} remaining",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Progress bar with gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(SurfaceDarkElevated)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress / 100f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(GradientStart, GradientMiddle, GradientEnd)
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Progress percentage
        Text(
            text = "$progress%",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = PrimaryBlue
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Steps list
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            steps.forEach { (name, status) ->
                StepItem(name = name, status = status)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Info text
        Text(
            text = "Please don't close the app while processing",
            style = MaterialTheme.typography.bodySmall,
            color = TextTertiary,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Animated circular loader with pulsing effect.
 */
@Composable
private fun AnimatedLoader() {
    val infiniteTransition = rememberInfiniteTransition(label = "loader")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier.size(160.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer rotating ring
        CircularProgressIndicator(
            progress = 0.7f,
            modifier = Modifier
                .size(160.dp)
                .graphicsLayer { rotationZ = rotation },
            color = PrimaryBlue.copy(alpha = 0.3f),
            strokeWidth = 8.dp
        )
        
        // Inner pulsing circle
        Box(
            modifier = Modifier
                .size((100 * scale).dp)
                .clip(RoundedCornerShape((50 * scale).dp))
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PrimaryBlue.copy(alpha = 0.4f),
                            SecondaryPurple.copy(alpha = 0.2f),
                            AccentPink.copy(alpha = 0.1f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🎬",
                style = MaterialTheme.typography.displayMedium
            )
        }
    }
}

/**
 * Individual step item with status indicator.
 */
@Composable
private fun StepItem(
    name: String,
    status: String
) {
    val (statusColor, statusIcon) = when (status.lowercase()) {
        "completed" -> SuccessGreen to "✓"
        "in_progress" -> PrimaryBlue to "◉"
        "failed" -> ErrorRed to "✗"
        else -> TextTertiary to "○"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDarkElevated)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = statusIcon,
                style = MaterialTheme.typography.titleMedium,
                color = statusColor
            )
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                color = if (status == "pending") TextTertiary else TextPrimary
            )
        }

        if (status.lowercase() == "in_progress") {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = PrimaryBlue,
                strokeWidth = 2.dp
            )
        }
    }
}
