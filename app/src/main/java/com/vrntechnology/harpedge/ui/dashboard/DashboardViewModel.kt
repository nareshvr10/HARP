package com.vrntechnology.harpedge.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vrntechnology.harpedge.data.model.*
import com.vrntechnology.harpedge.data.repository.HarpRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val nodes: List<SensorNode> = emptyList(),
    val events: List<HazardEvent> = emptyList(),
    val alerts: List<AlertItem> = emptyList(),
    val latestReadings: SensorReadings? = null,
    val isDemoMode: Boolean = true,
    val isOnline: Boolean = true,
    val currentScenario: String = "NORMAL",
    val selectedNode: SensorNode? = null,
    val selectedEvent: HazardEvent? = null,
    val showFirebaseSetupDialog: Boolean = false
)

class DashboardViewModel(private val repository: HarpRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.nodes.collect { nodes ->
                _uiState.value = _uiState.value.copy(
                    nodes = nodes,
                    selectedNode = _uiState.value.selectedNode ?: nodes.firstOrNull()
                )
            }
        }
        viewModelScope.launch {
            repository.events.collect { events ->
                _uiState.value = _uiState.value.copy(
                    events = events,
                    selectedEvent = events.firstOrNull()
                )
            }
        }
        viewModelScope.launch {
            repository.alerts.collect { alerts ->
                _uiState.value = _uiState.value.copy(alerts = alerts)
            }
        }
        viewModelScope.launch {
            repository.latestReadings.collect { readings ->
                _uiState.value = _uiState.value.copy(latestReadings = readings)
            }
        }
        viewModelScope.launch {
            repository.isDemoMode.collect { demo ->
                _uiState.value = _uiState.value.copy(isDemoMode = demo)
            }
        }
        viewModelScope.launch {
            repository.isOnline.collect { online ->
                _uiState.value = _uiState.value.copy(isOnline = online)
            }
        }
        viewModelScope.launch {
            repository.currentScenario.collect { scenario ->
                _uiState.value = _uiState.value.copy(currentScenario = scenario)
            }
        }
    }

    fun selectNode(node: SensorNode) {
        _uiState.value = _uiState.value.copy(selectedNode = node)
    }

    fun selectEvent(event: HazardEvent) {
        _uiState.value = _uiState.value.copy(selectedEvent = event)
    }

    fun triggerScenarioMultiNode() {
        repository.triggerScenarioMultiNodeConsensus()
    }

    fun triggerScenarioSensorFault() {
        repository.triggerScenarioSensorFault()
    }

    fun resetScenarioBaseline() {
        repository.resetToBaseline()
    }

    fun toggleDemoMode() {
        val next = !_uiState.value.isDemoMode
        repository.setDemoMode(next)
        if (!next) {
            _uiState.value = _uiState.value.copy(showFirebaseSetupDialog = true)
        }
    }

    fun toggleOnlineState() {
        repository.setOnlineState(!_uiState.value.isOnline)
    }

    fun dismissFirebaseSetupDialog() {
        _uiState.value = _uiState.value.copy(showFirebaseSetupDialog = false)
    }

    fun acknowledgeAlert(alertId: String, user: UserProfile) {
        repository.acknowledgeAlert(alertId, user)
    }
}
