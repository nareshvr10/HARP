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
import com.vrntechnology.harpedge.data.repository.HarpRepository
import com.vrntechnology.harpedge.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNodeScreen(
    repository: HarpRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var nodeId by remember { mutableStateOf("NODE-006") }
    var name by remember { mutableStateOf("Chemical Buffer Extension") }
    var locationName by remember { mutableStateOf("Sub-station West Corridor") }
    var latitudeStr by remember { mutableStateOf("28.6195") }
    var longitudeStr by remember { mutableStateOf("77.2115") }

    val commTypes = listOf("LoRaWAN 868MHz", "Wi-Fi Mesh 2.4GHz", "ESP-NOW Sub-GHz", "Cellular NB-IoT")
    var selectedCommType by remember { mutableStateOf(commTypes.first()) }
    var commExpanded by remember { mutableStateOf(false) }

    val profiles = listOf("Environmental Multi-Sensor V1", "Industrial High-TVOC V2", "Hydrology & Flood Monitor")
    var selectedProfile by remember { mutableStateOf(profiles.first()) }
    var profileExpanded by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.testTag("screen_add_node"),
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
                        text = "Provision New Sensor Node",
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                errorMessage?.let { msg ->
                    Text(text = msg, color = RiskCritical, fontSize = 12.sp)
                }

                OutlinedTextField(
                    value = nodeId,
                    onValueChange = { nodeId = it.uppercase() },
                    label = { Text("Node Identifier (e.g. NODE-006)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_node_id"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HarpTeal,
                        unfocusedBorderColor = HarpBorderColor,
                        focusedTextColor = HarpTextPrimary,
                        unfocusedTextColor = HarpTextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Node Designation / Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_node_name"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HarpTeal,
                        unfocusedBorderColor = HarpBorderColor,
                        focusedTextColor = HarpTextPrimary,
                        unfocusedTextColor = HarpTextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = locationName,
                    onValueChange = { locationName = it },
                    label = { Text("Location Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_node_location"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HarpTeal,
                        unfocusedBorderColor = HarpBorderColor,
                        focusedTextColor = HarpTextPrimary,
                        unfocusedTextColor = HarpTextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = latitudeStr,
                        onValueChange = { latitudeStr = it },
                        label = { Text("Latitude") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_node_lat"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HarpTeal,
                            unfocusedBorderColor = HarpBorderColor,
                            focusedTextColor = HarpTextPrimary,
                            unfocusedTextColor = HarpTextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = longitudeStr,
                        onValueChange = { longitudeStr = it },
                        label = { Text("Longitude") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_node_lng"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HarpTeal,
                            unfocusedBorderColor = HarpBorderColor,
                            focusedTextColor = HarpTextPrimary,
                            unfocusedTextColor = HarpTextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Communication Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = commExpanded,
                    onExpandedChange = { commExpanded = !commExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCommType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Communication Architecture") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = commExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HarpTeal,
                            unfocusedBorderColor = HarpBorderColor,
                            focusedTextColor = HarpTextPrimary,
                            unfocusedTextColor = HarpTextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = commExpanded,
                        onDismissRequest = { commExpanded = false },
                        modifier = Modifier.background(HarpNavyElevated)
                    ) {
                        commTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type, color = HarpTextPrimary) },
                                onClick = {
                                    selectedCommType = type
                                    commExpanded = false
                                }
                            )
                        }
                    }
                }

                // Sensor Profile Dropdown
                ExposedDropdownMenuBox(
                    expanded = profileExpanded,
                    onExpandedChange = { profileExpanded = !profileExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedProfile,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Hardware Sensor Profile") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = profileExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HarpTeal,
                            unfocusedBorderColor = HarpBorderColor,
                            focusedTextColor = HarpTextPrimary,
                            unfocusedTextColor = HarpTextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = profileExpanded,
                        onDismissRequest = { profileExpanded = false },
                        modifier = Modifier.background(HarpNavyElevated)
                    ) {
                        profiles.forEach { prof ->
                            DropdownMenuItem(
                                text = { Text(prof, color = HarpTextPrimary) },
                                onClick = {
                                    selectedProfile = prof
                                    profileExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val lat = latitudeStr.toDoubleOrNull()
                        val lng = longitudeStr.toDoubleOrNull()
                        if (nodeId.isBlank() || name.isBlank() || lat == null || lng == null) {
                            errorMessage = "Please enter valid coordinates and node attributes."
                            return@Button
                        }
                        isSaving = true
                        val res = repository.addNode(
                            nodeId = nodeId,
                            name = name,
                            locationName = locationName,
                            latitude = lat,
                            longitude = lng,
                            communicationType = selectedCommType,
                            sensorProfile = selectedProfile
                        )
                        isSaving = false
                        res.onSuccess {
                            onNavigateBack()
                        }.onFailure {
                            errorMessage = it.localizedMessage ?: "Failed to add node"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_save_node"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HarpTeal,
                        contentColor = HarpNavyDark
                    ),
                    enabled = !isSaving
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("REGISTER & DEPLOY NODE", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
