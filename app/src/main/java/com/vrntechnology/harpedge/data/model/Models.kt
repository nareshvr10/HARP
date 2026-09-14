package com.vrntechnology.harpedge.data.model

enum class UserRole {
    ADMIN,
    AUTHORITY,
    VIEWER
}

enum class HazardType(val displayName: String, val description: String) {
    NONE("No Hazard", "Normal ambient baseline readings"),
    FIRE("Possible Fire Pattern", "Thermal anomaly correlated with particulate spike"),
    FLOOD("Possible Flood Pattern", "Water level elevation and rapid humidity surge"),
    POLLUTION("Air Quality / Pollution Event", "Sustained particulate elevation above safe limits"),
    CHEMICAL_EVENT("Possible Chemical / VOC Event", "Rapid TVOC surge exceeding threshold baseline"),
    EXTREME_HEAT("Extreme Heat Anomaly", "Ambient temperature exceeding thermal threshold"),
    LANDSLIDE_PRECURSOR("Landslide Precursor Pattern", "Soil moisture saturation with micro-vibrations"),
    SENSOR_FAULT("Sensor Anomaly / Drift", "Sensor integrity deviation without consensus support")
}

enum class SeverityLevel(val label: String) {
    LOW("LOW"),
    MEDIUM("MEDIUM"),
    HIGH("HIGH"),
    CRITICAL("CRITICAL")
}

enum class NodeStatus(val label: String) {
    HEALTHY("Healthy"),
    ANOMALY("Anomaly"),
    FAULT("Sensor Fault"),
    OFFLINE("Offline")
}

enum class VerificationStatus(val label: String) {
    PENDING("Verification Pending"),
    CONFIRMED_EVENT("Confirmed Event"),
    FALSE_ALARM("False Alarm"),
    SENSOR_FAULT("Sensor Fault"),
    UNABLE_TO_VERIFY("Unable to Verify")
}

data class UserProfile(
    val uid: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val organization: String = "National Environmental Agency",
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

data class SensorNode(
    val nodeId: String,
    val name: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val status: NodeStatus,
    val batteryLevel: Double, // 0 - 100
    val overallHealth: Double, // 0 - 100
    val firmwareVersion: String = "HARP-v2.4-ESP32",
    val communicationType: String = "LoRaWAN / Wi-Fi",
    val lastSeenTimestamp: Long = System.currentTimeMillis(),
    val active: Boolean = true,
    val isOnline: Boolean = true
)

data class SensorReadings(
    val readingId: String,
    val nodeId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val temperature: Double?, // Celsius
    val humidity: Double?, // %
    val pressure: Double?, // hPa
    val pm25: Double?, // µg/m³
    val pm10: Double?, // µg/m³
    val tvoc: Double?, // ppb
    val waterLevel: Double? = null, // cm
    val vibrationRms: Double? = null, // mm/s
    val windowSeconds: Int = 60
)

data class HazardFingerprint(
    val fingerprintId: String,
    val nodeId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val hazardType: HazardType,
    val confidence: Double, // 0.0 - 1.0 (e.g. 0.93)
    val temperatureDelta: Double?, // Normalized feature contribution % (0-100)
    val pmDelta: Double?,
    val vocDelta: Double?,
    val humidityDelta: Double?,
    val rateTemperature: Double?,
    val ratePm: Double?,
    val rateVoc: Double?,
    val neighbourAgreement: Double? = 80.0,
    val fingerprintVersion: String = "HF-RF-1.0",
    val explanation: String = "Temperature, PM, and VOC rates of change indicate pattern consistent with possible anomaly."
)

data class SensorHealth(
    val healthId: String,
    val nodeId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val temperatureHealth: Double = 98.0,
    val humidityHealth: Double = 95.0,
    val pressureHealth: Double = 99.0,
    val pmHealth: Double = 92.0,
    val vocHealth: Double = 88.0,
    val waterHealth: Double = 96.0,
    val vibrationHealth: Double = 97.0,
    val overallHealth: Double = 94.0,
    val diagnosedIssues: List<String> = emptyList()
)

data class HazardEvent(
    val eventId: String,
    val title: String,
    val hazardType: HazardType,
    val severity: SeverityLevel,
    val aiConfidence: Double, // e.g. 0.93 for 93%
    val sensorIntegrity: Double, // e.g. 0.91 for 91%
    val startTime: Long,
    val lastUpdated: Long,
    val supportingNodes: List<String>,
    val totalNearbyNodes: Int = 5,
    val affectedNodeCount: Int,
    val estimatedPropagationDirection: String? = "North-East",
    val estimatedSpeedKmh: Double? = 4.2,
    val status: String = "ACTIVE", // ACTIVE, RESOLVED, INVESTIGATING
    val verificationStatus: VerificationStatus = VerificationStatus.PENDING,
    val locationDescription: String = "Industrial Riverside Sector B"
)

data class TimelineEntry(
    val timestamp: Long,
    val formattedTime: String,
    val description: String,
    val stage: String,
    val nodeId: String? = null
)

data class AlertItem(
    val alertId: String,
    val eventId: String,
    val alertLevel: SeverityLevel,
    val title: String,
    val hazardType: HazardType,
    val confidence: Double,
    val supportingNodesCount: Int,
    val totalNodesCount: Int,
    val createdAt: Long,
    val acknowledged: Boolean = false,
    val acknowledgedBy: String? = null,
    val verificationStatus: VerificationStatus = VerificationStatus.PENDING
)

data class PropagationData(
    val propagationId: String,
    val eventId: String,
    val nodesSequence: List<String>,
    val estimatedDirection: String = "North-East",
    val estimatedSpeed: Double = 4.8, // km/h
    val confidence: Double = 0.88,
    val calculatedAt: Long = System.currentTimeMillis()
)

data class InspectionRecord(
    val inspectionId: String,
    val eventId: String,
    val authorityId: String,
    val authorityName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double,
    val longitude: Double,
    val notes: String,
    val verificationStatus: VerificationStatus,
    val evidenceUrls: List<String> = emptyList(),
    val isSynced: Boolean = true
)

data class SystemConfiguration(
    val samplingIntervalSeconds: Int = 5,
    val analysisWindowSeconds: Int = 60,
    val mediumConfidenceThreshold: Double = 0.65,
    val highConfidenceThreshold: Double = 0.85,
    val minimumConsensusNodes: Int = 3,
    val modelVersion: String = "HF-RF-1.0",
    val isDemoMode: Boolean = true
)

data class ResearchDatasetItem(
    val recordId: String,
    val eventId: String,
    val predictedHazard: HazardType,
    val predictedConfidence: Double,
    val actualVerification: VerificationStatus,
    val isMatch: Boolean,
    val sensorHealth: Double,
    val consensusRatio: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class AuditLogEntry(
    val logId: String,
    val action: String,
    val performedBy: String,
    val role: UserRole,
    val timestamp: Long = System.currentTimeMillis(),
    val details: String
)
