package com.example.itantra

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import java.util.Locale

class TtsModeActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var textConnectionBadge: TextView
    private lateinit var textMsgType: TextView
    private lateinit var textMsgLanguage: TextView
    private lateinit var textReceivedContent: TextView
    private lateinit var textSpeakingState: TextView
    private lateinit var cardMessageContainer: CardView
    private lateinit var btnReplay: Button
    private lateinit var btnStopSpeaking: Button

    private lateinit var tts: TextToSpeech
    private var lastPacket: DataPacket? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tts_mode)

        setupUI()
        setupTts()
        setupBottomNavigation()

        btnReplay.setOnClickListener { replayMessage() }
        btnStopSpeaking.setOnClickListener { stopSpeaking() }

        // Simulation listener for incoming packets
        listenForIncomingMessages()
    }

    private fun setupUI() {
        textConnectionBadge = findViewById(R.id.textConnectionBadge)
        textMsgType = findViewById(R.id.textMsgType)
        textMsgLanguage = findViewById(R.id.textMsgLanguage)
        textReceivedContent = findViewById(R.id.textReceivedContent)
        textSpeakingState = findViewById(R.id.textSpeakingState)
        cardMessageContainer = findViewById(R.id.cardMessageContainer)
        btnReplay = findViewById(R.id.btnReplay)
        btnStopSpeaking = findViewById(R.id.btnStopSpeaking)
    }

    private fun setupTts() {
        tts = TextToSpeech(this, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale("en", "US")
        }
    }

    private fun listenForIncomingMessages() {
        // Simulating receipt of incoming packet after launch
        textReceivedContent.postDelayed({
            val samplePacket = DataPacket(
                type = "emergency",
                message = "Fire detected. Evacuate the area immediately.",
                language = "English"
            )
            onMessageReceived(samplePacket)
        }, 1200)
    }

    private fun onMessageReceived(packet: DataPacket) {
        lastPacket = packet
        displayIncomingMessage(packet)
        saveMessageToHistory(packet)

        if (packet.type == "emergency") {
            handleEmergencyMessage(packet)
        } else {
            speakText(packet.message)
        }
    }

    private fun displayIncomingMessage(packet: DataPacket) {
        textMsgType.text = "Type: ${packet.type.uppercase()}"
        textMsgLanguage.text = "Lang: ${packet.language}"
        textReceivedContent.text = packet.message
    }

    private fun handleEmergencyMessage(packet: DataPacket) {
        displayEmergencyUI()
        speakText(packet.message)
    }

    private fun displayEmergencyUI() {
        cardMessageContainer.setCardBackgroundColor(Color.parseColor("#FFEBEE"))
        textMsgType.setTextColor(Color.parseColor("#C62828"))
        textReceivedContent.setTextColor(Color.parseColor("#B71C1C"))
    }

    private fun speakText(text: String) {
        updateSpeakingState(true)
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)

        textSpeakingState.postDelayed({
            updateSpeakingState(false)
        }, 3000)
    }

    private fun stopSpeaking() {
        if (::tts.isInitialized) {
            tts.stop()
        }
        updateSpeakingState(false)
        Toast.makeText(this, "Speech stopped", Toast.LENGTH_SHORT).show()
    }

    private fun replayMessage() {
        lastPacket?.let {
            speakText(it.message)
        } ?: Toast.makeText(this, "No message to replay", Toast.LENGTH_SHORT).show()
    }

    private fun updateSpeakingState(isSpeaking: Boolean) {
        if (isSpeaking) {
            textSpeakingState.text = "Status: Speaking..."
        } else {
            textSpeakingState.text = "Status: Idle / Ready"
        }
    }

    private fun saveMessageToHistory(packet: DataPacket) {
        // Logs message to historical tracking
    }

    private fun setupBottomNavigation() {
        findViewById<LinearLayout>(R.id.navHomeContainer).setOnClickListener { openHome() }
        findViewById<LinearLayout>(R.id.navSttContainer).setOnClickListener { openSttMode() }
        findViewById<LinearLayout>(R.id.navTtsContainer).setOnClickListener { openTtsMode() }
        findViewById<LinearLayout>(R.id.navSettingsContainer).setOnClickListener { openSettings() }
    }

    private fun openHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    private fun openSttMode() {
        val intent = Intent(this, SttModeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    private fun openTtsMode() {
        // Already in TTS mode
    }

    private fun openSettings() {
        Toast.makeText(this, "Tactical Protocol Settings", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}