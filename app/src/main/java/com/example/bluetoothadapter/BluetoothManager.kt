package com.example.bluetoothadapter

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.compose.runtime.MutableState

class BluetoothManager(private val activity: MainActivity) {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    fun initializeBluetooth(bluetoothEnabled: MutableState<Boolean>) {
        if(hasRequiredPermissions()) {
            try{
                if(bluetoothAdapter == null){
                    activity.showMessage("This device doesn't support bluetooth")
                    return
                }

                if(!bluetoothAdapter!!.isEnabled){
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                } else {
                    bluetoothEnabled.value = true
                    startDiscovery()
                }
            } catch(e: SecurityException) {
                activity.showMessage("Bluetooth initialization failed: Permission not granted")
            }
        } else{
            activity.showMessage("Required permissions for bluetooth are not granted")
        }
    }

    fun startDiscovery() {
        if(hasRequiredPermissions()) {
            try{
                if(bluetoothAdapter?.isEnabled == true){
                    if(bluetoothAdapter.isDiscovering){
                        bluetoothAdapter.cancelDiscovery()
                    }

                    bluetoothAdapter.startDiscovery()
                    activity.showMessage("Started bluetooth discovery...")
                }
            } catch(e: SecurityException) {
                activity.showMessage("Bluetooth discovery failed: Permission not granted")
            }
        } else{
            activity.showMessage("Required permissions for bluetooth are not granted")
        }
    }

    private fun hasRequiredPermissions(): Boolean {
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