package com.example.robotcontrolapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.robotcontrolapp.data.RobotRepository
import com.example.robotcontrolapp.data.models.Direction
import com.example.robotcontrolapp.data.models.RobotStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

enum class StopReason {
    NONE, RED_LIGHT, STOP_SIGN, MANUAL_BRAKE
}

data class RobotUiState(
    val isConnected: Boolean = false,
    val status: RobotStatus = RobotStatus(),
    val currentDirection: Direction = Direction.STOP,
    val isBrakeActive: Boolean = false,
    val isHornActive: Boolean = false,
    val errorMessage: String? = "App is not connected to the server. Please try changing the IP address or port in the settings.",
    val isLoading: Boolean = false,
    val showSettings: Boolean = false,
    val stopReason: StopReason = StopReason.NONE,
    val currentIp: String = "192.168.1.100",
    val currentPort: Int = 5000
)

class RobotViewModel : ViewModel() {

    private val repository = RobotRepository()
    private val _uiState = MutableStateFlow(RobotUiState())
    val uiState: StateFlow<RobotUiState> = _uiState.asStateFlow()

    private var statusJob: Job? = null
    private var heartbeatJob: Job? = null

    private fun setConnectionError() {
        val state = _uiState.value
        val message = "Failed connection with the server at IP: ${state.currentIp} and Port: ${state.currentPort}. Please try changing the IP address or port in the settings."
        _uiState.value = _uiState.value.copy(
            errorMessage = message,
            isConnected = false,
            isLoading = false
        )
    }

    fun connectToRobot(ip: String, port: Int = 5000) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, currentIp = ip, currentPort = port)

            try {
                repository.updateRobotIp(ip, port)
                startHeartbeat()
                startStatusUpdates()
                _uiState.value = _uiState.value.copy(
                    isConnected = true,
                    isLoading = false,
                    errorMessage = null
                )
            } catch (e: Exception) {
                setConnectionError()
            }
        }
    }

    fun sendCommand(direction: Direction) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(currentDirection = direction, stopReason = StopReason.NONE)

            val result = when (direction) {
                Direction.FORWARD -> repository.forwardOn()
                Direction.BACKWARD -> repository.backwardOn()
                Direction.LEFT -> repository.leftOn()
                Direction.RIGHT -> repository.rightOn()
                Direction.STOP -> repository.stopAll()
            }

            result.onSuccess {
                _uiState.value = _uiState.value.copy(errorMessage = null, isConnected = true)
            }.onFailure {
                setConnectionError()
            }
        }
    }

    fun stopCommand() {
        viewModelScope.launch {
            val currentDir = _uiState.value.currentDirection
            _uiState.value = _uiState.value.copy(currentDirection = Direction.STOP)

            val result = when (currentDir) {
                Direction.FORWARD -> repository.forwardOff()
                Direction.BACKWARD -> repository.backwardOff()
                Direction.LEFT -> repository.leftOff()
                Direction.RIGHT -> repository.rightOff()
                Direction.STOP -> repository.stopAll()
            }
            result.onFailure { setConnectionError() }
        }
    }

    fun activateBrake() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBrakeActive = true)
            repository.brakeOn().onSuccess {
                handleStopReason(StopReason.MANUAL_BRAKE)
                _uiState.value = _uiState.value.copy(errorMessage = null, isConnected = true)
            }.onFailure {
                setConnectionError()
            }
        }
    }

    fun activateHorn() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isHornActive = true)
            repository.hornOn().onSuccess {
                _uiState.value = _uiState.value.copy(errorMessage = null, isConnected = true)
            }.onFailure {
                setConnectionError()
            }
        }
    }

    fun releaseBrake() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBrakeActive = false)
            repository.brakeOff().onFailure { setConnectionError() }
        }
    }

    fun releaseHorn() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isHornActive = false)
            repository.hornOff().onFailure { setConnectionError() }
        }
    }

    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = viewModelScope.launch {
            repository.startHeartbeat()
                .catch { setConnectionError() }
                .collect { result ->
                    result.onFailure { setConnectionError() }
                }
        }
    }

    private fun startStatusUpdates() {
        statusJob?.cancel()
        statusJob = viewModelScope.launch {
            repository.getRobotStatusFlow()
                .catch { setConnectionError() }
                .collect { result ->
                    result.onSuccess { status ->
                        _uiState.value = _uiState.value.copy(
                            status = status,
                            isConnected = true,
                            errorMessage = null
                        )
                    }.onFailure { setConnectionError() }
                }
        }
    }

    fun handleStopReason(reason: StopReason) {
        _uiState.value = _uiState.value.copy(stopReason = reason)
    }

    fun toggleSettings() {
        _uiState.value = _uiState.value.copy(showSettings = !_uiState.value.showSettings)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}