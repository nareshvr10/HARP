package com.vrntechnology.harpedge.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vrntechnology.harpedge.data.model.HazardEvent
import com.vrntechnology.harpedge.data.model.NodeStatus
import com.vrntechnology.harpedge.data.model.SensorNode
import com.vrntechnology.harpedge.data.model.UserRole
import com.vrntechnology.harpedge.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-precision Environmental GIS Geospatial Canvas
 * Renders node topology, spatial consensus mesh links, estimated propagation vectors,
 * and handles zoom/pan gesture transforms and node selection.
 */
@Composable
fun InteractiveGisMap(
    nodes: List<SensorNode>,
    activeEvents: List<HazardEvent>,
    selectedNode: SensorNode?,
    userRole: UserRole,
    onSelectNode: (SensorNode) -> Unit,
    onSelectEvent: (HazardEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Pulsing radar animation for active hazard zones
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 18f,
        targetValue = 65f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    // Map Layers
    var showConsensusMesh by remember { mutableStateOf(true) }
    var showPropagationVector by remember { mutableStateOf(true) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B14))
            .testTag("interactive_gis_map")
    ) {
        // Geospatial Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.6f, 3.5f)
                        offset = Offset(offset.x + pan.x, offset.y + pan.y)
                    }
                }
        ) {
            val canvasW = size.width
            val canvasH = size.height
            val centerX = canvasW / 2f + offset.x
            val centerY = canvasH / 2f + offset.y

            // Coordinate normalizer (Map lat/lng to canvas local coordinates)
            // Reference center: Lat 28.6150, Lng 77.2140
            val refLat = 28.6140
            val refLng = 77.2140
            val latScale = 14000f * scale
            val lngScale = 14000f * scale

            fun nodeToScreen(node: SensorNode): Offset {
                // If viewer, slightly obscure exact precision for defense/infrastructure security
                val jitter = if (userRole == UserRole.VIEWER) 0.0015 else 0.0
                val dx = ((node.longitude + jitter) - refLng) * lngScale
                val dy = -((node.latitude + jitter) - refLat) * latScale
                return Offset(centerX + dx.toFloat(), centerY + dy.toFloat())
            }

            // 1. Draw GIS Environmental Grid & Contour rings
            val gridStep = 60f * scale
            val startX = (offset.x % gridStep)
            val startY = (offset.y % gridStep)

            var x = startX
            while (x < canvasW) {
                drawLine(
                    color = Color(0xFF141F36).copy(alpha = 0.5f),
                    start = Offset(x, 0f),
                    end = Offset(x, canvasH),
                    strokeWidth = 1f
                )
                x += gridStep
            }

            var y = startY
            while (y < canvasH) {
                drawLine(
                    color = Color(0xFF141F36).copy(alpha = 0.5f),
                    start = Offset(0f, y),
                    end = Offset(canvasW, y),
                    strokeWidth = 1f
                )
                y += gridStep
            }

            // Radar concentric rings centered on primary active event
            drawCircle(
                color = Color(0xFF0F264A).copy(alpha = 0.4f),
                radius = 120f * scale,
                center = Offset(centerX, centerY),
                style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)))
            )
            drawCircle(
                color = Color(0xFF0F264A).copy(alpha = 0.25f),
                radius = 240f * scale,
                center = Offset(centerX, centerY),
                style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f)))
            )

            // 2. Spatial Consensus Mesh Lines (linking supporting nodes)
            if (showConsensusMesh) {
                val nodePositions = nodes.associate { it.nodeId to nodeToScreen(it) }
                for (evt in activeEvents) {
                    val supportingOffsets = evt.supportingNodes.mapNotNull { nodePositions[it] }
                    if (supportingOffsets.size >= 2) {
                        for (i in 0 until supportingOffsets.size - 1) {
                            val p1 = supportingOffsets[i]
                            val p2 = supportingOffsets[i + 1]
                            drawLine(
                                color = HarpTeal.copy(alpha = 0.6f),
                                start = p1,
                                end = p2,
                                strokeWidth = 2.5f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f))
                            )
                        }
                    }
                }
            }

            // 3. Estimated Propagation Vector Arrows
            if (showPropagationVector) {
                for (evt in activeEvents) {
                    if (evt.estimatedPropagationDirection != null && evt.supportingNodes.isNotEmpty()) {
                        val firstNode = nodes.find { it.nodeId == evt.supportingNodes.first() }
                        if (firstNode != null) {
                            val origin = nodeToScreen(firstNode)

                            // Vector direction North-East (+dx, -dy)
                            val angleRad = Math.toRadians(-45.0).toFloat()
                            val vectorLen = 140f * scale
                            val target = Offset(
                                origin.x + vectorLen * cos(angleRad),
                                origin.y + vectorLen * sin(angleRad)
                            )

                            // Draw gradient propagation cone
                            val conePath = Path().apply {
                                moveTo(origin.x, origin.y)
                                lineTo(target.x - 30f, target.y + 25f)
                                lineTo(target.x + 25f, target.y - 30f)
                                close()
                            }
                            drawPath(
                                path = conePath,
                                color = RiskHigh.copy(alpha = 0.18f)
                            )

                            // Draw main directional vector line
                            drawLine(
                                color = RiskHigh,
                                start = origin,
                                end = target,
                                strokeWidth = 3.5f
                            )

                            // Arrowhead
                            drawCircle(
                                color = RiskHigh,
                                radius = 6f * scale,
                                center = target
                            )
                        }
                    }
                }
            }

            // 4. Pulse rings around active anomaly/event nodes
            for (node in nodes) {
                if (node.status == NodeStatus.ANOMALY || node.status == NodeStatus.FAULT) {
                    val pos = nodeToScreen(node)
                    val pulseColor = if (node.status == NodeStatus.FAULT) SensorFaultColor else RiskCritical
                    drawCircle(
                        color = pulseColor.copy(alpha = pulseAlpha),
                        radius = pulseRadius * scale,
                        center = pos,
                        style = Stroke(width = 2.5f)
                    )
                }
            }

            // 5. Sensor Node Marker Pins
            for (node in nodes) {
                val pos = nodeToScreen(node)
                val markerColor = getNodeStatusColor(node.status)
                val isSelected = selectedNode?.nodeId == node.nodeId

                // Outer border / Selection ring
                if (isSelected) {
                    drawCircle(
                        color = Color.White,
                        radius = 16f * scale,
                        center = pos,
                        style = Stroke(width = 3f)
                    )
                }

                // Core Node Body
                drawCircle(
                    color = HarpNavyDark,
                    radius = 12f * scale,
                    center = pos
                )
                drawCircle(
                    color = markerColor,
                    radius = 8f * scale,
                    center = pos
                )
            }
        }

        // Map Header Overlay: Title & Coordinates
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = HarpNavyDark.copy(alpha = 0.88f),
                border = androidx.compose.foundation.BorderStroke(1.dp, HarpBorderColor)
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(RiskLow)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIVE GIS INTELLIGENCE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HarpTextPrimary,
                            letterSpacing = 0.8.sp
                        )
                    }
                    Text(
                        text = if (userRole == UserRole.VIEWER) "Regional View (Generalized Coordinates)" else "Center: Lat 28.6140°N, Lng 77.2140°E",
                        fontSize = 10.sp,
                        color = HarpTextSecondary
                    )
                }
            }

            // Layer Controls
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = HarpNavyDark.copy(alpha = 0.88f),
                border = androidx.compose.foundation.BorderStroke(1.dp, HarpBorderColor)
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    IconButton(
                        onClick = { showConsensusMesh = !showConsensusMesh },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Hub,
                            contentDescription = "Toggle Consensus Mesh",
                            tint = if (showConsensusMesh) HarpTeal else HarpTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = { showPropagationVector = !showPropagationVector },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Toggle Propagation Vectors",
                            tint = if (showPropagationVector) RiskHigh else HarpTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            scale = 1f
                            offset = Offset.Zero
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterCenterFocus,
                            contentDescription = "Recenter Map",
                            tint = HarpTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Map Legend (Bottom Left)
        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp),
            shape = RoundedCornerShape(10.dp),
            color = HarpNavyDark.copy(alpha = 0.9f),
            border = androidx.compose.foundation.BorderStroke(1.dp, HarpBorderColor)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "STATUS LEGEND",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = HarpTextSecondary,
                    letterSpacing = 0.6.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    LegendItem(color = RiskLow, label = "Healthy")
                    LegendItem(color = RiskMedium, label = "Anomaly")
                    LegendItem(color = SensorFaultColor, label = "Fault")
                    LegendItem(color = StatusOffline, label = "Offline")
                }
            }
        }

        // Quick Node Select Pills (Top horizontal scroll)
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 66.dp)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            nodes.forEach { node ->
                val isSel = selectedNode?.nodeId == node.nodeId
                Surface(
                    modifier = Modifier
                        .clickable { onSelectNode(node) }
                        .testTag("node_pin_${node.nodeId}"),
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSel) HarpTeal.copy(alpha = 0.25f) else HarpNavyDark.copy(alpha = 0.85f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSel) HarpTeal else getNodeStatusColor(node.status).copy(alpha = 0.6f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(getNodeStatusColor(node.status))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = node.nodeId,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) HarpTeal else HarpTextPrimary
                        )
                    }
                }
            }
        }

        // Bottom Selected Node / Event Details Card Sheet
        if (selectedNode != null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(14.dp)
                    .widthIn(max = 340.dp)
                    .testTag("gis_selected_node_sheet"),
                shape = RoundedCornerShape(14.dp),
                color = HarpNavyElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, HarpBorderColor)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = selectedNode.nodeId,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = HarpTextPrimary
                            )
                            Text(
                                text = selectedNode.name,
                                fontSize = 11.sp,
                                color = HarpTextSecondary
                            )
                        }
                        StatusChip(status = selectedNode.status)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Health: ${selectedNode.overallHealth.toInt()}%",
                            fontSize = 12.sp,
                            color = HarpTeal,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Battery: ${selectedNode.batteryLevel.toInt()}%",
                            fontSize = 12.sp,
                            color = HarpCyan,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (userRole != UserRole.VIEWER) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "GPS: ${"%.4f".format(selectedNode.latitude)}, ${"%.4f".format(selectedNode.longitude)}",
                            fontSize = 11.sp,
                            color = HarpTextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 10.sp, color = HarpTextSecondary)
    }
}
