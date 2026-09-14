package com.vrntechnology.harpedge.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.vrntechnology.harpedge.data.model.UserProfile
import com.vrntechnology.harpedge.ui.components.*
import com.vrntechnology.harpedge.ui.theme.*

@Composable
fun AuthorityDashboardScreen(
    dashboardViewModel: DashboardViewModel,
    currentUser: UserProfile?,
    onNavigateToMap: () -> Unit,
    onNavigateToVerify: (String) -> Unit,
    onNavigateToAlerts: () -> Unit,
    onNavigateToEvents: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by dashboardViewModel.uiState.collectAsState()
    val activeEvent = uiState.events.firstOrNull { it.status == "ACTIVE" } ?: uiState.events.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HarpNavyDark)
            .testTag("screen_authority_dashboard")
    ) {
        // Authority Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = HarpNavySurface,
            border = BorderStroke(0.8.dp, HarpBorderColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "HARP-Edge",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = HarpTextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = HarpCyan.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, HarpCyan.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "FIELD RESPONSE COMMAND",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = HarpCyan,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = currentUser?.name ?: "Officer Marcus Vance",
                        fontSize = 11.sp,
                        color = HarpTextSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateToAlerts) {
                        BadgedBox(
                            badge = {
                                val unacked = uiState.alerts.count { !it.acknowledged }
                                if (unacked > 0) {
                                    Badge(containerColor = RiskCritical) {
                                        Text("$unacked")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Alerts",
                                tint = HarpCyan
                            )
                        }
                    }

                    IconButton(onClick = onSignOut) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Sign Out",
                            tint = HarpTextMuted
                        )
                    }
                }
            }
        }

        SystemStatusBanner(
            isDemoMode = uiState.isDemoMode,
            isOnline = uiState.isOnline,
            onToggleDemo = { dashboardViewModel.toggleDemoMode() }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Rapid Field Verification Banner
            if (activeEvent != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = HarpNavyElevated),
                        border = BorderStroke(1.2.dp, RiskHigh.copy(alpha = 0.7f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AssignmentLate,
                                        contentDescription = null,
                                        tint = RiskHigh,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "FIELD VERIFICATION REQUIRED",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RiskHigh,
                                        letterSpacing = 0.8.sp
                                    )
                                }
                                SeverityBadge(severity = activeEvent.severity)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = activeEvent.title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = HarpTextPrimary
                            )
                            Text(
                                text = "Sector: ${activeEvent.locationDescription}",
                                fontSize = 12.sp,
                                color = HarpTextSecondary
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Consensus: ${activeEvent.supportingNodes.size}/${activeEvent.totalNearbyNodes} nodes • AI Confidence: ${(activeEvent.aiConfidence * 100).toInt()}% • Propagation: ${activeEvent.estimatedPropagationDirection ?: "Local"}",
                                fontSize = 12.sp,
                                color = HarpCyan
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = { onNavigateToVerify(activeEvent.eventId) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("btn_authority_verify_now"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = HarpTeal,
                                    contentColor = HarpNavyDark
                                )
                            ) {
                                Icon(Icons.Default.FactCheck, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("PROCEED TO ON-SITE VERIFICATION", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Hazard Intelligence Component
            if (activeEvent != null) {
                item {
                    HazardIntelligenceCard(
                        event = activeEvent,
                        onViewMap = onNavigateToMap,
                        onInspect = { onNavigateToVerify(activeEvent.eventId) }
                    )
                }
            }

            // Active Priority Alerts List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "INCIDENT ALERTS QUEUE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = HarpTextSecondary,
                        letterSpacing = 0.8.sp
                    )
                    TextButton(onClick = onNavigateToAlerts) {
                        Text("VIEW ALL (${uiState.alerts.size})", fontSize = 11.sp, color = HarpCyan, fontWeight = FontWeight.Bold)
                    }
                }
            }

            items(uiState.alerts) { alert ->
                AlertCard(
                    alert = alert,
                    onClick = { onNavigateToVerify(alert.eventId) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
