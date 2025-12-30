package com.example.robotcontrolapp.data.models

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class RobotStatus(
    val connected: Boolean = false,
    val sensors: String = "unknown",
    val lastCommand: String = "none",
    val videoUrl: String = ""
)

data class ControlResponse(
    val status: String,
    val action: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class Direction(val value: String) {
    FORWARD("forward"),
    BACKWARD("backward"),
    LEFT("left"),
    RIGHT("right"),
    STOP("stop")
}

enum class Action(val value: String) {
    BRAKE("brake"),
    HORN("horn")
}
