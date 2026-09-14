package com.vrntechnology.harpedge.ui.inspection

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
import com.vrntechnology.harpedge.data.model.HazardEvent
import com.vrntechnology.harpedge.data.model.UserProfile
import com.vrntechnology.harpedge.data.model.VerificationStatus
import com.vrntechnology.harpedge.data.repository.HarpRepository
import com.vrntechnology.harpedge.ui.components.SeverityBadge
import com.vrntechnology.harpedge.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FieldVerificationScreen(
    eventId: String,
    repository: HarpRepository,
    currentUser: UserProfile?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val events by repository.events.collectAsState()
    val event = events.find { it.eventId == eventId } ?: events.firstOrNull()

    var selectedStatus by remember { mutableStateOf(VerificationStatus.CONFIRMED_EVENT) }
    var statusExpanded by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("On-site perimeter inspection confirms industrial exhaust venting anomaly downwind.") }
    var latitudeStr by remember { mutableStateOf("28.6142") }
    var longitudeStr by remember { mutableStateOf("77.2104") }
    var hasPhotoAttached by remember { mutableStateOf(true) }
    var isSubmitted by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.testTag("screen_field_verification"),
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
                            text = "Field Incident Verification",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = HarpTextPrimary
                        )
                        Text(
                            text = "Incident ID: $eventId",
                            fontSize = 11.sp,
                            color = HarpCyan
                        )
                    }
                }
            }

            if (event == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Incident not found", color = HarpTextSecondary)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Incident Context Card
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
                                    text = event.hazardType.displayName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HarpTextPrimary
                                )
                                SeverityBadge(severity = event.severity)
                            }
                            Text(
                                text = event.locationDescription,
                                fontSize = 12.sp,
                                color = HarpTextSecondary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Model Confidence: ${(event.aiConfidence * 100).toInt()}% • Consensus: ${event.supportingNodes.size}/${event.totalNearbyNodes} Nodes",
                                fontSize = 12.sp,
                                color = HarpTeal,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Verification Form
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = HarpNavyElevated),
                        border = BorderStroke(1.dp, HarpBorderColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "OFFICIAL FIELD ASSESSMENT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = HarpTextSecondary,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            // Status Dropdown
                            ExposedDropdownMenuBox(
                                expanded = statusExpanded,
                                onExpandedChange = { statusExpanded = !statusExpanded }
                            ) {
                                OutlinedTextField(
                                    value = selectedStatus.label,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Ground Truth Determination") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                        .testTag("dropdown_verification_status"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = HarpTeal,
                                        unfocusedBorderColor = HarpBorderColor,
                                        focusedTextColor = HarpTextPrimary,
                                        unfocusedTextColor = HarpTextPrimary
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = statusExpanded,
                                    onDismissRequest = { statusExpanded = false },
                                    modifier = Modifier.background(HarpNavyElevated)
                                ) {
                                    VerificationStatus.values().forEach { st ->
                                        DropdownMenuItem(
                                            text = { Text(st.label, color = HarpTextPrimary) },
                                            onClick = {
                                                selectedStatus = st
                                                statusExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Location GPS
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = latitudeStr,
                                    onValueChange = { latitudeStr = it },
                                    label = { Text("Inspector Lat") },
                                    modifier = Modifier.weight(1f),
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
                                    label = { Text("Inspector Lng") },
                                    modifier = Modifier.weight(1f),
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

                            Spacer(modifier = Modifier.height(12.dp))

                            // Notes
                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                label = { Text("Field Inspection Notes & Observations") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .testTag("input_verification_notes"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = HarpTeal,
                                    unfocusedBorderColor = HarpBorderColor,
                                    focusedTextColor = HarpTextPrimary,
                                    unfocusedTextColor = HarpTextPrimary
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Photo evidence row
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = HarpNavyDark,
                                border = BorderStroke(1.dp, HarpBorderColor),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (hasPhotoAttached) Icons.Default.CheckCircle else Icons.Default.CameraAlt,
                                            contentDescription = null,
                                            tint = if (hasPhotoAttached) RiskLow else HarpTextSecondary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (hasPhotoAttached) "Photo Evidence Attached (photo_01.jpg)" else "Attach Photographic Evidence",
                                            fontSize = 12.sp,
                                            color = HarpTextPrimary
                                        )
                                    }
                                    TextButton(onClick = { hasPhotoAttached = !hasPhotoAttached }) {
                                        Text(if (hasPhotoAttached) "REMOVE" else "ATTACH", fontSize = 11.sp, color = HarpTeal)
                                    }
                                }
                            }
                        }
                    }

                    if (isSubmitted) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = RiskLow.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, RiskLow.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = RiskLow)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Verification successfully saved and synchronized into local Room database & Research feedback loop!",
                                    color = HarpTextPrimary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // Submit Button
                    Button(
                        onClick = {
                            val user = currentUser ?: return@Button
                            val lat = latitudeStr.toDoubleOrNull() ?: 28.6142
                            val lng = longitudeStr.toDoubleOrNull() ?: 77.2104
                            repository.submitInspection(
                                eventId = eventId,
                                authority = user,
                                latitude = lat,
                                longitude = lng,
                                notes = notes,
                                verificationStatus = selectedStatus,
                                evidenceUrls = if (hasPhotoAttached) listOf("https://storage.harpedge.gov/evidence/photo_01.jpg") else emptyList()
                            )
                            isSubmitted = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_submit_verification"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HarpTeal,
                            contentColor = HarpNavyDark
                        )
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SUBMIT FIELD VERIFICATION", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
