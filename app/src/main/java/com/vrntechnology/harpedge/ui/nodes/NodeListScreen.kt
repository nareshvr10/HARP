package com.vrntechnology.harpedge.ui.nodes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.vrntechnology.harpedge.data.model.NodeStatus
import com.vrntechnology.harpedge.data.model.SensorNode
import com.vrntechnology.harpedge.data.model.UserRole
import com.vrntechnology.harpedge.ui.components.NodeItemCard
import com.vrntechnology.harpedge.ui.theme.*

@Composable
fun NodeListScreen(
    nodes: List<SensorNode>,
    userRole: UserRole,
    onAddNode: () -> Unit,
    onSelectNode: (SensorNode) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf<NodeStatus?>(null) }

    val filteredNodes = nodes.filter { node ->
        val matchesSearch = node.nodeId.contains(searchQuery, ignoreCase = true) ||
                node.name.contains(searchQuery, ignoreCase = true) ||
                node.locationName.contains(searchQuery, ignoreCase = true)
        val matchesFilter = selectedFilter == null || node.status == selectedFilter
        matchesSearch && matchesFilter
    }

    Scaffold(
        modifier = modifier.testTag("screen_node_list"),
        containerColor = HarpNavyDark,
        floatingActionButton = {
            if (userRole == UserRole.ADMIN) {
                FloatingActionButton(
                    onClick = onAddNode,
                    containerColor = HarpTeal,
                    contentColor = HarpNavyDark,
                    modifier = Modifier.testTag("fab_add_node")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Node")
                }
            }
        }
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
                    Column {
                        Text(
                            text = "Sensor Node Network",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = HarpTextPrimary
                        )
                        Text(
                            text = "${nodes.count { it.isOnline }} of ${nodes.size} Nodes Transmitting",
                            fontSize = 11.sp,
                            color = HarpTeal
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search node ID, location, or zone...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = HarpTextSecondary)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_search_nodes"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HarpTeal,
                        unfocusedBorderColor = HarpBorderColor,
                        focusedTextColor = HarpTextPrimary,
                        unfocusedTextColor = HarpTextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedFilter == null,
                            onClick = { selectedFilter = null },
                            label = { Text("All (${nodes.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HarpTeal.copy(alpha = 0.2f),
                                selectedLabelColor = HarpTeal
                            )
                        )
                    }
                    items(NodeStatus.values()) { status ->
                        val count = nodes.count { it.status == status }
                        FilterChip(
                            selected = selectedFilter == status,
                            onClick = { selectedFilter = if (selectedFilter == status) null else status },
                            label = { Text("${status.label} ($count)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HarpTeal.copy(alpha = 0.2f),
                                selectedLabelColor = HarpTeal
                            )
                        )
                    }
                }
            }

            // Nodes List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredNodes) { node ->
                    NodeItemCard(
                        node = node,
                        onClick = { onSelectNode(node) }
                    )
                }

                if (filteredNodes.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No sensor nodes match your criteria.",
                                color = HarpTextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
