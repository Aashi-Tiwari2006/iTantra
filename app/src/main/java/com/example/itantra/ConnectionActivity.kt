package com.example.itantra

import android.widget.TextView
import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class ConnectionActivity : AppCompatActivity() {

    private lateinit var bluetoothManager: BluetoothManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connection)
        val bluetoothStatus = findViewById<TextView>(R.id.bluetoothStatus)

        // Ask for Bluetooth permissions on Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                ),
                100
            )
        }

        bluetoothManager = BluetoothManager(
            this,
            onMessageReceived = { packet ->
                // We'll handle received messages later
            },
            onStateChanged = { state ->
                runOnUiThread {
                    bluetoothStatus.text = when {
                        state == "WAITING_FOR_CONNECTION" ->
                            "Bluetooth status: Waiting for connection"

                        state == "CONNECTING" ->
                            "Bluetooth status: Connecting..."

                        state == "CONNECTED" ->
                            "Bluetooth status: Connected"

                        state == "DISCONNECTED" ->
                            "Bluetooth status: Disconnected"

                        state.startsWith("CONNECTION_FAILED") ->
                            "Bluetooth status: Connection failed"

                        else ->
                            "Bluetooth status: $state"
                    }
                }

            }
        )

        val device1 = findViewById<LinearLayout>(R.id.device1)
        val device2 = findViewById<LinearLayout>(R.id.device2)
        val device3 = findViewById<LinearLayout>(R.id.device3)

        device1.setOnClickListener {
            val pairedDevices = getPairedBluetoothDevices()

            val device = pairedDevices.firstOrNull {
                it.name == "Lingraj's A21s"
            }

            if (device != null) {

                if (
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Toast.makeText(
                        this,
                        "Bluetooth permission required",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }

                Toast.makeText(
                    this,
                    "Connecting to ${device.name}...",
                    Toast.LENGTH_SHORT
                ).show()

                bluetoothManager.connectToDevice(device)

            } else {
                Toast.makeText(
                    this,
                    "Phone B not found",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        device2.setOnClickListener {
            connectDevice("Field_Unit_7", "WIFI P2P", "-61 dBm")
        }

        device3.setOnClickListener {
            connectDevice("Base_Station_A", "WIFI P2P", "-63 dBm")
        }
    }

    private fun connectDevice(
        deviceName: String,
        connectionType: String,
        signal: String
    ) {
        val intent = Intent(this, ChatActivity::class.java)

        intent.putExtra("DEVICE_NAME", deviceName)
        intent.putExtra("CONNECTION_TYPE", connectionType)
        intent.putExtra("SIGNAL", signal)

        startActivity(intent)
    }

    @SuppressLint("MissingPermission")
    private fun getPairedBluetoothDevices(): Set<android.bluetooth.BluetoothDevice> {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return emptySet()
        }

        return bluetoothAdapter?.bondedDevices ?: emptySet()
    }
}