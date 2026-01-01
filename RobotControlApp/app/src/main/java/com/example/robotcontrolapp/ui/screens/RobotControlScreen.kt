package com.example.robotcontrolapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.robotcontrolapp.ui.components.ActionButton
import com.example.robotcontrolapp.ui.components.ControlPad
import com.example.robotcontrolapp.ui.theme.EmergencyRed
import com.example.robotcontrolapp.ui.theme.StatusWarning
import com.example.robotcontrolapp.viewmodel.RobotViewModel
import com.example.robotcontrolapp.R
import com.example.robotcontrolapp.ui.theme.EmergencyRedActive
import com.example.robotcontrolapp.ui.theme.StatusWarningActive
import com.example.robotcontrolapp.viewmodel.StopReason

@Composable
fun RobotControlScreen(
    viewModel: RobotViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        IconButton(
            onClick = { viewModel.toggleSettings() },
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(32.dp)
            )
        }

        Row(
            modifier = Modifier.align(Alignment.Center).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                ActionButton(
                    imageId = R.drawable.brake,
                    labelId = R.string.brake,
                    color = if (uiState.isBrakeActive) EmergencyRedActive else EmergencyRed,
                    isActive = uiState.isBrakeActive,
                    onPress = { viewModel.activateBrake() },
                    onRelease = { viewModel.releaseBrake() }
                )

                ActionButton(
                    imageId = R.drawable.horn,
                    labelId = R.string.horn,
                    color = if (uiState.isHornActive) StatusWarningActive else StatusWarning,
                    isActive = uiState.isHornActive,
                    onPress = { viewModel.activateHorn() },
                    onRelease = { viewModel.releaseHorn() }
                )
            }

            ControlPad(
                currentDirection = uiState.currentDirection,
                onDirectionChange = { viewModel.sendCommand(it) },
                onStop = { viewModel.stopCommand() }
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (!uiState.isConnected && uiState.errorMessage != null) {
                InfoWhiteBox(
                    message = uiState.errorMessage!!,
                    isError = true
                )
            }

            if (uiState.stopReason != StopReason.NONE) {
                val stopMessage = when (uiState.stopReason) {
                    StopReason.RED_LIGHT -> "Robot stopped: RED LIGHT DETECTED!"
                    StopReason.STOP_SIGN -> "Robot stopped: STOP SIGN DETECTED!"
                    StopReason.MANUAL_BRAKE -> "Robot stopped: MANUAL BRAKE APPLIED!"
                    else -> ""
                }

                if (stopMessage.isNotEmpty()) {
                    InfoWhiteBox(
                        message = stopMessage,
                        isError = false,
                        onDismiss = { viewModel.handleStopReason(StopReason.NONE) }
                    )
                }
            }

            if (uiState.isConnected) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "Robot Status: ${if (uiState.status.moving) "Moving" else "Stopped"}",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }

    if (uiState.showSettings) {
        SettingsDialog(
            onDismiss = { viewModel.toggleSettings() },
            onConnect = { ip, port ->
                viewModel.connectToRobot(ip, port)
                viewModel.toggleSettings()
            }
        )
    }
}

@Composable
fun InfoWhiteBox(
    message: String,
    isError: Boolean,
    onDismiss: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .border(2.dp, if (isError) Color.Red else Color.Black, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Text(
                text = message,
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            if (onDismiss != null) {
                TextButton(onClick = onDismiss, contentPadding = PaddingValues(start = 8.dp)) {
                    Text("OK", color = Color.Blue, fontWeight = FontWeight.Black, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    onConnect: (String, Int) -> Unit
) {
    var ipAddress by remember { mutableStateOf("192.168.1.100") }
    var port by remember { mutableStateOf("5000") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = "Postavke Robota", style = MaterialTheme.typography.titleLarge)

                OutlinedTextField(
                    value = ipAddress,
                    onValueChange = { ipAddress = it },
                    label = { Text("IP Adresa") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = port,
                    onValueChange = { port = it },
                    label = { Text("Port") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Otkaži")
                    }

                    Button(
                        onClick = {
                            val portInt = port.toIntOrNull() ?: 5000
                            onConnect(ipAddress, portInt)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Poveži")
                    }
                }
            }
        }
    }
}