package com.example.warming_up.ui.supplies

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.warming_up.navigation.BottomTab
import com.example.warming_up.ui.component.WarmingupBottomBar
import com.example.warming_up.ui.component.WarmingupHeader
import com.example.warming_up.ui.theme.WarmBackground
import com.example.warming_up.ui.theme.WarmingupTheme

@Composable
fun SuppliesScreen(
    modifier: Modifier = Modifier,
    onTabClick: (BottomTab) -> Unit = {},
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = WarmBackground,
        topBar = { WarmingupHeader() },
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
