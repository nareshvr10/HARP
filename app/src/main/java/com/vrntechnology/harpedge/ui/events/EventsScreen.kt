package com.vrntechnology.harpedge.ui.events

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vrntechnology.harpedge.data.model.HazardEvent
import com.vrntechnology.harpedge.ui.components.SeverityBadge
import com.vrntechnology.harpedge.ui.components.getSeverityColor
import com.vrntechnology.harpedge.ui.theme.*

@Composable
fun EventsScreen(
    events: List<HazardEvent>,
    onSelectEvent: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.testTag("screen_events"),
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
                        text = "Detected Hazard Events",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = HarpTextPrimary
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(events) { event ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectEvent(event.eventId) }
                            .testTag("event_item_${event.eventId}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = HarpNavyElevated),
                        border = BorderStroke(1.dp, getSeverityColor(event.severity).copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = event.eventId,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HarpTeal
                                )
                                SeverityBadge(severity = event.severity)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = event.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = HarpTextPrimary
                            )
                            Text(
                                text = event.locationDescription,
                                fontSize = 12.sp,
                                color = HarpTextSecondary
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Confidence: ${(event.aiConfidence * 100).toInt()}%",
                                    fontSize = 11.sp,
                                    color = HarpCyan,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Consensus: ${event.supportingNodes.size}/${event.totalNearbyNodes} Nodes",
                                    fontSize = 11.sp,
                                    color = HarpTextSecondary
                                )
                                Text(
                                    text = event.verificationStatus.label,
                                    fontSize = 11.sp,
                                    color = HarpTextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
