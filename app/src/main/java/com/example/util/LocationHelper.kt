package com.example.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

data class GpsResult(
    val coordinates: String,
    val accuracy: String,
    val rawLatitude: Double = 0.0,
    val rawLongitude: Double = 0.0
)

object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): GpsResult? {
        if (!hasLocationPermission(context)) return null

        return try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()

            val location: Location? = suspendCancellableCoroutine { cont ->
                fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                    .addOnSuccessListener { loc ->
                        if (loc != null) {
                            cont.resume(loc)
                        } else {
                            // Try last known location fallback
                            fusedClient.lastLocation.addOnSuccessListener { lastLoc ->
                                cont.resume(lastLoc)
                            }.addOnFailureListener {
                                cont.resume(null)
                            }
                        }
                    }
                    .addOnFailureListener {
                        cont.resume(null)
                    }

                cont.invokeOnCancellation {
                    cts.cancel()
                }
            }

            if (location != null) {
                formatLocation(location)
            } else {
                fallbackFromLocationManager(context)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            fallbackFromLocationManager(context)
        }
    }

    @SuppressLint("MissingPermission")
    private fun fallbackFromLocationManager(context: Context): GpsResult? {
        return try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
            val providers = lm.getProviders(true)
            var bestLoc: Location? = null
            for (p in providers) {
                val l = lm.getLastKnownLocation(p) ?: continue
                if (bestLoc == null || l.accuracy < bestLoc.accuracy) {
                    bestLoc = l
                }
            }
            if (bestLoc != null) formatLocation(bestLoc) else null
        } catch (e: Exception) {
            null
        }
    }

    private fun formatLocation(loc: Location): GpsResult {
        val latDir = if (loc.latitude >= 0) "N" else "S"
        val lonDir = if (loc.longitude >= 0) "E" else "W"
        val lat = String.format(Locale.US, "%.5f° %s", Math.abs(loc.latitude), latDir)
        val lon = String.format(Locale.US, "%.5f° %s", Math.abs(loc.longitude), lonDir)
        val acc = String.format(Locale.US, "± %.1f m", loc.accuracy)
        return GpsResult(
            coordinates = "$lat, $lon",
            accuracy = acc,
            rawLatitude = loc.latitude,
            rawLongitude = loc.longitude
        )
    }
}
