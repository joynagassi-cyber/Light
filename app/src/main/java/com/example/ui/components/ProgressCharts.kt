package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.StudyTask
import com.example.data.model.TaskCategory
import com.example.ui.theme.AppleDivider
import com.example.ui.theme.AppleTextPrimary
import com.example.ui.theme.AppleTextSecondary
import com.example.ui.theme.LocalLightGradient
import kotlin.math.roundToInt

@Composable
fun StudyTrendLineChart(
    dayLabels: List<String>,
    minutesPerDay: List<Int>,
    modifier: Modifier = Modifier
) {
    val gradient = LocalLightGradient.current
    val count = minOf(dayLabels.size, minutesPerDay.size)
    if (count == 0) return

    var selectedIndex by remember(count) { mutableIntStateOf(count - 1) }
    val safeSelected = selectedIndex.coerceIn(0, count - 1)
    val maxVal = (minutesPerDay.maxOrNull() ?: 60).coerceAtLeast(30)
    val selectedMinutes = minutesPerDay.getOrElse(safeSelected) { 0 }
    val selectedLabel = dayLabels.getOrElse(safeSelected) { "" }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = gradient.softContainer
            ) {
                Text(
                    text = "$selectedLabel · ${formatDurationCompact(selectedMinutes)}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = gradient.accentColor,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Text(
                text = "Pic : ${formatDurationCompact(minutesPerDay.maxOrNull() ?: 0)}",
                style = MaterialTheme.typography.labelSmall,
                color = AppleTextSecondary
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        val animatedPoints = minutesPerDay.take(count).mapIndexed { index, mins ->
            val ratio by animateFloatAsState(
                targetValue = mins.toFloat() / maxVal.toFloat(),
                animationSpec = spring(dampingRatio = 0.78f, stiffness = 220f),
                label = "trendPt_$index"
            )
            ratio
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(154.dp)
                .testTag("progress_trend_chart")
                .pointerInput(count) {
                    detectTapGestures { offset ->
                        if (count > 1 && size.width > 0) {
                            val pad = 16f
                            val usable = (size.width - pad * 2f).coerceAtLeast(1f)
                            val rel = ((offset.x - pad) / usable).coerceIn(0f, 1f)
                            selectedIndex = (rel * (count - 1)).roundToInt().coerceIn(0, count - 1)
                        }
                    }
                }
        ) {
            val hPad = 14.dp.toPx()
            val vPadTop = 14.dp.toPx()
            val vPadBottom = 16.dp.toPx()
            val usableW = (size.width - hPad * 2f).coerceAtLeast(1f)
            val usableH = (size.height - vPadTop - vPadBottom).coerceAtLeast(1f)

            // Subtle horizontal reference lines
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
            listOf(0.25f, 0.5f, 0.75f, 1f).forEach { frac ->
                val y = vPadTop + usableH * (1f - frac)
                drawLine(
                    color = AppleDivider.copy(alpha = 0.85f),
                    start = Offset(hPad, y),
                    end = Offset(size.width - hPad, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = dashEffect
                )
            }

            val stepX = if (count > 1) usableW / (count - 1) else 0f
            val points = animatedPoints.mapIndexed { idx, ratio ->
                val x = if (count > 1) hPad + idx * stepX else size.width / 2f
                val y = vPadTop + usableH * (1f - ratio.coerceIn(0f, 1f))
                Offset(x, y)
            }

            if (points.size >= 2) {
                val strokePath = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 0 until points.lastIndex) {
                        val p0 = points[i]
                        val p1 = points[i + 1]
                        val ctrlX = (p0.x + p1.x) / 2f
                        cubicTo(ctrlX, p0.y, ctrlX, p1.y, p1.x, p1.y)
                    }
                }

                val fillPath = Path().apply {
                    addPath(strokePath)
                    lineTo(points.last().x, vPadTop + usableH)
                    lineTo(points.first().x, vPadTop + usableH)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            gradient.accentColor.copy(alpha = 0.28f),
                            gradient.midColor.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        startY = vPadTop,
                        endY = vPadTop + usableH
                    )
                )

                drawPath(
                    path = strokePath,
                    brush = gradient.linearBrush(),
                    style = Stroke(
                        width = 3.2.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            points.forEachIndexed { idx, pt ->
                val isSelected = idx == safeSelected
                if (isSelected) {
                    drawLine(
                        color = gradient.accentColor.copy(alpha = 0.3f),
                        start = Offset(pt.x, pt.y),
                        end = Offset(pt.x, vPadTop + usableH),
                        strokeWidth = 1.5.dp.toPx()
                    )
                    drawCircle(
                        color = gradient.accentColor.copy(alpha = 0.2f),
                        radius = 10.dp.toPx(),
                        center = pt
                    )
                }
                drawCircle(
                    color = Color.White,
                    radius = if (isSelected) 5.5.dp.toPx() else 3.5.dp.toPx(),
                    center = pt
                )
                drawCircle(
                    color = gradient.accentColor,
                    radius = if (isSelected) 3.8.dp.toPx() else 2.2.dp.toPx(),
                    center = pt
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val step = if (count > 8) 3 else 1
            dayLabels.take(count).forEachIndexed { index, label ->
                val show = index == safeSelected || index == 0 || index == count - 1 || index % step == 0
                Text(
                    text = if (show) label else "",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (index == safeSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (index == safeSelected) gradient.accentColor else AppleTextSecondary,
                    modifier = Modifier.clickable { selectedIndex = index }
                )
            }
        }
    }
}

@Composable
fun CategoryDistributionDonutChart(
    tasks: List<StudyTask>,
    modifier: Modifier = Modifier
) {
    val total = tasks.size
    val counts = remember(tasks) {
        TaskCategory.entries.map { cat ->
            val catTasks = tasks.filter { TaskCategory.fromKey(it.category) == cat }
            Triple(cat, catTasks.size, catTasks.count { it.completed })
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("progress_category_donut"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Box(
            modifier = Modifier.size(134.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(134.dp)) {
                val strokePx = 18.dp.toPx()
                val arcSize = Size(size.width - strokePx, size.height - strokePx)
                val topLeft = Offset(strokePx / 2f, strokePx / 2f)

                if (total == 0) {
                    drawArc(
                        color = AppleDivider,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )
                } else {
                    var startAngle = -90f
                    val gapDegrees = 4f
                    counts.filter { it.second > 0 }.forEach { (category, count, _) ->
                        val rawSweep = 360f * (count.toFloat() / total.toFloat())
                        val sweep = (rawSweep - gapDegrees).coerceAtLeast(4f)
                        drawArc(
                            color = category.accentColor(),
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokePx, cap = StrokeCap.Round)
                        )
                        startAngle += rawSweep
                    }
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$total",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppleTextPrimary
                )
                Text(
                    text = if (total <= 1) "tâche" else "tâches",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppleTextSecondary
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            counts.forEach { (cat, count, done) ->
                val catColor = cat.accentColor()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(catColor)
                        )
                        Text(
                            text = cat.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = AppleTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (count > 0) "$done/$count" else "0",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (count > 0) catColor else AppleTextSecondary
                    )
                }
            }
        }
    }
}

private fun formatDurationCompact(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0) "${h}h${m.toString().padStart(2, '0')}" else "${m} min"
}
