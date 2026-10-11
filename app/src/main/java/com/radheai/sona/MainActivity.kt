package com.radheai.sona

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private var recognizer: SpeechRecognizer? = null
    private var listening = false
    private var awaitingCommand = false
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var statusText: TextView
    private lateinit var transcriptText: TextView
    private lateinit var toggleButton: Button

    private val micPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startListening()
        else statusText.text = "SONA ko microphone permission chahiye."
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        transcriptText = findViewById(R.id.transcriptText)
        toggleButton = findViewById(R.id.startSonaButton)

        tts = TextToSpeech(this) { result ->
            ttsReady = result == TextToSpeech.SUCCESS
            if (ttsReady) {
                val language = tts?.setLanguage(Locale("hi", "IN"))
                if (language == TextToSpeech.LANG_MISSING_DATA ||
                    language == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.setLanguage(Locale.US)
                }
            }
        }

        toggleButton.setOnClickListener {
            if (listening) stopListening()
            else {
                if (ContextCompat.checkSelfPermission(
                        this, Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED) {
                    startListening()
                } else {
                    micPermission.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
        }
    }

    private fun speak(message: String) {
        if (ttsReady) {
            tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "sona_reply")
        }
    }

    private fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            statusText.text = "Speech recognition available nahi hai."
            return
        }
        listening = true
        toggleButton.text = "Stop Sona"
        statusText.text = "SONA ko bulao..."
        if (recognizer == null) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
                setRecognitionListener(listener)
            }
        }
        listenAgain(300)
    }

    private fun listenAgain(delay: Long) {
        if (!listening) return
        handler.postDelayed({
            if (!listening) return@postDelayed
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                }
                recognizer?.startListening(intent)
            } catch (_: Exception) {
                listenAgain(800)
            }
        }, delay)
    }

    private fun stopListening() {
        listening = false
        awaitingCommand = false
        handler.removeCallbacksAndMessages(null)
        recognizer?.cancel()
        toggleButton.text = "Start Sona"
        statusText.text = "SONA band hai."
        speak("SONA band hai")
    }

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            statusText.text = if (awaitingCommand) "Haan, command bataiye" else "SONA ko bulao..."
        }
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onError(error: Int) { listenAgain(500) }
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}

        override fun onResults(results: Bundle?) {
            val phrase = results?.getStringArrayList(
                SpeechRecognizer.RESULTS_RECOGNITION
            )?.firstOrNull()?.trim()

            if (!phrase.isNullOrBlank()) handlePhrase(phrase)
            listenAgain(700)
        }
    }

    private fun handlePhrase(phrase: String) {
        transcriptText.text = phrase
        val wakeRegex = Regex("(?i)sia|सीया|सिया")
        val hasWake = wakeRegex.containsMatchIn(phrase)

        if (hasWake) {
            val commandText = wakeRegex.replace(phrase, "").trim(' ', ',', '.', '!')
            if (commandText.isBlank()) {
                awaitingCommand = true
                statusText.text = "SONA ready hai."
                speak("Yes boss!")
                return
            }
            awaitingCommand = false
            executeCommand(commandText)
            return
        }

        if (awaitingCommand) {
            awaitingCommand = false
            executeCommand(phrase)
        } else {
            statusText.text = "SIA ko bulaane ke liye Sia bolo."
        }
    }

    private fun executeCommand(text: String) {
        val command = SonaCommandParser.parse(text)
        transcriptText.text = "$text\nIntent: ${command.intent} (${command.target})"

        if (command.intent == "UNKNOWN" ||
            (command.intent == "OPEN_APP" && command.target.isBlank())) {
            statusText.text = "Command samajh nahi aayi."
            speak("माफ कीजिए, कमांड समझ नहीं आई")
            return
        }

        val service = SonaAccessibilityService.instance
        if (service == null) {
            statusText.text = "Accessibility permission enable karein."
            speak("कृपया SONA की Accessibility अनुमति चालू करें")
            try {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            } catch (_: Exception) {}
            return
        }

        val success = service.executeVoiceAction(command.intent, command.target)
        if (success) {
            statusText.text = "Command chal gayi: ${command.intent}"
            speak("जी, कर दिया")
        } else {
            statusText.text = "Command nahi chali: ${command.intent}"
            speak("माफ कीजिए, यह कमांड नहीं चली")
        }
    }

    override fun onDestroy() {
        listening = false
        handler.removeCallbacksAndMessages(null)
        recognizer?.destroy()
        recognizer = null
        tts?.stop()
        tts?.shutdown()
        tts = null
        super.onDestroy()
    }
}
