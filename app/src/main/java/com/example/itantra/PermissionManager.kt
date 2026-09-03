package com.example.itantra

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionManager {
    private const val PERMISSION_REQUEST_CODE = 1001

    fun checkAndRequestPermissions(activity: Activity) {
        val permissionsList = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE
        )

        // Bluetooth & Nearby Devices permissions based on Android API level
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionsList.add(Manifest.permission.BLUETOOTH_SCAN)
            permissionsList.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissionsList.add(Manifest.permission.BLUETOOTH_ADVERTISE)
        } else {
            permissionsList.add(Manifest.permission.BLUETOOTH)
            permissionsList.add(Manifest.permission.BLUETOOTH_ADMIN)
        }

        // Notification permission for Foreground Services (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsList.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val neededPermissions = permissionsList.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (neededPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, neededPermissions, PERMISSION_REQUEST_CODE)
        }
    }
}