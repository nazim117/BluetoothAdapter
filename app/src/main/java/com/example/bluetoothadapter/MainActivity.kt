package com.example.bluetoothadapter

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import com.example.bluetoothadapter.ui.theme.BluetoothStatusUI
import com.example.bluetoothadapter.ui.theme.PermissionDeniedDialog

class MainActivity : ComponentActivity() {

    private val bluetoothManager = BluetoothManager(this)
    private val permissionHandler = PermissionHandler(this)
    private val permissionDenied = mutableStateOf(false)
    private val bluetoothEnabled = mutableStateOf(false)
    private val permissions = arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ).let{
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            it.plus(Manifest.permission.BLUETOOTH_SCAN).plus(Manifest.permission.BLUETOOTH_CONNECT)
        } else it
    }

    private val enableBluetoothLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode == RESULT_OK) {
            bluetoothEnabled.value = true
            bluetoothManager.startDiscovery()
        } else {
            showMessage("Bluetooth is required to discover devices.")
        }
    }

    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsResult ->
        val granted = permissionsResult.all { it.value }
        permissionDenied.value = !granted

        if(granted){
            bluetoothManager.initializeBluetooth(bluetoothEnabled)
        }
    }

    fun showMessage(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    fun promptEnableBluetooth(){
        val enableBtIntent = Intent(android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE)
        enableBluetoothLauncher.launch(enableBtIntent)
    }

    @Composable
    private fun BluetoothAdapter(permissionDenied: MutableState<Boolean>, bluetoothEnabled: MutableState<Boolean>) {
        MaterialTheme{
            Scaffold { paddingValues ->
                Surface (modifier = Modifier.padding(paddingValues)) {
                    if(permissionDenied.value){
                        PermissionDeniedDialog {
                            permissionDenied.value = false
                        }
                    } else{
                        BluetoothStatusUI(bluetoothEnabled)
                    }
                }

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BluetoothAdapter(permissionDenied, bluetoothEnabled)
        }
        if(!permissionHandler.hasRequiredPermissions()){
            requestPermissions.launch(permissions)
        } else {
            bluetoothManager.initializeBluetooth(bluetoothEnabled)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(bluetoothManager.receiver)
    }
}