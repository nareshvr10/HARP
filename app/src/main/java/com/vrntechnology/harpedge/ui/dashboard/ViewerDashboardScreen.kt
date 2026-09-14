package com.vrntechnology.harpedge.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.vrntechnology.harpedge.data.model.SeverityLevel
import com.vrntechnology.harpedge.data.model.UserProfile
import com.vrntechnology.harpedge.ui.components.*
import com.vrntechnology.harpedge.ui.theme.*

@Composable
fun ViewerDashboardScreen(
    dashboardViewModel: DashboardViewModel,
    currentUser: UserProfile?,
    onNavigateToMap: () -> Unit,
    onNavigateToEnvironmental: () -> Unit = {},
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by dashboardViewModel.uiState.collectAsState()
    val activeEvent = uiState.events.firstOrNull { it.status == "ACTIVE" }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HarpNavyDark)
            .testTag("screen_viewer_dashboard")
    ) {
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
                            color = HarpPurple.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, HarpPurple.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "COMMUNITY OBSERVATORY",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = HarpPurple,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Public Environmental Safety Feed",
                        fontSize = 11.sp,
                        color = HarpTextSecondary
                    )
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

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Public Safety Advisory Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = HarpNavyElevated),
                    border = BorderStroke(
                        1.dp,
                        if (activeEvent != null) RiskHigh.copy(alpha = 0.5f) else RiskLow.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "REGIONAL ADVISORY STATUS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = HarpTextSecondary,
                                letterSpacing = 0.8.sp
                            )
                            SeverityBadge(severity = activeEvent?.severity ?: SeverityLevel.LOW)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (activeEvent != null)
                                "Notice: Elevated atmospheric readings detected in ${activeEvent.locationDescription}. Official verification in progress."
                            else
                                "Ambient baseline normal across all regional environmental monitoring zones.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = HarpTextPrimary,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = onNavigateToMap,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HarpTeal,
                                contentColor = HarpNavyDark
                            )
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("EXPLORE COMMUNITY REGIONAL MAP", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Public Telemetry Indicators
            item {
                Text(
                    text = "REGIONAL AMBIENT TELEMETRY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = HarpTextSecondary,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                val r = uiState.latestReadings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LiveSensorMetricCard(
                        name = "Air Temp",
                        value = "${r?.temperature ?: 34.8}",
                        unit = "°C",
                        icon = Icons.Default.Thermostat,
                        statusColor = HarpTeal,
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
                        name = "Particulate",
                        value = "${r?.pm25 ?: 18.4}",
                        unit = "µg/m³",
                        icon = Icons.Default.Grain,
                        statusColor = if ((r?.pm25 ?: 0.0) > 50.0) RiskHigh else RiskLow,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onNavigateToEnvironmental,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("btn_viewer_open_environmental"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, HarpTeal.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HarpTeal)
                ) {
                    Icon(Icons.Default.ShowChart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("VIEW DETAILED ENVIRONMENTAL CHARTS & AQI", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Public Notice Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = HarpNavyElevated),
                    border = BorderStroke(1.dp, HarpBorderColor)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = HarpTeal, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Public Safety Information",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = HarpTextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "HARP-Edge provides non-statutory decision-support telemetry. Always heed civil emergency bulletins published through civil defense emergency broadcast channels.",
                            fontSize = 12.sp,
                            color = HarpTextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
