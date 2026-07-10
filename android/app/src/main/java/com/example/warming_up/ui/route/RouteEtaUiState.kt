package com.example.warming_up.ui.route

import com.example.warming_up.data.route.RouteEta

data class RouteEtaUiState(
    val status: RouteEtaStatus = RouteEtaStatus.NeedsPermission,
    val eta: RouteEta? = null,
    val errorMessage: String? = null,
) {
    val isLoading: Boolean
        get() = status == RouteEtaStatus.LoadingLocation || status == RouteEtaStatus.LoadingEta
}

enum class RouteEtaStatus {
    NeedsPermission,
    LoadingLocation,
    LoadingEta,
    Success,
    PermissionDenied,
    Error,
}
