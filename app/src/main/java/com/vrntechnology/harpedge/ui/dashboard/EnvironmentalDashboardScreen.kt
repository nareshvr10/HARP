package com.vrntechnology.harpedge.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.vrntechnology.harpedge.data.repository.HarpRepository
import com.vrntechnology.harpedge.ui.components.*
import com.vrntechnology.harpedge.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnvironmentalDashboardScreen(
    repository: HarpRepository,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val nodes by repository.nodes.collectAsState()
    val latestReading by repository.latestReadings.collectAsState()
    val currentScenario by repository.currentScenario.collectAsState()

    var selectedNodeId by remember { mutableStateOf("NODE-001") }
    var selectedTimeRange by remember { mutableStateOf(TimeRange.TWENTY_FOUR_HOURS) }
    var selectedMetric by remember { mutableStateOf(EnvironmentalMetric.PM25) }
    var showNodeMenu by remember { mutableStateOf(false) }

    val currentNode = nodes.find { it.nodeId == selectedNodeId } ?: nodes.firstOrNull()

    // Generate responsive time-series points
    val timeSeriesPoints = remember(selectedNodeId, selectedTimeRange, currentScenario, latestReading) {
        EnvironmentalDataService.generateTimeSeries(
            nodeId = selectedNodeId,
            timeRange = selectedTimeRange,
            currentScenario = currentScenario,
            latestReading = latestReading
        )
    }

    val latestPoint = timeSeriesPoints.lastOrNull()
    val aqiSummary = remember(latestPoint) {
        EnvironmentalDataService.computeAirQuality(latestPoint)
    }

    val activeStats = remember(timeSeriesPoints, selectedMetric) {
        EnvironmentalDataService.calculateStats(timeSeriesPoints, selectedMetric)
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("screen_environmental_dashboard"),
        containerColor = HarpNavyDark
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = HarpNavySurface,
                border = BorderStroke(0.8.dp, HarpBorderColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (onNavigateBack != null) {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = HarpTextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Column {
                            Text(
                                text = "Environmental Intelligence",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = HarpTextPrimary
                            )
                            Text(
                                text = "Atmospheric & Micro-Climate Diagnostics",
                                fontSize = 11.sp,
                                color = HarpTeal
                            )
                        }
                    }

                    // Node Selector Dropdown
                    Box {
                        Surface(
                            onClick = { showNodeMenu = true },
                            shape = RoundedCornerShape(8.dp),
                            color = HarpNavyDark,
                            border = BorderStroke(1.dp, HarpBorderColor),
                            modifier = Modifier.testTag("dropdown_node_selector")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (currentNode?.isOnline == true) RiskLow else StatusOffline)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = currentNode?.nodeId ?: "NODE-001",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HarpTextPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = HarpTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showNodeMenu,
                            onDismissRequest = { showNodeMenu = false },
                            modifier = Modifier.background(HarpNavyElevated)
                        ) {
                            nodes.forEach { node ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = "${node.nodeId} — ${node.name}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = HarpTextPrimary
                                            )
                                            Text(
                                                text = node.locationName,
                                                fontSize = 10.sp,
                                                color = HarpTextSecondary
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedNodeId = node.nodeId
                                        showNodeMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Scrollable Dashboard Body
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Time Range Segmented Selector
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "OBSERVATION WINDOW",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HarpTextSecondary,
                            letterSpacing = 0.8.sp
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            TimeRange.values().forEach { range ->
                                val isSelected = selectedTimeRange == range
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedTimeRange = range },
                                    label = {
                                        Text(
                                            text = range.label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = HarpTeal,
                                        selectedLabelColor = HarpNavyDark,
                                        containerColor = HarpNavyElevated,
                                        labelColor = HarpTextSecondary
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) HarpTeal else HarpBorderColor
                                    ),
                                    modifier = Modifier.testTag("chip_time_range_${range.label}")
                                )
                            }
                        }
                    }
                }

                // Air Quality Index Gauge & Primary Condition
                item {
                    AirQualityRadialGauge(
                        summary = aqiSummary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Main Interactive Line/Area Chart with Metric Filters
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_main_environmental_chart"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = HarpNavyElevated),
                        border = BorderStroke(1.dp, HarpBorderColor)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${selectedMetric.displayName.uppercase()} TREND",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = HarpTextPrimary
                                    )
                                    Text(
                                        text = "Current: ${activeStats.current} ${selectedMetric.unit} (${activeStats.statusLabel})",
                                        fontSize = 11.sp,
                                        color = HarpTeal
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = HarpNavyDark,
                                    border = BorderStroke(0.8.dp, HarpBorderColor)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(HarpTeal)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("LIVE BUFFER", fontSize = 9.sp, color = HarpTeal, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Metric selection row
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(EnvironmentalMetric.values()) { m ->
                                    val isSelected = selectedMetric == m
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedMetric = m },
                                        label = {
                                            Text(
                                                text = m.displayName,
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
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) HarpTeal else HarpBorderColor
                                        ),
                                        modifier = Modifier.testTag("filter_metric_${m.name.lowercase()}")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // The Line & Area Chart
                            val chartColor = when (selectedMetric) {
                                EnvironmentalMetric.TEMPERATURE -> RiskHigh
                                EnvironmentalMetric.HUMIDITY -> HarpCyan
                                EnvironmentalMetric.PM25, EnvironmentalMetric.PM10 -> HarpPurple
                                EnvironmentalMetric.TVOC -> RiskCritical
                                EnvironmentalMetric.WATER_LEVEL -> HarpCyan
                                EnvironmentalMetric.PRESSURE -> HarpTeal
                                EnvironmentalMetric.VIBRATION -> RiskMedium
                            }

                            EnvironmentalLineAreaChart(
                                points = timeSeriesPoints,
                                metric = selectedMetric,
                                lineColor = chartColor
                            )
                        }
                    }
                }

                // Hourly Distribution Bar Chart
                item {
                    EnvironmentalBarChart(
                        points = timeSeriesPoints.takeLast(12),
                        metric = selectedMetric,
                        barColor = HarpTeal
                    )
                }

                // Telemetry Metric Sparkline Grid
                item {
                    Text(
                        text = "ENVIRONMENTAL SENSOR MATRIX",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = HarpTextSecondary,
                        letterSpacing = 0.8.sp
                    )
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val tempStats = remember(timeSeriesPoints) {
                                EnvironmentalDataService.calculateStats(timeSeriesPoints, EnvironmentalMetric.TEMPERATURE)
                            }
                            EnvironmentalMetricSparkCard(
                                metric = EnvironmentalMetric.TEMPERATURE,
                                stats = tempStats,
                                points = timeSeriesPoints,
                                isSelected = selectedMetric == EnvironmentalMetric.TEMPERATURE,
                                onClick = { selectedMetric = EnvironmentalMetric.TEMPERATURE },
                                modifier = Modifier.weight(1f)
                            )

                            val humStats = remember(timeSeriesPoints) {
                                EnvironmentalDataService.calculateStats(timeSeriesPoints, EnvironmentalMetric.HUMIDITY)
                            }
                            EnvironmentalMetricSparkCard(
                                metric = EnvironmentalMetric.HUMIDITY,
                                stats = humStats,
                                points = timeSeriesPoints,
                                isSelected = selectedMetric == EnvironmentalMetric.HUMIDITY,
                                onClick = { selectedMetric = EnvironmentalMetric.HUMIDITY },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val pmStats = remember(timeSeriesPoints) {
                                EnvironmentalDataService.calculateStats(timeSeriesPoints, EnvironmentalMetric.PM25)
                            }
                            EnvironmentalMetricSparkCard(
                                metric = EnvironmentalMetric.PM25,
                                stats = pmStats,
                                points = timeSeriesPoints,
                                isSelected = selectedMetric == EnvironmentalMetric.PM25,
                                onClick = { selectedMetric = EnvironmentalMetric.PM25 },
                                modifier = Modifier.weight(1f)
                            )

                            val tvocStats = remember(timeSeriesPoints) {
                                EnvironmentalDataService.calculateStats(timeSeriesPoints, EnvironmentalMetric.TVOC)
                            }
                            EnvironmentalMetricSparkCard(
                                metric = EnvironmentalMetric.TVOC,
                                stats = tvocStats,
                                points = timeSeriesPoints,
                                isSelected = selectedMetric == EnvironmentalMetric.TVOC,
                                onClick = { selectedMetric = EnvironmentalMetric.TVOC },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val pressureStats = remember(timeSeriesPoints) {
                                EnvironmentalDataService.calculateStats(timeSeriesPoints, EnvironmentalMetric.PRESSURE)
                            }
                            EnvironmentalMetricSparkCard(
                                metric = EnvironmentalMetric.PRESSURE,
                                stats = pressureStats,
                                points = timeSeriesPoints,
                                isSelected = selectedMetric == EnvironmentalMetric.PRESSURE,
                                onClick = { selectedMetric = EnvironmentalMetric.PRESSURE },
                                modifier = Modifier.weight(1f)
                            )

                            val waterStats = remember(timeSeriesPoints) {
                                EnvironmentalDataService.calculateStats(timeSeriesPoints, EnvironmentalMetric.WATER_LEVEL)
                            }
                            EnvironmentalMetricSparkCard(
                                metric = EnvironmentalMetric.WATER_LEVEL,
                                stats = waterStats,
                                points = timeSeriesPoints,
                                isSelected = selectedMetric == EnvironmentalMetric.WATER_LEVEL,
                                onClick = { selectedMetric = EnvironmentalMetric.WATER_LEVEL },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Comparative Multi-Node Telemetry Overview
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
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
                                        imageVector = Icons.Default.CompareArrows,
                                        contentDescription = null,
                                        tint = HarpTeal,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "MULTI-NODE ENVIRONMENTAL MATRIX",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = HarpTextSecondary,
                                        letterSpacing = 0.8.sp
                                    )
                                }

                                Text(
                                    text = "${nodes.count { it.isOnline }}/${nodes.size} Online",
                                    fontSize = 10.sp,
                                    color = HarpCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            nodes.forEach { node ->
                                val isSelected = node.nodeId == selectedNodeId
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) HarpTeal.copy(alpha = 0.12f) else Color.Transparent)
                                        .clickable { selectedNodeId = node.nodeId }
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1.2f)) {
                                        Text(
                                            text = node.nodeId,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) HarpTeal else HarpTextPrimary
                                        )
                                        Text(
                                            text = node.name,
                                            fontSize = 10.sp,
                                            color = HarpTextSecondary,
                                            maxLines = 1
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.weight(1.8f),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("TEMP", fontSize = 8.sp, color = HarpTextMuted)
                                            Text(
                                                text = if (node.nodeId == "NODE-002" && currentScenario == "SENSOR_FAULT_DRIFT") "78.4°C" else "34.2°C",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = HarpTextPrimary
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("PM2.5", fontSize = 8.sp, color = HarpTextMuted)
                                            Text(
                                                text = if (currentScenario == "MULTI_NODE_HAZARD") "114 µg" else "18 µg",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = HarpTextPrimary
                                            )
                                        }

                                        StatusChip(status = node.status)
                                    }
                                }
                                if (node != nodes.last()) {
                                    Divider(
                                        color = HarpBorderColor.copy(alpha = 0.4f),
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Regulatory Safety Baseline Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = HarpNavyElevated),
                        border = BorderStroke(1.dp, HarpBorderColor)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = HarpTeal,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "REGULATORY GUIDELINE BENCHMARKS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HarpTextSecondary,
                                    letterSpacing = 0.8.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "• WHO PM2.5 24h Guideline: < 15 µg/m³\n• TVOC Safe Exposure Baseline: < 220 ppb\n• Extreme Thermal Threshold: > 40.0 °C\n• Hydrological Flood Warning: > 60.0 cm weir crest",
                                fontSize = 11.sp,
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
}
