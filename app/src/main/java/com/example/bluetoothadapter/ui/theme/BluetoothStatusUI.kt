package com.example.bluetoothadapter.ui.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState

@Composable
fun BluetoothStatusUI(bluetoothEnabled: MutableState<Boolean>){
    Column{
        if(!bluetoothEnabled.value){
            Text("Enable bluetooth to discovery nearby devices.")
        } else {
            Text("Bluetooth is enabled. Discovering devices...")
        }
    }
}
