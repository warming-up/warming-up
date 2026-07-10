package com.example.warming_up.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LocationProvider(
    private val context: Context,
) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    suspend fun getCurrentLocation(): Result<DeviceLocation> {
        if (!hasLocationPermission()) {
            return Result.failure(LocationPermissionMissingException())
        }

        return runCatching {
            val lastLocation = getLastLocation()
            val location = lastLocation ?: requestCurrentLocation()
            location?.let {
                DeviceLocation(latitude = it.latitude, longitude = it.longitude)
            } ?: throw LocationUnavailableException()
        }
    }

    fun hasLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        return fineGranted || coarseGranted
    }

    @SuppressLint("MissingPermission")
    private suspend fun getLastLocation(): android.location.Location? {
        return fusedLocationClient.lastLocation.await()
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestCurrentLocation(): android.location.Location? {
        val cancellationTokenSource = CancellationTokenSource()
        return try {
            fusedLocationClient
                .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cancellationTokenSource.token)
                .await(cancellationTokenSource)
        } finally {
            cancellationTokenSource.cancel()
        }
    }
}

class LocationPermissionMissingException : RuntimeException("위치 권한이 필요합니다.")

class LocationUnavailableException : RuntimeException("현재 위치를 가져오지 못했습니다.")

private suspend fun <T> Task<T>.await(
    cancellationTokenSource: CancellationTokenSource? = null,
): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result ->
        continuation.resume(result)
    }
    addOnFailureListener { exception ->
        continuation.resumeWithException(exception)
    }
    addOnCanceledListener {
        continuation.cancel()
    }
    continuation.invokeOnCancellation {
        cancellationTokenSource?.cancel()
    }
}
