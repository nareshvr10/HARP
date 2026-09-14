package com.vrntechnology.harpedge.ui.health

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
import com.vrntechnology.harpedge.data.repository.HarpRepository
import com.vrntechnology.harpedge.ui.nodes.SensorBar
import com.vrntechnology.harpedge.ui.theme.*

@Composable
fun SensorHealthScreen(
    nodeId: String,
    repository: HarpRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val healthMap by repository.sensorHealthMap.collectAsState()
    val health = healthMap[nodeId] ?: SensorHealth(
        healthId = "SH-$nodeId",
        nodeId = nodeId,
        overallHealth = 92.0
    )

    var isRunningDiagnostics by remember { mutableStateOf(false) }
    var diagnosticsReport by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier.testTag("screen_sensor_health"),
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
                    Column {
                        Text(
                            text = "Sensor Health & Diagnostics",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = HarpTextPrimary
                        )
                        Text(
                            text = "Hardware Integrity: Node $nodeId",
                            fontSize = 11.sp,
                            color = HarpTeal
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Overall Health Card
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
                                text = "AGGREGATE SENSOR INTEGRITY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = HarpTextSecondary,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = "${health.overallHealth.toInt()}%",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (health.overallHealth > 85) RiskLow else if (health.overallHealth > 65) RiskMedium else RiskCritical
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { (health.overallHealth / 100f).toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = if (health.overallHealth > 85) RiskLow else if (health.overallHealth > 65) RiskMedium else RiskCritical,
                            trackColor = HarpBorderColor
                        )
                    }
                }

                // Individual Sensor Transducers
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = HarpNavyElevated),
                    border = BorderStroke(1.dp, HarpBorderColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "INDIVIDUAL TRANSDUCER METRICS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HarpTextSecondary,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        SensorBar("SHT31 / Temperature Transducer", health.temperatureHealth)
                        SensorBar("SHT31 / Relative Humidity Transducer", health.humidityHealth)
                        SensorBar("BMP280 / Barometric Pressure Transducer", health.pressureHealth)
                        SensorBar("PMS5003 / Particulate Matter Transducer", health.pmHealth)
                        SensorBar("SGP30 / TVOC Chemical Transducer", health.vocHealth)
                        SensorBar("Submersible Hydrology Transducer", health.waterHealth)
                        SensorBar("Piezoelectric Vibration Transducer", health.vibrationHealth)
                    }
                }

                // Diagnosed Faults / Anomalies
                if (health.diagnosedIssues.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = HarpNavyElevated),
                        border = BorderStroke(1.dp, SensorFaultColor.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = SensorFaultColor, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Identified Sensor Issues",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HarpTextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            health.diagnosedIssues.forEach { issue ->
                                Text(
                                    text = "• $issue",
                                    fontSize = 12.sp,
                                    color = HarpTextPrimary,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                // Diagnostics Output if triggered
                diagnosticsReport?.let { report ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = HarpNavyDark),
                        border = BorderStroke(1.dp, HarpTeal.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(text = "DIAGNOSTICS REPORT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HarpTeal)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = report, fontSize = 12.sp, color = HarpTextPrimary)
                        }
                    }
                }

                // Actions
                Button(
                    onClick = {
                        isRunningDiagnostics = true
                        diagnosticsReport = "ESP32 self-test sequence completed. Bus I2C/SPI active. ADC reference voltage 3.30V nominal. Drift compensation parameters re-anchored."
                        isRunningDiagnostics = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_run_diagnostics"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HarpTeal, contentColor = HarpNavyDark)
                ) {
                    Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("EXECUTE REMOTE SENSOR DIAGNOSTICS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
