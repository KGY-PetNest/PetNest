package com.example.pet.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.example.pet.data.GeoPoint
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority

private const val LOCATION_INTERVAL_MS = 10_000L

sealed interface LocationOutcome {
    data class Found(val point: GeoPoint) : LocationOutcome
    data object PermissionDenied : LocationOutcome
    data object PermissionBlocked : LocationOutcome
    data object LocationOff : LocationOutcome
    data object Failed : LocationOutcome
}

class LocationRequester internal constructor(private val start: () -> Unit) {
    fun request() = start()
}

fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
}

fun openLocationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
}

fun fetchLocationSilently(context: Context, onResult: (GeoPoint?) -> Unit) {
    if (hasLocationPermission(context)) fetchLocation(context, onResult) else onResult(null)
}

@Composable
fun rememberLocationRequester(onOutcome: (LocationOutcome) -> Unit): LocationRequester {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val currentOnOutcome by rememberUpdatedState(onOutcome)

    fun fetch() {
        fetchLocation(context) { point ->
            currentOnOutcome(if (point != null) LocationOutcome.Found(point) else LocationOutcome.Failed)
        }
    }

    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) fetch() else currentOnOutcome(LocationOutcome.LocationOff)
    }

    fun ensureLocationEnabled() {
        if (isPlayServicesAvailable(context)) {
            val request = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, LOCATION_INTERVAL_MS).build()
            val settingsRequest = LocationSettingsRequest.Builder()
                .addLocationRequest(request)
                .setAlwaysShow(true)
                .build()
            LocationServices.getSettingsClient(context)
                .checkLocationSettings(settingsRequest)
                .addOnSuccessListener { fetch() }
                .addOnFailureListener { error ->
                    if (error is ResolvableApiException) {
                        runCatching {
                            settingsLauncher.launch(IntentSenderRequest.Builder(error.resolution).build())
                        }.onFailure { currentOnOutcome(LocationOutcome.LocationOff) }
                    } else {
                        currentOnOutcome(LocationOutcome.LocationOff)
                    }
                }
        } else if (isLocationEnabled(context)) {
            fetch()
        } else {
            currentOnOutcome(LocationOutcome.LocationOff)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        when {
            result.isEmpty() -> currentOnOutcome(LocationOutcome.PermissionDenied)
            result.values.any { it } -> ensureLocationEnabled()
            activity != null && !ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) -> currentOnOutcome(LocationOutcome.PermissionBlocked)
            else -> currentOnOutcome(LocationOutcome.PermissionDenied)
        }
    }

    val latestEnsure by rememberUpdatedState(::ensureLocationEnabled)
    return remember(permissionLauncher) {
        LocationRequester {
            if (hasLocationPermission(context)) {
                latestEnsure()
            } else {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )
            }
        }
    }
}

private fun isPlayServicesAvailable(context: Context): Boolean =
    GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS

private fun isLocationEnabled(context: Context): Boolean {
    val manager = context.getSystemService(LocationManager::class.java) ?: return false
    return LocationManagerCompat.isLocationEnabled(manager)
}

@SuppressLint("MissingPermission")
private fun fetchLocation(context: Context, onResult: (GeoPoint?) -> Unit) {
    if (!isPlayServicesAvailable(context)) {
        requestCurrentLocation(context, onResult)
        return
    }
    LocationServices.getFusedLocationProviderClient(context)
        .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
        .addOnSuccessListener { location ->
            if (location != null) {
                onResult(GeoPoint(location.latitude, location.longitude))
            } else {
                requestCurrentLocation(context, onResult)
            }
        }
        .addOnFailureListener { requestCurrentLocation(context, onResult) }
}