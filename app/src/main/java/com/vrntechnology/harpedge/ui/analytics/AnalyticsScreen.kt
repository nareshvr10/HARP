package com.vrntechnology.harpedge.ui.analytics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.vrntechnology.harpedge.data.model.ResearchDatasetItem
import com.vrntechnology.harpedge.data.model.UserRole
import com.vrntechnology.harpedge.data.repository.HarpRepository
import com.vrntechnology.harpedge.ui.components.MetricBox
import com.vrntechnology.harpedge.ui.theme.*

@Composable
fun AnalyticsScreen(
    repository: HarpRepository,
    userRole: UserRole,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val researchItems by repository.researchDataset.collectAsState()
    val events by repository.events.collectAsState()
    var showExportToast by remember { mutableStateOf(false) }

    val totalEvents = events.size + researchItems.size
    val confirmedCount = researchItems.count { it.actualVerification.name.contains("CONFIRMED") }
    val falseAlarmCount = researchItems.count { it.actualVerification.name.contains("FALSE") }
    val sensorFaultCount = researchItems.count { it.actualVerification.name.contains("FAULT") }

    Scaffold(
        modifier = modifier.testTag("screen_analytics"),
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
                            text = "AI Analytics & Research Bench",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = HarpTextPrimary
                        )
                        Text(
                            text = "Model HF-RF-1.0 Validation Engine",
                            fontSize = 11.sp,
                            color = HarpTeal
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Model Performance KPIs
                item {
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
                                    text = "MODEL PERFORMANCE BENCHMARKS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HarpTextSecondary,
                                    letterSpacing = 0.8.sp
                                )
                                Text(
                                    text = "VALIDATED DATA",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HarpTeal
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MetricBox("Precision", "94.2%", HarpTeal, Modifier.weight(1f))
                                MetricBox("Recall", "91.8%", HarpCyan, Modifier.weight(1f))
                                MetricBox("F1 Score", "93.0%", HarpPurple, Modifier.weight(1f))
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MetricBox("False Alarm Rate", "4.8%", RiskLow, Modifier.weight(1f))
                                MetricBox("Mean Latency", "1.4s", HarpCyan, Modifier.weight(1f))
                                MetricBox("Features", "18 Eng.", HarpTextPrimary, Modifier.weight(1f))
                            }
                        }
                    }
                }

                // Incident Outcome Distribution
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = HarpNavyElevated),
                        border = BorderStroke(1.dp, HarpBorderColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "INCIDENT OUTCOME DISTRIBUTION",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = HarpTextSecondary,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                OutcomeStat("Total Incidents", "$totalEvents", HarpTextPrimary)
                                OutcomeStat("Confirmed Events", "$confirmedCount", RiskHigh)
                                OutcomeStat("False Alarms", "$falseAlarmCount", HarpCyan)
                                OutcomeStat("Sensor Faults", "$sensorFaultCount", SensorFaultColor)
                            }
                        }
                    }
                }

                // Research Dataset Feedback Loop Section (Admin only)
                if (userRole == UserRole.ADMIN) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "RESEARCH DATASET FEEDBACK LOOP",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = HarpTextSecondary,
                                letterSpacing = 0.8.sp
                            )
                            TextButton(onClick = { showExportToast = true }) {
                                Icon(Icons.Default.Download, contentDescription = null, tint = HarpTeal, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("EXPORT CSV", fontSize = 11.sp, color = HarpTeal, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (showExportToast) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = HarpTeal.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, HarpTeal.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Export generated: 'harp_edge_research_dataset_${System.currentTimeMillis()}.csv' ready for model retraining.",
                                    fontSize = 11.sp,
                                    color = HarpTextPrimary,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }

                    items(researchItems) { item ->
                        ResearchItemCard(item = item)
                    }
                }
            }
        }
    }
}

@Composable
fun OutcomeStat(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = color)
        Text(text = label, fontSize = 10.sp, color = HarpTextSecondary)
    }
}

@Composable
fun ResearchItemCard(item: ResearchDatasetItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = HarpNavyElevated),
        border = BorderStroke(1.dp, HarpBorderColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.recordId,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = HarpTeal
                )
                Text(
                    text = if (item.isMatch) "ACCURATE MATCH" else "MISMATCH / DRIFT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (item.isMatch) RiskLow else RiskMedium
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Predicted: ${item.predictedHazard.displayName} (${(item.predictedConfidence * 100).toInt()}%)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = HarpTextPrimary
            )
            Text(
                text = "Ground Truth: ${item.actualVerification.label}",
                fontSize = 12.sp,
                color = if (item.isMatch) RiskLow else RiskHigh
            )

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Consensus: ${item.consensusRatio}",
                    fontSize = 11.sp,
                    color = HarpTextSecondary
                )
                Text(
                    text = "Sensor Health: ${item.sensorHealth.toInt()}%",
                    fontSize = 11.sp,
                    color = HarpCyan
                )
            }
        }
    }
}
