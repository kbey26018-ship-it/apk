package com.halil.sonsuzyolarkadasi

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var txtStorageStatus: TextView
    private lateinit var txtChatHistory: TextView
    private lateinit var scrollChat: ScrollView
    private lateinit var edtPrompt: EditText
    private lateinit var switchHybrid: SwitchCompat

    private lateinit var sdManager: SDCardModelManager
    private lateinit var mediaController: DeviceMediaController
    private val aiRouter = HybridAIRouter()

    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtStorageStatus = findViewById(R.id.txtStorageStatus)
        txtChatHistory = findViewById(R.id.txtChatHistory)
        scrollChat = findViewById(R.id.scrollChat)
        edtPrompt = findViewById(R.id.edtPrompt)
        switchHybrid = findViewById(R.id.switchHybridMode)

        sdManager = SDCardModelManager(this)
        mediaController = DeviceMediaController(this)
        tts = TextToSpeech(this, this)

        checkSDCardModels()
        setupVoiceRecognizer()

        findViewById<Button>(R.id.btnSend).setOnClickListener {
            val text = edtPrompt.text.toString().trim()
            if (text.isNotEmpty()) {
                edtPrompt.text.clear()
                processUserInput(text)
            }
        }

        findViewById<Button>(R.id.btnVoice).setOnClickListener {
            startListening()
        }
    }

    private fun checkSDCardModels() {
        val models = sdManager.listAvailableModels()
        txtStorageStatus.text = if (models.isNotEmpty()) {
            "SD Kart Hazır (${models.size} model): ${models.first().name}"
        } else {
            "SD Kart Hazır: /SonsuzYolArkadasi/models içine .gguf modeli atabilirsin."
        }
    }

    private fun processUserInput(input: String) {
        appendChat("Halil: $input")

        val deviceActionReply = mediaController.handleCommand(input)
        if (deviceActionReply != null) {
            appendChat("Yol Arkadaşın: $deviceActionReply")
            speakOut(deviceActionReply)
            return
        }

        val activeModel = sdManager.listAvailableModels().firstOrNull()?.name
        lifecycleScope.launch {
            val reply = aiRouter.sendQuery(
                prompt = input,
                preferLocalPc = switchHybrid.isChecked,
                localModelName = activeModel
            )
            appendChat("Yol Arkadaşın: $reply")
            speakOut(reply)
        }
    }

    private fun appendChat(line: String) {
        txtChatHistory.append("
$line
")
        scrollChat.post { scrollChat.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    private fun speakOut(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "assistant_reply")
    }

    private fun setupVoiceRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        processUserInput(matches[0])
                    }
                }
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onError(error: Int) {}
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR")
        }
        speechRecognizer?.startListening(intent)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale("tr", "TR")
            tts?.setSpeechRate(1.03f)
            tts?.setPitch(1.02f)
        }
    }

    override fun onDestroy() {
        tts?.shutdown()
        speechRecognizer?.destroy()
        super.onDestroy()
    }
}
