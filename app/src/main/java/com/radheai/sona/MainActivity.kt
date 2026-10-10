package com.radheai.sona

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListeningForSona = false
    private val mainHandler = Handler(Looper.getMainLooper())

    private lateinit var statusText: TextView
    private lateinit var transcriptText: TextView
    private lateinit var toggleButton: Button

    private val wakeWords = listOf("sona", "सोना", "sohna", "sonaa", "shona", "सोन", "शोना", "sonu")

    private val micPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startSonaListening()
        } else {
            statusText.text = "Microphone permission is required to detect 'Sona' / 'सोना'."
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        transcriptText = findViewById(R.id.transcriptText)
        toggleButton = findViewById(R.id.startSonaButton)

        toggleButton.setOnClickListener {
            if (isListeningForSona) {
                stopSonaListening()
            } else {
                checkMicPermissionAndStart()
            }
        }
    }

    private fun checkMicPermissionAndStart() {
        val permissionState = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        )
        if (permissionState == PackageManager.PERMISSION_GRANTED) {
            startSonaListening()
        } else {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startSonaListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            statusText.text = "SpeechRecognizer is not available on this Android device."
            return
        }

        isListeningForSona = true
        toggleButton.text = "Stop Sona"
        statusText.text = "Listening for 'Sona' or 'सोना'..."

        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
                setRecognitionListener(sonaRecognitionListener)
            }
        }

        launchRecognitionCycle()
    }

    private fun launchRecognitionCycle() {
        if (!isListeningForSona) return
        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
        speechRecognizer?.startListening(recognizerIntent)
    }

    private fun stopSonaListening() {
        isListeningForSona = false
        mainHandler.removeCallbacksAndMessages(null)
        speechRecognizer?.stopListening()
        speechRecognizer?.cancel()
        toggleButton.text = "Start Sona"
        statusText.text = "Sona is paused. Tap 'Start Sona' to resume."
    }

    private val sonaRecognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            statusText.text = "Listening... Say 'Sona' or 'सोना'"
        }

        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}

        override fun onError(error: Int) {
            if (isListeningForSona) {
                mainHandler.postDelayed(
                    { launchRecognitionCycle() },
                    350L
                )
            }
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            handleTranscriptMatches(matches, isFinal = true)
            if (isListeningForSona) {
                mainHandler.postDelayed(
                    { launchRecognitionCycle() },
                    350L
                )
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            handleTranscriptMatches(matches, isFinal = false)
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    private fun handleTranscriptMatches(matches: List<String>?, isFinal: Boolean) {
        val topPhrase = matches?.firstOrNull()?.trim() ?: return
        transcriptText.text = topPhrase

        val normalized = topPhrase.lowercase()
        val woke = wakeWords.any { alias -> normalized.contains(alias) }

        if (woke) {
            statusText.text = "Wake Word Detected: Sona (सोना)!"
            val parsed = SonaCommandParser.parse(topPhrase)
            transcriptText.text = "$topPhrase\n→ Intent: ${parsed.intent} (${parsed.target})"
        } else if (isFinal) {
            statusText.text = "Heard speech (no wake word). Still listening..."
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isListeningForSona = false
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}
