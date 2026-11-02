
package com.raunak.jarvis

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.*
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private lateinit var status: TextView
    private lateinit var input: EditText
    private var speechRecognizer: SpeechRecognizer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this, this)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 48, 32, 32)
            setBackgroundColor(0xFF07111F.toInt())
        }

        val title = TextView(this).apply {
            text = "J.A.R.V.I.S"
            textSize = 30f
            setTextColor(0xFF00E5FF.toInt())
            gravity = android.view.Gravity.CENTER
        }

        status = TextView(this).apply {
            text = "Your AI Assistant is ready"
            textSize = 17f
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(0, 30, 0, 30)
        }

        input = EditText(this).apply {
            hint = "Ask me anything..."
            setTextColor(0xFFFFFFFF.toInt())
            setHintTextColor(0xFFAAAAAA.toInt())
            setSingleLine(true)
        }

        val send = Button(this).apply {
            text = "SEND"
            setOnClickListener {
                processCommand(input.text.toString())
            }
        }

        val voice = Button(this).apply {
            text = "🎙 TALK TO JARVIS"
            setOnClickListener {
                startListening()
            }
        }

        layout.addView(title)
        layout.addView(status)
        layout.addView(input)
        layout.addView(send)
        layout.addView(voice)

        setContentView(layout)
    }

    private fun processCommand(command: String) {
        val text = command.lowercase(Locale.getDefault())

        val reply = when {
            text.contains("hello") || text.contains("नमस्ते") ->
                "Hello! I am Jarvis. How can I help you?"

            text.contains("youtube") ->
                "Opening YouTube"

            text.contains("settings") ->
                "Opening Settings"

            text.contains("समय") || text.contains("time") ->
                "The time is " +
                java.text.SimpleDateFormat(
                    "hh:mm a", Locale.getDefault()
                ).format(java.util.Date())

            else ->
                "I am ready to help. AI connection will be added next."
        }

        status.text = reply
        speak(reply)

        if (text.contains("youtube")) {
            openApp("com.google.android.youtube")
        }

        if (text.contains("settings")) {
            startActivity(Intent(android.provider.Settings.ACTION_SETTINGS))
        }
    }

    private fun openApp(packageName: String) {
        val launchIntent = packageManager
            .getLaunchIntentForPackage(packageName)

        if (launchIntent != null) {
            startActivity(launchIntent)
        } else {
            speak("App not found")
        }
    }

    private fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            speak("Speech recognition is unavailable")
            return
        }

        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        speechRecognizer?.setRecognitionListener(
            object : RecognitionListener {

                override fun onReadyForSpeech(params: Bundle?) {
                    status.text = "Listening..."
                }

                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    status.text = "Processing..."
                }

                override fun onError(error: Int) {
                    status.text = "Voice error. Try again."
                }

                override fun onResults(results: Bundle?) {
                    val words = results?.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION
                    )

                    val command = words?.firstOrNull() ?: ""
                    input.setText(command)
                    processCommand(command)
                }

                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            }
        )

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            "hi-IN"
        )
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
            "hi-IN"
        )

        speechRecognizer?.startListening(intent)
    }

    private fun speak(message: String) {
        tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "JARVIS")
    }

    override fun onInit(statusCode: Int) {
        if (statusCode == TextToSpeech.SUCCESS) {
            tts.language = Locale("hi", "IN")
        }
    }

    override fun onDestroy() {
        speechRecognizer?.destroy()
        tts.shutdown()
        super.onDestroy()
    }
}
