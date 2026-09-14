package com.vrntechnology.harpedge.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vrntechnology.harpedge.data.model.UserProfile
import com.vrntechnology.harpedge.ui.theme.*

@Composable
fun ProfileScreen(
    user: UserProfile?,
    onSignOut: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.testTag("screen_profile"),
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
                        text = "User Profile & Credentials",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = HarpTextPrimary
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(HarpTeal.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = HarpTeal,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Text(
                    text = user?.name ?: "Authorized User",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = HarpTextPrimary
                )
                Text(
                    text = user?.email ?: "user@harpedge.gov",
                    fontSize = 13.sp,
                    color = HarpTextSecondary
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = HarpNavyElevated,
                    border = BorderStroke(1.dp, HarpBorderColor)
                ) {
                    Text(
                        text = "ROLE: ${user?.role?.name ?: "VIEWER"}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = HarpTeal,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = HarpNavyElevated),
                    border = BorderStroke(1.dp, HarpBorderColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Organization: ${user?.organization ?: "National Environmental Agency"}",
                            fontSize = 13.sp,
                            color = HarpTextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Security Clearance: Level 4 Classified",
                            fontSize = 13.sp,
                            color = HarpCyan
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "App Identity: HARP-Edge (Smart India Hackathon 2024)",
                            fontSize = 12.sp,
                            color = HarpTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onSignOut,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_profile_sign_out"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RiskCritical.copy(alpha = 0.2f),
                        contentColor = RiskCritical
                    ),
                    border = BorderStroke(1.dp, RiskCritical.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SIGN OUT OF APPLICATION", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
