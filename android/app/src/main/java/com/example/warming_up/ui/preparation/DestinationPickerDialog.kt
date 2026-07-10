package com.example.warming_up.ui.preparation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.warming_up.data.route.Coordinate
import com.example.warming_up.data.route.DESTINATION_PRESETS
import com.example.warming_up.data.route.Destination

private const val CUSTOM_OPTION_KEY = "__custom__"

@Composable
fun DestinationPickerDialog(
    currentDestination: Destination,
    onDismiss: () -> Unit,
    onConfirm: (Destination) -> Unit,
) {
    val presetNames = remember { DESTINATION_PRESETS.map { it.name } }
    var selectedKey by remember {
        mutableStateOf(
            if (presetNames.contains(currentDestination.name)) currentDestination.name else CUSTOM_OPTION_KEY,
        )
    }
    var customName by remember { mutableStateOf("") }
    var customLatitude by remember { mutableStateOf("") }
    var customLongitude by remember { mutableStateOf("") }

    val customCoordinate = customLatitude.toValidLatitudeOrNull()?.let { lat ->
        customLongitude.toValidLongitudeOrNull()?.let { lng -> Coordinate(lat, lng) }
    }
    val isCustomValid = customName.isNotBlank() && customCoordinate != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "도착지 선택") },
        text = {
            Column {
                DESTINATION_PRESETS.forEach { destination ->
                    DestinationOptionRow(
                        label = destination.name,
                        selected = selectedKey == destination.name,
                        onClick = { selectedKey = destination.name },
                    )
                }
                DestinationOptionRow(
                    label = "직접 입력",
                    selected = selectedKey == CUSTOM_OPTION_KEY,
                    onClick = { selectedKey = CUSTOM_OPTION_KEY },
                )
                if (selectedKey == CUSTOM_OPTION_KEY) {
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("장소 이름") },
                        singleLine = true,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customLatitude,
                        onValueChange = { customLatitude = it },
                        label = { Text("위도") },
                        singleLine = true,
                        isError = customLatitude.isNotBlank() && customLatitude.toValidLatitudeOrNull() == null,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customLongitude,
                        onValueChange = { customLongitude = it },
                        label = { Text("경도") },
                        singleLine = true,
                        isError = customLongitude.isNotBlank() && customLongitude.toValidLongitudeOrNull() == null,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = selectedKey != CUSTOM_OPTION_KEY || isCustomValid,
                onClick = {
                    val destination = if (selectedKey == CUSTOM_OPTION_KEY) {
                        Destination(name = customName, coordinate = requireNotNull(customCoordinate))
                    } else {
                        DESTINATION_PRESETS.first { it.name == selectedKey }
                    }
                    onConfirm(destination)
                },
            ) {
                Text("선택")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        },
    )
}

@Composable
private fun DestinationOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .selectable(selected = selected, onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text = label)
    }
}

private fun String.toValidLatitudeOrNull(): Double? {
    return toDoubleOrNull()?.takeIf { it in -90.0..90.0 }
}

private fun String.toValidLongitudeOrNull(): Double? {
    return toDoubleOrNull()?.takeIf { it in -180.0..180.0 }
}
