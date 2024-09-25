package com.example.bluetoothadapter

import android.content.Context
import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionManager(private val context: Context) {
    private val REQUEST_CODE_PERMISSION = 1
    private val TAG = "PermissionManager"

    fun requestBluetoothPermissions(){
        Log.d(TAG, "requestBluetoothPermissions called")
        val bluetoothPermissions = arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if(bluetoothPermissions.all{
          ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }) {
            Log.d(TAG, "All Bluetooth permissions granted")
            (context as? MainActivity)?.bluetoothManager?.initializeA2DPProxy()
        } else{
            Log.d(TAG, "Requesting Bluetooth permissions")
            ActivityCompat.requestPermissions(context as Activity, bluetoothPermissions, REQUEST_CODE_PERMISSION)
        }
    }

    fun onRequestPermissionsResult(requestCode: Int, permission: Array<String>, grantResults: IntArray){
        if(requestCode == REQUEST_CODE_PERMISSION){
            if(grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }){
                (context as? MainActivity)?.bluetoothManager?.initializeA2DPProxy()
            } else{
                Log.e(TAG, "Permissions denied")
            }
        }
    }

    fun checkBluetoothConnectPermission(): Boolean{
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun checkBluetoothScanPermission(): Boolean{
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED
    }
}