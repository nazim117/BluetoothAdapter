package com.example.bluetoothadapter

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat

class PermissionHandler(private val activity: ComponentActivity) {

    fun hasRequiredPermissions(): Boolean {
        val hasBluetoothPermission = ContextCompat.checkSelfPermission(
            activity, Manifest.permission.BLUETOOTH
        ) == PackageManager.PERMISSION_GRANTED
        val hasBluetoothAdminPermission = ContextCompat.checkSelfPermission(
            activity, Manifest.permission.BLUETOOTH_ADMIN
        ) == PackageManager.PERMISSION_GRANTED
        val hasLocationPermission = ContextCompat.checkSelfPermission(
            activity, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return hasBluetoothPermission && hasBluetoothAdminPermission && hasLocationPermission
    }

    companion object {
        private const val REQUEST_ENABLE_BT =1
    }
}