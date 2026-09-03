package com.example.itantra

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Locale

class SttModeActivity : AppCompatActivity() {

    private lateinit var spinnerLanguage: Spinner
    private lateinit var btnStartListening: Button
    private lateinit var btnStopListening: Button
    private lateinit var textMicStatusIcon: TextView
    private lateinit var textListeningState: TextView
    private lateinit var textRecognizedOutput: TextView
    private lateinit var textTransmissionStatus: TextView

    private lateinit var speechRecognizer: SpeechRecognizer

    companion object {
        private const val PERMISSION_REQUEST_CODE = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stt_mode)

        setupUI()
        setupLanguageSelector()
        setupBottomNavigation()
        checkMicrophonePermission()

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        setupSpeechListener()

        btnStartListening.setOnClickListener { startListening() }
        btnStopListening.setOnClickListener { stopListening() }
    }

    private fun setupUI() {
        spinnerLanguage = findViewById(R.id.spinnerLanguage)
        btnStartListening = findViewById(R.id.btnStartListening)
        btnStopListening = findViewById(R.id.btnStopListening)
        textMicStatusIcon = findViewById(R.id.textMicStatusIcon)
        textListeningState = findViewById(R.id.textListeningState)
        textRecognizedOutput = findViewById(R.id.textRecognizedOutput)
        textTransmissionStatus = findViewById(R.id.textTransmissionStatus)
    }

    private fun setupLanguageSelector() {
        val languages = arrayOf(
            "Hindi",
            "Gujarati",
            "Marathi",
            "Kannada",
            "Malayalam",
            "Tamil",
            "Telugu",
            "Odia",
            "Bengali",
            "English"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, languages)
        spinnerLanguage.adapter = adapter
    }

    private fun checkMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSION_REQUEST_CODE)
        }
    }

    private fun startListening() {
        updateListeningState(true)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
        try {
            speechRecognizer.startListening(intent)
        } catch ( _ : Exception) {
            Toast.makeText(this, "Microphone start failed", Toast.LENGTH_SHORT).show()
            updateListeningState(false)
        }
    }

    private fun stopListening() {
        speechRecognizer.stopListening()
        updateListeningState(false)
    }

    private fun processSpeech(rawText: String) {
        // Placeholder function for custom STT models or local model parsing
        onTextRecognized(rawText)
    }

    private fun setupSpeechListener() {
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                textListeningState.text = "Listening..."
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                updateListeningState(false)
            }
            override fun onError(error: Int) {
                updateListeningState(false)
                Toast.makeText(applicationContext, "Recognition Error: $error", Toast.LENGTH_SHORT).show()
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    processSpeech(matches[0])
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun onTextRecognized(text: String) {
        textRecognizedOutput.text = text
        sendRecognizedText(text)
    }

    private fun sendRecognizedText(text: String) {
        updateTransmissionStatus("Status: Sending...")

        // Simulating packet dispatch via Bluetooth / Wi-Fi layer
        textTransmissionStatus.postDelayed({
            updateTransmissionStatus("Status: Sent ➔ Delivered (38ms)")
            saveMessageToHistory(text)
        }, 700)
    }

    private fun updateListeningState(isListening: Boolean) {
        if (isListening) {
            textMicStatusIcon.text = "🎙️"
            textListeningState.text = "Listening..."
            btnStartListening.isEnabled = false
            btnStopListening.isEnabled = true
        } else {
            textMicStatusIcon.text = "🤖"
            textListeningState.text = "Tap to Start Listening"
            btnStartListening.isEnabled = true
            btnStopListening.isEnabled = false
        }
    }

    private fun updateTransmissionStatus(status: String) {
        textTransmissionStatus.text = status
    }

    private fun saveMessageToHistory(text: String) {
        Toast.makeText(this, "Logged message: $text", Toast.LENGTH_SHORT).show()
    }

    private fun setupBottomNavigation() {
        findViewById<LinearLayout>(R.id.navHomeContainer).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
        findViewById<LinearLayout>(R.id.navSttContainer).setOnClickListener {
            // Already in STT mode
        }
        findViewById<LinearLayout>(R.id.navTtsContainer).setOnClickListener {
            val intent = Intent(this, TtsModeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
        findViewById<LinearLayout>(R.id.navSettingsContainer).setOnClickListener {
            Toast.makeText(this, "Tactical Protocol Settings", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        if (::speechRecognizer.isInitialized) {
            speechRecognizer.destroy()
        }
        super.onDestroy()
    }
}