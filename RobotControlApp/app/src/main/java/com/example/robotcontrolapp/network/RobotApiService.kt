package com.example.robotcontrolapp.network

import com.example.robotcontrolapp.data.models.ControlResponse
import com.example.robotcontrolapp.data.models.RobotStatus
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface RobotApiService {

    @POST("heartbeat")
    suspend fun sendHeartbeat(): Response<ControlResponse>

    @GET("forward/on")
    suspend fun forwardOn(
        @Header("sequence") seq : Int
    ): Response<ControlResponse>

    @GET("forward/off")
    suspend fun forwardOff(
        @Header("sequence") seq : Int
    ): Response<ControlResponse>

    @GET("backward/on")
    suspend fun backwardOn(
        @Header("sequence") seq : Int
    ): Response<ControlResponse>

    @GET("backward/off")
    suspend fun backwardOff(
        @Header("sequence") seq : Int
    ): Response<ControlResponse>

    @GET("left/on")
    suspend fun leftOn(
        @Header("sequence") seq : Int

    ): Response<ControlResponse>

    @GET("left/off")
    suspend fun leftOff(
        @Header("sequence") seq : Int
    ): Response<ControlResponse>

    @GET("right/on")
    suspend fun rightOn(
        @Header("sequence") seq : Int
    ): Response<ControlResponse>

    @GET("right/off")
    suspend fun rightOff(
        @Header("sequence") seq : Int
    ): Response<ControlResponse>

    @GET("stop")
    suspend fun stopAll(): Response<ControlResponse>
    @GET("horn/on")
    suspend fun hornOn(
        @Header("sequence") seq : Int
    ): Response<ControlResponse>

    @GET("horn/off")
    suspend fun hornOff(
        @Header("sequence") seq : Int
    ): Response<ControlResponse>

    @GET("is_moving")
    suspend fun isMoving(): Response<RobotStatus>
}
