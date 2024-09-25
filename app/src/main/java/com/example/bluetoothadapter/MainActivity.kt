package com.example.bluetoothadapter

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.pm.PackageManager
import android.util.Log
import android.widget.ListView

class MainActivity : ComponentActivity() {
    lateinit var bluetoothManager: BluetoothManager
    private lateinit var audioFileManager: AudioFileManager
    private lateinit var permissionManager: PermissionManager
    private lateinit var deviceListAdapter: DeviceListAdapter
    private lateinit var devicesListView: ListView

    private val TAG = "MainActivity"
    private val REQUEST_ENABLE_BT = 2

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_OK){
                bluetoothManager.initializeA2DPProxy()
            } else {
                Toast.makeText(this, "Bluetooth needed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        permissionManager = PermissionManager(this)
        deviceListAdapter = DeviceListAdapter(this)
        bluetoothManager = BluetoothManager(this, deviceListAdapter)
        audioFileManager = AudioFileManager(this)

        devicesListView = findViewById(R.id.devices_list_view)
        devicesListView.adapter = deviceListAdapter

        devicesListView.setOnItemClickListener{_, _, position, _ ->
            val selectedDevice = deviceListAdapter.getItem(position) as BluetoothDevice
            connectToAudioDevice(selectedDevice)
        }

        permissionManager.requestBluetoothPermissions()
    }

    private fun connectToAudioDevice(device: BluetoothDevice) {
        if(permissionManager.checkBluetoothConnectPermission()){
            bluetoothManager.connectToAudioDevice(device)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothManager.onDestroy()
        audioFileManager.onDestroy()
    }
}