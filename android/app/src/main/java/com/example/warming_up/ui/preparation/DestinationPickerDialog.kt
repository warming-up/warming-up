package com.example.warming_up.ui.preparation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.warming_up.data.route.DESTINATION_PRESETS
import com.example.warming_up.data.route.Destination
import com.example.warming_up.data.route.GeocodeApiException
import com.example.warming_up.data.route.GeocodeRepository
import com.example.warming_up.data.route.GeocodeResult
import kotlinx.coroutines.launch

private const val CUSTOM_OPTION_KEY = "__custom__"

private sealed interface GeocodeSearchState {
    data object Idle : GeocodeSearchState
    data object Loading : GeocodeSearchState
    data class Success(val result: GeocodeResult) : GeocodeSearchState
    data class Error(val message: String) : GeocodeSearchState
}

@Composable
fun DestinationPickerDialog(
    currentDestination: Destination,
    onDismiss: () -> Unit,
    onConfirm: (Destination) -> Unit,
    geocodeRepository: GeocodeRepository = remember { GeocodeRepository() },
) {
    val coroutineScope = rememberCoroutineScope()
    val presetNames = remember { DESTINATION_PRESETS.map { it.name } }
    var selectedKey by remember {
        mutableStateOf(
            if (presetNames.contains(currentDestination.name)) currentDestination.name else CUSTOM_OPTION_KEY,
        )
    }
    var addressQuery by remember { mutableStateOf("") }
    var searchState by remember { mutableStateOf<GeocodeSearchState>(GeocodeSearchState.Idle) }

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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            modifier = Modifier.weight(1f),
                            value = addressQuery,
                            onValueChange = {
                                addressQuery = it
                                searchState = GeocodeSearchState.Idle
                            },
                            label = { Text("장소 이름 또는 주소") },
                            singleLine = true,
                            isError = searchState is GeocodeSearchState.Error,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            enabled = addressQuery.isNotBlank() && searchState != GeocodeSearchState.Loading,
                            onClick = {
                                val query = addressQuery
                                searchState = GeocodeSearchState.Loading
                                coroutineScope.launch {
                                    geocodeRepository.geocode(query)
                                        .onSuccess { result ->
                                            searchState = GeocodeSearchState.Success(result)
                                        }
                                        .onFailure { throwable ->
                                            searchState = GeocodeSearchState.Error(throwable.toGeocodeErrorMessage())
                                        }
                                }
                            },
                        ) {
                            Text("검색")
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    when (val state = searchState) {
                        is GeocodeSearchState.Loading -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.height(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("주소를 찾는 중입니다.")
                            }
                        }
                        is GeocodeSearchState.Success -> {
                            Text(text = state.result.formattedAddress)
                        }
                        is GeocodeSearchState.Error -> {
                            Text(text = state.message)
                        }
                        GeocodeSearchState.Idle -> Unit
                    }
                }
            }
        },
        confirmButton = {
            val successState = searchState as? GeocodeSearchState.Success
            TextButton(
                enabled = selectedKey != CUSTOM_OPTION_KEY || successState != null,
                onClick = {
                    val destination = if (selectedKey == CUSTOM_OPTION_KEY) {
                        Destination(name = addressQuery, coordinate = requireNotNull(successState).result.coordinate)
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

private fun Throwable.toGeocodeErrorMessage(): String {
    return when (this) {
        is GeocodeApiException -> when (statusCode) {
            400 -> "주소를 입력해 주세요."
            401 -> "로그인이 필요합니다."
            404 -> "주소를 찾을 수 없습니다."
            else -> message ?: "주소를 찾지 못했습니다."
        }
        else -> message ?: "주소를 찾지 못했습니다."
    }
}
