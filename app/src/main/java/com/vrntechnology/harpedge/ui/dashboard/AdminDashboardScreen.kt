package com.vrntechnology.harpedge.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vrntechnology.harpedge.data.model.*
import com.vrntechnology.harpedge.data.repository.EnvironmentalDataService
import com.vrntechnology.harpedge.ui.components.*
import com.vrntechnology.harpedge.ui.theme.*

@Composable
fun AdminDashboardScreen(
    dashboardViewModel: DashboardViewModel,
    currentUser: UserProfile?,
    onNavigateToMap: () -> Unit,
    onNavigateToInspect: (String) -> Unit,
    onNavigateToNodes: () -> Unit,
    onNavigateToEvents: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToEnvironmental: () -> Unit = {},
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by dashboardViewModel.uiState.collectAsState()

    var previewMetric by remember { mutableStateOf(EnvironmentalMetric.PM25) }
    val previewPoints = remember(uiState.latestReadings, uiState.currentScenario, previewMetric) {
        EnvironmentalDataService.generateTimeSeries(
            nodeId = uiState.latestReadings?.nodeId ?: "NODE-001",
            timeRange = TimeRange.TWENTY_FOUR_HOURS,
            currentScenario = uiState.currentScenario,
            latestReading = uiState.latestReadings
        )
    }

    val activeHazardEvent = uiState.events.firstOrNull { it.status == "ACTIVE" } ?: uiState.events.firstOrNull()
    val highestSeverity = uiState.events.maxByOrNull { it.severity.ordinal }?.severity ?: SeverityLevel.LOW
    val activeNodesCount = uiState.nodes.count { it.active && it.isOnline }
    val healthyNodesCount = uiState.nodes.count { it.status == NodeStatus.HEALTHY }
    val activeAlertsCount = uiState.alerts.count { !it.acknowledged }
    val offlineNodesCount = uiState.nodes.count { !it.isOnline }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HarpNavyDark)
            .testTag("screen_admin_dashboard")
    ) {
        // Top Operational Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = HarpNavySurface,
            border = BorderStroke(0.8.dp, HarpBorderColor)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "HARP-Edge",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = HarpTextPrimary,
                                letterSpacing = 0.6.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = HarpTeal.copy(alpha = 0.16f),
                                border = BorderStroke(1.dp, HarpTeal.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "ADMIN COMMAND",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HarpTeal,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = currentUser?.name ?: "Dr. Sarah Chen (Administrator)",
                            fontSize = 11.sp,
                            color = HarpTextSecondary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { dashboardViewModel.toggleOnlineState() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (uiState.isOnline) Icons.Default.CloudQueue else Icons.Default.CloudOff,
                                contentDescription = "Toggle Network Simulation",
                                tint = if (uiState.isOnline) HarpTeal else RiskMedium,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = HarpTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = onSignOut,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Sign Out",
                                tint = HarpTextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Offline / Demo Mode Banner
        SystemStatusBanner(
            isDemoMode = uiState.isDemoMode,
            isOnline = uiState.isOnline,
            onToggleDemo = { dashboardViewModel.toggleDemoMode() }
        )

        // Main Scrollable Content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Evaluator Demo Actions Quick Panel
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = HarpNavyElevated),
                    border = BorderStroke(1.dp, HarpPurple.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PlayCircle,
                                    contentDescription = null,
                                    tint = HarpPurple,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "LIVE EVALUATOR DEMO CONTROLS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HarpPurple,
                                    letterSpacing = 0.8.sp
                                )
                            }
                            Text(
                                text = "ACTIVE: ${uiState.currentScenario}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = HarpCyan
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { dashboardViewModel.triggerScenarioMultiNode() },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_scenario_multi_hazard"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RiskHigh.copy(alpha = 0.25f),
                                    contentColor = RiskHigh
                                ),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.LocalFireDepartment, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("1. Consensus Hazard", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = { dashboardViewModel.triggerScenarioSensorFault() },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_scenario_sensor_fault"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SensorFaultColor.copy(alpha = 0.25f),
                                    contentColor = SensorFaultColor
                                ),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("2. Sensor Drift", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            OutlinedButton(
                                onClick = { dashboardViewModel.resetScenarioBaseline() },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_scenario_reset"),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, HarpBorderColor),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = HarpTextSecondary),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Reset Baseline", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Summary Risk Overview Card
            item {
                CurrentRiskCard(
                    highestSeverity = highestSeverity,
                    activeNodesCount = activeNodesCount,
                    healthyNodesCount = healthyNodesCount,
                    activeAlertsCount = activeAlertsCount,
                    offlineNodesCount = offlineNodesCount
                )
            }

            // SIGNATURE COMPONENT: Hazard Intelligence Card
            if (activeHazardEvent != null) {
                item {
                    HazardIntelligenceCard(
                        event = activeHazardEvent,
                        onViewMap = onNavigateToMap,
                        onInspect = { onNavigateToInspect(activeHazardEvent.eventId) }
                    )
                }
            }

            // Live Environmental Sensor Telemetry (Node 01 readings)
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LIVE ESP32 TELEMETRY",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = HarpTextSecondary,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "Node: ${uiState.latestReadings?.nodeId ?: "NODE-001"}",
                            fontSize = 11.sp,
                            color = HarpTeal,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val r = uiState.latestReadings
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LiveSensorMetricCard(
                            name = "Temperature",
                            value = "${r?.temperature ?: 34.8}",
                            unit = "°C",
                            icon = Icons.Default.Thermostat,
                            statusColor = if ((r?.temperature ?: 0.0) > 40.0) RiskCritical else HarpTeal,
                            modifier = Modifier.weight(1f)
                        )
                        LiveSensorMetricCard(
                            name = "Humidity",
                            value = "${r?.humidity ?: 61.2}",
                            unit = "%",
                            icon = Icons.Default.WaterDrop,
                            statusColor = HarpCyan,
                            modifier = Modifier.weight(1f)
                        )
                        LiveSensorMetricCard(
                            name = "Pressure",
                            value = "${r?.pressure ?: 1012.4}",
                            unit = "hPa",
                            icon = Icons.Default.Speed,
                            statusColor = HarpPurple,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LiveSensorMetricCard(
                            name = "TVOC",
                            value = "${r?.tvoc ?: 142.0}",
                            unit = "ppb",
                            icon = Icons.Default.Biotech,
                            statusColor = if ((r?.tvoc ?: 0.0) > 500.0) RiskCritical else RiskLow,
                            modifier = Modifier.weight(1f)
                        )
                        LiveSensorMetricCard(
                            name = "PM2.5",
                            value = "${r?.pm25 ?: 18.4}",
                            unit = "µg/m³",
                            icon = Icons.Default.Grain,
                            statusColor = if ((r?.pm25 ?: 0.0) > 60.0) RiskHigh else RiskLow,
                            modifier = Modifier.weight(1f)
                        )
                        LiveSensorMetricCard(
                            name = "Water Level",
                            value = "${r?.waterLevel ?: 42.5}",
                            unit = "cm",
                            icon = Icons.Default.Waves,
                            statusColor = HarpCyan,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Embedded Interactive Environmental Line/Area Chart Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_dashboard_environmental_preview"),
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
                                        imageVector = Icons.Default.ShowChart,
                                        contentDescription = null,
                                        tint = HarpTeal,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "ENVIRONMENTAL TRENDS (24H)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = HarpTextSecondary,
                                        letterSpacing = 0.8.sp
                                    )
                                }

                                TextButton(
                                    onClick = onNavigateToEnvironmental,
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "FULL CHARTS →",
                                        fontSize = 11.sp,
                                        color = HarpCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Metric selection chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    EnvironmentalMetric.PM25,
                                    EnvironmentalMetric.TEMPERATURE,
                                    EnvironmentalMetric.TVOC,
                                    EnvironmentalMetric.WATER_LEVEL
                                ).forEach { metric ->
                                    val isSelected = previewMetric == metric
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { previewMetric = metric },
                                        label = {
                                            Text(
                                                text = metric.displayName,
                                                fontSize = 10.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = HarpTeal.copy(alpha = 0.2f),
                                            selectedLabelColor = HarpTeal,
                                            containerColor = HarpNavyDark,
                                            labelColor = HarpTextSecondary
                                        ),
                                        border = BorderStroke(1.dp, if (isSelected) HarpTeal else HarpBorderColor),
                                        modifier = Modifier.testTag("chip_preview_${metric.name.lowercase()}")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            val chartColor = when (previewMetric) {
                                EnvironmentalMetric.TEMPERATURE -> RiskHigh
                                EnvironmentalMetric.HUMIDITY -> HarpCyan
                                EnvironmentalMetric.PM25, EnvironmentalMetric.PM10 -> HarpPurple
                                EnvironmentalMetric.TVOC -> RiskCritical
                                EnvironmentalMetric.WATER_LEVEL -> HarpCyan
                                else -> HarpTeal
                            }

                            EnvironmentalLineAreaChart(
                                points = previewPoints,
                                metric = previewMetric,
                                lineColor = chartColor
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = onNavigateToEnvironmental,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .testTag("btn_open_environmental_dashboard"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = HarpTeal.copy(alpha = 0.2f),
                                    contentColor = HarpTeal
                                ),
                                border = BorderStroke(1.dp, HarpTeal.copy(alpha = 0.5f))
                            ) {
                                Icon(Icons.Default.Insights, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("OPEN ENVIRONMENTAL INTELLIGENCE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Quick Nodes List Preview
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DISTRIBUTED SENSOR NODES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = HarpTextSecondary,
                        letterSpacing = 0.8.sp
                    )
                    TextButton(onClick = onNavigateToNodes) {
                        Text("MANAGE (${uiState.nodes.size})", fontSize = 11.sp, color = HarpTeal, fontWeight = FontWeight.Bold)
                    }
                }
            }

            items(uiState.nodes.take(3)) { node ->
                NodeItemCard(
                    node = node,
                    onClick = {
                        dashboardViewModel.selectNode(node)
                        onNavigateToNodes()
                    }
                )
            }

            // Quick Navigation Shortcuts (Research Dataset & Audit Logs)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateToEnvironmental,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("btn_goto_env_charts"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, HarpTeal.copy(alpha = 0.8f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HarpTeal)
                    ) {
                        Icon(Icons.Default.ShowChart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ENV CHARTS", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onNavigateToAnalytics,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("btn_goto_analytics"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, HarpCyan.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HarpCyan)
                    ) {
                        Icon(Icons.Default.Insights, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ANALYTICS", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onNavigateToEvents,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("btn_goto_events"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, HarpPurple.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HarpPurple)
                    ) {
                        Icon(Icons.Default.Timeline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("TIMELINES", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Bottom Spacing for Navigation Bar
            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Firebase Setup Dialog Guide (if Real Data Mode selected without config)
    if (uiState.showFirebaseSetupDialog) {
        AlertDialog(
            onDismissRequest = { dashboardViewModel.dismissFirebaseSetupDialog() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudSync, contentDescription = null, tint = HarpTeal)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Real Data Firebase Setup", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = HarpTextPrimary)
                }
            },
            text = {
                Column {
                    Text(
                        text = "To stream live data from physical ESP32 nodes via Firebase Firestore:",
                        fontSize = 12.sp,
                        color = HarpTextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "1. Download google-services.json from your Firebase project console.\n2. Place google-services.json in the /app root folder.\n3. Publish ESP32 MQTT/HTTP payload into collection 'nodes/{nodeId}/readings'.",
                        fontSize = 11.sp,
                        color = HarpTextPrimary,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "The application will continue operating with local Room caching and high-fidelity simulated ESP32 consensus.",
                        fontSize = 11.sp,
                        color = HarpTeal
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { dashboardViewModel.dismissFirebaseSetupDialog() },
                    colors = ButtonDefaults.buttonColors(containerColor = HarpTeal, contentColor = HarpNavyDark)
                ) {
                    Text("UNDERSTOOD")
                }
            },
            containerColor = HarpNavyElevated
        )
    }
}
