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

data class RobotUiState(
    val isConnected: Boolean = false,
    val status: RobotStatus = RobotStatus(),
    val currentDirection: Direction = Direction.STOP,
    val isBrakeActive: Boolean = false,
    val isHornActive: Boolean = false,
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val showSettings: Boolean = false
)

class RobotViewModel : ViewModel() {

    private val repository = RobotRepository()

    private val _uiState = MutableStateFlow(RobotUiState())
    val uiState: StateFlow<RobotUiState> = _uiState.asStateFlow()

    private var statusJob: Job? = null
    private var heartbeatJob: Job? = null

    fun connectToRobot(ip: String, port: Int = 5000) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

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
                _uiState.value = _uiState.value.copy(
                    isConnected = false,
                    isLoading = false,
                    errorMessage = "Failed to connect: ${e.message}"
                )
            }
        }
    }

    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = viewModelScope.launch {
            repository.startHeartbeat()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isConnected = false,
                        errorMessage = "Heartbeat lost: ${e.message}"
                    )
                }
                .collect { result ->
                    result.onFailure {
                        _uiState.value = _uiState.value.copy(isConnected = false)
                    }
                }
        }
    }

    fun sendCommand(direction: Direction) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(currentDirection = direction)

            val result = when (direction) {
                Direction.FORWARD -> repository.forwardOn()
                Direction.BACKWARD -> repository.backwardOn()
                Direction.LEFT -> repository.leftOn()
                Direction.RIGHT -> repository.rightOn()
                Direction.STOP -> repository.stopAll()
            }

            result.onSuccess {
                _uiState.value = _uiState.value.copy(errorMessage = null)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Command failed: ${error.message}",
                    currentDirection = Direction.STOP
                )
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

            result.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Stop failed: ${error.message}"
                )
            }
        }
    }

    fun activateBrake() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBrakeActive = true)

            val result = repository.brakeOn()

            result.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Brake failed: ${error.message}"
                )
            }
        }
    }

    fun releaseBrake() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBrakeActive = false)
            repository.brakeOff()
        }
    }

    fun activateHorn() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isHornActive = true)

            val result = repository.hornOn()

            result.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Horn failed: ${error.message}"
                )
            }
        }
    }

    fun releaseHorn() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isHornActive = false)
            repository.hornOff()
        }
    }

    private fun startStatusUpdates() {
        statusJob?.cancel()
        statusJob = viewModelScope.launch {
            repository.getRobotStatusFlow()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isConnected = false,
                        errorMessage = "Connection lost: ${e.message}"
                    )
                }
                .collect { result ->
                    result.onSuccess { status ->
                        _uiState.value = _uiState.value.copy(
                            status = status,
                            isConnected = true,
                            errorMessage = null
                        )
                    }.onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            isConnected = false,
                            errorMessage = "Status update failed: ${error.message}"
                        )
                    }
                }
        }
    }

    fun toggleSettings() {
        _uiState.value = _uiState.value.copy(
            showSettings = !_uiState.value.showSettings
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun disconnect() {
        statusJob?.cancel()
        heartbeatJob?.cancel()

        viewModelScope.launch {
            repository.stopAll()
        }

        _uiState.value = _uiState.value.copy(
            isConnected = false,
            currentDirection = Direction.STOP,
            status = RobotStatus()
        )
    }

    override fun onCleared() {
        super.onCleared()
        statusJob?.cancel()
        heartbeatJob?.cancel()

        viewModelScope.launch {
            repository.stopAll()
        }
    }
}