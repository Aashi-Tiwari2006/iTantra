package com.example.itantra

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val connectButton = findViewById<Button>(R.id.btnConnect)

        connectButton.setOnClickListener {
            val intent = Intent(this, ConnectionActivity::class.java)
            startActivity(intent)
        }
    }
}