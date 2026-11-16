package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.TaskCategory
import com.example.ui.theme.AppleAmber
import com.example.ui.theme.AppleCardSurface
import com.example.ui.theme.AppleDivider
import com.example.ui.theme.AppleEmerald
import com.example.ui.theme.AppleIndigo
import com.example.ui.theme.AppleRose
import com.example.ui.theme.AppleSky
import com.example.ui.theme.AppleTextPrimary
import com.example.ui.theme.AppleTextSecondary
import com.example.ui.theme.LightGradientTheme
import com.example.ui.theme.LightGradients
import com.example.ui.theme.LocalLightGradient

@Composable
fun AppleCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    cornerRadius: Dp = 22.dp,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Surface(
        modifier = modifier
            .clip(shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = shape,
        color = AppleCardSurface.copy(alpha = 0.90f),
        tonalElevation = 0.dp,
        shadowElevation = 2.dp,
        border = BorderStroke(0.9.dp, Color.White.copy(alpha = 0.92f))
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

@Composable
fun GradientHeroCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 26.dp,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val gradient = LocalLightGradient.current
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .clip(shape)
            .background(gradient.linearBrush())
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.14f),
                radius = size.minDimension * 0.48f,
                center = Offset(size.width * 0.88f, size.height * 0.18f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.08f),
                radius = size.minDimension * 0.35f,
                center = Offset(size.width * 0.12f, size.height * 0.92f)
            )
        }
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

@Composable
fun SyncStatusPill(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "syncPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = Color.White,
        border = BorderStroke(0.8.dp, AppleDivider),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(AppleEmerald)
            )
            Icon(
                imageVector = Icons.Rounded.CloudDone,
                contentDescription = "Synchronisation cloud active",
                tint = AppleTextSecondary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "Cloud",
                style = MaterialTheme.typography.labelSmall,
                color = AppleTextSecondary
            )
        }
    }
}

@Composable
fun GradientProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 11.dp,
    trackColor: Color = Color(0xFFEAECEF),
    centerContent: @Composable () -> Unit = {}
) {
    val gradient = LocalLightGradient.current
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = 0.78f,
            stiffness = 180f
        ),
        label = "ringProgress"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val arcSize = Size(size.width - strokePx, size.height - strokePx)
            val topLeft = Offset(strokePx / 2f, strokePx / 2f)

            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            if (animatedProgress > 0.005f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            gradient.startColor,
                            gradient.midColor,
                            gradient.endColor,
                            gradient.startColor
                        )
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }
        centerContent()
    }
}

@Composable
fun WeeklyStudyBarChart(
    dayLabels: List<String>,
    minutesPerDay: List<Int>,
    activeIndex: Int,
    modifier: Modifier = Modifier
) {
    val gradient = LocalLightGradient.current
    val maxMinutes = (minutesPerDay.maxOrNull() ?: 60).coerceAtLeast(30)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        dayLabels.forEachIndexed { index, label ->
            val mins = minutesPerDay.getOrElse(index) { 0 }
            val ratio = (mins.toFloat() / maxMinutes.toFloat()).coerceIn(0.08f, 1f)
            val animatedRatio by animateFloatAsState(
                targetValue = if (mins == 0) 0.06f else ratio,
                animationSpec = spring(dampingRatio = 0.75f, stiffness = 240f),
                label = "barHeight_$index"
            )
            val isToday = index == activeIndex
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (mins > 0) "${mins}m" else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isToday) gradient.accentColor else AppleTextSecondary,
                    maxLines = 1
                )
                Box(
                    modifier = Modifier
                        .height(110.dp)
                        .width(22.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((110f * animatedRatio).dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isToday || mins >= maxMinutes * 0.75f) {
                                    gradient.verticalBrush()
                                } else {
                                    Brush.verticalGradient(
                                        listOf(
                                            gradient.midColor.copy(alpha = 0.28f),
                                            gradient.startColor.copy(alpha = 0.18f)
                                        )
                                    )
                                }
                            )
                    )
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                    color = if (isToday) AppleTextPrimary else AppleTextSecondary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradientPickerSheet(
    selectedGradientId: String,
    onSelectGradient: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppleCardSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(LocalLightGradient.current.linearBrush()),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Palette,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "Studio Dégradés",
                        style = MaterialTheme.typography.headlineSmall,
                        color = AppleTextPrimary
                    )
                    Text(
                        text = "8 atmosphères fluides synchronisées avec votre compte",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppleTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(410.dp)
            ) {
                items(LightGradients.all, key = { it.id }) { preset ->
                    GradientSwatchCard(
                        preset = preset,
                        isSelected = preset.id == selectedGradientId,
                        onClick = { onSelectGradient(preset.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun GradientSwatchCard(
    preset: LightGradientTheme,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) preset.accentColor else AppleDivider,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "swatchBorder"
    )
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
        label = "swatchScale"
    )

    Surface(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("gradient_option_${preset.id}"),
        shape = RoundedCornerShape(20.dp),
        color = AppleCardSurface,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        shadowElevation = if (isSelected) 4.dp else 1.dp
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(preset.linearBrush()),
                contentAlignment = Alignment.TopEnd
            ) {
                this@Column.AnimatedVisibility(visible = isSelected) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Thème sélectionné",
                            tint = preset.accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = preset.name,
                style = MaterialTheme.typography.titleSmall,
                color = AppleTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = preset.subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = AppleTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

fun TaskCategory.accentColor(): Color = when (this) {
    TaskCategory.EXAM -> AppleRose
    TaskCategory.PROJECT -> AppleIndigo
    TaskCategory.ASSIGNMENT -> AppleSky
    TaskCategory.REVISION -> AppleEmerald
    TaskCategory.READING -> AppleAmber
}

fun TaskCategory.icon(): ImageVector = when (this) {
    TaskCategory.EXAM -> Icons.Rounded.School
    TaskCategory.PROJECT -> Icons.Rounded.Code
    TaskCategory.ASSIGNMENT -> Icons.AutoMirrored.Rounded.Assignment
    TaskCategory.REVISION -> Icons.Rounded.Psychology
    TaskCategory.READING -> Icons.AutoMirrored.Rounded.MenuBook
}

@Composable
fun TaskCategoryPill(
    category: TaskCategory,
    modifier: Modifier = Modifier,
    showIcon: Boolean = true
) {
    val color = category.accentColor()
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = color.copy(alpha = 0.13f),
        border = BorderStroke(0.8.dp, color.copy(alpha = 0.28f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (showIcon) {
                Icon(
                    imageVector = category.icon(),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(12.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
            Text(
                text = category.label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = color,
                maxLines = 1
            )
        }
    }
}
