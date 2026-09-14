package com.vrntechnology.harpedge.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_nodes")
data class CachedNodeEntity(
    @PrimaryKey val nodeId: String,
    val name: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val status: String,
    val batteryLevel: Double,
    val overallHealth: Double,
    val communicationType: String,
    val lastSeenTimestamp: Long,
    val isOnline: Boolean
)

@Entity(tableName = "cached_readings")
data class CachedReadingEntity(
    @PrimaryKey val readingId: String,
    val nodeId: String,
    val timestamp: Long,
    val temperature: Double?,
    val humidity: Double?,
    val pressure: Double?,
    val pm25: Double?,
    val pm10: Double?,
    val tvoc: Double?,
    val waterLevel: Double?,
    val vibrationRms: Double?
)

@Entity(tableName = "cached_events")
data class CachedEventEntity(
    @PrimaryKey val eventId: String,
    val title: String,
    val hazardType: String,
    val severity: String,
    val aiConfidence: Double,
    val sensorIntegrity: Double,
    val startTime: Long,
    val lastUpdated: Long,
    val supportingNodesJson: String,
    val affectedNodeCount: Int,
    val estimatedPropagation: String?,
    val status: String,
    val verificationStatus: String
)

@Entity(tableName = "cached_alerts")
data class CachedAlertEntity(
    @PrimaryKey val alertId: String,
    val eventId: String,
    val alertLevel: String,
    val title: String,
    val hazardType: String,
    val confidence: Double,
    val supportingNodesCount: Int,
    val totalNodesCount: Int,
    val createdAt: Long,
    val acknowledged: Boolean
)

@Entity(tableName = "pending_inspections")
data class CachedInspectionEntity(
    @PrimaryKey val inspectionId: String,
    val eventId: String,
    val authorityId: String,
    val authorityName: String,
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val notes: String,
    val verificationStatus: String,
    val evidenceUrlsJson: String,
    val isSynced: Boolean = false
)
