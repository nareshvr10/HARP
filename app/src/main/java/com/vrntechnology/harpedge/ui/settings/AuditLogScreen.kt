package com.vrntechnology.harpedge.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Security
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
import com.vrntechnology.harpedge.data.model.AuditLogEntry
import com.vrntechnology.harpedge.data.repository.HarpRepository
import com.vrntechnology.harpedge.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AuditLogScreen(
    repository: HarpRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val logs by repository.auditLogs.collectAsState()
    val dateFormat = SimpleDateFormat("HH:mm:ss dd-MMM", Locale.getDefault())

    Scaffold(
        modifier = modifier.testTag("screen_audit_logs"),
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
                            text = "Security Audit Logs",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = HarpTextPrimary
                        )
                        Text(
                            text = "System Execution History",
                            fontSize = 11.sp,
                            color = HarpCyan
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(logs) { entry ->
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
                                    text = entry.action,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HarpTeal
                                )
                                Text(
                                    text = dateFormat.format(Date(entry.timestamp)),
                                    fontSize = 10.sp,
                                    color = HarpTextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = entry.details,
                                fontSize = 12.sp,
                                color = HarpTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "By: ${entry.performedBy} (${entry.role.name})",
                                fontSize = 10.sp,
                                color = HarpTextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
