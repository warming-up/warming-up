package com.example.warming_up.ui.preparation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.warming_up.navigation.BottomTab
import com.example.warming_up.ui.component.WarmingupBottomBar
import com.example.warming_up.ui.component.WarmingupHeader
import com.example.warming_up.ui.theme.WarmBackground
import com.example.warming_up.ui.theme.WarmingupTheme

@Composable
fun PreparationScreen(
    modifier: Modifier = Modifier,
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
            item { CurrentPreparationCard() }
            item { PreparationProgressSummary() }
            item { DestinationSummaryCard() }
            item { PreparationStepSection() }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreparationScreenPreview() {
    WarmingupTheme {
        PreparationScreen()
    }
}
