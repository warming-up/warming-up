package com.example.warming_up

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.warming_up.data.appointment.Appointment
import com.example.warming_up.data.appointment.AppointmentRepository
import com.example.warming_up.data.auth.AuthRepository
import com.example.warming_up.data.route.DESTINATION_PRESETS
import com.example.warming_up.data.routine.Routine
import com.example.warming_up.data.routine.RoutineApiException
import com.example.warming_up.data.routine.RoutineRepository
import com.example.warming_up.navigation.BottomTab
import com.example.warming_up.network.NetworkModule
import com.example.warming_up.ui.login.LoginScreen
import com.example.warming_up.ui.login.LoginViewModel
import com.example.warming_up.ui.preparation.DestinationPickerDialog
import com.example.warming_up.ui.preparation.PreparationScreen
import com.example.warming_up.ui.route.RouteEtaViewModel
import com.example.warming_up.ui.routine.RoutineCreateViewModel
import com.example.warming_up.ui.routine.RoutineSelectionScreen
import com.example.warming_up.ui.supplies.SuppliesScreen

private const val LoginRoute = "login"
private const val RoutineSelectionRoute = "routine-selection"

@Composable
fun WarmingupApp(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val routineRepository = remember(context) {
        RoutineRepository(NetworkModule.routineApi(context))
    }
    val appointmentRepository = remember(context) {
        AppointmentRepository(NetworkModule.appointmentApi(context))
    }
    val routeEtaViewModel: RouteEtaViewModel = viewModel()
    var routineUiState by remember { mutableStateOf(RoutineUiState()) }
    var currentAppointment by remember { mutableStateOf<Appointment?>(null) }
    var destination by remember { mutableStateOf(DESTINATION_PRESETS.first()) }
    var showDestinationPicker by remember { mutableStateOf(false) }
    var isAuthenticated by remember { mutableStateOf(false) }
    var routineReloadKey by remember { mutableStateOf(0) }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        routeEtaViewModel.onPermissionResult(granted, destination.coordinate)
    }
    val navigateToRoutineSelection = {
        currentAppointment = null
        navController.navigate(RoutineSelectionRoute) {
            popUpTo(BottomTab.Now.route) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    LaunchedEffect(isAuthenticated, routineReloadKey, routineRepository) {
        if (!isAuthenticated) return@LaunchedEffect

        routineUiState = routineUiState.copy(isLoading = true, errorMessage = null)

        routineRepository.getRoutines()
            .onSuccess { routines ->
                routineUiState = RoutineUiState(routines = routines)
            }
            .onFailure { throwable ->
                routineUiState = RoutineUiState(errorMessage = throwable.toUserMessage())
            }
    }

    LaunchedEffect(context, destination) {
        if (context.hasLocationPermission()) {
            routeEtaViewModel.loadEta(destination.coordinate)
        }
    }

    NavHost(
        navController = navController,
        startDestination = LoginRoute,
        modifier = modifier.fillMaxSize(),
    ) {
        composable(LoginRoute) {
            val authRepository = remember(context) {
                AuthRepository(NetworkModule.authApi(context))
            }
            val loginViewModel: LoginViewModel = viewModel(
                factory = LoginViewModel.factory(authRepository),
            )

            LoginScreen(
                viewModel = loginViewModel,
                onSignInClick = {
                    isAuthenticated = true
                    navController.navigate(RoutineSelectionRoute) {
                        popUpTo(LoginRoute) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(RoutineSelectionRoute) {
            val routineCreateViewModel: RoutineCreateViewModel = viewModel(
                factory = RoutineCreateViewModel.factory(
                    routineRepository = routineRepository,
                    appointmentRepository = appointmentRepository,
                ),
            )

            RoutineSelectionScreen(
                viewModel = routineCreateViewModel,
                onRoutineCreated = { appointment ->
                    currentAppointment = appointment
                    routineReloadKey += 1
                    navController.navigate(BottomTab.Now.route) {
                        popUpTo(RoutineSelectionRoute) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(BottomTab.Now.route) {
            PreparationScreen(
                routine = routineUiState.currentRoutine,
                appointment = currentAppointment,
                isLoading = routineUiState.isLoading,
                errorMessage = routineUiState.errorMessage,
                destinationName = destination.name,
                routeEtaUiState = routeEtaViewModel.uiState,
                onRequestRouteEta = {
                    if (context.hasLocationPermission()) {
                        routeEtaViewModel.loadEta(destination.coordinate)
                    } else {
                        locationPermissionLauncher.launch(LOCATION_PERMISSIONS)
                    }
                },
                onDestinationClick = { showDestinationPicker = true },
                onResetClick = navigateToRoutineSelection,
                onTabClick = { tab -> navController.navigateToTab(tab) },
            )
        }
        composable(BottomTab.Supplies.route) {
            SuppliesScreen(
                routine = routineUiState.currentRoutine,
                appointment = currentAppointment,
                isLoading = routineUiState.isLoading,
                errorMessage = routineUiState.errorMessage,
                onResetClick = navigateToRoutineSelection,
                onTabClick = { tab -> navController.navigateToTab(tab) },
            )
        }
    }

    if (showDestinationPicker) {
        DestinationPickerDialog(
            currentDestination = destination,
            onDismiss = { showDestinationPicker = false },
            onConfirm = { selected ->
                destination = selected
                showDestinationPicker = false
                if (context.hasLocationPermission()) {
                    routeEtaViewModel.loadEta(selected.coordinate)
                }
            },
        )
    }
}

private val LOCATION_PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION,
)

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

private fun Context.hasLocationPermission(): Boolean {
    val fineGranted = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED
    val coarseGranted = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED

    return fineGranted || coarseGranted
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
