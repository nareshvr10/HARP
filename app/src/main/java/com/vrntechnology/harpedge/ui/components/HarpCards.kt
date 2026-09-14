package com.vrntechnology.harpedge.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vrntechnology.harpedge.data.model.*
import com.vrntechnology.harpedge.ui.theme.*

fun getSeverityColor(severity: SeverityLevel): Color {
    return when (severity) {
        SeverityLevel.LOW -> RiskLow
        SeverityLevel.MEDIUM -> RiskMedium
        SeverityLevel.HIGH -> RiskHigh
        SeverityLevel.CRITICAL -> RiskCritical
    }
}

fun getNodeStatusColor(status: NodeStatus): Color {
    return when (status) {
        NodeStatus.HEALTHY -> RiskLow
        NodeStatus.ANOMALY -> RiskMedium
        NodeStatus.FAULT -> SensorFaultColor
        NodeStatus.OFFLINE -> StatusOffline
    }
}

@Composable
fun SeverityBadge(severity: SeverityLevel, modifier: Modifier = Modifier) {
    val color = getSeverityColor(severity)
    Surface(
        modifier = modifier.testTag("severity_badge_${severity.name}"),
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = severity.label,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StatusChip(status: NodeStatus, modifier: Modifier = Modifier) {
    val color = getNodeStatusColor(status)
    Surface(
        modifier = modifier.testTag("node_status_chip_${status.name}"),
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = status.label.uppercase(),
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Signature Component: HAZARD INTELLIGENCE
 * Displays: Hazard Type, AI Confidence, Sensor Integrity, Node Consensus,
 * Propagation Direction, Affected Nodes, Severity, and Decision-Support Disclaimer.
 */
@Composable
fun HazardIntelligenceCard(
    event: HazardEvent,
    onViewMap: () -> Unit,
    onInspect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val severityColor = getSeverityColor(event.severity)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_hazard_intelligence"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = HarpNavyElevated
        ),
        border = BorderStroke(1.2.dp, severityColor.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            severityColor.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
                .padding(18.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = null,
                        tint = HarpTeal,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "HAZARD INTELLIGENCE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = HarpTeal,
                        letterSpacing = 1.1.sp
                    )
                }
                SeverityBadge(severity = event.severity)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Hazard Title
            Text(
                text = event.hazardType.displayName,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = HarpTextPrimary
            )
            Text(
                text = event.locationDescription,
                fontSize = 13.sp,
                color = HarpTextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Primary 4 Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricBox(
                    title = "AI Confidence",
                    value = "${(event.aiConfidence * 100).toInt()}%",
                    accentColor = HarpTeal,
                    modifier = Modifier.weight(1f)
                )
                MetricBox(
                    title = "Sensor Integrity",
                    value = "${(event.sensorIntegrity * 100).toInt()}%",
                    accentColor = if (event.sensorIntegrity > 0.8) RiskLow else RiskMedium,
                    modifier = Modifier.weight(1f)
                )
                MetricBox(
                    title = "Node Consensus",
                    value = "${event.supportingNodes.size} / ${event.totalNearbyNodes}",
                    accentColor = HarpCyan,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Secondary Metrics Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(HarpNavyDark.copy(alpha = 0.6f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = null,
                        tint = HarpTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Estimated Propagation: ${event.estimatedPropagationDirection ?: "Local / Static"}",
                        fontSize = 12.sp,
                        color = HarpTextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = "${event.affectedNodeCount} Nodes Active",
                    fontSize = 12.sp,
                    color = HarpCyan,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Decision-Support Disclaimer Notice
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = HarpTextMuted,
                    modifier = Modifier
                        .size(14.dp)
                        .padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Decision-Support Intelligence: Predictive multi-node correlation. Field verification required before statutory emergency action.",
                    fontSize = 11.sp,
                    color = HarpTextMuted,
                    lineHeight = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onViewMap,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("btn_hazard_view_map"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, HarpTeal.copy(alpha = 0.7f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HarpTeal)
                ) {
                    Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("VIEW MAP", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onInspect,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("btn_hazard_inspect"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HarpTeal,
                        contentColor = HarpNavyDark
                    )
                ) {
                    Icon(Icons.Default.Troubleshoot, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("INSPECT", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MetricBox(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = HarpNavyDark.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, HarpBorderColor.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 10.sp,
                color = HarpTextSecondary,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Main Environmental Risk Card on Dashboard
 */
@Composable
fun CurrentRiskCard(
    highestSeverity: SeverityLevel,
    activeNodesCount: Int,
    healthyNodesCount: Int,
    activeAlertsCount: Int,
    offlineNodesCount: Int,
    modifier: Modifier = Modifier
) {
    val severityColor = getSeverityColor(highestSeverity)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_current_environmental_risk"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = HarpNavyElevated),
        border = BorderStroke(1.dp, severityColor.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CURRENT ENVIRONMENTAL RISK",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = HarpTextSecondary,
                    letterSpacing = 0.8.sp
                )
                SeverityBadge(severity = highestSeverity)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4 summary counters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                RiskSummaryItem(count = activeNodesCount, label = "Active Nodes", color = HarpCyan)
                RiskSummaryItem(count = healthyNodesCount, label = "Healthy", color = RiskLow)
                RiskSummaryItem(count = activeAlertsCount, label = "Alerts", color = if (activeAlertsCount > 0) RiskHigh else HarpTextSecondary)
                RiskSummaryItem(count = offlineNodesCount, label = "Offline", color = if (offlineNodesCount > 0) StatusOffline else HarpTextMuted)
            }
        }
    }
}

@Composable
fun RiskSummaryItem(count: Int, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = HarpTextSecondary
        )
    }
}

/**
 * Compact Live Sensor Telemetry Card
 */
@Composable
fun LiveSensorMetricCard(
    name: String,
    value: String,
    unit: String,
    icon: ImageVector,
    statusColor: Color = HarpTeal,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("metric_card_${name.lowercase()}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = HarpNavyElevated),
        border = BorderStroke(1.dp, HarpBorderColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = name, fontSize = 11.sp, color = HarpTextSecondary, fontWeight = FontWeight.Medium)
                Icon(imageVector = icon, contentDescription = null, tint = statusColor, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = HarpTextPrimary
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = unit,
                    fontSize = 11.sp,
                    color = HarpTextSecondary,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}

/**
 * Node Status Card for Sensor Node Management
 */
@Composable
fun NodeItemCard(
    node: SensorNode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("node_item_${node.nodeId}"),
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
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(HarpNavyDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = null,
                            tint = getNodeStatusColor(node.status),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = node.nodeId,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = HarpTextPrimary
                        )
                        Text(
                            text = node.name,
                            fontSize = 12.sp,
                            color = HarpTextSecondary
                        )
                    }
                }
                StatusChip(status = node.status)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = HarpBorderColor.copy(alpha = 0.5f), thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Health", fontSize = 10.sp, color = HarpTextSecondary)
                    Text(
                        text = "${node.overallHealth.toInt()}%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (node.overallHealth > 85) RiskLow else RiskMedium
                    )
                }
                Column {
                    Text(text = "Battery", fontSize = 10.sp, color = HarpTextSecondary)
                    Text(
                        text = "${node.batteryLevel.toInt()}%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (node.batteryLevel > 30) HarpCyan else RiskHigh
                    )
                }
                Column {
                    Text(text = "Comm Type", fontSize = 10.sp, color = HarpTextSecondary)
                    Text(
                        text = node.communicationType,
                        fontSize = 12.sp,
                        color = HarpTextPrimary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Location", fontSize = 10.sp, color = HarpTextSecondary)
                    Text(
                        text = node.locationName.take(15) + "...",
                        fontSize = 12.sp,
                        color = HarpTextPrimary
                    )
                }
            }
        }
    }
}

/**
 * Alert Center Card
 */
@Composable
fun AlertCard(
    alert: AlertItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val severityColor = getSeverityColor(alert.alertLevel)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("alert_card_${alert.alertId}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = HarpNavyElevated),
        border = BorderStroke(1.dp, severityColor.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(severityColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = severityColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SeverityBadge(severity = alert.alertLevel)
                    Text(
                        text = if (alert.acknowledged) "ACKNOWLEDGED" else "ACTIVE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (alert.acknowledged) RiskLow else RiskHigh
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = alert.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = HarpTextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "AI Confidence: ${(alert.confidence * 100).toInt()}% • Supporting Nodes: ${alert.supportingNodesCount}/${alert.totalNodesCount}",
                    fontSize = 11.sp,
                    color = HarpTextSecondary
                )
            }
        }
    }
}

/**
 * Offline Mode / Demo Mode Top Banner
 */
@Composable
fun SystemStatusBanner(
    isDemoMode: Boolean,
    isOnline: Boolean,
    onToggleDemo: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isDemoMode || !isOnline) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .testTag("banner_system_status"),
            color = if (!isOnline) RiskMedium.copy(alpha = 0.2f) else HarpPurple.copy(alpha = 0.2f),
            border = BorderStroke(
                1.dp,
                if (!isOnline) RiskMedium.copy(alpha = 0.5f) else HarpPurple.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (!isOnline) Icons.Default.CloudOff else Icons.Default.Science,
                        contentDescription = null,
                        tint = if (!isOnline) RiskMedium else HarpPurple,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (!isOnline) "OFFLINE MODE: Local Cache Active" else "DEMO MODE: Simulated ESP32 Mesh",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = HarpTextPrimary
                    )
                }

                TextButton(
                    onClick = onToggleDemo,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isDemoMode) "LIVE SETUP" else "DEMO",
                        fontSize = 11.sp,
                        color = HarpTeal,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
