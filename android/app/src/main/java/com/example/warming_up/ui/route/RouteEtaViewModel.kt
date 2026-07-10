package com.example.warming_up.ui.route

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.warming_up.data.route.Coordinate
import com.example.warming_up.data.route.RoutesApiException
import com.example.warming_up.data.route.RoutesRepository
import com.example.warming_up.location.LocationPermissionMissingException
import com.example.warming_up.location.LocationProvider
import com.example.warming_up.location.LocationUnavailableException
import kotlinx.coroutines.launch

class RouteEtaViewModel(application: Application) : AndroidViewModel(application) {
    private val locationProvider = LocationProvider(application.applicationContext)
    private val routesRepository = RoutesRepository()

    var uiState by mutableStateOf(RouteEtaUiState())
        private set

    fun syncPermissionState() {
        if (locationProvider.hasLocationPermission()) {
            if (uiState.status == RouteEtaStatus.NeedsPermission ||
                uiState.status == RouteEtaStatus.PermissionDenied
            ) {
                uiState = RouteEtaUiState(status = RouteEtaStatus.Error, errorMessage = "이동시간을 새로고침해 주세요.")
            }
            return
        }

        uiState = RouteEtaUiState(status = RouteEtaStatus.NeedsPermission)
    }

    fun onPermissionResult(granted: Boolean, destination: Coordinate) {
        if (granted) {
            loadEta(destination)
        } else {
            uiState = RouteEtaUiState(
                status = RouteEtaStatus.PermissionDenied,
                errorMessage = "위치 권한이 거부되었습니다.",
            )
        }
    }

    fun loadEta(destination: Coordinate) {
        if (uiState.isLoading) return

        viewModelScope.launch {
            uiState = RouteEtaUiState(status = RouteEtaStatus.LoadingLocation)

            locationProvider.getCurrentLocation()
                .onSuccess { location ->
                    uiState = RouteEtaUiState(status = RouteEtaStatus.LoadingEta)

                    routesRepository.getEta(
                        origin = Coordinate(location.latitude, location.longitude),
                        destination = destination,
                    ).onSuccess { eta ->
                        uiState = RouteEtaUiState(status = RouteEtaStatus.Success, eta = eta)
                    }.onFailure { throwable ->
                        uiState = RouteEtaUiState(
                            status = RouteEtaStatus.Error,
                            errorMessage = throwable.toUserMessage(),
                        )
                    }
                }
                .onFailure { throwable ->
                    uiState = RouteEtaUiState(
                        status = if (throwable is LocationPermissionMissingException) {
                            RouteEtaStatus.NeedsPermission
                        } else {
                            RouteEtaStatus.Error
                        },
                        errorMessage = throwable.toUserMessage(),
                    )
                }
        }
    }
}

private fun Throwable.toUserMessage(): String {
    return when (this) {
        is LocationPermissionMissingException -> "위치 권한이 필요합니다."
        is LocationUnavailableException -> "현재 위치를 가져오지 못했습니다."
        is RoutesApiException -> when (statusCode) {
            400 -> "도착지 좌표가 올바르지 않습니다."
            401 -> "로그인이 필요합니다."
            500 -> "지도 API 설정이 필요합니다."
            502 -> "이동 경로를 계산하지 못했습니다."
            else -> message ?: "이동시간을 불러오지 못했습니다."
        }
        else -> message ?: "이동시간을 불러오지 못했습니다."
    }
}
