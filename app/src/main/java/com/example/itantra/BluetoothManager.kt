package com.example.itantra

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import com.google.gson.Gson
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class BluetoothManager(
    private val onMessageReceived: (DataPacket) -> Unit,
    private val onStateChanged: (String) -> Unit
) {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val appUuid: UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66")
    private val gson = Gson()

    private var clientSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null
    private var isConnected = false

    fun connectToDevice(device: BluetoothDevice) {
        Thread {
            try {
                onStateChanged("CONNECTING")
                clientSocket = device.createRfcommSocketToServiceRecord(appUuid)
                bluetoothAdapter?.cancelDiscovery()
                clientSocket?.connect()

                outputStream = clientSocket?.outputStream
                inputStream = clientSocket?.inputStream
                isConnected = true
                onStateChanged("CONNECTED")

                startListeningForIncomingData()
            } catch (e: Exception) {
                Log.e("BluetoothManager", "Connection failed", e)
                isConnected = false
                onStateChanged("CONNECTION_FAILED")
            }
        }.start()
    }

    fun sendPacket(packet: DataPacket) {
        Thread {
            try {
                if (isConnected && outputStream != null) {
                    val jsonString = gson.toJson(packet) + "\n"
                    outputStream?.write(jsonString.toByteArray())
                    outputStream?.flush()
                }
            } catch (e: Exception) {
                Log.e("BluetoothManager", "Send failed", e)
            }
        }.start()
    }

    private fun startListeningForIncomingData() {
        val buffer = ByteArray(1024)
        var bytes: Int
        while (isConnected) {
            try {
                bytes = inputStream?.read(buffer) ?: -1
                if (bytes > 0) {
                    val incomingJson = String(buffer, 0, bytes).trim()
                    val packet = gson.fromJson(incomingJson, DataPacket::class.java)
                    onMessageReceived(packet)
                }
            } catch (e: Exception) {
                isConnected = false
                onStateChanged("DISCONNECTED")
                break
            }
        }
    }

    fun disconnect() {
        try {
            isConnected = false
            clientSocket?.close()
        } catch (e: Exception) {
            Log.e("BluetoothManager", "Close error", e)
        }
    }
}