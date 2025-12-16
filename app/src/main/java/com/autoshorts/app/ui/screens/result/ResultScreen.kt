package com.autoshorts.app.ui.screens.result

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.autoshorts.app.ui.components.*
import com.autoshorts.app.ui.theme.*

/**
 * Result Screen displaying generated video clips.
 * Features video preview, caption customization, and sharing options.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    jobId: String,
    onNavigateToRocket: (clipId: String, videoUrl: String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ResultViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Initialize with job ID
    LaunchedEffect(jobId) {
        viewModel.initialize(jobId)
    }

    // Color picker dialog
    if (uiState.showColorPicker) {
        ColorPickerDialog(
            currentColor = uiState.captionColor,
            onColorSelected = viewModel::onCaptionColorChanged,
            onDismiss = { viewModel.toggleColorPicker(false) }
        )
    }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = { Text("Your Shorts") },
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
            if (uiState.clips.isEmpty()) {
                // Loading or empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    // Current clip preview
                    val currentClip = uiState.clips.getOrNull(uiState.selectedClipIndex)
                    currentClip?.let { clip ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                        ) {
                            VideoPlayer(
                                videoUrl = clip.url,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Clip selector (thumbnail carousel)
                    if (uiState.clips.size > 1) {
                        ClipCarousel(
                            clips = uiState.clips,
                            selectedIndex = uiState.selectedClipIndex,
                            onClipSelected = viewModel::selectClip
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Customization section
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Text(
                            text = "Customize Captions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Caption style selector
                        CaptionStyleSelector(
                            selectedStyle = uiState.captionStyle,
                            onStyleSelected = viewModel::onCaptionStyleChanged
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Color picker
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Caption Color",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextSecondary
                            )
                            
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(uiState.captionColor)
                                    .clickable { viewModel.toggleColorPicker(true) }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Font size slider
                        FontSizeSlider(
                            fontSize = uiState.fontSize,
                            onFontSizeChanged = viewModel::onFontSizeChanged
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Regenerate button
                        SecondaryButton(
                            text = if (uiState.isRegenerating) "Regenerating..." else "Regenerate with Style",
                            onClick = viewModel::regenerateClip,
                            enabled = !uiState.isRegenerating,
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Default.Refresh
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Download button
                            OutlinedButton(
                                onClick = {
                                    currentClip?.let { clip ->
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(clip.url))
                                        context.startActivity(intent)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Download")
                            }

                            // Share button
                            OutlinedButton(
                                onClick = {
                                    currentClip?.let { clip ->
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, clip.url)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share video"))
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Share")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Rocket Share button (main CTA)
                        PrimaryButton(
                            text = "Rocket Share 🚀",
                            onClick = {
                                currentClip?.let { clip ->
                                    onNavigateToRocket(clip.id, clip.url)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Default.RocketLaunch
                        )

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }

            // Error snackbar
            AnimatedVisibility(
                visible = uiState.error != null,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.9f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = uiState.error ?: "",
                            color = Color.White
                        )
                    }
                }
            }

            // Success snackbar
            AnimatedVisibility(
                visible = uiState.successMessage != null,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.9f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = uiState.successMessage ?: "",
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Horizontal carousel for selecting clips.
 */
@Composable
private fun ClipCarousel(
    clips: List<com.autoshorts.app.data.model.GeneratedClip>,
    selectedIndex: Int,
    onClipSelected: (Int) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(clips) { index, clip ->
            ClipThumbnail(
                thumbnailUrl = clip.thumbnailUrl,
                clipNumber = index + 1,
                isSelected = index == selectedIndex,
                onClick = { onClipSelected(index) }
            )
        }
    }
}

/**
 * Individual clip thumbnail in the carousel.
 */
@Composable
private fun ClipThumbnail(
    thumbnailUrl: String?,
    clipNumber: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(80.dp)
            .aspectRatio(9f / 16f),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) {
            CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    listOf(PrimaryBlue, AccentPink)
                )
            )
        } else null,
        colors = CardDefaults.cardColors(containerColor = SurfaceDarkElevated)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (thumbnailUrl != null) {
                AsyncImage(
                    model = thumbnailUrl,
                    contentDescription = "Clip $clipNumber",
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            // Clip number badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isSelected) PrimaryBlue else OverlayDark)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "#$clipNumber",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
        }
    }
}
