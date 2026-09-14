package com.vrntechnology.harpedge.ui.nodes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vrntechnology.harpedge.data.model.SensorHealth
import com.vrntechnology.harpedge.data.model.SensorNode
import com.vrntechnology.harpedge.data.model.UserRole
import com.vrntechnology.harpedge.data.repository.HarpRepository
import com.vrntechnology.harpedge.ui.components.StatusChip
import com.vrntechnology.harpedge.ui.theme.*

@Composable
fun NodeDetailScreen(
    node: SensorNode,
    repository: HarpRepository,
    userRole: UserRole,
    onNavigateBack: () -> Unit,
    onNavigateToHealth: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val healthMap by repository.sensorHealthMap.collectAsState()
    val health = healthMap[node.nodeId] ?: SensorHealth(
        healthId = "SH-${node.nodeId}",
        nodeId = node.nodeId,
        overallHealth = node.overallHealth
    )

    Scaffold(
        modifier = modifier.testTag("screen_node_detail"),
        containerColor = HarpNavyDark
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Top Bar
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
                        text = "Node ${node.nodeId} Telemetry",
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
                // Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = HarpNavyElevated),
                    border = BorderStroke(1.dp, HarpBorderColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = node.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = HarpTextPrimary
                            )
                            StatusChip(status = node.status)
                        }
                        Text(
                            text = node.locationName,
                            fontSize = 12.sp,
                            color = HarpTextSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Overall Health", fontSize = 11.sp, color = HarpTextSecondary)
                                Text(
                                    text = "${node.overallHealth.toInt()}%",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (node.overallHealth > 85) RiskLow else RiskMedium
                                )
                            }
                            Column {
                                Text("Battery Capacity", fontSize = 11.sp, color = HarpTextSecondary)
                                Text(
                                    text = "${node.batteryLevel.toInt()}%",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HarpCyan
                                )
                            }
                            Column {
                                Text("Firmware", fontSize = 11.sp, color = HarpTextSecondary)
                                Text(
                                    text = node.firmwareVersion,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = HarpTextPrimary
                                )
                            }
                        }
                    }
                }

                // Sensor Health Diagnosis Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = HarpNavyElevated),
                    border = BorderStroke(1.dp, HarpBorderColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SENSOR HEALTH BREAKDOWN",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = HarpTextSecondary,
                                letterSpacing = 0.8.sp
                            )
                            TextButton(
                                onClick = { onNavigateToHealth(node.nodeId) },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("FULL DIAGNOSTICS", fontSize = 11.sp, color = HarpTeal, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        SensorBar(label = "Temperature Sensor", health = health.temperatureHealth)
                        SensorBar(label = "Humidity Sensor", health = health.humidityHealth)
                        SensorBar(label = "Particulate Matter (PM)", health = health.pmHealth)
                        SensorBar(label = "TVOC Chemical Sensor", health = health.vocHealth)
                        SensorBar(label = "Water Level / Hydrology", health = health.waterHealth)

                        if (health.diagnosedIssues.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SensorFaultColor.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, SensorFaultColor.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Build, contentDescription = null, tint = SensorFaultColor, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = health.diagnosedIssues.first(),
                                        fontSize = 11.sp,
                                        color = HarpTextPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // Node Actions (Admin only)
                if (userRole == UserRole.ADMIN) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onNavigateToHealth(node.nodeId) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, HarpTeal),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = HarpTeal)
                        ) {
                            Text("RUN DIAGNOSTICS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                repository.disableNode(node.nodeId)
                                onNavigateBack()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RiskCritical.copy(alpha = 0.25f),
                                contentColor = RiskCritical
                            )
                        ) {
                            Text("DEACTIVATE NODE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SensorBar(label: String, health: Double) {
    val color = if (health > 85) RiskLow else if (health > 65) RiskMedium else RiskCritical
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 11.sp, color = HarpTextPrimary)
            Text("${health.toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { (health / 100f).toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = color,
            trackColor = HarpBorderColor
        )
    }
}
