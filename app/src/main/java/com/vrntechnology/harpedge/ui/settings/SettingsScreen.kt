package com.vrntechnology.harpedge.ui.settings

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
import com.vrntechnology.harpedge.data.model.UserRole
import com.vrntechnology.harpedge.data.repository.HarpRepository
import com.vrntechnology.harpedge.ui.theme.*

@Composable
fun SettingsScreen(
    repository: HarpRepository,
    userRole: UserRole,
    onNavigateBack: () -> Unit,
    onNavigateToAuditLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val config by repository.configuration.collectAsState()
    val isDemo by repository.isDemoMode.collectAsState()
    val isOnline by repository.isOnline.collectAsState()

    var samplingInterval by remember { mutableStateOf(config.samplingIntervalSeconds.toString()) }
    var analysisWindow by remember { mutableStateOf(config.analysisWindowSeconds.toString()) }
    var minConsensus by remember { mutableStateOf(config.minimumConsensusNodes.toString()) }
    var savedSuccess by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.testTag("screen_settings"),
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
                        text = "System Settings & Thresholds",
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
                // Operation Mode Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = HarpNavyElevated),
                    border = BorderStroke(1.dp, HarpBorderColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "OPERATIONAL ENGINE MODE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HarpTextSecondary,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isDemo) "DEMO MODE (Simulated ESP32 Mesh)" else "REAL DATA MODE (Firebase / MQTT)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HarpTextPrimary
                                )
                                Text(
                                    text = if (isDemo) "Synthetic multi-hazard triggers active" else "Listening for physical node telemetry",
                                    fontSize = 11.sp,
                                    color = HarpTextSecondary
                                )
                            }
                            Switch(
                                checked = isDemo,
                                onCheckedChange = { repository.setDemoMode(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = HarpNavyDark,
                                    checkedTrackColor = HarpTeal
                                )
                            )
                        }
                    }
                }

                // Cloud Sync & Room Cache
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = HarpNavyElevated),
                    border = BorderStroke(1.dp, HarpBorderColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "LOCAL ROOM CACHE & CLOUD SYNC",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HarpTextSecondary,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Offline Persistence Engine",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HarpTextPrimary
                                )
                                Text(
                                    text = "Local SQLite Room database active",
                                    fontSize = 11.sp,
                                    color = RiskLow
                                )
                            }
                            Button(
                                onClick = { repository.triggerOfflineSync() },
                                colors = ButtonDefaults.buttonColors(containerColor = HarpTeal, contentColor = HarpNavyDark)
                            ) {
                                Text("SYNC NOW", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // AI & Consensus Thresholds (Admin only)
                if (userRole == UserRole.ADMIN) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = HarpNavyElevated),
                        border = BorderStroke(1.dp, HarpBorderColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "CONSENSUS & RECOGNITION THRESHOLDS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = HarpTextSecondary,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = samplingInterval,
                                onValueChange = { samplingInterval = it },
                                label = { Text("Sampling Interval (Seconds)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = HarpTeal,
                                    unfocusedBorderColor = HarpBorderColor,
                                    focusedTextColor = HarpTextPrimary,
                                    unfocusedTextColor = HarpTextPrimary
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = analysisWindow,
                                onValueChange = { analysisWindow = it },
                                label = { Text("Resonant Analysis Window (Seconds)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = HarpTeal,
                                    unfocusedBorderColor = HarpBorderColor,
                                    focusedTextColor = HarpTextPrimary,
                                    unfocusedTextColor = HarpTextPrimary
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = minConsensus,
                                onValueChange = { minConsensus = it },
                                label = { Text("Minimum Consensus Nodes Required") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = HarpTeal,
                                    unfocusedBorderColor = HarpBorderColor,
                                    focusedTextColor = HarpTextPrimary,
                                    unfocusedTextColor = HarpTextPrimary
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            if (savedSuccess) {
                                Text(
                                    text = "System parameters successfully saved!",
                                    color = RiskLow,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }

                            Button(
                                onClick = {
                                    val si = samplingInterval.toIntOrNull() ?: 5
                                    val aw = analysisWindow.toIntOrNull() ?: 60
                                    val mc = minConsensus.toIntOrNull() ?: 3
                                    repository.updateConfiguration(
                                        config.copy(
                                            samplingIntervalSeconds = si,
                                            analysisWindowSeconds = aw,
                                            minimumConsensusNodes = mc
                                        )
                                    )
                                    savedSuccess = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = HarpTeal, contentColor = HarpNavyDark)
                            ) {
                                Text("UPDATE SYSTEM PARAMETERS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Audit Logs Link
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = HarpNavyElevated),
                        border = BorderStroke(1.dp, HarpBorderColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("System Audit Trail", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HarpTextPrimary)
                                Text("Immutable security & command logs", fontSize = 11.sp, color = HarpTextSecondary)
                            }
                            Button(
                                onClick = onNavigateToAuditLogs,
                                colors = ButtonDefaults.buttonColors(containerColor = HarpCyan, contentColor = HarpNavyDark)
                            ) {
                                Text("VIEW LOGS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
