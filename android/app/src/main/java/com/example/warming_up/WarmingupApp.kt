package com.example.warming_up

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.warming_up.data.routine.Routine
import com.example.warming_up.data.routine.RoutineApiException
import com.example.warming_up.data.routine.RoutineRepository
import com.example.warming_up.navigation.BottomTab
import com.example.warming_up.ui.preparation.PreparationScreen
import com.example.warming_up.ui.supplies.SuppliesScreen

@Composable
fun WarmingupApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val routineRepository = remember { RoutineRepository() }
    var routineUiState by remember { mutableStateOf(RoutineUiState()) }

    LaunchedEffect(routineRepository) {
        routineUiState = routineUiState.copy(isLoading = true, errorMessage = null)

        routineRepository.getRoutines()
            .onSuccess { routines ->
                routineUiState = RoutineUiState(routines = routines)
            }
            .onFailure { throwable ->
                routineUiState = RoutineUiState(errorMessage = throwable.toUserMessage())
            }
    }

    NavHost(
        navController = navController,
        startDestination = BottomTab.Now.route,
        modifier = modifier.fillMaxSize(),
    ) {
        composable(BottomTab.Now.route) {
            PreparationScreen(
                routine = routineUiState.currentRoutine,
                isLoading = routineUiState.isLoading,
                errorMessage = routineUiState.errorMessage,
                onTabClick = { tab -> navController.navigateToTab(tab) },
            )
        }
        composable(BottomTab.Supplies.route) {
            SuppliesScreen(
                routine = routineUiState.currentRoutine,
                isLoading = routineUiState.isLoading,
                errorMessage = routineUiState.errorMessage,
                onTabClick = { tab -> navController.navigateToTab(tab) },
            )
        }
    }
}

private data class RoutineUiState(
    val isLoading: Boolean = false,
    val routines: List<Routine> = emptyList(),
    val errorMessage: String? = null,
) {
    val currentRoutine: Routine?
        get() = routines.firstOrNull()
}

private fun Throwable.toUserMessage(): String {
    if (this is RoutineApiException && statusCode == 401) {
        return "로그인이 필요합니다."
    }

    return message ?: "루틴을 불러오지 못했습니다."
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
