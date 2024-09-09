package com.example.bluetoothadapter.ui.theme

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun PermissionDeniedDialog(onDismissRequest: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = "Permission required") },
        text = { Text(text = "Bluetooth and Location permissions are required for this application. Please enable them in settings") },
        confirmButton = {
            Button(onClick = {
                onDismissRequest()
            }) { Text("OK") }
        }
    )
}