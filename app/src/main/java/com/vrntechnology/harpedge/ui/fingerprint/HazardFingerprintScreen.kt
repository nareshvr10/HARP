package com.vrntechnology.harpedge.ui.fingerprint

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
import com.vrntechnology.harpedge.data.model.HazardFingerprint
import com.vrntechnology.harpedge.data.model.HazardType
import com.vrntechnology.harpedge.data.repository.HarpRepository
import com.vrntechnology.harpedge.ui.theme.*

@Composable
fun HazardFingerprintScreen(
    eventId: String,
    repository: HarpRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fingerprints by repository.hazardFingerprints.collectAsState()
    val fingerprint = fingerprints[eventId] ?: HazardFingerprint(
        fingerprintId = "HFP-$eventId",
        nodeId = "NODE-001",
        hazardType = HazardType.CHEMICAL_EVENT,
        confidence = 0.93,
        temperatureDelta = 82.0,
        pmDelta = 91.0,
        vocDelta = 94.0,
        humidityDelta = 47.0,
        rateTemperature = 87.0,
        ratePm = 89.0,
        rateVoc = 95.0,
        neighbourAgreement = 80.0
    )

    Scaffold(
        modifier = modifier.testTag("screen_hazard_fingerprint"),
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
                            text = "AI Hazard Fingerprint Analysis",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = HarpTextPrimary
                        )
                        Text(
                            text = "Model: ${fingerprint.fingerprintVersion} • Incident $eventId",
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
                // Classification Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = HarpNavyElevated),
                    border = BorderStroke(1.dp, HarpTeal.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "PRIMARY PATTERN CLASSIFICATION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HarpTextSecondary,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = fingerprint.hazardType.displayName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = HarpTextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Model Confidence: ${(fingerprint.confidence * 100).toInt()}%",
                                fontSize = 13.sp,
                                color = HarpTeal,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Spatial Consensus: ${fingerprint.neighbourAgreement?.toInt() ?: 80}%",
                                fontSize = 13.sp,
                                color = HarpCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Normalized Feature Contributions
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = HarpNavyElevated),
                    border = BorderStroke(1.dp, HarpBorderColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "NORMALIZED FEATURE CONTRIBUTIONS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HarpTextSecondary,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        FeatureBar("TVOC Concentration Surge", fingerprint.vocDelta ?: 94.0, RiskCritical)
                        FeatureBar("Particulate (PM2.5) Spike", fingerprint.pmDelta ?: 91.0, RiskHigh)
                        FeatureBar("Temperature Rate of Change (dT/dt)", fingerprint.rateTemperature ?: 87.0, RiskMedium)
                        FeatureBar("TVOC Rate of Change (dTVOC/dt)", fingerprint.rateVoc ?: 95.0, RiskCritical)
                        FeatureBar("Thermal Anomaly Deviation", fingerprint.temperatureDelta ?: 82.0, HarpTeal)
                        FeatureBar("Neighbour Node Spatial Agreement", fingerprint.neighbourAgreement ?: 80.0, HarpCyan)
                        FeatureBar("Humidity Depression Index", fingerprint.humidityDelta ?: 47.0, HarpPurple)
                    }
                }

                // Explainability Panel
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = HarpNavyElevated),
                    border = BorderStroke(1.dp, HarpBorderColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = HarpTeal, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Why was this event flagged?",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = HarpTextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = fingerprint.explanation,
                            fontSize = 13.sp,
                            color = HarpTextPrimary,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Notice: Feature contributions are normalized indicators, not absolute scientific probabilities. Designed for decision-support routing.",
                            fontSize = 11.sp,
                            color = HarpTextMuted,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureBar(label: String, value: Double, barColor: androidx.compose.ui.graphics.Color) {
    Column(modifier = Modifier.padding(vertical = 5.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 12.sp, color = HarpTextPrimary)
            Text("${value.toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = barColor)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (value / 100f).toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = barColor,
            trackColor = HarpBorderColor
        )
    }
}
