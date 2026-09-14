package com.vrntechnology.harpedge.data.repository

import android.content.Context
import com.vrntechnology.harpedge.data.local.AppDatabase
import com.vrntechnology.harpedge.data.local.CachedAlertEntity
import com.vrntechnology.harpedge.data.local.CachedEventEntity
import com.vrntechnology.harpedge.data.local.CachedInspectionEntity
import com.vrntechnology.harpedge.data.local.CachedNodeEntity
import com.vrntechnology.harpedge.data.local.CachedReadingEntity
import com.vrntechnology.harpedge.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HarpRepository(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val dao = db.harpDao()
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    // Demo Mode & Connectivity State
    private val _isDemoMode = MutableStateFlow(true)
    val isDemoMode: StateFlow<Boolean> = _isDemoMode.asStateFlow()

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _lastCloudSyncTime = MutableStateFlow(System.currentTimeMillis())
    val lastCloudSyncTime: StateFlow<Long> = _lastCloudSyncTime.asStateFlow()

    // Config
    private val _configuration = MutableStateFlow(
        SystemConfiguration(
            samplingIntervalSeconds = 5,
            analysisWindowSeconds = 60,
            mediumConfidenceThreshold = 0.65,
            highConfidenceThreshold = 0.85,
            minimumConsensusNodes = 3,
            modelVersion = "HF-RF-1.0",
            isDemoMode = true
        )
    )
    val configuration: StateFlow<SystemConfiguration> = _configuration.asStateFlow()

    // Nodes State
    private val _nodes = MutableStateFlow<List<SensorNode>>(emptyList())
    val nodes: StateFlow<List<SensorNode>> = _nodes.asStateFlow()

    // Current Readings State
    private val _latestReadings = MutableStateFlow<SensorReadings?>(null)
    val latestReadings: StateFlow<SensorReadings?> = _latestReadings.asStateFlow()

    // Active Hazard Events
    private val _events = MutableStateFlow<List<HazardEvent>>(emptyList())
    val events: StateFlow<List<HazardEvent>> = _events.asStateFlow()

    // Alerts
    private val _alerts = MutableStateFlow<List<AlertItem>>(emptyList())
    val alerts: StateFlow<List<AlertItem>> = _alerts.asStateFlow()

    // Sensor Health mapping nodeId -> SensorHealth
    private val _sensorHealthMap = MutableStateFlow<Map<String, SensorHealth>>(emptyMap())
    val sensorHealthMap: StateFlow<Map<String, SensorHealth>> = _sensorHealthMap.asStateFlow()

    // Hazard Fingerprints mapping eventId/nodeId -> HazardFingerprint
    private val _hazardFingerprints = MutableStateFlow<Map<String, HazardFingerprint>>(emptyMap())
    val hazardFingerprints: StateFlow<Map<String, HazardFingerprint>> = _hazardFingerprints.asStateFlow()

    // Event Timelines mapping eventId -> List<TimelineEntry>
    private val _eventTimelines = MutableStateFlow<Map<String, List<TimelineEntry>>>(emptyMap())
    val eventTimelines: StateFlow<Map<String, List<TimelineEntry>>> = _eventTimelines.asStateFlow()

    // Field Inspections (offline / synced)
    private val _inspections = MutableStateFlow<List<InspectionRecord>>(emptyList())
    val inspections: StateFlow<List<InspectionRecord>> = _inspections.asStateFlow()

    // Research Dataset (Admin only feedback loop)
    private val _researchDataset = MutableStateFlow<List<ResearchDatasetItem>>(emptyList())
    val researchDataset: StateFlow<List<ResearchDatasetItem>> = _researchDataset.asStateFlow()

    // Audit Logs
    private val _auditLogs = MutableStateFlow<List<AuditLogEntry>>(emptyList())
    val auditLogs: StateFlow<List<AuditLogEntry>> = _auditLogs.asStateFlow()

    // Active Scenario State
    private val _currentScenario = MutableStateFlow("NORMAL")
    val currentScenario: StateFlow<String> = _currentScenario.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        val initialNodes = listOf(
            SensorNode(
                nodeId = "NODE-001",
                name = "River Zone Node Alpha",
                locationName = "Industrial Riverside Sector B",
                latitude = 28.6139,
                longitude = 77.2090,
                status = NodeStatus.HEALTHY,
                batteryLevel = 92.0,
                overallHealth = 96.0,
                communicationType = "LoRaWAN 868MHz",
                lastSeenTimestamp = System.currentTimeMillis() - 25000
            ),
            SensorNode(
                nodeId = "NODE-002",
                name = "Chemical Storage Buffer Node",
                locationName = "North Buffer Corridor Zone 3",
                latitude = 28.6180,
                longitude = 77.2140,
                status = NodeStatus.HEALTHY,
                batteryLevel = 84.0,
                overallHealth = 91.0,
                communicationType = "LoRaWAN / Wi-Fi Mesh",
                lastSeenTimestamp = System.currentTimeMillis() - 14000
            ),
            SensorNode(
                nodeId = "NODE-003",
                name = "Forest Perimeter Node Gamma",
                locationName = "Downwind Perimeter Sector 4",
                latitude = 28.6225,
                longitude = 77.2195,
                status = NodeStatus.HEALTHY,
                batteryLevel = 78.0,
                overallHealth = 95.0,
                communicationType = "LoRaWAN 868MHz",
                lastSeenTimestamp = System.currentTimeMillis() - 40000
            ),
            SensorNode(
                nodeId = "NODE-004",
                name = "Reservoir Drainage Monitor",
                locationName = "East Weir Hydrology Gate",
                latitude = 28.6080,
                longitude = 77.2250,
                status = NodeStatus.HEALTHY,
                batteryLevel = 88.0,
                overallHealth = 94.0,
                communicationType = "ESP-NOW / LoRa",
                lastSeenTimestamp = System.currentTimeMillis() - 60000
            ),
            SensorNode(
                nodeId = "NODE-005",
                name = "Urban Interface Node",
                locationName = "South Transit Boundary",
                latitude = 28.6010,
                longitude = 77.2020,
                status = NodeStatus.OFFLINE,
                batteryLevel = 18.0,
                overallHealth = 64.0,
                communicationType = "Wi-Fi 802.11 b/g/n",
                lastSeenTimestamp = System.currentTimeMillis() - 3600000,
                isOnline = false
            )
        )
        _nodes.value = initialNodes

        _latestReadings.value = SensorReadings(
            readingId = "RD-00109",
            nodeId = "NODE-001",
            temperature = 34.8,
            humidity = 61.2,
            pressure = 1012.4,
            pm25 = 18.4,
            pm10 = 32.1,
            tvoc = 142.0,
            waterLevel = 42.5,
            vibrationRms = 0.12
        )

        // Seed initial event: Possible Chemical Event
        val initialEvent = HazardEvent(
            eventId = "EVT-0042",
            title = "Possible Chemical / VOC Anomaly",
            hazardType = HazardType.CHEMICAL_EVENT,
            severity = SeverityLevel.HIGH,
            aiConfidence = 0.93,
            sensorIntegrity = 0.91,
            startTime = System.currentTimeMillis() - 900000,
            lastUpdated = System.currentTimeMillis() - 120000,
            supportingNodes = listOf("NODE-001", "NODE-002", "NODE-003", "NODE-004"),
            totalNearbyNodes = 5,
            affectedNodeCount = 4,
            estimatedPropagationDirection = "North-East",
            estimatedSpeedKmh = 4.8,
            status = "ACTIVE",
            verificationStatus = VerificationStatus.PENDING,
            locationDescription = "Industrial Riverside Sector B"
        )

        val initialEvent2 = HazardEvent(
            eventId = "EVT-0039",
            title = "Extreme Heat Warning Pattern",
            hazardType = HazardType.EXTREME_HEAT,
            severity = SeverityLevel.MEDIUM,
            aiConfidence = 0.81,
            sensorIntegrity = 0.96,
            startTime = System.currentTimeMillis() - 7200000,
            lastUpdated = System.currentTimeMillis() - 1800000,
            supportingNodes = listOf("NODE-001", "NODE-004"),
            totalNearbyNodes = 5,
            affectedNodeCount = 2,
            estimatedPropagationDirection = "South",
            estimatedSpeedKmh = 1.2,
            status = "MONITORING",
            verificationStatus = VerificationStatus.CONFIRMED_EVENT,
            locationDescription = "Southern Transit Corridor"
        )
        _events.value = listOf(initialEvent, initialEvent2)

        _alerts.value = listOf(
            AlertItem(
                alertId = "ALT-881",
                eventId = "EVT-0042",
                alertLevel = SeverityLevel.HIGH,
                title = "Possible Chemical Event Detected",
                hazardType = HazardType.CHEMICAL_EVENT,
                confidence = 0.93,
                supportingNodesCount = 4,
                totalNodesCount = 5,
                createdAt = System.currentTimeMillis() - 120000,
                acknowledged = false
            ),
            AlertItem(
                alertId = "ALT-879",
                eventId = "EVT-0039",
                alertLevel = SeverityLevel.MEDIUM,
                title = "Extreme Heat Anomaly",
                hazardType = HazardType.EXTREME_HEAT,
                confidence = 0.81,
                supportingNodesCount = 2,
                totalNodesCount = 5,
                createdAt = System.currentTimeMillis() - 3600000,
                acknowledged = true,
                acknowledgedBy = "Officer Marcus Vance"
            )
        )

        // Seed Sensor Health
        val healthMap = mapOf(
            "NODE-001" to SensorHealth(
                healthId = "SH-001",
                nodeId = "NODE-001",
                temperatureHealth = 97.0,
                humidityHealth = 94.0,
                pressureHealth = 99.0,
                pmHealth = 91.0,
                vocHealth = 78.0,
                waterHealth = 96.0,
                vibrationHealth = 98.0,
                overallHealth = 91.0,
                diagnosedIssues = listOf("Possible VOC sensor drift / calibration check recommended")
            ),
            "NODE-002" to SensorHealth(
                healthId = "SH-002",
                nodeId = "NODE-002",
                temperatureHealth = 98.0,
                humidityHealth = 96.0,
                pressureHealth = 99.0,
                pmHealth = 95.0,
                vocHealth = 92.0,
                waterHealth = 95.0,
                vibrationHealth = 97.0,
                overallHealth = 96.0
            ),
            "NODE-003" to SensorHealth(
                healthId = "SH-003",
                nodeId = "NODE-003",
                temperatureHealth = 96.0,
                humidityHealth = 95.0,
                pressureHealth = 98.0,
                pmHealth = 94.0,
                vocHealth = 90.0,
                waterHealth = 97.0,
                vibrationHealth = 96.0,
                overallHealth = 95.0
            )
        )
        _sensorHealthMap.value = healthMap

        // Seed Hazard Fingerprint
        _hazardFingerprints.value = mapOf(
            "EVT-0042" to HazardFingerprint(
                fingerprintId = "HFP-0042",
                nodeId = "NODE-001",
                hazardType = HazardType.CHEMICAL_EVENT,
                confidence = 0.93,
                temperatureDelta = 82.0,
                pmDelta = 91.0,
                vocDelta = 94.0,
                humidityDelta = 47.0,
                rateTemperature = 87.0,
                ratePm = 89.0,
                rateVoc = 95.0,
                neighbourAgreement = 80.0,
                explanation = "Normalized feature indicators show significant concurrent surge in TVOC and PM2.5 with 4/5 neighbour node spatial agreement."
            )
        )

        // Seed Event Timeline
        _eventTimelines.value = mapOf(
            "EVT-0042" to listOf(
                TimelineEntry(
                    timestamp = System.currentTimeMillis() - 840000,
                    formattedTime = "14:21:02",
                    description = "Node 01 detected TVOC baseline deviation exceeding 120 ppb",
                    stage = "Anomaly Detected",
                    nodeId = "NODE-001"
                ),
                TimelineEntry(
                    timestamp = System.currentTimeMillis() - 790000,
                    formattedTime = "14:21:41",
                    description = "Node 02 generated matching chemical/particulate fingerprint",
                    stage = "Fingerprint Correlated",
                    nodeId = "NODE-002"
                ),
                TimelineEntry(
                    timestamp = System.currentTimeMillis() - 750000,
                    formattedTime = "14:22:10",
                    description = "Node 03 confirmed matching resonant pattern downwind",
                    stage = "Spatial Verification",
                    nodeId = "NODE-003"
                ),
                TimelineEntry(
                    timestamp = System.currentTimeMillis() - 730000,
                    formattedTime = "14:22:14",
                    description = "Neighbour consensus reached (4 / 5 active nodes agree)",
                    stage = "Consensus Established"
                ),
                TimelineEntry(
                    timestamp = System.currentTimeMillis() - 710000,
                    formattedTime = "14:22:18",
                    description = "HIGH alert generated: Possible Chemical Event (Estimated Propagation: North-East)",
                    stage = "Alert Raised"
                ),
                TimelineEntry(
                    timestamp = System.currentTimeMillis() - 650000,
                    formattedTime = "14:23:05",
                    description = "Authority & field dispatch notified via FCM",
                    stage = "Authority Notified"
                ),
                TimelineEntry(
                    timestamp = System.currentTimeMillis() - 120000,
                    formattedTime = "14:35:20",
                    description = "Field verification pending on-site sensor inspection",
                    stage = "Field Verification"
                )
            )
        )

        // Seed Research Records
        _researchDataset.value = listOf(
            ResearchDatasetItem(
                recordId = "REC-101",
                eventId = "EVT-0038",
                predictedHazard = HazardType.FIRE,
                predictedConfidence = 0.89,
                actualVerification = VerificationStatus.CONFIRMED_EVENT,
                isMatch = true,
                sensorHealth = 95.0,
                consensusRatio = "4 / 4",
                timestamp = System.currentTimeMillis() - 86400000 * 2
            ),
            ResearchDatasetItem(
                recordId = "REC-102",
                eventId = "EVT-0035",
                predictedHazard = HazardType.CHEMICAL_EVENT,
                predictedConfidence = 0.74,
                actualVerification = VerificationStatus.FALSE_ALARM,
                isMatch = false,
                sensorHealth = 88.0,
                consensusRatio = "2 / 5",
                timestamp = System.currentTimeMillis() - 86400000 * 4
            ),
            ResearchDatasetItem(
                recordId = "REC-103",
                eventId = "EVT-0031",
                predictedHazard = HazardType.SENSOR_FAULT,
                predictedConfidence = 0.94,
                actualVerification = VerificationStatus.SENSOR_FAULT,
                isMatch = true,
                sensorHealth = 61.0,
                consensusRatio = "1 / 4",
                timestamp = System.currentTimeMillis() - 86400000 * 7
            )
        )

        // Seed Audit Logs
        _auditLogs.value = listOf(
            AuditLogEntry(
                logId = "LOG-01",
                action = "SYSTEM_INITIALIZED",
                performedBy = "System",
                role = UserRole.ADMIN,
                timestamp = System.currentTimeMillis() - 3600000 * 5,
                details = "HARP-Edge intelligence engine booted with model HF-RF-1.0"
            ),
            AuditLogEntry(
                logId = "LOG-02",
                action = "ALERT_ACKNOWLEDGED",
                performedBy = "Officer Marcus Vance",
                role = UserRole.AUTHORITY,
                timestamp = System.currentTimeMillis() - 3600000,
                details = "Acknowledged Alert #ALT-879 (Extreme Heat Anomaly)"
            ),
            AuditLogEntry(
                logId = "LOG-03",
                action = "NODE_STATUS_CHECK",
                performedBy = "System Watchdog",
                role = UserRole.ADMIN,
                timestamp = System.currentTimeMillis() - 1800000,
                details = "Node NODE-005 flagged offline due to timeout (>1 hr)"
            )
        )

        // Asynchronously save to Room for offline readiness
        saveCurrentStateToRoom()
    }

    private fun saveCurrentStateToRoom() {
        repositoryScope.launch {
            try {
                val nodeEntities = _nodes.value.map {
                    CachedNodeEntity(
                        nodeId = it.nodeId,
                        name = it.name,
                        locationName = it.locationName,
                        latitude = it.latitude,
                        longitude = it.longitude,
                        status = it.status.name,
                        batteryLevel = it.batteryLevel,
                        overallHealth = it.overallHealth,
                        communicationType = it.communicationType,
                        lastSeenTimestamp = it.lastSeenTimestamp,
                        isOnline = it.isOnline
                    )
                }
                dao.insertNodes(nodeEntities)

                val eventEntities = _events.value.map {
                    CachedEventEntity(
                        eventId = it.eventId,
                        title = it.title,
                        hazardType = it.hazardType.name,
                        severity = it.severity.name,
                        aiConfidence = it.aiConfidence,
                        sensorIntegrity = it.sensorIntegrity,
                        startTime = it.startTime,
                        lastUpdated = it.lastUpdated,
                        supportingNodesJson = it.supportingNodes.joinToString(","),
                        affectedNodeCount = it.affectedNodeCount,
                        estimatedPropagation = it.estimatedPropagationDirection,
                        status = it.status,
                        verificationStatus = it.verificationStatus.name
                    )
                }
                dao.insertEvents(eventEntities)

                val alertEntities = _alerts.value.map {
                    CachedAlertEntity(
                        alertId = it.alertId,
                        eventId = it.eventId,
                        alertLevel = it.alertLevel.name,
                        title = it.title,
                        hazardType = it.hazardType.name,
                        confidence = it.confidence,
                        supportingNodesCount = it.supportingNodesCount,
                        totalNodesCount = it.totalNodesCount,
                        createdAt = it.createdAt,
                        acknowledged = it.acknowledged
                    )
                }
                dao.insertAlerts(alertEntities)
            } catch (e: Exception) {
                // Room cache persistence fallback
            }
        }
    }

    // Toggle Demo Mode
    fun setDemoMode(enabled: Boolean) {
        _isDemoMode.value = enabled
        _configuration.value = _configuration.value.copy(isDemoMode = enabled)
        logAudit(
            action = "CONFIG_CHANGE",
            by = "System",
            role = UserRole.ADMIN,
            details = "Operational mode switched to ${if (enabled) "DEMO MODE" else "REAL DATA MODE"}"
        )
    }

    // Toggle Online/Offline State
    fun setOnlineState(online: Boolean) {
        _isOnline.value = online
        if (online) {
            triggerOfflineSync()
        }
    }

    // Execute Demo Scenario 1: Multi-node Hazard Consensus
    fun triggerScenarioMultiNodeConsensus() {
        _currentScenario.value = "MULTI_NODE_HAZARD"

        // Update nodes to reflect anomaly sequence
        val updatedNodes = _nodes.value.map { node ->
            when (node.nodeId) {
                "NODE-001" -> node.copy(status = NodeStatus.ANOMALY, overallHealth = 89.0)
                "NODE-002" -> node.copy(status = NodeStatus.ANOMALY, overallHealth = 91.0)
                "NODE-003" -> node.copy(status = NodeStatus.ANOMALY, overallHealth = 93.0)
                else -> node
            }
        }
        _nodes.value = updatedNodes

        // Spike in readings
        _latestReadings.value = SensorReadings(
            readingId = "RD-${System.currentTimeMillis() % 10000}",
            nodeId = "NODE-001",
            temperature = 42.6,
            humidity = 38.4,
            pressure = 1008.2,
            pm25 = 114.5,
            pm10 = 198.0,
            tvoc = 860.0,
            waterLevel = 41.0,
            vibrationRms = 0.28
        )

        val newEvent = HazardEvent(
            eventId = "EVT-DEMO-01",
            title = "Possible Chemical / Fire Pattern",
            hazardType = HazardType.CHEMICAL_EVENT,
            severity = SeverityLevel.CRITICAL,
            aiConfidence = 0.94,
            sensorIntegrity = 0.92,
            startTime = System.currentTimeMillis() - 180000,
            lastUpdated = System.currentTimeMillis(),
            supportingNodes = listOf("NODE-001", "NODE-002", "NODE-003"),
            totalNearbyNodes = 3,
            affectedNodeCount = 3,
            estimatedPropagationDirection = "North-East (Speed: 5.4 km/h)",
            estimatedSpeedKmh = 5.4,
            status = "ACTIVE",
            verificationStatus = VerificationStatus.PENDING,
            locationDescription = "Industrial Riverside Corridor"
        )
        _events.value = listOf(newEvent) + _events.value.filter { it.eventId != newEvent.eventId }

        val newAlert = AlertItem(
            alertId = "ALT-DEMO-01",
            eventId = "EVT-DEMO-01",
            alertLevel = SeverityLevel.CRITICAL,
            title = "High Risk: Multi-Node Resonant Hazard",
            hazardType = HazardType.CHEMICAL_EVENT,
            confidence = 0.94,
            supportingNodesCount = 3,
            totalNodesCount = 3,
            createdAt = System.currentTimeMillis(),
            acknowledged = false
        )
        _alerts.value = listOf(newAlert) + _alerts.value.filter { it.alertId != newAlert.alertId }

        _hazardFingerprints.value = _hazardFingerprints.value + ("EVT-DEMO-01" to HazardFingerprint(
            fingerprintId = "HFP-DEMO-01",
            nodeId = "NODE-001",
            hazardType = HazardType.CHEMICAL_EVENT,
            confidence = 0.94,
            temperatureDelta = 92.0,
            pmDelta = 96.0,
            vocDelta = 98.0,
            humidityDelta = 64.0,
            rateTemperature = 91.0,
            ratePm = 94.0,
            rateVoc = 99.0,
            neighbourAgreement = 100.0,
            explanation = "Spatial consensus verified: 3/3 nodes show coordinated TVOC and particulate deviation propagating North-East."
        ))

        _eventTimelines.value = _eventTimelines.value + ("EVT-DEMO-01" to listOf(
            TimelineEntry(System.currentTimeMillis() - 180000, "15:00:10", "NODE-001 detected sharp TVOC surge (860 ppb)", "Anomaly Initiated", "NODE-001"),
            TimelineEntry(System.currentTimeMillis() - 140000, "15:00:50", "NODE-002 confirmed correlated particulate rise downwind", "Correlated", "NODE-002"),
            TimelineEntry(System.currentTimeMillis() - 90000, "15:01:40", "NODE-003 matched resonant pattern (Consensus: 3/3)", "Consensus 3/3", "NODE-003"),
            TimelineEntry(System.currentTimeMillis() - 30000, "15:02:40", "Estimated Propagation confirmed: North-East vector", "Propagation Tracked"),
            TimelineEntry(System.currentTimeMillis(), "15:03:10", "CRITICAL Alert issued. Field verification required.", "Alert Issued")
        ))

        logAudit("DEMO_SCENARIO", "Presenter", UserRole.ADMIN, "Executed Scenario 1: Multi-node Hazard Consensus")
        saveCurrentStateToRoom()
    }

    // Execute Demo Scenario 2: Sensor Fault / Drift
    fun triggerScenarioSensorFault() {
        _currentScenario.value = "SENSOR_FAULT_DRIFT"

        // Single node anomaly, neighbours normal
        val updatedNodes = _nodes.value.map { node ->
            if (node.nodeId == "NODE-002") {
                node.copy(status = NodeStatus.FAULT, overallHealth = 68.0)
            } else {
                node.copy(status = NodeStatus.HEALTHY, overallHealth = 96.0)
            }
        }
        _nodes.value = updatedNodes

        // NODE-002 temp sensor abnormal reading, but other readings normal & neighbours do not agree
        _latestReadings.value = SensorReadings(
            readingId = "RD-${System.currentTimeMillis() % 10000}",
            nodeId = "NODE-002",
            temperature = 78.4, // Absurd temp spike alone
            humidity = 58.0,
            pressure = 1013.0,
            pm25 = 12.0, // Normal
            pm10 = 22.0, // Normal
            tvoc = 90.0, // Normal
            waterLevel = 40.0,
            vibrationRms = 0.05
        )

        val faultEvent = HazardEvent(
            eventId = "EVT-FAULT-02",
            title = "Sensor Anomaly: Possible Temperature Sensor Drift",
            hazardType = HazardType.SENSOR_FAULT,
            severity = SeverityLevel.LOW,
            aiConfidence = 0.91,
            sensorIntegrity = 0.52, // Degraded integrity
            startTime = System.currentTimeMillis() - 60000,
            lastUpdated = System.currentTimeMillis(),
            supportingNodes = listOf("NODE-002"),
            totalNearbyNodes = 4,
            affectedNodeCount = 1,
            estimatedPropagationDirection = "None (Isolated Node)",
            estimatedSpeedKmh = 0.0,
            status = "ACTIVE",
            verificationStatus = VerificationStatus.SENSOR_FAULT,
            locationDescription = "North Buffer Corridor Zone 3"
        )
        _events.value = listOf(faultEvent) + _events.value.filter { it.eventId != faultEvent.eventId }

        val faultAlert = AlertItem(
            alertId = "ALT-FAULT-02",
            eventId = "EVT-FAULT-02",
            alertLevel = SeverityLevel.LOW,
            title = "Sensor Integrity Anomaly: NODE-002",
            hazardType = HazardType.SENSOR_FAULT,
            confidence = 0.91,
            supportingNodesCount = 1,
            totalNodesCount = 4,
            createdAt = System.currentTimeMillis(),
            acknowledged = false
        )
        _alerts.value = listOf(faultAlert) + _alerts.value.filter { it.alertId != faultAlert.alertId }

        _sensorHealthMap.value = _sensorHealthMap.value + ("NODE-002" to SensorHealth(
            healthId = "SH-002-FAULT",
            nodeId = "NODE-002",
            temperatureHealth = 48.0, // Severe drift
            humidityHealth = 96.0,
            pressureHealth = 99.0,
            pmHealth = 95.0,
            vocHealth = 92.0,
            waterHealth = 95.0,
            vibrationHealth = 97.0,
            overallHealth = 68.0,
            diagnosedIssues = listOf("Temperature sensor reading exceeds physical gradient. Neighbor consensus 0/3. Isolated sensor drift detected.")
        ))

        _eventTimelines.value = _eventTimelines.value + ("EVT-FAULT-02" to listOf(
            TimelineEntry(System.currentTimeMillis() - 60000, "15:10:00", "NODE-002 reported isolated thermal spike to 78.4°C", "Thermal Spike", "NODE-002"),
            TimelineEntry(System.currentTimeMillis() - 40000, "15:10:20", "Neighbour nodes queried: NODE-001 (34.8°C), NODE-003 (33.9°C)", "Spatial Cross-Check"),
            TimelineEntry(System.currentTimeMillis() - 20000, "15:10:40", "Consensus: 0 / 3 neighbours support event. Particulate & VOC normal.", "Zero Spatial Consensus"),
            TimelineEntry(System.currentTimeMillis(), "15:11:00", "Classified as Sensor Anomaly / Drift. Emergency escalation avoided.", "Decision Support Safe Guard")
        ))

        logAudit("DEMO_SCENARIO", "Presenter", UserRole.ADMIN, "Executed Scenario 2: Sensor Fault / Drift Demonstration")
        saveCurrentStateToRoom()
    }

    // Reset Scenarios back to clean Baseline
    fun resetToBaseline() {
        _currentScenario.value = "NORMAL"
        loadInitialData()
        logAudit("RESET", "Admin", UserRole.ADMIN, "System state reset to initial baseline")
    }

    // Node Operations
    fun addNode(
        nodeId: String,
        name: String,
        locationName: String,
        latitude: Double,
        longitude: Double,
        communicationType: String,
        sensorProfile: String
    ): Result<SensorNode> {
        val existing = _nodes.value.find { it.nodeId == nodeId }
        if (existing != null) {
            return Result.failure(IllegalArgumentException("Node with ID $nodeId already exists"))
        }
        val newNode = SensorNode(
            nodeId = nodeId,
            name = name,
            locationName = locationName,
            latitude = latitude,
            longitude = longitude,
            status = NodeStatus.HEALTHY,
            batteryLevel = 100.0,
            overallHealth = 100.0,
            communicationType = communicationType,
            lastSeenTimestamp = System.currentTimeMillis(),
            active = true
        )
        _nodes.value = _nodes.value + newNode
        logAudit("NODE_CREATED", "Admin", UserRole.ADMIN, "Provisioned new sensor node $nodeId ($name)")
        saveCurrentStateToRoom()
        return Result.success(newNode)
    }

    fun disableNode(nodeId: String) {
        _nodes.value = _nodes.value.map {
            if (it.nodeId == nodeId) it.copy(active = false, status = NodeStatus.OFFLINE) else it
        }
        logAudit("NODE_DISABLED", "Admin", UserRole.ADMIN, "Disabled node $nodeId")
        saveCurrentStateToRoom()
    }

    fun acknowledgeAlert(alertId: String, user: UserProfile) {
        _alerts.value = _alerts.value.map {
            if (it.alertId == alertId) it.copy(acknowledged = true, acknowledgedBy = user.name) else it
        }
        logAudit("ALERT_ACK", user.name, user.role, "Acknowledged alert $alertId")
        saveCurrentStateToRoom()
    }

    // Field Verification (Authority only)
    fun submitInspection(
        eventId: String,
        authority: UserProfile,
        latitude: Double,
        longitude: Double,
        notes: String,
        verificationStatus: VerificationStatus,
        evidenceUrls: List<String>
    ): InspectionRecord {
        val isOnlineNow = _isOnline.value
        val inspection = InspectionRecord(
            inspectionId = "INSP-${System.currentTimeMillis() % 100000}",
            eventId = eventId,
            authorityId = authority.uid,
            authorityName = authority.name,
            timestamp = System.currentTimeMillis(),
            latitude = latitude,
            longitude = longitude,
            notes = notes,
            verificationStatus = verificationStatus,
            evidenceUrls = evidenceUrls,
            isSynced = isOnlineNow
        )

        _inspections.value = listOf(inspection) + _inspections.value

        // Update corresponding event
        _events.value = _events.value.map { evt ->
            if (evt.eventId == eventId) {
                evt.copy(
                    verificationStatus = verificationStatus,
                    status = if (verificationStatus == VerificationStatus.CONFIRMED_EVENT) "CONFIRMED" else "CLOSED",
                    lastUpdated = System.currentTimeMillis()
                )
            } else evt
        }

        // Add to Research Dataset loop
        val targetEvent = _events.value.find { it.eventId == eventId }
        if (targetEvent != null) {
            val isMatch = (targetEvent.hazardType == HazardType.SENSOR_FAULT && verificationStatus == VerificationStatus.SENSOR_FAULT) ||
                    (targetEvent.severity != SeverityLevel.LOW && verificationStatus == VerificationStatus.CONFIRMED_EVENT)
            val researchItem = ResearchDatasetItem(
                recordId = "REC-${System.currentTimeMillis() % 10000}",
                eventId = eventId,
                predictedHazard = targetEvent.hazardType,
                predictedConfidence = targetEvent.aiConfidence,
                actualVerification = verificationStatus,
                isMatch = isMatch,
                sensorHealth = targetEvent.sensorIntegrity * 100,
                consensusRatio = "${targetEvent.supportingNodes.size} / ${targetEvent.totalNearbyNodes}",
                timestamp = System.currentTimeMillis()
            )
            _researchDataset.value = listOf(researchItem) + _researchDataset.value
        }

        // Add timeline record
        val currentTimeline = _eventTimelines.value[eventId] ?: emptyList()
        val newTimelineEntry = TimelineEntry(
            timestamp = System.currentTimeMillis(),
            formattedTime = "Now",
            description = "Field Verification submitted by ${authority.name}: ${verificationStatus.label}. Notes: $notes",
            stage = "Verification Completed"
        )
        _eventTimelines.value = _eventTimelines.value + (eventId to (currentTimeline + newTimelineEntry))

        // Store into Room queue
        repositoryScope.launch {
            try {
                dao.insertInspection(
                    CachedInspectionEntity(
                        inspectionId = inspection.inspectionId,
                        eventId = inspection.eventId,
                        authorityId = inspection.authorityId,
                        authorityName = inspection.authorityName,
                        timestamp = inspection.timestamp,
                        latitude = inspection.latitude,
                        longitude = inspection.longitude,
                        notes = inspection.notes,
                        verificationStatus = inspection.verificationStatus.name,
                        evidenceUrlsJson = evidenceUrls.joinToString(","),
                        isSynced = isOnlineNow
                    )
                )
            } catch (e: Exception) {
                // Room fallback
            }
        }

        logAudit("FIELD_VERIFICATION", authority.name, authority.role, "Submitted verification for $eventId: ${verificationStatus.label}")
        return inspection
    }

    // Trigger Offline Sync
    fun triggerOfflineSync() {
        repositoryScope.launch {
            try {
                val unsynced = dao.getUnsyncedInspections()
                for (item in unsynced) {
                    dao.updateInspection(item.copy(isSynced = true))
                }
                _inspections.value = _inspections.value.map { it.copy(isSynced = true) }
                _lastCloudSyncTime.value = System.currentTimeMillis()
                logAudit("CLOUD_SYNC", "SyncWorker", UserRole.ADMIN, "Successfully synchronized ${unsynced.size} pending field inspections to cloud")
            } catch (e: Exception) {
                // Ignore sync errors
            }
        }
    }

    // Update system configuration (Admin only)
    fun updateConfiguration(newConfig: SystemConfiguration) {
        _configuration.value = newConfig
        logAudit("CONFIG_UPDATE", "Admin", UserRole.ADMIN, "Updated confidence thresholds & consensus node parameters")
    }

    private fun logAudit(action: String, by: String, role: UserRole, details: String) {
        val entry = AuditLogEntry(
            logId = "LOG-${System.currentTimeMillis() % 100000}",
            action = action,
            performedBy = by,
            role = role,
            timestamp = System.currentTimeMillis(),
            details = details
        )
        _auditLogs.value = listOf(entry) + _auditLogs.value.take(49)
    }
}
