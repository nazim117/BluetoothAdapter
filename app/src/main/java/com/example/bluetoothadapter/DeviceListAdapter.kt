package com.example.bluetoothadapter

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class DeviceListAdapter(context: Context): ArrayAdapter<BluetoothDevice>(context, 0) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val device = getItem(position)
        val view = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false)

        val text1 = view.findViewById<TextView>(android.R.id.text1)
        val text2 = view.findViewById<TextView>(android.R.id.text2)

        text1.text = device?.name ?: "Unknown device"
        text2.text = device?.address

        return view
    }

    fun addDevice(device: BluetoothDevice) {
        if(!contains(device)){
            add(device)
            notifyDataSetChanged()
        }
    }

    private fun contains(device: BluetoothDevice): Boolean{
        return(0 until count).any { getItem(it)?.address == device.address}
    }

    override fun clear(){
        super.clear()
        notifyDataSetChanged()
    }
}