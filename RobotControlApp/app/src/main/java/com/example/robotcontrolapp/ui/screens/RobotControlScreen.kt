package com.example.robotcontrolapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.robotcontrolapp.ui.components.ActionButton
import com.example.robotcontrolapp.ui.components.ControlPad
import com.example.robotcontrolapp.ui.theme.EmergencyRed
import com.example.robotcontrolapp.ui.theme.StatusWarning
import com.example.robotcontrolapp.viewmodel.RobotViewModel
import com.example.robotcontrolapp.R
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
                    isActive = uiState.isBrakeActive,
                    color = EmergencyRed,
                    onPress = { viewModel.activateBrake() },
                    onRelease = { viewModel.releaseBrake() }
                )

                ActionButton(
                    imageId = R.drawable.horn,
                    labelId = R.string.horn,
                    isActive = uiState.isHornActive,
                    color = StatusWarning,
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

        if (uiState.isConnected) {
            Card(
                modifier = Modifier.align(Alignment.BottomCenter),
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

        uiState.errorMessage?.let { error ->
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp),
                action = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("OK")
                    }
                }
            ) {
                Text(error)
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
