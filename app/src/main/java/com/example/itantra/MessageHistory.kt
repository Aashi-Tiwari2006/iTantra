package com.example.itantra

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LogEntry(
    val timestamp: String,
    val direction: String, // "SENT" or "RECEIVED"
    val message: String,
    val type: String       // "normal", "alert", "emergency"
)

object MessageHistory {
    val logs = mutableListOf<LogEntry>()

    fun addLog(direction: String, message: String, type: String = "normal") {
        val timeFormat = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date())
        logs.add(0, LogEntry(timeFormat, direction, message, type))
    }
}