package com.example.warming_up.ui.preparation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.warming_up.data.appointment.Appointment
import com.example.warming_up.ui.component.InfoChip
import com.example.warming_up.ui.route.RouteEtaStatus
import com.example.warming_up.ui.route.RouteEtaUiState
import com.example.warming_up.ui.theme.WarmBlue
import com.example.warming_up.ui.theme.WarmSubText
import com.example.warming_up.ui.theme.WarmText
import com.example.warming_up.ui.theme.WarmingupTheme

@Composable
fun DestinationSummaryCard(
    appointment: Appointment? = null,
    destinationName: String = "강남 오피스",
    routeEtaUiState: RouteEtaUiState = RouteEtaUiState(),
    onRequestRouteEta: () -> Unit = {},
    onDestinationClick: () -> Unit = {},
) {
    val travelMinutes = appointment?.let {
        minutesBetween(it.departureTime, it.arrivalTime)
    }
    val summaryChips = listOfNotNull(
        "준비 ${appointment?.preparationStartTime?.toClockText() ?: "--:--"}",
        "출발 ${appointment?.departureTime?.toClockText() ?: "--:--"}",
        "도착 ${appointment?.arrivalTime?.toClockText() ?: "--:--"}",
        travelMinutes?.let { "이동 ${it}분" },
        routeEtaUiState.durationLabel(),
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 15.dp, vertical = 13.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.clickable(onClick = onDestinationClick)) {
                    Text(
                        text = appointment?.name ?: "약속",
                        color = WarmSubText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = destinationName,
                        color = WarmText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
                TextButton(onClick = onRequestRouteEta, enabled = !routeEtaUiState.isLoading) {
                    Text(
                        text = routeEtaUiState.actionText(),
                        color = WarmBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(modifier = Modifier.height(9.dp))
            WrappingChipRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalSpacing = 8.dp,
                verticalSpacing = 6.dp,
            ) {
                summaryChips.forEach { chipText ->
                    InfoChip(text = chipText)
                }
            }

            routeEtaUiState.supportingText()?.let { message ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    color = WarmSubText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun WrappingChipRow(
    modifier: Modifier = Modifier,
    horizontalSpacing: Dp,
    verticalSpacing: Dp,
    content: @Composable () -> Unit,
) {
    Layout(
        modifier = modifier,
        content = content,
    ) { measurables, constraints ->
        val horizontalSpacingPx = horizontalSpacing.roundToPx()
        val verticalSpacingPx = verticalSpacing.roundToPx()
        val childConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        val placeables = measurables.map { measurable ->
            measurable.measure(childConstraints)
        }
        val positions = mutableListOf<Pair<Int, Int>>()
        var rowWidth = 0
        var y = 0
        var rowHeight = 0
        var contentWidth = 0

        placeables.forEach { placeable ->
            val nextWidth = if (rowWidth == 0) {
                placeable.width
            } else {
                rowWidth + horizontalSpacingPx + placeable.width
            }
            if (rowWidth > 0 && nextWidth > constraints.maxWidth) {
                y += rowHeight + verticalSpacingPx
                rowWidth = 0
                rowHeight = 0
            }

            val placeX = if (rowWidth == 0) {
                0
            } else {
                rowWidth + horizontalSpacingPx
            }
            positions += placeX to y
            rowWidth = placeX + placeable.width
            rowHeight = maxOf(rowHeight, placeable.height)
            contentWidth = maxOf(contentWidth, rowWidth)
        }

        val contentHeight = if (placeables.isEmpty()) 0 else y + rowHeight
        layout(
            width = contentWidth.coerceIn(constraints.minWidth, constraints.maxWidth),
            height = contentHeight.coerceIn(constraints.minHeight, constraints.maxHeight),
        ) {
            placeables.forEachIndexed { index, placeable ->
                val (placeX, placeY) = positions[index]
                placeable.placeRelative(placeX, placeY)
            }
        }
    }
}

private fun RouteEtaUiState.actionText(): String {
    return when (status) {
        RouteEtaStatus.NeedsPermission -> "위치 허용"
        RouteEtaStatus.LoadingLocation,
        RouteEtaStatus.LoadingEta -> "계산 중"
        RouteEtaStatus.Success -> "새로고침"
        RouteEtaStatus.PermissionDenied,
        RouteEtaStatus.Error -> "재시도"
    }
}

private fun RouteEtaUiState.durationLabel(): String {
    return when (status) {
        RouteEtaStatus.LoadingLocation -> "위치 확인 중"
        RouteEtaStatus.LoadingEta -> "이동시간 계산 중"
        RouteEtaStatus.Success -> eta?.let { "차 ${it.durationText}" } ?: "차 --"
        else -> "차 --"
    }
}

private fun RouteEtaUiState.supportingText(): String? {
    return when (status) {
        RouteEtaStatus.Success -> eta?.let { "현재 위치 기준 ${it.distanceText}" }
        RouteEtaStatus.PermissionDenied,
        RouteEtaStatus.Error -> errorMessage
        RouteEtaStatus.NeedsPermission -> "현재 위치 권한을 허용하면 이동시간을 계산합니다."
        else -> null
    }
}

@Preview(showBackground = true)
@Composable
private fun DestinationSummaryCardPreview() {
    WarmingupTheme {
        DestinationSummaryCard()
    }
}
