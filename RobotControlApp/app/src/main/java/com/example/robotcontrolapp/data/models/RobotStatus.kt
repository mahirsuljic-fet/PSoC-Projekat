package com.example.robotcontrolapp.data.models

data class RobotStatus(
    val connected: Boolean = false,
    val moving: Boolean = false,
    val lastCommand: String = "none"
)

data class ControlResponse(
    val status: String
)

enum class Direction(val value: String) {
    FORWARD("forward"),
    BACKWARD("backward"),
    LEFT("left"),
    RIGHT("right"),
    STOP("stop")
}