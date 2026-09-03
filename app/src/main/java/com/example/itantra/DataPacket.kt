package com.example.itantra

data class DataPacket(
    val language: String,
    val type: String, // "normal", "alert", "emergency"
    val message: String
)

object LanguageRegistry {
    val languages = mapOf(
        "English" to "en-IN",
        "Hindi" to "hi-IN",
        "Gujarati" to "gu-IN",
        "Marathi" to "mr-IN",
        "Kannada" to "kn-IN",
        "Malayalam" to "ml-IN",
        "Tamil" to "ta-IN",
        "Telugu" to "te-IN",
        "Odia" to "or-IN",
        "Bengali" to "bn-IN"
    )
    var currentSelectedLangCode: String = "en-IN"
}