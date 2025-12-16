package com.autoshorts.app.ui.screens.rocket

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.autoshorts.app.ui.components.*
import com.autoshorts.app.ui.theme.*
import com.autoshorts.app.util.copyToClipboard
import com.autoshorts.app.util.shareText

/**
 * Rocket Share Screen for AI-generated metadata and one-click sharing.
 * Features editable title, description, and hashtags with copy/share actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RocketShareScreen(
    clipId: String,
    videoUrl: String,
    onNavigateBack: () -> Unit,
    viewModel: RocketShareViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Initialize with clip info
    LaunchedEffect(clipId, videoUrl) {
        viewModel.initialize(clipId, videoUrl)
    }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Rocket Share")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("🚀", style = MaterialTheme.typography.titleLarge)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::regenerateMetadata) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Regenerate"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary,
                    actionIconContentColor = TextPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                // Loading state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = PrimaryBlue)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "AI is crafting your content...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(24.dp)
                ) {
                    // Title Section
                    MetadataSection(
                        label = "Title",
                        value = uiState.title,
                        onValueChange = viewModel::onTitleChanged,
                        isEditing = uiState.isEditingTitle,
                        onEditToggle = viewModel::toggleTitleEditing,
                        onCopy = { context.copyToClipboard(uiState.title, "Title") }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Description Section
                    MetadataSection(
                        label = "Description",
                        value = uiState.description,
                        onValueChange = viewModel::onDescriptionChanged,
                        isEditing = uiState.isEditingDescription,
                        onEditToggle = viewModel::toggleDescriptionEditing,
                        onCopy = { context.copyToClipboard(uiState.description, "Description") },
                        multiLine = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Hashtags Section
                    HashtagsSection(
                        hashtags = uiState.hashtags,
                        onRemoveHashtag = viewModel::removeHashtag,
                        onAddHashtag = viewModel::addHashtag,
                        onCopyAll = { 
                            context.copyToClipboard(viewModel.getFormattedHashtags(), "Hashtags")
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Suggested posting time
                    uiState.suggestedTime?.let { time ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDarkElevated),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = PrimaryBlue
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Best time to post",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = time,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Copy All button
                    SecondaryButton(
                        text = "Copy All",
                        onClick = { 
                            context.copyToClipboard(viewModel.getFullShareText(), "Content")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Default.ContentCopy
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Share button
                    PrimaryButton(
                        text = "Share Now",
                        onClick = { context.shareText(viewModel.getFullShareText()) },
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Default.Share
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Platform buttons
                    Text(
                        text = "Share directly to",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PlatformButton(
                            name = "Instagram",
                            emoji = "📸",
                            onClick = { viewModel.shareToplatform("instagram") },
                            modifier = Modifier.weight(1f)
                        )
                        PlatformButton(
                            name = "YouTube",
                            emoji = "▶️",
                            onClick = { viewModel.shareToplatform("youtube") },
                            modifier = Modifier.weight(1f)
                        )
                        PlatformButton(
                            name = "TikTok",
                            emoji = "🎵",
                            onClick = { viewModel.shareToplatform("tiktok") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            // Messages
            AnimatedVisibility(
                visible = uiState.successMessage != null || uiState.error != null,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.error != null) 
                            ErrorRed.copy(alpha = 0.9f) 
                        else 
                            SuccessGreen.copy(alpha = 0.9f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (uiState.error != null) 
                                Icons.Default.Error 
                            else 
                                Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = uiState.error ?: uiState.successMessage ?: "",
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Editable metadata section with copy button.
 */
@Composable
private fun MetadataSection(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isEditing: Boolean,
    onEditToggle: (Boolean) -> Unit,
    onCopy: () -> Unit,
    multiLine: Boolean = false
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = TextSecondary
            )
            Row {
                IconButton(onClick = onCopy) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = { onEditToggle(!isEditing) }) {
                    Icon(
                        imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                        contentDescription = if (isEditing) "Done" else "Edit",
                        tint = if (isEditing) SuccessGreen else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        if (isEditing) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                minLines = if (multiLine) 3 else 1,
                maxLines = if (multiLine) 6 else 2,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = BorderDark,
                    focusedContainerColor = SurfaceDarkElevated,
                    unfocusedContainerColor = SurfaceDarkElevated
                )
            )
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDarkElevated),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = value,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
            }
        }
    }
}

/**
 * Hashtags section with add/remove functionality.
 */
@Composable
private fun HashtagsSection(
    hashtags: List<String>,
    onRemoveHashtag: (String) -> Unit,
    onAddHashtag: (String) -> Unit,
    onCopyAll: () -> Unit
) {
    var newHashtag by remember { mutableStateOf("") }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hashtags",
                style = MaterialTheme.typography.titleSmall,
                color = TextSecondary
            )
            IconButton(onClick = onCopyAll) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy all",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Hashtags chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(hashtags) { hashtag ->
                HashtagChip(
                    hashtag = hashtag,
                    onRemove = { onRemoveHashtag(hashtag) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Add hashtag input
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = newHashtag,
                onValueChange = { newHashtag = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Add hashtag", color = TextTertiary) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = BorderDark,
                    focusedContainerColor = SurfaceDarkElevated,
                    unfocusedContainerColor = SurfaceDarkElevated
                )
            )
            IconButton(
                onClick = {
                    if (newHashtag.isNotBlank()) {
                        onAddHashtag(newHashtag)
                        newHashtag = ""
                    }
                },
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryBlue)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = Color.White
                )
            }
        }
    }
}

/**
 * Individual hashtag chip with remove button.
 */
@Composable
private fun HashtagChip(
    hashtag: String,
    onRemove: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#$hashtag",
                style = MaterialTheme.typography.labelMedium,
                color = PrimaryBlue
            )
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Platform share button.
 */
@Composable
private fun PlatformButton(
    name: String,
    emoji: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceDarkElevated),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}
