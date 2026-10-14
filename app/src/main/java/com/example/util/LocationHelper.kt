package com.example.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import com.example.data.model.CityLocation
import com.example.domain.SpecialLocation
import com.example.domain.SpecialLocationInsults
import com.example.domain.WeatherForeignInsults
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }

    fun isValidCoordinate(latitude: Double, longitude: Double): Boolean {
        return latitude.isFinite() && longitude.isFinite() &&
                latitude in -90.0..90.0 && longitude in -180.0..180.0
    }

    suspend fun getCurrentLocation(context: Context): Location? {
        if (!hasLocationPermission(context)) return null

        return try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()

            suspendCancellableCoroutine { continuation ->
                try {
                    fusedClient.getCurrentLocation(
                        Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                        cts.token
                    ).addOnSuccessListener { location: Location? ->
                        if (continuation.isActive) {
                            if (location != null && isValidCoordinate(location.latitude, location.longitude)) {
                                continuation.resume(location)
                            } else {
                                continuation.resume(null)
                            }
                        }
                    }.addOnFailureListener {
                        if (continuation.isActive) {
                            continuation.resume(null)
                        }
                    }
                } catch (e: Throwable) {
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                }

                continuation.invokeOnCancellation {
                    try {
                        cts.cancel()
                    } catch (_: Throwable) {}
                }
            }
        } catch (t: Throwable) {
            null
        }
    }

    @Suppress("DEPRECATION")
    suspend fun getCityLocationDetails(context: Context, latitude: Double, longitude: Double): CityLocation {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val safeLat = if (isValidCoordinate(latitude, longitude)) latitude else 9.9281
            val safeLon = if (isValidCoordinate(latitude, longitude)) longitude else -84.0907
            val isOutsideCr = !WeatherForeignInsults.isInsideCostaRica(safeLat, safeLon)
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(safeLat, safeLon, 1)
                val address = addresses?.firstOrNull()

                val addressSummary = listOfNotNull(
                    address?.subLocality,
                    address?.thoroughfare,
                    address?.featureName,
                    address?.locality,
                    address?.subAdminArea
                ).joinToString(" ")

                val specialLoc = SpecialLocationInsults.detectSpecialLocation(addressSummary, safeLat, safeLon)
                val detectedName = when (specialLoc) {
                    SpecialLocation.RIO_CLARO -> "Río Claro"
                    SpecialLocation.CIUDAD_NEILY -> "Ciudad Neily"
                    SpecialLocation.PASO_CANOAS -> "Paso Canoas"
                    SpecialLocation.CERRO_DE_LA_MUERTE -> "El Cerro de la Muerte"
                    SpecialLocation.GOLFITO -> "Golfito"
                    null -> null
                }

                val city = detectedName
                    ?: address?.subLocality?.takeIf { it.isNotBlank() }
                    ?: address?.locality
                    ?: address?.subAdminArea
                    ?: address?.adminArea
                    ?: if (isOutsideCr) "Ubicación en el Extranjero" else "Mi Ubicación Actual"
                val country = address?.countryName?.trim()?.ifBlank { null }
                    ?: if (isOutsideCr) "Extranjero" else "Costa Rica"
                CityLocation(name = city, subtitle = country, latitude = safeLat, longitude = safeLon, country = country)
            } catch (e: Throwable) {
                val fallbackCountry = if (isOutsideCr) "Extranjero" else "Costa Rica"
                val fallbackCity = if (isOutsideCr) "Ubicación en el Extranjero" else "Mi Ubicación Actual"
                CityLocation(fallbackCity, fallbackCountry, safeLat, safeLon, fallbackCountry)
            }
        }
    }
}
