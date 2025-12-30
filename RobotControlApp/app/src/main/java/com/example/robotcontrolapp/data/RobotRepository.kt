package com.example.robotcontrolapp.data

import com.example.robotcontrolapp.data.models.ControlResponse
import com.example.robotcontrolapp.data.models.RobotStatus
import com.example.robotcontrolapp.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import retrofit2.Response

class RobotRepository {

    private val apiService = RetrofitClient.apiService

    fun startHeartbeat(): Flow<Result<Unit>> = flow {
        while (true) {
            try {
                val response = apiService.sendHeartbeat()
                if (response.isSuccessful) {
                    emit(Result.success(Unit))
                } else {
                    emit(Result.failure(Exception("Heartbeat failed")))
                }
            } catch (e: Exception) {
                emit(Result.failure(e))
            }
            delay(1000)
        }
    }.flowOn(Dispatchers.IO)

    suspend fun forwardOn(): Result<ControlResponse> {
        return executeRequest { apiService.forwardOn() }
    }

    suspend fun forwardOff(): Result<ControlResponse> {
        return executeRequest { apiService.forwardOff() }
    }

    suspend fun backwardOn(): Result<ControlResponse> {
        return executeRequest { apiService.backwardOn() }
    }

    suspend fun backwardOff(): Result<ControlResponse> {
        return executeRequest { apiService.backwardOff() }
    }

    suspend fun leftOn(): Result<ControlResponse> {
        return executeRequest { apiService.leftOn() }
    }

    suspend fun leftOff(): Result<ControlResponse> {
        return executeRequest { apiService.leftOff() }
    }

    suspend fun rightOn(): Result<ControlResponse> {
        return executeRequest { apiService.rightOn() }
    }

    suspend fun rightOff(): Result<ControlResponse> {
        return executeRequest { apiService.rightOff() }
    }

    suspend fun stopAll(): Result<ControlResponse> {
        return executeRequest { apiService.stopAll() }
    }

    suspend fun brakeOn(): Result<ControlResponse> {
        return executeRequest { apiService.brakeOn() }
    }

    suspend fun brakeOff(): Result<ControlResponse> {
        return executeRequest { apiService.brakeOff() }
    }

    suspend fun hornOn(): Result<ControlResponse> {
        return executeRequest { apiService.hornOn() }
    }

    suspend fun hornOff(): Result<ControlResponse> {
        return executeRequest { apiService.hornOff() }
    }

    fun getRobotStatusFlow(): Flow<Result<RobotStatus>> = flow {
        while (true) {
            try {
                val response = apiService.isMoving()
                if (response.isSuccessful && response.body() != null) {
                    emit(Result.success(response.body()!!))
                } else {
                    emit(Result.failure(Exception("Failed to get status")))
                }
            } catch (e: Exception) {
                emit(Result.failure(e))
            }
            delay(500)
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun executeRequest(
        request: suspend () -> Response<ControlResponse>
    ): Result<ControlResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = request()
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Request failed: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    fun updateRobotIp(ip: String, port: Int = 5000) {
        RetrofitClient.updateBaseUrl(ip, port)
    }
}