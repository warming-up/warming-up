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
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.warming_up.ui.component.InfoChip
import com.example.warming_up.ui.route.RouteEtaStatus
import com.example.warming_up.ui.route.RouteEtaUiState
import com.example.warming_up.ui.theme.WarmBlue
import com.example.warming_up.ui.theme.WarmSubText
import com.example.warming_up.ui.theme.WarmText
import com.example.warming_up.ui.theme.WarmingupTheme

@Composable
fun DestinationSummaryCard(
    destinationName: String = "강남 오피스",
    routeEtaUiState: RouteEtaUiState = RouteEtaUiState(),
    onRequestRouteEta: () -> Unit = {},
    onDestinationClick: () -> Unit = {},
) {
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
                        text = "팀 미팅 · 오전",
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoChip(text = "도착 09:00")
                InfoChip(text = routeEtaUiState.durationLabel())
                InfoChip(text = "출발 08:20")
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
