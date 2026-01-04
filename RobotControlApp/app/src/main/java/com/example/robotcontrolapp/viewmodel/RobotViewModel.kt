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
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch

enum class StopReason {
    NONE, RED_LIGHT, STOP_SIGN
}

data class RobotUiState(
    val isConnected: Boolean = false,
    val status: RobotStatus = RobotStatus(),
    val currentDirection: Direction = Direction.STOP,
    val isHornActive: Boolean = false,
    val errorMessage: String? = "App is not connected to the server. Please try changing the IP address or port in the settings.",
    val isLoading: Boolean = false,
    val showSettings: Boolean = false,
    val stopReason: StopReason = StopReason.NONE,
    val currentIp: String = "192.168.1.132  ",
    val currentPort: Int = 5000,
    val sequence: Int = 0
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
            val seq = _uiState.updateAndGet {
                it.copy(
                    currentDirection = direction,
                    stopReason = StopReason.NONE,
                    sequence = it.sequence + 1
                )
            }.sequence

            val result = when (direction) {
                Direction.FORWARD -> repository.forwardOn(seq)
                Direction.BACKWARD -> repository.backwardOn(seq)
                Direction.LEFT -> repository.leftOn(seq)
                Direction.RIGHT -> repository.rightOn(seq)
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

            val seq = _uiState.updateAndGet {
                it.copy(
                    currentDirection = Direction.STOP,
                    sequence = it.sequence + 1
                )
            }.sequence

            val result = when (currentDir) {
                Direction.FORWARD -> repository.forwardOff(seq)
                Direction.BACKWARD -> repository.backwardOff(seq)
                Direction.LEFT -> repository.leftOff(seq)
                Direction.RIGHT -> repository.rightOff(seq)
                Direction.STOP -> repository.stopAll()
            }
            result.onFailure { setConnectionError() }
        }
    }

    fun activateHorn() {
        viewModelScope.launch {
            val seq = _uiState.updateAndGet {
                it.copy(
                    isHornActive = true,
                    sequence = it.sequence + 1
                )
            }.sequence

            repository.hornOn(seq).onSuccess {
                _uiState.value = _uiState.value.copy(errorMessage = null, isConnected = true)
            }.onFailure {
                setConnectionError()
            }
        }
    }

    fun releaseHorn() {
        viewModelScope.launch {
            val seq = _uiState.updateAndGet {
                it.copy(
                    isHornActive = false,
                    sequence = it.sequence + 1
                )
            }.sequence

            repository.hornOff(seq).onFailure { setConnectionError() }
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