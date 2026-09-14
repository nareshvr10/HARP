package com.vrntechnology.harpedge.ui.map

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
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
import com.vrntechnology.harpedge.data.model.SensorNode
import com.vrntechnology.harpedge.data.model.UserRole
import com.vrntechnology.harpedge.ui.components.InteractiveGisMap
import com.vrntechnology.harpedge.ui.dashboard.DashboardViewModel
import com.vrntechnology.harpedge.ui.theme.*

@Composable
fun MapScreen(
    dashboardViewModel: DashboardViewModel,
    userRole: UserRole,
    onNavigateBack: () -> Unit,
    onNavigateToEvent: (String) -> Unit,
    onNavigateToNode: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by dashboardViewModel.uiState.collectAsState()
    var selectedNode by remember { mutableStateOf<SensorNode?>(uiState.nodes.firstOrNull()) }

    Scaffold(
        modifier = modifier.testTag("screen_map"),
        containerColor = HarpNavyDark
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Interactive GIS Map Canvas
            InteractiveGisMap(
                nodes = uiState.nodes,
                activeEvents = uiState.events,
                selectedNode = selectedNode,
                userRole = userRole,
                onSelectNode = { selectedNode = it },
                onSelectEvent = { onNavigateToEvent(it.eventId) }
            )

            // Top Floating Navigation Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                shape = RoundedCornerShape(12.dp),
                color = HarpNavySurface.copy(alpha = 0.92f),
                border = BorderStroke(1.dp, HarpBorderColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = HarpTextPrimary)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = "Geospatial Intelligence Map",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = HarpTextPrimary
                            )
                            Text(
                                text = "Live Resonant Mesh Topology",
                                fontSize = 11.sp,
                                color = HarpTeal
                            )
                        }
                    }

                    if (selectedNode != null && userRole != UserRole.VIEWER) {
                        TextButton(
                            onClick = { selectedNode?.let { onNavigateToNode(it.nodeId) } }
                        ) {
                            Text("NODE DETAILS", fontSize = 11.sp, color = HarpCyan, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
