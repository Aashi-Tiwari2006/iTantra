package com.example.itantra

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class BluetoothManager(
    private val context: android.content.Context,
    private val onMessageReceived: (DataPacket) -> Unit,
    private val onStateChanged: (String) -> Unit
) {

    private val bluetoothAdapter: BluetoothAdapter? =
        BluetoothAdapter.getDefaultAdapter()

    private val appUuid: UUID =
        UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66")

    private val gson = Gson()

    private var clientSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null
    private var isConnected = false
    private var serverSocket: BluetoothServerSocket? = null

    fun connectToDevice(device: BluetoothDevice) {
        Thread {
            try {
                onStateChanged("CONNECTING")

                if (!hasBluetoothConnectPermission()) {
                    onStateChanged("BLUETOOTH_CONNECT_PERMISSION_REQUIRED")
                    return@Thread
                }

                if (
                    android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_SCAN
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    bluetoothAdapter?.cancelDiscovery()
                }

                clientSocket =
                    device.createRfcommSocketToServiceRecord(appUuid)

                clientSocket?.connect()

                outputStream = clientSocket?.outputStream
                inputStream = clientSocket?.inputStream

                isConnected = true
                onStateChanged("CONNECTED")

                startListeningForIncomingData()

            } catch (e: Exception) {
                Log.e("BluetoothManager", "Connection failed", e)
                isConnected = false
                onStateChanged("CONNECTION_FAILED: ${e.message}")
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

        while (isConnected) {
            try {
                val bytes = inputStream?.read(buffer) ?: -1

                if (bytes > 0) {
                    val incomingJson =
                        String(buffer, 0, bytes).trim()

                    val packet =
                        gson.fromJson(incomingJson, DataPacket::class.java)

                    onMessageReceived(packet)
                }

            } catch (e: Exception) {
                Log.e("BluetoothManager", "Connection failed", e)
                isConnected = false
                onStateChanged("DISCONNECTED")
                break
            }
        }
    }

    private fun hasBluetoothConnectPermission(): Boolean {
        return android.os.Build.VERSION.SDK_INT <
                android.os.Build.VERSION_CODES.S ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    fun startServer() {
        Thread {
            try {
                onStateChanged("WAITING_FOR_CONNECTION")

                if (!hasBluetoothConnectPermission()) {
                    onStateChanged("BLUETOOTH_CONNECT_PERMISSION_REQUIRED")
                    return@Thread
                }

                serverSocket =
                    bluetoothAdapter?.listenUsingRfcommWithServiceRecord(
                        "iTantra",
                        appUuid
                    )

                val socket = serverSocket?.accept()

                serverSocket?.close()

                if (socket != null) {
                    clientSocket = socket
                    outputStream = socket.outputStream
                    inputStream = socket.inputStream

                    isConnected = true
                    onStateChanged("CONNECTED")

                    startListeningForIncomingData()
                }

            } catch (e: Exception) {
                Log.e("BluetoothManager", "Server failed", e)
                onStateChanged("CONNECTION_FAILED: ${e.message}")
            }
        }.start()
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