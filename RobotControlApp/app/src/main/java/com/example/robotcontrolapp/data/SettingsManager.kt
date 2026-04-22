package com.example.robotcontrolapp.data

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("robot_settings", Context.MODE_PRIVATE)

    fun saveConnectionDetails(ip: String, port: Int) {
        prefs.edit()
            .putString("last_ip", ip)
            .putInt("last_port", port)
            .apply()
    }

    fun getLastIp(): String = prefs.getString("last_ip", "192.168.1.132") ?: "192.168.1.132"
    
    fun getLastPort(): Int = prefs.getInt("last_port", 5000)
}