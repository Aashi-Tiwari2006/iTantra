package com.example.itantra

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ConnectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connection)

        val device1 = findViewById<LinearLayout>(R.id.device1)
        val device2 = findViewById<LinearLayout>(R.id.device2)
        val device3 = findViewById<LinearLayout>(R.id.device3)

        device1.setOnClickListener {
            connectDevice("Priya_Tablet", "BLUETOOTH", "-48 dBm")
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
}