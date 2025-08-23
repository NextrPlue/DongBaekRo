package com.redstonetorch.dongbaekro.util

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await

object CommonUtils {
    @SuppressLint("MissingPermission")
    suspend fun getXY(context: Context): Pair<Double, Double>? {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val location = fusedLocationClient.lastLocation.await()
        return if (location != null) {
            Pair(location.latitude, location.longitude)
        } else {
            null
        }
    }
}
