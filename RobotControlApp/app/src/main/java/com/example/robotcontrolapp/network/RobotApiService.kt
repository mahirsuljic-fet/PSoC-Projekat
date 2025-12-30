package com.example.robotcontrolapp.network

import com.example.robotcontrolapp.data.models.ControlResponse
import com.example.robotcontrolapp.data.models.RobotStatus
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST

interface RobotApiService {

    @POST("heartbeat")
    suspend fun sendHeartbeat(): Response<ControlResponse>

    @GET("forward/on")
    suspend fun forwardOn(): Response<ControlResponse>

    @GET("forward/off")
    suspend fun forwardOff(): Response<ControlResponse>

    @GET("backward/on")
    suspend fun backwardOn(): Response<ControlResponse>

    @GET("backward/off")
    suspend fun backwardOff(): Response<ControlResponse>

    @GET("left/on")
    suspend fun leftOn(): Response<ControlResponse>

    @GET("left/off")
    suspend fun leftOff(): Response<ControlResponse>

    @GET("right/on")
    suspend fun rightOn(): Response<ControlResponse>

    @GET("right/off")
    suspend fun rightOff(): Response<ControlResponse>

    @GET("stop")
    suspend fun stopAll(): Response<ControlResponse>

    @GET("brake/on")
    suspend fun brakeOn(): Response<ControlResponse>

    @GET("brake/off")
    suspend fun brakeOff(): Response<ControlResponse>

    @GET("horn/on")
    suspend fun hornOn(): Response<ControlResponse>

    @GET("horn/off")
    suspend fun hornOff(): Response<ControlResponse>

    @GET("is_moving")
    suspend fun isMoving(): Response<RobotStatus>
}
