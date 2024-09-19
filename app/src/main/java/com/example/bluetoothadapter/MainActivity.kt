package com.example.bluetoothadapter

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothA2dp
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.io.OutputStream
import java.util.IllegalFormatException

class MainActivity : ComponentActivity() {
    private val permissions = arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT
    )

    private val REQUEST_CODE_PERMISSION = 1
    private val REQUEST_ENABLE_BT = 2

    private var bluetoothA2dp: BluetoothA2dp? = null
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private val discoveredDevices = mutableListOf<BluetoothDevice>()
    private lateinit var devicesAdapter: ArrayAdapter<String>
    private lateinit var devicesListView: ListView
    private lateinit var mediaPlayer: MediaPlayer
    private val handler = Handler(Looper.getMainLooper())

    private val receiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            val action: String? = intent?.action
            Log.d(TAG, "Broadcast received: $action")

            when(action){
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                    device?.let{
                        val deviceClass = it.bluetoothClass.deviceClass
                        Log.d(TAG, "Device class: $deviceClass")

                        if(deviceClass == BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES ||
                            deviceClass == BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER ||
                            deviceClass == BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET){

                            Log.d(TAG, "Device found: ${it.name} - ${it.address}")
                            val permission = Manifest.permission.BLUETOOTH_CONNECT
                            val hasPermissions =
                                context?.let { ctx ->
                                    ContextCompat.checkSelfPermission(ctx, permission) == PackageManager.PERMISSION_GRANTED
                                } ?: false
                            if(hasPermissions) {
                                Log.d(TAG, "Device found ${it.name} - ${it.address}")
                                val deviceName = it.name ?: "Unknown device"
                                val deviceHardwareAddress = it.address
                                val deviceInfo = "$deviceName \n$deviceHardwareAddress"

                                if(!discoveredDevices.contains(it)){
                                    discoveredDevices.add(it)
                                    devicesAdapter.add(deviceInfo)
                                    devicesAdapter.notifyDataSetChanged()
                                } else {
                                    Log.d(TAG, "Device is null")
                                }
                            } else{
                                Toast.makeText(context, "Enable bluetooth permissions in settings to use application", Toast.LENGTH_SHORT).show()
                            }
                        } else{
                            Log.d(TAG, "Device is not audio")
                        }
                    }
                } BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d(TAG, "Bluetooth discovery finished")
                    Toast.makeText(context, "Discovery finished", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private val bondStateReceiver = object: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if(action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                val device: BluetoothDevice? = intent.getParcelableExtra(
                    BluetoothDevice.EXTRA_DEVICE
                )
                val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)
                val previousBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR)

                Log.d(TAG, "Bond state changed for ${device?.name}: $previousBondState")

                if(bondState == BluetoothDevice.BOND_BONDED){
                    Log.d(TAG, "${device?.name} is bonded")
                    device?.let {connectToAudioDevice(it)}
                }
            }
        }
    }

    private fun requestBluetoothPermissions() {
        Log.d(TAG, "requestBluetoothPermissions called")
        // Android 12 and above
        val bluetoothPermissions = arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (bluetoothPermissions.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }) {
            Log.d(TAG, "All Bluetooth permissions granted for Android 12+")
            initializeBluetooth()
        } else {
            Log.d(TAG, "Requesting Bluetooth permissions for Android 12+")
            ActivityCompat.requestPermissions(this, bluetoothPermissions, REQUEST_CODE_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Log.d(TAG, "All permissions granted.")
                initializeBluetooth()
            } else {
                Log.e(TAG, "Permissions denied.")
                Toast.makeText(this, "Bluetooth permissions are required.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val bluetoothProfileListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile == BluetoothProfile.A2DP) {
                bluetoothA2dp = proxy as BluetoothA2dp
                Log.d(TAG, "A2DP profile connected")
                startBluetoothScanning()
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.A2DP) {
                bluetoothA2dp = null
                Log.d(TAG, "A2DP profile disconnected")
            }
        }
    }

    private fun initializeBluetooth() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), REQUEST_CODE_PERMISSION)
            return
        }

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if(bluetoothAdapter == null){
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show()
            return
        }
        Log.d(TAG, "Bluetooth initialized")

        if(!bluetoothAdapter.isEnabled){
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        } else{
            bluetoothAdapter.getProfileProxy(this, bluetoothProfileListener, BluetoothProfile.A2DP)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_OK){
                bluetoothAdapter.getProfileProxy(this, bluetoothProfileListener, BluetoothProfile.A2DP)
            } else {
                Toast.makeText(this, "Bluetooth needed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startBluetoothScanning() {
        Log.d(TAG, "Starting Bluetooth scanning...")
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_SCAN), REQUEST_CODE_PERMISSION)
            return
        }

        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        registerReceiver(receiver, filter)

        discoveredDevices.clear()
        devicesAdapter.clear()

        if(bluetoothAdapter.isDiscovering){
            bluetoothAdapter.cancelDiscovery()
        }

        val started = bluetoothAdapter.startDiscovery()
        if(started){
            Toast.makeText(this, "Scanning for devices...", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Bluetooth scanning started")
        } else{
            Toast.makeText(this, "Failed to start discovery.", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Bluetooth scanning failed to start")
        }
    }

    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        devicesListView = findViewById(R.id.devices_list_view)
        Log.d(TAG, "devicesListView initialized successfully")
        devicesAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        devicesListView.adapter = devicesAdapter
        devicesAdapter.add("Dummy Device\n00:11:22:33:44:55")
        devicesAdapter.notifyDataSetChanged()

        devicesListView.setOnItemClickListener {parent, view, position, id ->
            val selectedDevice = discoveredDevices[position]
            connectToAudioDevice(selectedDevice)
        }

        val bondIntentFilter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        registerReceiver(bondStateReceiver, bondIntentFilter)

        requestBluetoothPermissions()
    }

    private fun playAudio() {
        if(mediaPlayer.isPlaying){
            mediaPlayer.stop()
            mediaPlayer.prepare()
        }
        mediaPlayer.start()
        Log.d(TAG, "Audio playback started")
    }


    private fun connectToAudioDevice(device: BluetoothDevice) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), REQUEST_CODE_PERMISSION)
            return
        }

        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }

        bluetoothA2dp?.let { a2dpProfile ->
            // Check if the device is already bonded

            if (device.bondState != BluetoothDevice.BOND_BONDED) {
                device.createBond()
            }

            // Use reflection to invoke the hidden connect method
            try {
                val connectMethod = BluetoothA2dp::class.java.getMethod("connect", BluetoothDevice::class.java)
                connectMethod.invoke(a2dpProfile, device)
                Log.d(TAG, "Connected to ${device.name}")

                mediaPlayer = MediaPlayer.create(this, R.raw.audio_file)

                handler.postDelayed(object: Runnable{
                    override fun run(){
                        playAudio()
                        handler.postDelayed(this, 30000)
                    }
                }, 0)
                runOnUiThread {
                    Toast.makeText(this, "Connected to ${device.name}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Failed to connect to ${device.name}", Toast.LENGTH_SHORT).show()
                }
            }
        } ?: run {
            Log.e(TAG, "A2DP profile not connected")
            Toast.makeText(this, "A2DP profile not connected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        try{
            handler.removeCallbacksAndMessages(null)
            if(::mediaPlayer.isInitialized && mediaPlayer.isPlaying){
                mediaPlayer.stop()
                mediaPlayer.release()
            }

            unregisterReceiver(receiver)
        } catch (e: IllegalFormatException){
            Toast.makeText(this, "Receiver not registered", Toast.LENGTH_SHORT).show()
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            if (bluetoothAdapter.isDiscovering) {
                bluetoothAdapter.cancelDiscovery()
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_SCAN), REQUEST_CODE_PERMISSION)
        }
    }
}