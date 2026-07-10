package com.example.warming_up

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.warming_up.navigation.BottomTab
import com.example.warming_up.ui.preparation.PreparationScreen
import com.example.warming_up.ui.supplies.SuppliesScreen

@Composable
fun WarmingupApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = BottomTab.Now.route,
        modifier = modifier.fillMaxSize(),
    ) {
        composable(BottomTab.Now.route) {
            PreparationScreen(
                onTabClick = { tab -> navController.navigateToTab(tab) },
            )
        }
        composable(BottomTab.Supplies.route) {
            SuppliesScreen(
                onTabClick = { tab -> navController.navigateToTab(tab) },
            )
        }
    }
}

private fun NavHostController.navigateToTab(tab: BottomTab) {
    navigate(tab.route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
