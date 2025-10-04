package com.example.voicenotes

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class VoiceNoteActivity : AppCompatActivity() {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var resultTextView: TextView
    private lateinit var recordButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(generateLayout())

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        recordButton.setOnClickListener {
            startVoiceRecognition()
        }
    }

    private fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val data = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spokenText = data?.get(0) ?: ""
                val extracted = extractKeywords(spokenText).joinToString("\n")
                resultTextView.text = extracted
                saveNote("Voice Note", extracted)
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                Toast.makeText(this@VoiceNoteActivity, "Error recognizing speech", Toast.LENGTH_SHORT).show()
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer.startListening(intent)
    }

    private fun extractKeywords(input: String): List<String> {
        val actions = listOf("call", "remind", "schedule", "email", "meet", "appointment")
        val results = mutableListOf<String>()

        actions.forEach { action ->
            if (input.contains(action, ignoreCase = true)) {
                val start = input.indexOf(action, ignoreCase = true)
                val phrase = input.substring(start).split(" and", " then", ".").firstOrNull()
                phrase?.let { results.add(it.trim().replaceFirstChar { it.uppercase() }) }
            }
        }

        return if (results.isEmpty()) listOf("No actionable items found") else results
    }

    private fun saveNote(title: String, content: String) {
        val prefs = getSharedPreferences("smart_notes", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(title + "_" + System.currentTimeMillis(), content)
        editor.apply()
        Toast.makeText(this, "Note saved!", Toast.LENGTH_SHORT).show()
    }

    private fun generateLayout(): android.view.View {
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(40, 60, 40, 40)
        }

        resultTextView = TextView(this).apply {
            text = "🎤 Press the button and speak your note"
            textSize = 16f
        }

        recordButton = Button(this).apply {
            text = "Start Recording"
        }

        layout.addView(resultTextView)
        layout.addView(recordButton)

        return layout
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }
}
