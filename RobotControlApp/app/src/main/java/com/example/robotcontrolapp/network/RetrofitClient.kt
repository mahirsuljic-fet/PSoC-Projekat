package com.example.robotcontrolapp.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val DEFAULT_IP = "192.168.1.103"
    private const val DEFAULT_PORT = 5000

    private var currentBaseUrl = "http://$DEFAULT_IP:$DEFAULT_PORT/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .build()

    private fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private var retrofit = createRetrofit(currentBaseUrl)

    val apiService: RobotApiService
        get() = retrofit.create(RobotApiService::class.java)

    fun updateBaseUrl(ip: String, port: Int = DEFAULT_PORT) {
        currentBaseUrl = "http://$ip:$port/"
        retrofit = createRetrofit(currentBaseUrl)
    }
}