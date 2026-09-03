package com.example.itantra

import android.util.Log
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

class WiFiSocketManager(
    private val port: Int = 8888,
    private val onMessageReceived: (DataPacket) -> Unit,
    private val onStateChanged: (String) -> Unit
) {
    private val gson = Gson()
    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null
    private var writer: PrintWriter? = null
    private var isRunning = false

    fun startServer() {
        Thread {
            try {
                serverSocket = ServerSocket(port)
                onStateChanged("Wi-Fi Server Listening on port $port")
                isRunning = true

                while (isRunning) {
                    clientSocket = serverSocket?.accept()
                    onStateChanged("Wi-Fi Device Connected")
                    val reader = BufferedReader(InputStreamReader(clientSocket?.getInputStream()))
                    val incomingJson = reader.readLine()
                    if (incomingJson != null) {
                        val packet = gson.fromJson(incomingJson, DataPacket::class.java)
                        onMessageReceived(packet)
                    }
                }
            } catch (e: Exception) {
                Log.e("WiFiManager", "Server error", e)
                onStateChanged("Wi-Fi Connection Failed")
            }
        }.start()
    }

    fun connectToServer(serverIp: String) {
        Thread {
            try {
                onStateChanged("Connecting to Wi-Fi Host: $serverIp")
                clientSocket = Socket(serverIp, port)
                writer = PrintWriter(clientSocket?.getOutputStream(), true)
                onStateChanged("Wi-Fi Connected")
            } catch (e: Exception) {
                Log.e("WiFiManager", "Client connect error", e)
                onStateChanged("Wi-Fi Connection Failed")
            }
        }.start()
    }

    fun sendPacket(packet: DataPacket) {
        Thread {
            try {
                val jsonString = gson.toJson(packet)
                writer?.println(jsonString)
            } catch (e: Exception) {
                Log.e("WiFiManager", "Send error", e)
            }
        }.start()
    }

    fun stop() {
        isRunning = false
        serverSocket?.close()
        clientSocket?.close()
    }
}