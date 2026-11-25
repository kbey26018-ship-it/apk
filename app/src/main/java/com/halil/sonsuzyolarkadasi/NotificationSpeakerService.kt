package com.halil.sonsuzyolarkadasi

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.speech.tts.TextToSpeech
import java.util.Locale

class NotificationSpeakerService : NotificationListenerService(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isReady = false

    override fun onCreate() {
        super.onCreate()
        tts = TextToSpeech(this, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale("tr", "TR")
            tts?.setSpeechRate(1.03f)
            tts?.setPitch(1.02f)
            isReady = true
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (!isReady || sbn == null) return
        val extras = sbn.notification.extras
        val title = extras.getString("android.title") ?: return
        val text = extras.getCharSequence("android.text")?.toString() ?: ""

        if (title.isNotBlank() && !sbn.isOngoing) {
            val announcement = "Halil, $title uygulamasından mesajın var: $text"
            tts?.speak(announcement, TextToSpeech.QUEUE_ADD, null, sbn.key)
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
