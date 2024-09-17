package com.example.bluetoothadapter

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier

class BluetoothManager(private val activity: MainActivity) {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val permissionHandler = PermissionHandler(activity)
    val discoveredDevices = mutableStateListOf<BluetoothDevice>()

    companion object {
        const val REQUEST_ENABLE_BT =1
    }
    
    fun initializeBluetooth(bluetoothEnabled: MutableState<Boolean>) {
        if(permissionHandler.hasRequiredPermissions()) {
            try{
                if(bluetoothAdapter == null){
                    activity.showMessage("This device doesn't support bluetooth")
                    return
                }

                if(!bluetoothAdapter.isEnabled){
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    activity.promptEnableBluetooth()
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
        if(permissionHandler.hasRequiredPermissions()) {
            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            activity.registerReceiver(receiver, filter)
            try{
                if (bluetoothAdapter?.isDiscovering == true) {
                    bluetoothAdapter.cancelDiscovery()
                }

                bluetoothAdapter?.startDiscovery()
                activity.showMessage("Started bluetooth discovery...")
            }catch (e: SecurityException){
                activity.showMessage("Bluetooth discovery failed: Permission not granted")
            }
        } else {
            activity.showMessage("Required permissions for bluetooth are not granted")
        }
    }

    val receiver = object  : android.content.BroadcastReceiver() {
        override fun onReceive(context: android.content.Context, intent: android.content.Intent) {
            val action: String? = intent.action
            if(BluetoothDevice.ACTION_FOUND == action){
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                device?.let { discoveredDevices.add(it) }
            }
        }
    }
}