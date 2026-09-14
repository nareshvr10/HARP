package com.vrntechnology.harpedge.ui.events

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vrntechnology.harpedge.data.model.HazardEvent
import com.vrntechnology.harpedge.data.model.UserRole
import com.vrntechnology.harpedge.data.repository.HarpRepository
import com.vrntechnology.harpedge.ui.components.EventTimelineView
import com.vrntechnology.harpedge.ui.components.MetricBox
import com.vrntechnology.harpedge.ui.components.SeverityBadge
import com.vrntechnology.harpedge.ui.components.getSeverityColor
import com.vrntechnology.harpedge.ui.theme.*

@Composable
fun EventDetailScreen(
    event: HazardEvent,
    repository: HarpRepository,
    userRole: UserRole,
    onNavigateBack: () -> Unit,
    onNavigateToFingerprint: (String) -> Unit,
    onNavigateToVerify: (String) -> Unit,
    onNavigateToMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timelinesMap by repository.eventTimelines.collectAsState()
    val timeline = timelinesMap[event.eventId] ?: emptyList()
    val severityColor = getSeverityColor(event.severity)

    Scaffold(
        modifier = modifier.testTag("screen_event_detail"),
        containerColor = HarpNavyDark
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = HarpNavySurface,
                border = BorderStroke(0.8.dp, HarpBorderColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = HarpTextPrimary)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Incident Intelligence: ${event.eventId}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = HarpTextPrimary
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main Incident Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = HarpNavyElevated),
                    border = BorderStroke(1.2.dp, severityColor.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = event.hazardType.displayName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = HarpTextPrimary
                            )
                            SeverityBadge(severity = event.severity)
                        }

                        Text(
                            text = event.locationDescription,
                            fontSize = 13.sp,
                            color = HarpTextSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MetricBox(
                                title = "AI Confidence",
                                value = "${(event.aiConfidence * 100).toInt()}%",
                                accentColor = HarpTeal,
                                modifier = Modifier.weight(1f)
                            )
                            MetricBox(
                                title = "Integrity",
                                value = "${(event.sensorIntegrity * 100).toInt()}%",
                                accentColor = HarpCyan,
                                modifier = Modifier.weight(1f)
                            )
                            MetricBox(
                                title = "Consensus",
                                value = "${event.supportingNodes.size}/${event.totalNearbyNodes}",
                                accentColor = RiskHigh,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Estimated Propagation Banner
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(HarpNavyDark, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = RiskHigh, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Estimated Propagation: ${event.estimatedPropagationDirection ?: "Local"}",
                                    fontSize = 11.sp,
                                    color = HarpTextPrimary
                                )
                            }
                            Text(
                                text = "${event.estimatedSpeedKmh ?: 0.0} km/h",
                                fontSize = 11.sp,
                                color = HarpCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onNavigateToFingerprint(event.eventId) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, HarpTeal),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = HarpTeal)
                            ) {
                                Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("FINGERPRINT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            if (userRole != UserRole.VIEWER) {
                                Button(
                                    onClick = { onNavigateToVerify(event.eventId) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = HarpTeal, contentColor = HarpNavyDark)
                                ) {
                                    Icon(Icons.Default.FactCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("VERIFY", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Vertical Event Progression Timeline
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = HarpNavyElevated),
                    border = BorderStroke(1.dp, HarpBorderColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timeline, contentDescription = null, tint = HarpTeal, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "INCIDENT CHRONOLOGY TIMELINE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = HarpTextSecondary,
                                letterSpacing = 0.8.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (timeline.isNotEmpty()) {
                            EventTimelineView(timelineEntries = timeline)
                        } else {
                            Text("No timeline entries recorded yet.", fontSize = 12.sp, color = HarpTextSecondary)
                        }
                    }
                }
            }
        }
    }
}
