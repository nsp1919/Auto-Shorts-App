package com.autoshorts.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autoshorts.app.ui.theme.*
import com.autoshorts.app.util.Constants

/**
 * Duration selector with animated chip selection.
 * Options: 30s, 60s, 90s, 120s
 */
@Composable
fun DurationSelector(
    selectedDuration: Int,
    onDurationSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Duration",
            style = MaterialTheme.typography.titleSmall,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(Constants.DURATION_OPTIONS) { duration ->
                DurationChip(
                    duration = duration,
                    isSelected = duration == selectedDuration,
                    onClick = { onDurationSelected(duration) }
                )
            }
        }
    }
}

@Composable
private fun DurationChip(
    duration: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryBlue else SurfaceDarkElevated,
        label = "duration_bg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else TextSecondary,
        label = "duration_text"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${duration}s",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}

/**
 * Quantity selector with plus/minus buttons.
 * Range: 1-10
 */
@Composable
fun QuantitySelector(
    quantity: Int,
    onQuantityChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Number of Clips",
            style = MaterialTheme.typography.titleSmall,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceDarkElevated)
                .padding(4.dp)
        ) {
            // Minus button
            IconButton(
                onClick = { 
                    if (quantity > Constants.MIN_QUANTITY) {
                        onQuantityChanged(quantity - 1)
                    }
                },
                enabled = quantity > Constants.MIN_QUANTITY
            ) {
                Text(
                    text = "−",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (quantity > Constants.MIN_QUANTITY) PrimaryBlue else TextDisabled
                )
            }
            
            // Quantity display
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = quantity.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            
            // Plus button
            IconButton(
                onClick = { 
                    if (quantity < Constants.MAX_QUANTITY) {
                        onQuantityChanged(quantity + 1)
                    }
                },
                enabled = quantity < Constants.MAX_QUANTITY
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (quantity < Constants.MAX_QUANTITY) PrimaryBlue else TextDisabled
                )
            }
        }
    }
}

/**
 * Language selector dropdown.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelector(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = Constants.LANGUAGE_OPTIONS.find { it.second == selectedLanguage }?.first ?: "English"

    Column(modifier = modifier) {
        Text(
            text = "Language",
            style = MaterialTheme.typography.titleSmall,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedLabel,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = BorderDark,
                    focusedContainerColor = SurfaceDarkElevated,
                    unfocusedContainerColor = SurfaceDarkElevated
                )
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(SurfaceDark)
            ) {
                Constants.LANGUAGE_OPTIONS.forEach { (label, code) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            onLanguageSelected(code)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
