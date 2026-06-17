package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WeightEntry
import java.util.Locale
import kotlin.math.abs

@Composable
fun WeightChart(
    entries: List<WeightEntry>,
    goalWeight: Double?,
    modifier: Modifier = Modifier,
    onEntrySelected: (WeightEntry?) -> Unit = {}
) {
    if (entries.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Trage Gewichtsdaten ein, um das Diagramm anzuzeigen.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    // Sort entries ascending by date to display chronologically
    val sortedEntries = remember(entries) {
        entries.sortedBy { it.dateString }
    }

    var selectedIndex by remember(entries) { mutableStateOf<Int?>(null) }
    
    // Notification callback for selection
    LaunchedEffect(selectedIndex) {
        if (selectedIndex != null && selectedIndex!! in sortedEntries.indices) {
            onEntrySelected(sortedEntries[selectedIndex!!])
        } else {
            onEntrySelected(null)
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(16.dp)
    ) {
        val density = LocalDensity.current
        val width = constraints.maxWidth.toFloat()
        val height = with(density) { 220.dp.toPx() }

        // Padding within Canvas
        val padLeft = with(density) { 45.dp.toPx() }
        val padRight = with(density) { 20.dp.toPx() }
        val padTop = with(density) { 25.dp.toPx() }
        val padBottom = with(density) { 30.dp.toPx() }

        val viewportWidth = width - padLeft - padRight
        val viewportHeight = height - padTop - padBottom

        // Compute weights range
        val weights = sortedEntries.map { it.weight }
        val minWeight = weights.minOrNull() ?: 60.0
        val maxWeight = weights.maxOrNull() ?: 80.0

        var minY = minWeight
        var maxY = maxWeight

        if (goalWeight != null) {
            minY = minOf(minY, goalWeight)
            maxY = maxOf(maxY, goalWeight)
        }

        val rawRange = maxY - minY
        if (rawRange < 1.0) {
            minY -= 2.0
            maxY += 2.0
        } else {
            val pad = rawRange * 0.15
            minY -= pad
            maxY += pad
        }

        val yRange = maxY - minY

        // Animation state for progress drawing
        var animationExecuted by remember { mutableStateOf(false) }
        val animProgress by animateFloatAsState(
            targetValue = if (animationExecuted) 1f else 0f,
            animationSpec = tween(durationMillis = 800),
            label = "chartReveal"
        )

        LaunchedEffect(entries) {
            animationExecuted = true
        }

        // Color Palette
        val primaryColor = MaterialTheme.colorScheme.primary
        val accentColor = MaterialTheme.colorScheme.tertiary
        val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        val onSurfaceColor = MaterialTheme.colorScheme.onSurface
        val goalLineColor = Color(0xFF10B981) // Beautiful emerald goal line

        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .pointerInput(sortedEntries) {
                        detectTapGestures { offset ->
                            val touchX = offset.x
                            // Find closest point based on touchX in viewport bounds
                            if (sortedEntries.size > 1 && touchX >= padLeft && touchX <= (width - padRight)) {
                                val fractionalX = (touchX - padLeft) / viewportWidth
                                val approximateIndex = (fractionalX * (sortedEntries.size - 1))
                                val closestIndex = approximateIndex.plus(0.5f).toInt()
                                    .coerceIn(0, sortedEntries.size - 1)
                                
                                // Calculate distance to make sure user clicked somewhat near
                                val ptX = padLeft + closestIndex * (viewportWidth / (sortedEntries.size - 1))
                                if (abs(touchX - ptX) < 120f) {
                                    selectedIndex = if (selectedIndex == closestIndex) null else closestIndex
                                } else {
                                    selectedIndex = null
                                }
                            } else if (sortedEntries.size == 1 && touchX >= (width / 2f - 100f) && touchX <= (width / 2f + 100f)) {
                                selectedIndex = if (selectedIndex == 0) null else 0
                            } else {
                                selectedIndex = null
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // 1. Draw horizontal grid lines and Y Axis labels
                    val horizontalLinesCount = 4
                    for (i in 0..horizontalLinesCount) {
                        val fraction = i.toFloat() / horizontalLinesCount
                        val yValue = maxY - (fraction * yRange)
                        val yPos = padTop + (fraction * viewportHeight)

                        // Grid line
                        drawLine(
                            color = gridColor,
                            start = Offset(padLeft, yPos),
                            end = Offset(width - padRight, yPos),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )

                        // Y Label
                        drawContext.canvas.nativeCanvas.drawText(
                            java.lang.String.format(Locale.getDefault(), "%.1f", yValue),
                            10f,
                            yPos + 4.dp.toPx(),
                            android.graphics.Paint().apply {
                                color = onSurfaceColor.copy(alpha = 0.6f).value.toInt()
                                textSize = 10.sp.toPx()
                                typeface = android.graphics.Typeface.DEFAULT
                            }
                        )
                    }

                    // 2. Draw goal weight target line (if present)
                    if (goalWeight != null) {
                        val goalY = padTop + (viewportHeight - (((goalWeight - minY) / yRange) * viewportHeight)).toFloat()
                        if (goalY in padTop..(height - padBottom)) {
                            // Dotted horizontal line
                            drawLine(
                                color = goalLineColor,
                                start = Offset(padLeft, goalY),
                                end = Offset(width - padRight, goalY),
                                strokeWidth = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
                            )
                            // Goal Tag
                            drawContext.canvas.nativeCanvas.drawText(
                                "Ziel: ${java.lang.String.format(Locale.getDefault(), "%.1f", goalWeight)} kg",
                                width - padRight - 80.dp.toPx(),
                                goalY - 4.dp.toPx(),
                                android.graphics.Paint().apply {
                                    color = goalLineColor.value.toInt()
                                    textSize = 10.sp.toPx()
                                    isFakeBoldText = true
                                }
                            )
                        }
                    }

                    // Map entry to point
                    fun getCoordinatesForIndex(index: Int): Offset {
                        val entry = sortedEntries[index]
                        val x = if (sortedEntries.size > 1) {
                            padLeft + index * (viewportWidth / (sortedEntries.size - 1))
                        } else {
                            padLeft + (viewportWidth / 2f)
                        }
                        // Animate y-coordinates scale from centerline for nice loading transition
                        val normalizedY = ((entry.weight - minY) / yRange).toFloat()
                        val animatedNormalizedY = 0.5f + (normalizedY - 0.5f) * animProgress
                        val y = padTop + (viewportHeight - (animatedNormalizedY * viewportHeight))
                        return Offset(x, y)
                    }

                    // Prepare Points
                    val points = List(sortedEntries.size) { getCoordinatesForIndex(it) }

                    // 3. Draw Gradient Under-curve shading
                    if (points.size > 1 && animProgress > 0.05f) {
                        val areaPath = Path().apply {
                            moveTo(points.first().x, height - padBottom)
                            for (point in points) {
                                lineTo(point.x, point.y)
                            }
                            lineTo(points.last().x, height - padBottom)
                            close()
                        }
                        drawPath(
                            path = areaPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = 0.35f * animProgress),
                                    Color.Transparent
                                ),
                                startY = padTop,
                                endY = height - padBottom
                            )
                        )
                    }

                    // 4. Draw connection line
                    if (points.size > 1) {
                        val linePath = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            for (i in 1 until points.size) {
                                lineTo(points[i].x, points[i].y)
                            }
                        }
                        drawPath(
                            path = linePath,
                            color = primaryColor,
                            style = Stroke(
                                width = 3.dp.toPx(),
                                pathEffect = PathEffect.cornerPathEffect(15f)
                            )
                        )
                    }

                    // 5. Draw data points and interactive selection vertical rule
                    points.forEachIndexed { i, point ->
                        val isSelected = selectedIndex == i

                        // Draw Point
                        drawCircle(
                            color = if (isSelected) accentColor else primaryColor,
                            radius = if (isSelected) 7.dp.toPx() else 4.dp.toPx(),
                            center = point
                        )
                        drawCircle(
                            color = Color.White,
                            radius = if (isSelected) 4.dp.toPx() else 2.dp.toPx(),
                            center = point
                        )

                        // If selected, draw indicator vertical dotted helper
                        if (isSelected) {
                            drawLine(
                                color = accentColor,
                                start = Offset(point.x, padTop),
                                end = Offset(point.x, height - padBottom),
                                strokeWidth = 1.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                            )
                        }

                        // 6. Draw X Axis Text labels (only if space permits, e.g., first, middle, last points)
                        val shouldDrawLabel = sortedEntries.size <= 5 || 
                            i == 0 || 
                            i == sortedEntries.size - 1 || 
                            (sortedEntries.size > 2 && i == sortedEntries.size / 2)

                        if (shouldDrawLabel) {
                            val rawDate = sortedEntries[i].dateString
                            val displayDate = try {
                                // Formatiere YYYY-MM-DD zu DD.MM.
                                val parts = rawDate.split("-")
                                if (parts.size == 3) "${parts[2]}.${parts[1]}" else rawDate
                            } catch (e: Exception) {
                                rawDate
                            }

                            drawContext.canvas.nativeCanvas.drawText(
                                displayDate,
                                point.x - 12.dp.toPx(),
                                height - 8.dp.toPx(),
                                android.graphics.Paint().apply {
                                    color = onSurfaceColor.copy(alpha = 0.6f).value.toInt()
                                    textSize = 10.sp.toPx()
                                }
                            )
                        }
                    }
                }
            }

            // Show details card of selected point below the graph
            selectedIndex?.let { index ->
                if (index in sortedEntries.indices) {
                    val entry = sortedEntries[index]
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Ausgewählt: ${formatGermanDate(entry.dateString)}",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Gewicht: ${entry.weight} kg",
                                style = MaterialTheme.typography.titleMedium
                            )
                            if (entry.note.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Notiz: ${entry.note}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Hilfsfunktion zur deutschen Datumsdarstellung
fun formatGermanDate(dateString: String): String {
    return try {
        val parts = dateString.split("-")
        if (parts.size == 3) {
            val monthsGerman = listOf(
                "Januar", "Februar", "März", "April", "Mai", "Juni",
                "Juli", "August", "September", "Oktober", "November", "Dezember"
            )
            val monthIndex = parts[1].toInt() - 1
            val monthStr = if (monthIndex in 0..11) monthsGerman[monthIndex] else parts[1]
            "${parts[2]}. $monthStr ${parts[0]}"
        } else {
            dateString
        }
    } catch (e: Exception) {
        dateString
    }
}
