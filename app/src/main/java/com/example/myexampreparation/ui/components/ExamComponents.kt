package com.example.myexampreparation.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// =============================================================================
// 1. EXAM CARD (Unified 16.dp radius, 1.dp outline)
// =============================================================================
@Composable
fun ExamCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    content: @Composable () -> Unit
) {
    val cardModifier = if (onClick != null) {
        modifier
            .fillMaxWidth()
            .clickable { onClick() }
    } else {
        modifier.fillMaxWidth()
    }

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            content()
        }
    }
}

// =============================================================================
// 2. EXAM PROGRESS BAR (Unified 8.dp height, rounded cap)
// =============================================================================
@Composable
fun ExamProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.primaryContainer
) {
    LinearProgressIndicator(
        progress = { progress.coerceIn(0f, 1f) },
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp),
        color = color,
        trackColor = trackColor
    )
}

// =============================================================================
// 3. BUTTON STANDARDS (ExamButton: 48.dp, CompactActionButton: 36.dp)
// =============================================================================
@Composable
fun ExamButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isOutlined: Boolean = false
) {
    if (isOutlined) {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = enabled,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    } else {
        Button(
            onClick = onClick,
            modifier = modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = enabled,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun ExamPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isOutlined: Boolean = false
) {
    ExamButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        isOutlined = isOutlined
    )
}

@Composable
fun CompactActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isOutlined: Boolean = true
) {
    if (isOutlined) {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(36.dp),
            enabled = enabled,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )
        }
    } else {
        Button(
            onClick = onClick,
            modifier = modifier.height(36.dp),
            enabled = enabled,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun ExamCompactButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isOutlined: Boolean = true
) {
    CompactActionButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        isOutlined = isOutlined
    )
}

// =============================================================================
// 4. EXAM TOP APP BAR (Consistent Scaffold header with 48.dp back touch target)
// =============================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamTopAppBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    backContentDescription: String = "Back",
    actions: @Composable () -> Unit = {}
) {
    TopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        ),
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = backContentDescription,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        actions = { actions() }
    )
}

// =============================================================================
// 5. QUIZ OPTION CARD (Default, Selected, Correct, Wrong States)
// =============================================================================
enum class OptionState {
    DEFAULT,
    SELECTED,
    CORRECT,
    WRONG
}

@Composable
fun QuizOptionCard(
    optionKey: String,
    optionText: String,
    state: OptionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val borderStroke = when (state) {
        OptionState.DEFAULT -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        OptionState.SELECTED -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        OptionState.CORRECT -> BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
        OptionState.WRONG -> BorderStroke(2.dp, MaterialTheme.colorScheme.error)
    }

    val containerColor = when (state) {
        OptionState.DEFAULT -> MaterialTheme.colorScheme.surface
        OptionState.SELECTED -> MaterialTheme.colorScheme.primaryContainer
        OptionState.CORRECT -> MaterialTheme.colorScheme.secondaryContainer
        OptionState.WRONG -> MaterialTheme.colorScheme.errorContainer
    }

    val textColor = when (state) {
        OptionState.DEFAULT -> MaterialTheme.colorScheme.onSurface
        OptionState.SELECTED -> MaterialTheme.colorScheme.onPrimaryContainer
        OptionState.CORRECT -> MaterialTheme.colorScheme.onSecondaryContainer
        OptionState.WRONG -> MaterialTheme.colorScheme.onErrorContainer
    }

    val scale by animateFloatAsState(
        targetValue = if (state == OptionState.SELECTED || state == OptionState.CORRECT) 1.01f else 1.0f,
        animationSpec = tween(200),
        label = "optionScale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = borderStroke
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$optionKey.",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = textColor
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = optionText,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
