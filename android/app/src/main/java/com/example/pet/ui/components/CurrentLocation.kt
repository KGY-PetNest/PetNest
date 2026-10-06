package com.example.pet.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.pet.data.GeoPoint

private const val FRESH_LOCATION_MS = 10 * 60 * 1000L
private const val LOCATION_TIMEOUT_MS = 30_000L

@SuppressLint("MissingPermission")
fun requestCurrentLocation(context: Context, onResult: (GeoPoint?) -> Unit) {
    val manager = context.getSystemService(LocationManager::class.java)
    if (manager == null) {
        onResult(null)
        return
    }
    val providers = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
        .filter { runCatching { manager.isProviderEnabled(it) }.getOrDefault(false) }
    if (providers.isEmpty()) {
        onResult(null)
        return
    }

    val last = providers
        .mapNotNull { runCatching { manager.getLastKnownLocation(it) }.getOrNull() }
        .maxByOrNull { it.time }
    if (last != null && System.currentTimeMillis() - last.time < FRESH_LOCATION_MS) {
        onResult(last.toGeoPoint())
        return
    }

    val provider = providers.first()
    runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            manager.getCurrentLocation(provider, null, context.mainExecutor) { location ->
                onResult(location?.toGeoPoint() ?: last?.toGeoPoint())
            }
        } else {
            var delivered = false
            val handler = Handler(Looper.getMainLooper())
            lateinit var listener: LocationListener
            val timeout = Runnable {
                if (!delivered) {
                    delivered = true
                    manager.removeUpdates(listener)
                    onResult(last?.toGeoPoint())
                }
            }
            listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    if (!delivered) {
                        delivered = true
                        handler.removeCallbacks(timeout)
                        onResult(location.toGeoPoint())
                    }
                }

                override fun onProviderEnabled(provider: String) = Unit

                override fun onProviderDisabled(provider: String) = Unit

                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
            }
            handler.postDelayed(timeout, LOCATION_TIMEOUT_MS)
            @Suppress("DEPRECATION")
            manager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
        }
    }.onFailure { onResult(last?.toGeoPoint()) }
}

private fun Location.toGeoPoint() = GeoPoint(latitude, longitude)