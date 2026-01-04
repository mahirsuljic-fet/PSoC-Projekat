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

    suspend fun forwardOn(seq: Int): Result<ControlResponse> {
        return executeRequest { apiService.forwardOn(seq) }
    }

    suspend fun forwardOff(seq: Int): Result<ControlResponse> {
        return executeRequest { apiService.forwardOff(seq) }
    }

    suspend fun backwardOn(seq: Int): Result<ControlResponse> {
        return executeRequest { apiService.backwardOn(seq) }
    }

    suspend fun backwardOff(seq: Int): Result<ControlResponse> {
        return executeRequest { apiService.backwardOff(seq) }
    }

    suspend fun leftOn(seq: Int): Result<ControlResponse> {
        return executeRequest { apiService.leftOn(seq) }
    }

    suspend fun leftOff(seq: Int): Result<ControlResponse> {
        return executeRequest { apiService.leftOff(seq) }
    }

    suspend fun rightOn(seq: Int): Result<ControlResponse> {
        return executeRequest { apiService.rightOn(seq) }
    }

    suspend fun rightOff(seq: Int): Result<ControlResponse> {
        return executeRequest { apiService.rightOff(seq) }
    }

    suspend fun stopAll(): Result<ControlResponse> {
        return executeRequest { apiService.stopAll() }
    }
    suspend fun hornOn(seq: Int): Result<ControlResponse> {
        return executeRequest { apiService.hornOn(seq) }
    }

    suspend fun hornOff(seq: Int): Result<ControlResponse> {
        return executeRequest { apiService.hornOff(seq) }
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