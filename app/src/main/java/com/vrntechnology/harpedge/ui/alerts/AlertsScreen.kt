package com.vrntechnology.harpedge.ui.alerts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.vrntechnology.harpedge.data.model.UserProfile
import com.vrntechnology.harpedge.data.repository.HarpRepository
import com.vrntechnology.harpedge.ui.components.AlertCard
import com.vrntechnology.harpedge.ui.theme.*

@Composable
fun AlertsScreen(
    repository: HarpRepository,
    currentUser: UserProfile?,
    onSelectAlert: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alerts by repository.alerts.collectAsState()

    Scaffold(
        modifier = modifier.testTag("screen_alerts"),
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
                            text = "Environmental Alert Center",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = HarpTextPrimary
                        )
                        Text(
                            text = "${alerts.count { !it.acknowledged }} Active Alerts Pending Verification",
                            fontSize = 11.sp,
                            color = RiskHigh
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(alerts) { alert ->
                    Column {
                        AlertCard(
                            alert = alert,
                            onClick = { onSelectAlert(alert.eventId) }
                        )

                        if (!alert.acknowledged && currentUser != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = { repository.acknowledgeAlert(alert.alertId, currentUser) }
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = HarpTeal, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("ACKNOWLEDGE ALERT", fontSize = 11.sp, color = HarpTeal, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
