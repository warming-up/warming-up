package com.example.warming_up.ui.preparation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.warming_up.data.routine.Routine
import com.example.warming_up.navigation.BottomTab
import com.example.warming_up.ui.component.WarmingupBottomBar
import com.example.warming_up.ui.component.WarmingupHeader
import com.example.warming_up.ui.theme.WarmBackground
import com.example.warming_up.ui.theme.WarmSubText
import com.example.warming_up.ui.theme.WarmingupTheme
import com.example.warming_up.ui.route.RouteEtaUiState

@Composable
fun PreparationScreen(
    modifier: Modifier = Modifier,
    routine: Routine? = null,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    destinationName: String = "강남 오피스",
    routeEtaUiState: RouteEtaUiState = RouteEtaUiState(),
    onRequestRouteEta: () -> Unit = {},
    onDestinationClick: () -> Unit = {},
    onTabClick: (BottomTab) -> Unit = {},
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = WarmBackground,
        topBar = { WarmingupHeader() },
        bottomBar = {
            WarmingupBottomBar(
                selectedTab = BottomTab.Now,
                onTabClick = onTabClick,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = 18.dp,
                end = 18.dp,
                top = 8.dp,
                bottom = 18.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                when {
                    isLoading -> StatusText(text = "루틴을 불러오는 중입니다.")
                    errorMessage != null -> StatusText(text = errorMessage)
                    routine == null -> StatusText(text = "등록된 루틴이 없습니다.")
                    else -> CurrentPreparationCard(routine = routine)
                }
            }
            item {
                PreparationProgressSummary(
                    totalDurationMinutes = routine?.totalDurationMinutes ?: 0,
                    totalStepCount = routine?.steps?.size ?: 0,
                )
            }
            item {
                DestinationSummaryCard(
                    destinationName = destinationName,
                    routeEtaUiState = routeEtaUiState,
                    onRequestRouteEta = onRequestRouteEta,
                    onDestinationClick = onDestinationClick,
                )
            }
            item { PreparationStepSection(steps = routine?.steps.orEmpty()) }
        }
    }
}

@Composable
private fun StatusText(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 24.dp),
        color = WarmSubText,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
    )
}

@Preview(showBackground = true)
@Composable
private fun PreparationScreenPreview() {
    WarmingupTheme {
        PreparationScreen()
    }
}
