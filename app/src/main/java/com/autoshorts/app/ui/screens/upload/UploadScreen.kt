package com.autoshorts.app.ui.screens.upload

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.autoshorts.app.ui.components.*
import com.autoshorts.app.ui.theme.*
import com.autoshorts.app.util.FileUtils
import com.autoshorts.app.util.isValidYouTubeUrl
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

/**
 * Upload Screen for selecting video source and configuring processing options.
 * Supports local video file upload and YouTube URL input.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun UploadScreen(
    onNavigateToProcessing: (jobId: String) -> Unit,
    viewModel: UploadViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Permission for media access
    val mediaPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_VIDEO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    val permissionState = rememberPermissionState(mediaPermission)

    // Video picker launcher
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = FileUtils.getFileName(context, it)
            viewModel.onVideoSelected(it, fileName)
        }
    }

    // Handle navigation when processing starts
    LaunchedEffect(uiState.jobId) {
        uiState.jobId?.let { jobId ->
            onNavigateToProcessing(jobId)
        }
    }

    Scaffold(
        containerColor = BackgroundDark
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(24.dp)
            ) {
                // Header
                Text(
                    text = "Create Shorts",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                Text(
                    text = "Upload a video or paste a YouTube link",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Video Source Selection
                VideoSourceCard(
                    selectedUri = uiState.selectedVideoUri,
                    fileName = uiState.selectedFileName,
                    youtubeUrl = uiState.youtubeUrl,
                    onFilePickerClick = {
                        if (permissionState.status.isGranted) {
                            videoPickerLauncher.launch("video/*")
                        } else {
                            permissionState.launchPermissionRequest()
                        }
                    },
                    onYouTubeUrlChanged = viewModel::onYouTubeUrlChanged,
                    onClearSelection = {
                        viewModel.onVideoSelected(Uri.EMPTY, null)
                        viewModel.onYouTubeUrlChanged("")
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Duration Selector
                DurationSelector(
                    selectedDuration = uiState.selectedDuration,
                    onDurationSelected = viewModel::onDurationSelected
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Quantity Selector
                QuantitySelector(
                    quantity = uiState.selectedQuantity,
                    onQuantityChanged = viewModel::onQuantityChanged
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Language Selector
                LanguageSelector(
                    selectedLanguage = uiState.selectedLanguage,
                    onLanguageSelected = viewModel::onLanguageSelected
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Start Processing Button
                PrimaryButton(
                    text = when {
                        uiState.isUploading -> "Uploading..."
                        uiState.isProcessing -> "Starting..."
                        else -> "Generate Shorts ✨"
                    },
                    onClick = {
                        scope.launch {
                            val uploadSuccess = if (uiState.selectedVideoUri != null) {
                                viewModel.uploadVideoFile(context)
                            } else {
                                viewModel.uploadYouTubeUrl()
                            }
                            
                            if (uploadSuccess) {
                                viewModel.startProcessing()
                            }
                        }
                    },
                    enabled = viewModel.isReadyToUpload() && !uiState.isUploading && !uiState.isProcessing,
                    isLoading = uiState.isUploading || uiState.isProcessing,
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Default.AutoAwesome
                )

                // Error Message
                AnimatedVisibility(visible = uiState.error != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = ErrorRed
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = uiState.error ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ErrorRed
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

/**
 * Card for video source selection (file upload or YouTube URL).
 */
@Composable
private fun VideoSourceCard(
    selectedUri: Uri?,
    fileName: String?,
    youtubeUrl: String,
    onFilePickerClick: () -> Unit,
    onYouTubeUrlChanged: (String) -> Unit,
    onClearSelection: () -> Unit
) {
    Column {
        // File Upload Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceDarkElevated)
                .border(
                    width = 2.dp,
                    brush = if (selectedUri != null && selectedUri != Uri.EMPTY) {
                        Brush.linearGradient(listOf(SuccessGreen, SuccessGreen))
                    } else {
                        Brush.linearGradient(listOf(BorderDark, BorderLight))
                    },
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable(onClick = onFilePickerClick),
            contentAlignment = Alignment.Center
        ) {
            if (selectedUri != null && selectedUri != Uri.EMPTY) {
                // File selected state
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VideoFile,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = fileName ?: "Video selected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onClearSelection) {
                        Text("Change video", color = PrimaryBlue)
                    }
                }
            } else {
                // Empty state
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap to select video",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                    Text(
                        text = "MP4, MOV, AVI supported",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // OR Divider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = BorderDark
            )
            Text(
                text = "  OR  ",
                style = MaterialTheme.typography.labelMedium,
                color = TextTertiary
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = BorderDark
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // YouTube URL Input
        OutlinedTextField(
            value = youtubeUrl,
            onValueChange = onYouTubeUrlChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("Paste YouTube URL", color = TextTertiary)
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = null,
                    tint = if (youtubeUrl.isValidYouTubeUrl()) SuccessGreen else TextSecondary
                )
            },
            trailingIcon = {
                if (youtubeUrl.isNotEmpty()) {
                    IconButton(onClick = { onYouTubeUrlChanged("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = TextSecondary
                        )
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (youtubeUrl.isValidYouTubeUrl()) SuccessGreen else PrimaryBlue,
                unfocusedBorderColor = BorderDark,
                focusedContainerColor = SurfaceDarkElevated,
                unfocusedContainerColor = SurfaceDarkElevated,
                cursorColor = PrimaryBlue
            ),
            singleLine = true
        )

        // URL validation message
        AnimatedVisibility(visible = youtubeUrl.isNotEmpty() && !youtubeUrl.isValidYouTubeUrl()) {
            Text(
                text = "Please enter a valid YouTube URL",
                style = MaterialTheme.typography.labelSmall,
                color = WarningYellow,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}
