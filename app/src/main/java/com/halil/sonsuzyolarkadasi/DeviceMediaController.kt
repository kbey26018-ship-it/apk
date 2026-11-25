package com.halil.sonsuzyolarkadasi

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.provider.Settings
import android.view.KeyEvent

class DeviceMediaController(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun handleCommand(command: String): String? {
        val cmd = command.lowercase()
        return when {
            cmd.contains("sesi artır") || cmd.contains("sesi aç") -> {
                audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
                "Tamamdır Halil, sesi biraz daha açtım."
            }
            cmd.contains("sesi kıs") || cmd.contains("sesi azalt") -> {
                audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
                "Sesi kıstım."
            }
            cmd.contains("müziği durdur") || cmd.contains("duraklat") -> {
                dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PAUSE)
                "Müziği duraklattım."
            }
            cmd.contains("müziği başlat") || cmd.contains("devam et") -> {
                dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY)
                "Devam ediyoruz, müziği başlattım."
            }
            cmd.contains("sonraki parça") || cmd.contains("şarkıyı geç") -> {
                dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT)
                "Sıradaki parçaya geçtim."
            }
            cmd.contains("wi-fi") || cmd.contains("wifi") -> {
                val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                "Wi-Fi ayarlarını açıyorum."
            }
            else -> null
        }
    }

    private fun dispatchMediaKey(keyCode: Int) {
        audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
        audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
    }
}
