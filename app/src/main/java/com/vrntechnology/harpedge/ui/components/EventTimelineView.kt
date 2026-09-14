package com.vrntechnology.harpedge.ui.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vrntechnology.harpedge.data.model.TimelineEntry
import com.vrntechnology.harpedge.ui.theme.*

@Composable
fun EventTimelineView(
    timelineEntries: List<TimelineEntry>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("event_timeline_view")
    ) {
        timelineEntries.forEachIndexed { index, entry ->
            val isLast = index == timelineEntries.size - 1
            TimelineItemRow(
                entry = entry,
                isLast = isLast
            )
        }
    }
}

@Composable
private fun TimelineItemRow(
    entry: TimelineEntry,
    isLast: Boolean
) {
    val stageIcon: ImageVector = when {
        entry.stage.contains("Anomaly", ignoreCase = true) -> Icons.Default.Warning
        entry.stage.contains("Correlat", ignoreCase = true) -> Icons.Default.Fingerprint
        entry.stage.contains("Consensus", ignoreCase = true) -> Icons.Default.Hub
        entry.stage.contains("Alert", ignoreCase = true) -> Icons.Default.NotificationImportant
        entry.stage.contains("Authority", ignoreCase = true) -> Icons.Default.Shield
        entry.stage.contains("Verification", ignoreCase = true) -> Icons.Default.FactCheck
        else -> Icons.Default.Circle
    }

    val stageColor: Color = when {
        entry.stage.contains("Alert", ignoreCase = true) -> RiskHigh
        entry.stage.contains("Consensus", ignoreCase = true) -> HarpTeal
        entry.stage.contains("Verification", ignoreCase = true) -> HarpCyan
        else -> HarpPurple
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        // Time column
        Column(
            modifier = Modifier.width(62.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = entry.formattedTime,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = HarpTextSecondary
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Vertical line & Indicator node
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(26.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(stageColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = stageIcon,
                    contentDescription = null,
                    tint = stageColor,
                    modifier = Modifier.size(13.dp)
                )
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(48.dp)
                        .background(HarpBorderColor)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = entry.stage,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = stageColor
                )
                entry.nodeId?.let { id ->
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = HarpNavyDark,
                        border = androidx.compose.foundation.BorderStroke(0.8.dp, HarpBorderColor)
                    ) {
                        Text(
                            text = id,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = HarpTextSecondary,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = entry.description,
                fontSize = 12.sp,
                color = HarpTextPrimary,
                lineHeight = 16.sp
            )
        }
    }
}
