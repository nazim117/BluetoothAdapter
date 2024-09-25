package com.example.bluetoothadapter

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat

class BluetoothManager(private val context: Context, private val deviceListAdapter: DeviceListAdapter) {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothA2dp: BluetoothA2dp? = null
    private var currentlyConnectedDevice: BluetoothDevice? = null
    private var isPendingConnection = false
    private val discoveredDevices = mutableListOf<BluetoothDevice>()
    private lateinit var devicesAdapter: ArrayAdapter<String>
    private lateinit var  audioManager : AudioFileManager
    private lateinit var permissionManager: PermissionManager

    private val TAG = "BluetoothAdapter"

    init{
        initializeDeviceAdapter()
        initializePermissionManager()
        initializeBluetooth()
        initializeAudioManager()
        registerReceivers()
    }

    private fun initializeDeviceAdapter() {
        devicesAdapter = ArrayAdapter(
            context,
            android.R.layout.simple_list_item_1,
            mutableListOf()
        )
    }

    private fun initializePermissionManager() {
        permissionManager = PermissionManager(context)
    }

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
                            deviceListAdapter.addDevice(it)
//                            val permission = Manifest.permission.BLUETOOTH_CONNECT
//                            val hasPermissions =
//                                context?.let { ctx ->
//                                    ContextCompat.checkSelfPermission(ctx, permission) == PackageManager.PERMISSION_GRANTED
//                                } ?: false
//                            if(hasPermissions) {
//                                Log.d(TAG, "Device found ${it.name} - ${it.address}")
//                                val deviceName = it.name ?: "Unknown device"
//                                val deviceHardwareAddress = it.address
//                                val deviceInfo = "$deviceName \n$deviceHardwareAddress"
//
//                                if(!discoveredDevices.contains(it)){
//                                    deviceListAdapter.addDevice(it)
//                                    devicesAdapter.add(deviceInfo)
//                                    devicesAdapter.notifyDataSetChanged()
//                                } else {
//                                    Log.d(TAG, "Device is null")
//                                }
//                            } else{
//                                Toast.makeText(context, "Enable bluetooth permissions in settings to use application", Toast.LENGTH_SHORT).show()
//                            }
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

    private val bondStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if(!permissionManager.checkBluetoothConnectPermission()){
                permissionManager.requestBluetoothPermissions()
            }
            val action = intent?.action
            if (action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                val device: BluetoothDevice? = intent.getParcelableExtra(
                    BluetoothDevice.EXTRA_DEVICE
                )
                val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)
                val previousBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR)

                Log.d(TAG, "Bond state changed for ${device?.name}: $previousBondState -> $bondState")

                when (bondState) {
                    BluetoothDevice.BOND_BONDED -> {
                        Log.d(TAG, "${device?.name} is bonded")
                        if (isPendingConnection) {
                            device?.let { connectToA2DPProfile(it) }
                        }
                    }
                    BluetoothDevice.BOND_NONE -> {
                        Log.d(TAG, "${device?.name} is not bonded")
                        if (device == currentlyConnectedDevice) {
                            currentlyConnectedDevice = null
                            audioManager.stopAudioPlayback()
                        }
                    }
                }
            }
        }
    }

    fun connectToAudioDevice(device: BluetoothDevice) {
        if(!permissionManager.checkBluetoothConnectPermission()){
            permissionManager.requestBluetoothPermissions()
        }

        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }

        if (device == currentlyConnectedDevice) {
            unpairAndStopPlayback(device)
        } else {
            when (device.bondState) {
                BluetoothDevice.BOND_NONE -> {
                    isPendingConnection = true
                    device.createBond()
                }
                BluetoothDevice.BOND_BONDED -> {
                    connectToA2DPProfile(device)
                }
                else -> {
                    Log.d(TAG, "Device is in bonding process, waiting...")
                    isPendingConnection = true
                }
            }
        }
    }

    private fun unpairAndStopPlayback(device: BluetoothDevice) {
        if(permissionManager.checkBluetoothConnectPermission()){
            audioManager.stopAudioPlayback()
        }

        try{
            audioManager.stopAudioPlayback()

            val removeBondMethod = device.javaClass.getMethod("removeBond")
            removeBondMethod.invoke(device)

            currentlyConnectedDevice = null

        } catch (e: Exception){
            e.printStackTrace()
        }
    }

    private fun connectToA2DPProfile(device: BluetoothDevice) {
        if(permissionManager.checkBluetoothConnectPermission()){
            permissionManager.requestBluetoothPermissions()
        }

        bluetoothA2dp?.let { a2dpProfile ->
            try {
                val connectMethod = BluetoothA2dp::class.java.getMethod("connect", BluetoothDevice::class.java)
                connectMethod.invoke(a2dpProfile, device)
                Log.d(TAG, "Connecting to ${device.name} via A2DP")

                // Wait for the connection to be established
                audioManager.handler.postDelayed({
                    if (isA2DPConnected(device)) {
                        currentlyConnectedDevice = device
                        if (!audioManager.isPlaying) {
                            audioManager.startAudioPlayback()
                        }
                    } else {
                        Log.e(TAG, "Failed to connect to ${device.name} via A2DP")
                    }
                    isPendingConnection = false
                }, 2000) // Wait for 2 seconds before checking the connection

            } catch (e: Exception) {
                e.printStackTrace()
                isPendingConnection = false
            }
        } ?: run {
            Log.e(TAG, "A2DP profile not connected")
            isPendingConnection = false
        }
    }

    private fun isA2DPConnected(device: BluetoothDevice): Boolean {

        if(permissionManager.checkBluetoothConnectPermission()){
            permissionManager.requestBluetoothPermissions()
        }

        return bluetoothA2dp?.getConnectionState(device) == BluetoothProfile.STATE_CONNECTED
    }

    private val bluetoothProfileListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile == BluetoothProfile.A2DP) {
                bluetoothA2dp = proxy as BluetoothA2dp
                Log.d(TAG, "A2DP profile connected")
                startBluetoothScanning()
            }
        }

        private fun startBluetoothScanning() {
            Log.d(TAG, "Starting Bluetooth scanning...")
            if(!permissionManager.checkBluetoothConnectPermission()){
                permissionManager.requestBluetoothPermissions()
            }

            val filter = IntentFilter().apply {
                addAction(BluetoothDevice.ACTION_FOUND)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            }
            registerReceivers()

            discoveredDevices.clear()
            devicesAdapter.clear()

            if(bluetoothAdapter.isDiscovering){
                bluetoothAdapter.cancelDiscovery()
            }

            val started = bluetoothAdapter.startDiscovery()
            if(started){
                Log.d(TAG, "Bluetooth scanning started")
            } else{
                Log.e(TAG, "Bluetooth scanning failed to start")
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.A2DP) {
                bluetoothA2dp = null
                Log.d(TAG, "A2DP profile disconnected")
            }
        }
    }

    private fun registerReceivers() {
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        context.registerReceiver(receiver, filter)

        val bondIntentFilter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        context.registerReceiver(bondStateReceiver, bondIntentFilter)
    }

    private fun initializeAudioManager() {
        audioManager = AudioFileManager(context)
    }

    private fun initializeBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        Log.d(TAG, "Bluetooth initialized")

        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

            if(!permissionManager.checkBluetoothConnectPermission()){
                permissionManager.requestBluetoothPermissions()
            }

            if(context is Activity){
                context.startActivity(enableBtIntent)
            }else{
                Log.e(TAG,"Bluetooth not enabled")
            }
        } else {
            initializeA2DPProxy()
        }
    }

    fun initializeA2DPProxy() {
        bluetoothAdapter.getProfileProxy(context, bluetoothProfileListener, BluetoothProfile.A2DP)
    }

    fun onDestroy(){
        context.unregisterReceiver(receiver)
        context.unregisterReceiver(bondStateReceiver)

        if(!permissionManager.checkBluetoothConnectPermission()){
            permissionManager.requestBluetoothPermissions()
        }

        if(bluetoothAdapter.isDiscovering){
            bluetoothAdapter.cancelDiscovery()
        }
    }
}