package com.example.warming_up.ui.supplies

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.warming_up.data.appointment.Appointment
import com.example.warming_up.data.routine.Routine
import com.example.warming_up.navigation.BottomTab
import com.example.warming_up.ui.component.WarmingupBottomBar
import com.example.warming_up.ui.component.WarmingupHeader
import com.example.warming_up.ui.theme.WarmBackground
import com.example.warming_up.ui.theme.WarmingupTheme

@Composable
fun SuppliesScreen(
    modifier: Modifier = Modifier,
    routine: Routine? = null,
    appointment: Appointment? = null,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onResetClick: () -> Unit = {},
    onTabClick: (BottomTab) -> Unit = {},
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = WarmBackground,
        topBar = { WarmingupHeader(onResetClick = onResetClick) },
        bottomBar = {
            WarmingupBottomBar(
                selectedTab = BottomTab.Supplies,
                onTabClick = onTabClick,
            )
        },
    ) { innerPadding ->
        SuppliesChecklistContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            supplies = appointment?.checklist?.map { it.name }
                ?: routine?.checklist?.map { it.name }
                ?: emptyList(),
            isLoading = isLoading,
            errorMessage = errorMessage,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SuppliesScreenPreview() {
    WarmingupTheme {
        SuppliesScreen()
    }
}
