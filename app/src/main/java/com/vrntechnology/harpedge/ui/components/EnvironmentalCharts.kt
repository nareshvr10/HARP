package com.vrntechnology.harpedge.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vrntechnology.harpedge.data.model.*
import com.vrntechnology.harpedge.data.repository.EnvironmentalDataService
import com.vrntechnology.harpedge.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Interactive Time-Series Environmental Line & Area Chart.
 * Features: Smooth cubic Bézier interpolation, gradient fill, grid axes,
 * threshold warnings, anomaly callouts, and interactive touch scrubbing.
 */
@Composable
fun EnvironmentalLineAreaChart(
    points: List<EnvironmentalDataPoint>,
    metric: EnvironmentalMetric,
    modifier: Modifier = Modifier,
    lineColor: Color = HarpTeal,
    showThreshold: Boolean = true
) {
    if (points.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(HarpNavyDark.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("No telemetry points available", color = HarpTextMuted, fontSize = 12.sp)
        }
        return
    }

    var selectedIndex by remember(points, metric) { mutableStateOf<Int?>(null) }
    val textMeasurer = rememberTextMeasurer()

    val rawValues = points.map { EnvironmentalDataService.getMetricValue(it, metric) }
    val minVal = (rawValues.minOrNull() ?: 0f).coerceAtLeast(0f)
    val maxVal = rawValues.maxOrNull() ?: 100f
    val paddingSpan = ((maxVal - minVal) * 0.15f).coerceAtLeast(1.5f)
    val yMin = (minVal - paddingSpan).coerceAtLeast(0f)
    val yMax = maxVal + paddingSpan
    val yRange = if (yMax - yMin > 0.001f) yMax - yMin else 1f

    val avgVal = rawValues.average().toFloat()

    // Smooth animation on load/metric switch
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 650),
        label = "chartProgress"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        // Scrubber Tooltip Banner if user is touching/dragging
        val activePoint = selectedIndex?.let { points.getOrNull(it) }
        val activeValue = activePoint?.let { EnvironmentalDataService.getMetricValue(it, metric) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (activePoint != null && activeValue != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (activePoint.isAnomaly) RiskHigh else lineColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${activePoint.timeLabel} — ",
                        fontSize = 12.sp,
                        color = HarpTextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "$activeValue ${metric.unit}",
                        fontSize = 13.sp,
                        color = if (activePoint.isAnomaly) RiskHigh else HarpTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    if (activePoint.isAnomaly) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = RiskHigh.copy(alpha = 0.2f),
                            border = BorderStroke(0.8.dp, RiskHigh)
                        ) {
                            Text(
                                text = activePoint.anomalyNote ?: "ANOMALY",
                                fontSize = 9.sp,
                                color = RiskHigh,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Touch/drag chart to inspect values",
                        fontSize = 11.sp,
                        color = HarpTextMuted
                    )
                }
            }

            // Quick High / Low Indicators
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "L: ${(minVal * 10).toInt() / 10f}",
                    fontSize = 11.sp,
                    color = HarpCyan,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "AVG: ${(avgVal * 10).toInt() / 10f}",
                    fontSize = 11.sp,
                    color = HarpTextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "H: ${(maxVal * 10).toInt() / 10f}",
                    fontSize = 11.sp,
                    color = RiskHigh,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Canvas Drawing
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(HarpNavyDark)
                .pointerInput(points, metric) {
                    detectTapGestures { offset ->
                        val n = points.size
                        if (n > 1) {
                            val w = size.width
                            val stepX = w / (n - 1)
                            val idx = (offset.x / stepX)
                                .toInt()
                                .coerceIn(0, n - 1)
                            selectedIndex = idx
                        }
                    }
                }
                .pointerInput(points, metric) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            val n = points.size
                            if (n > 1) {
                                val w = size.width
                                val stepX = w / (n - 1)
                                val idx = (offset.x / stepX)
                                    .toInt()
                                    .coerceIn(0, n - 1)
                                selectedIndex = idx
                            }
                        },
                        onHorizontalDrag = { change, _ ->
                            val n = points.size
                            if (n > 1) {
                                val w = size.width
                                val stepX = w / (n - 1)
                                val idx = (change.position.x / stepX)
                                    .toInt()
                                    .coerceIn(0, n - 1)
                                selectedIndex = idx
                            }
                        },
                        onDragEnd = {
                            // keep selected or auto-dismiss
                        }
                    )
                }
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 10.dp)
                    .testTag("chart_canvas_${metric.name.lowercase()}")
            ) {
                val chartWidth = size.width
                val chartHeight = size.height
                val pointCount = points.size

                if (pointCount < 2 || chartWidth <= 20f || chartHeight <= 20f) return@Canvas

                // 1. Grid lines (3 horizontal levels)
                val gridLevels = 3
                for (g in 0..gridLevels) {
                    val y = ((chartHeight / gridLevels) * g).coerceIn(0f, chartHeight)
                    val gridValue = yMax - (g.toFloat() / gridLevels) * (yMax - yMin)

                    drawLine(
                        color = HarpBorderColor.copy(alpha = 0.35f),
                        start = Offset(0f, y),
                        end = Offset(chartWidth, y),
                        strokeWidth = 0.8.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                    )

                    // Draw grid label safely bounded inside canvas
                    val labelY = (y - 12.dp.toPx()).coerceIn(0f, (chartHeight - 12.dp.toPx()).coerceAtLeast(0f))
                    drawText(
                        textMeasurer = textMeasurer,
                        text = "${(gridValue * 10).toInt() / 10f}",
                        topLeft = Offset(4f, labelY),
                        style = TextStyle(
                            color = HarpTextMuted.copy(alpha = 0.7f),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Normal
                        )
                    )
                }

                // 2. Threshold Guide Line (Danger threshold)
                if (showThreshold && metric.thresholdWarning in yMin..yMax) {
                    val threshY = (chartHeight - ((metric.thresholdWarning - yMin) / yRange) * chartHeight).coerceIn(0f, chartHeight)
                    drawLine(
                        color = RiskHigh.copy(alpha = 0.6f),
                        start = Offset(0f, threshY),
                        end = Offset(chartWidth, threshY),
                        strokeWidth = 1.2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f))
                    )

                    val threshLabelY = (threshY - 12.dp.toPx()).coerceIn(0f, (chartHeight - 12.dp.toPx()).coerceAtLeast(0f))
                    val threshLabelX = (chartWidth - 90.dp.toPx()).coerceIn(0f, (chartWidth - 10.dp.toPx()).coerceAtLeast(0f))
                    drawText(
                        textMeasurer = textMeasurer,
                        text = "Threshold: ${metric.thresholdWarning.toInt()}",
                        topLeft = Offset(threshLabelX, threshLabelY),
                        style = TextStyle(
                            color = RiskHigh,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // Compute screen coordinates for each data point
                val coords = mutableListOf<Offset>()
                val stepX = chartWidth / (pointCount - 1)

                for (i in 0 until pointCount) {
                    val valY = EnvironmentalDataService.getMetricValue(points[i], metric)
                    val x = i * stepX
                    val normalizedY = ((valY - yMin) / yRange).coerceIn(0f, 1f)
                    val y = chartHeight - (normalizedY * chartHeight * animatedProgress)
                    coords.add(Offset(x, y))
                }

                // 3. Build smooth cubic Bézier curve
                val linePath = Path()
                val fillPath = Path()

                linePath.moveTo(coords[0].x, coords[0].y)
                fillPath.moveTo(coords[0].x, chartHeight)
                fillPath.lineTo(coords[0].x, coords[0].y)

                for (i in 1 until coords.size) {
                    val prev = coords[i - 1]
                    val curr = coords[i]
                    val controlX1 = prev.x + (curr.x - prev.x) / 2f
                    val controlY1 = prev.y
                    val controlX2 = prev.x + (curr.x - prev.x) / 2f
                    val controlY2 = curr.y

                    linePath.cubicTo(controlX1, controlY1, controlX2, controlY2, curr.x, curr.y)
                    fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, curr.x, curr.y)
                }

                fillPath.lineTo(coords.last().x, chartHeight)
                fillPath.close()

                // Draw gradient area under the curve
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            lineColor.copy(alpha = 0.38f),
                            lineColor.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = chartHeight
                    )
                )

                // Draw line curve
                drawPath(
                    path = linePath,
                    color = lineColor,
                    style = Stroke(
                        width = 2.4.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // 4. Anomaly indicator pulses along the curve
                for (i in 0 until pointCount) {
                    if (points[i].isAnomaly) {
                        val pt = coords[i]
                        // Outer pulse ring
                        drawCircle(
                            color = RiskHigh.copy(alpha = 0.3f),
                            radius = 9.dp.toPx(),
                            center = pt
                        )
                        // Inner solid alert core
                        drawCircle(
                            color = RiskHigh,
                            radius = 4.dp.toPx(),
                            center = pt
                        )
                    }
                }

                // 5. Interactive Scrubber indicator if selected
                selectedIndex?.let { selIdx ->
                    if (selIdx in coords.indices) {
                        val selectedCoord = coords[selIdx]

                        // Vertical dashed hairline
                        drawLine(
                            color = HarpTeal.copy(alpha = 0.75f),
                            start = Offset(selectedCoord.x, 0f),
                            end = Offset(selectedCoord.x, chartHeight),
                            strokeWidth = 1.2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                        )

                        // Highlight beacon circle
                        drawCircle(
                            color = HarpNavyDark,
                            radius = 6.dp.toPx(),
                            center = selectedCoord
                        )
                        drawCircle(
                            color = lineColor,
                            radius = 4.5.dp.toPx(),
                            center = selectedCoord
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Time labels along bottom axis
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val step = (points.size / 4).coerceAtLeast(1)
            for (i in points.indices step step) {
                Text(
                    text = points[i].timeLabel,
                    fontSize = 9.sp,
                    color = HarpTextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Circular / 240-degree Air Quality Index (AQI) Radial Gauge
 */
@Composable
fun AirQualityRadialGauge(
    summary: AirQualitySummary,
    modifier: Modifier = Modifier
) {
    val animatedScore by animateFloatAsState(
        targetValue = summary.aqiScore.toFloat(),
        animationSpec = tween(durationMillis = 800),
        label = "aqiProgress"
    )

    Card(
        modifier = modifier.testTag("card_aqi_gauge"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = HarpNavyElevated),
        border = BorderStroke(1.dp, Color(summary.categoryColorHex).copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Air,
                        contentDescription = null,
                        tint = Color(summary.categoryColorHex),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AIR QUALITY INDEX",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = HarpTextSecondary,
                        letterSpacing = 0.8.sp
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(summary.categoryColorHex).copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, Color(summary.categoryColorHex).copy(alpha = 0.4f))
                ) {
                    Text(
                        text = summary.categoryName.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(summary.categoryColorHex),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Gauge Arc Canvas
            Box(
                modifier = Modifier
                    .size(170.dp, 120.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 14.dp.toPx()
                    val arcSize = Size(size.width - strokeWidth, (size.height * 2) - strokeWidth)
                    val arcTopLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                    val startAngle = 150f
                    val sweepAngle = 240f

                    // Background Track Arc
                    drawArc(
                        color = HarpNavySurface,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Color Segments gradient
                    val aqiRatio = (animatedScore / 500f).coerceIn(0.02f, 1f)
                    val activeSweep = sweepAngle * aqiRatio

                    val gaugeBrush = Brush.sweepGradient(
                        0.0f to RiskLow,
                        0.25f to RiskMedium,
                        0.5f to RiskHigh,
                        0.8f to RiskCritical,
                        1.0f to Color(0xFF7F1D1D)
                    )

                    drawArc(
                        brush = gaugeBrush,
                        startAngle = startAngle,
                        sweepAngle = activeSweep,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Small indicator beacon on tip
                    val currentAngleRad = Math.toRadians((startAngle + activeSweep).toDouble())
                    val radiusX = (arcSize.width / 2)
                    val radiusY = (arcSize.height / 2)
                    val centerX = arcTopLeft.x + radiusX
                    val centerY = arcTopLeft.y + radiusY
                    val beaconX = centerX + (radiusX * cos(currentAngleRad)).toFloat()
                    val beaconY = centerY + (radiusY * sin(currentAngleRad)).toFloat()

                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = Offset(beaconX, beaconY)
                    )
                }

                // Center Score Display
                Column(
                    modifier = Modifier.offset(y = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${animatedScore.toInt()}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(summary.categoryColorHex),
                        lineHeight = 36.sp
                    )
                    Text(
                        text = "AQI US",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HarpTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Primary Pollutant: ${summary.dominantPollutant}",
                    fontSize = 11.sp,
                    color = HarpTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "EPA Scale 0-500",
                    fontSize = 10.sp,
                    color = HarpTextMuted
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = summary.healthAdvice,
                fontSize = 11.sp,
                color = HarpTextSecondary,
                lineHeight = 15.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Environmental Bar Chart: Visualizes pollutant concentration or hourly variations.
 */
@Composable
fun EnvironmentalBarChart(
    points: List<EnvironmentalDataPoint>,
    metric: EnvironmentalMetric,
    modifier: Modifier = Modifier,
    barColor: Color = HarpTeal
) {
    if (points.isEmpty()) return

    val rawValues = points.map { EnvironmentalDataService.getMetricValue(it, metric) }
    val maxVal = (rawValues.maxOrNull() ?: 100f).coerceAtLeast(10f)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = HarpNavyElevated),
        border = BorderStroke(1.dp, HarpBorderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = barColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${metric.displayName.uppercase()} DISTRIBUTION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = HarpTextSecondary,
                        letterSpacing = 0.8.sp
                    )
                }

                Text(
                    text = "Max: ${(maxVal * 10).toInt() / 10f} ${metric.unit}",
                    fontSize = 11.sp,
                    color = HarpTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bar Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 20.dp)
                ) {
                    val count = points.size
                    val totalWidth = size.width
                    val totalHeight = size.height
                    val barWidth = (totalWidth / count) * 0.65f
                    val slotWidth = totalWidth / count

                    for (i in 0 until count) {
                        val pt = points[i]
                        val value = EnvironmentalDataService.getMetricValue(pt, metric)
                        val barHeight = ((value / maxVal) * totalHeight).coerceIn(4.dp.toPx(), totalHeight)
                        val x = (i * slotWidth) + (slotWidth - barWidth) / 2f
                        val y = totalHeight - barHeight

                        val isExceeding = value >= metric.thresholdWarning
                        val color = if (pt.isAnomaly || value >= metric.thresholdDanger) RiskHigh
                        else if (isExceeding) RiskMedium
                        else barColor

                        drawRoundRect(
                            color = color,
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }

                    // Baseline
                    drawLine(
                        color = HarpBorderColor,
                        start = Offset(0f, totalHeight),
                        end = Offset(totalWidth, totalHeight),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }

            // Time axis labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val step = (points.size / 4).coerceAtLeast(1)
                for (i in points.indices step step) {
                    Text(
                        text = points[i].timeLabel,
                        fontSize = 9.sp,
                        color = HarpTextMuted
                    )
                }
            }
        }
    }
}

/**
 * Responsive Environmental Metric Card with Mini Sparkline
 */
@Composable
fun EnvironmentalMetricSparkCard(
    metric: EnvironmentalMetric,
    stats: EnvironmentalStats,
    points: List<EnvironmentalDataPoint>,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = when (stats.statusLabel) {
        "DANGER" -> RiskHigh
        "WARNING" -> RiskMedium
        "LOW" -> HarpCyan
        else -> HarpTeal
    }

    val icon: ImageVector = when (metric) {
        EnvironmentalMetric.TEMPERATURE -> Icons.Default.Thermostat
        EnvironmentalMetric.HUMIDITY -> Icons.Default.WaterDrop
        EnvironmentalMetric.PM25, EnvironmentalMetric.PM10 -> Icons.Default.Grain
        EnvironmentalMetric.TVOC -> Icons.Default.Biotech
        EnvironmentalMetric.PRESSURE -> Icons.Default.Speed
        EnvironmentalMetric.WATER_LEVEL -> Icons.Default.Waves
        EnvironmentalMetric.VIBRATION -> Icons.Default.Vibration
    }

    Card(
        onClick = onClick,
        modifier = modifier.testTag("spark_card_${metric.name.lowercase()}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) HarpTeal.copy(alpha = 0.12f) else HarpNavyElevated
        ),
        border = BorderStroke(
            1.2.dp,
            if (isSelected) HarpTeal else HarpBorderColor
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = metric.displayName,
                        fontSize = 11.sp,
                        color = HarpTextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = accentColor.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = stats.statusLabel,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${stats.current}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = HarpTextPrimary
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = stats.unit,
                            fontSize = 11.sp,
                            color = HarpTextSecondary,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }

                    // Delta indicator
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (stats.changePercent >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (stats.changePercent >= 0) RiskHigh else HarpTeal,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${if (stats.changePercent >= 0) "+" else ""}${stats.changePercent}%",
                            fontSize = 9.sp,
                            color = HarpTextSecondary
                        )
                    }
                }

                // Inline mini sparkline
                MiniSparkline(
                    points = points,
                    metric = metric,
                    lineColor = accentColor,
                    modifier = Modifier
                        .width(70.dp)
                        .height(30.dp)
                )
            }
        }
    }
}

/**
 * Mini Sparkline for embedding inside metric cards
 */
@Composable
fun MiniSparkline(
    points: List<EnvironmentalDataPoint>,
    metric: EnvironmentalMetric,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    if (points.size < 2) return

    Canvas(modifier = modifier) {
        val vals = points.map { EnvironmentalDataService.getMetricValue(it, metric) }
        val min = vals.minOrNull() ?: 0f
        val max = vals.maxOrNull() ?: 1f
        val range = if (max - min > 0.001f) max - min else 1f

        val w = size.width
        val h = size.height
        val step = w / (points.size - 1)

        val path = Path()
        for (i in points.indices) {
            val v = vals[i]
            val x = i * step
            val y = h - ((v - min) / range) * h
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}
