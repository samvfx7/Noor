package com.example.service

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

/**
 * 100% Native Android FOSS Location Helper.
 * Operates purely using Android Open Source Project (AOSP) android.location.LocationManager.
 * Zero reliance on Google Play Services, Google Maps, or proprietary closed-source SDKs.
 */
object NativeLocationHelper {

    data class LocationResult(
        val latitude: Double,
        val longitude: Double,
        val cityName: String,
        val countryName: String,
        val timezoneOffsetHours: Double,
        val isCached: Boolean = false
    )

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    suspend fun getBestNativeLocation(context: Context): LocationResult? = withContext(Dispatchers.IO) {
        if (!hasLocationPermission(context)) return@withContext null

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return@withContext null

        var bestLocation: Location? = null

        // 1. Try GPS provider
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                val loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (loc != null) bestLocation = loc
            }
        } catch (_: Exception) {}

        // 2. Try Network provider
        try {
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                val loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (loc != null && (bestLocation == null || loc.accuracy < bestLocation.accuracy)) {
                    bestLocation = loc
                }
            }
        } catch (_: Exception) {}

        // 3. Try Passive provider
        try {
            val loc = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
            if (loc != null && bestLocation == null) {
                bestLocation = loc
            }
        } catch (_: Exception) {}

        if (bestLocation == null) {
            return@withContext null
        }

        return@withContext resolveLocationResult(context, bestLocation.latitude, bestLocation.longitude, isCached = false)
    }

    suspend fun resolveLocationResult(
        context: Context,
        latitude: Double,
        longitude: Double,
        isCached: Boolean = false
    ): LocationResult = withContext(Dispatchers.IO) {
        var cityName = "Custom Location"
        var countryName = ""

        try {
            if (Geocoder.isPresent()) {
                val geocoder = Geocoder(context, Locale.getDefault())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val addresses = mutableListOf<Address>()
                    val lock = Object()
                    geocoder.getFromLocation(latitude, longitude, 1) { list ->
                        synchronized(lock) {
                            addresses.addAll(list)
                            lock.notifyAll()
                        }
                    }
                    synchronized(lock) {
                        if (addresses.isEmpty()) {
                            try { lock.wait(1500) } catch (_: Exception) {}
                        }
                    }
                    if (addresses.isNotEmpty()) {
                        val addr = addresses[0]
                        cityName = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "Local Area"
                        countryName = addr.countryName ?: ""
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val list = geocoder.getFromLocation(latitude, longitude, 1)
                    if (!list.isNullOrEmpty()) {
                        val addr = list[0]
                        cityName = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "Local Area"
                        countryName = addr.countryName ?: ""
                    }
                }
            }
        } catch (_: Exception) {
            // Geocoder failure or offline mode - use formatted coordinates
            cityName = "Lat ${String.format(Locale.US, "%.2f", latitude)}, Lon ${String.format(Locale.US, "%.2f", longitude)}"
        }

        // Calculate timezone offset from system default TimeZone
        val defaultTz = TimeZone.getDefault()
        val offsetHours = (defaultTz.rawOffset + defaultTz.dstSavings).toDouble() / (1000.0 * 3600.0)

        LocationResult(
            latitude = latitude,
            longitude = longitude,
            cityName = cityName,
            countryName = countryName,
            timezoneOffsetHours = offsetHours,
            isCached = isCached
        )
    }
}
