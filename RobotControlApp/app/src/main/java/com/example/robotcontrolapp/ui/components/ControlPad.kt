package com.example.robotcontrolapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.robotcontrolapp.data.models.Direction
import com.example.robotcontrolapp.ui.theme.ControlButtonActive
import com.example.robotcontrolapp.ui.theme.ControlButtonInactive

@Composable
fun ControlButton(
    icon: ImageVector,
    direction: Direction,
    isPressed: Boolean,
    onPress: () -> Unit,
    onRelease: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isPressed) ControlButtonActive
                else ControlButtonInactive
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onPress()
                        tryAwaitRelease()
                        onRelease()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = direction.value,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(40.dp)
        )
    }
}

@Composable
fun ControlPad(
    currentDirection: Direction,
    onDirectionChange: (Direction) -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ControlButton(
            icon = Icons.Default.KeyboardArrowUp,
            direction = Direction.FORWARD,
            isPressed = currentDirection == Direction.FORWARD,
            onPress = { onDirectionChange(Direction.FORWARD) },
            onRelease = onStop
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ControlButton(
                icon = Icons.Default.KeyboardArrowLeft,
                direction = Direction.LEFT,
                isPressed = currentDirection == Direction.LEFT,
                onPress = { onDirectionChange(Direction.LEFT) },
                onRelease = onStop
            )

            ControlButton(
                icon = Icons.Default.KeyboardArrowDown,
                direction = Direction.BACKWARD,
                isPressed = currentDirection == Direction.BACKWARD,
                onPress = { onDirectionChange(Direction.BACKWARD) },
                onRelease = onStop
            )

            ControlButton(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                direction = Direction.RIGHT,
                isPressed = currentDirection == Direction.RIGHT,
                onPress = { onDirectionChange(Direction.RIGHT) },
                onRelease = onStop
            )
        }
    }
}