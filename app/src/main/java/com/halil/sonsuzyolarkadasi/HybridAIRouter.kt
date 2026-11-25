package com.halil.sonsuzyolarkadasi

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class HybridAIRouter(
    private var pcServerUrl: String = "http://192.168.1.100:8080/ask",
    private var cloudApiUrl: String = ""
) {
    private val recentHistory = mutableListOf<Pair<String, String>>()

    companion object {
        const val PERSONA_PROMPT =
            "Sen Halil'in kişisel asistanı ve yol arkadaşı 'Sonsuz Yol Arkadaşı'sın. " +
            "Onunla robotik kalıplardan uzak, içten, sıcak, samimi ve doğal bir Türkçe ile konuş. " +
            "Gerektiğinde hafif esprili ve destekleyici ol; lafı gereksiz uzatmadan doğrudan, net ve pratik cevaplar ver. " +
            "Ona ismiyle (Halil) hitap edebilirsin, yanında duran gerçek bir yol arkadaşı gibi davran."
    }

    suspend fun sendQuery(prompt: String, preferLocalPc: Boolean, localModelName: String?): String {
        return withContext(Dispatchers.IO) {
            val reply = if (preferLocalPc) {
                try {
                    postJson(pcServerUrl, prompt, localModelName ?: "gemma3")
                } catch (e: Exception) {
                    "Halil, şu an bilgisayardaki sunucuya ulaşamadım (${e.message}). Sunucu açık mı bir bakalım mı?"
                }
            } else {
                if (cloudApiUrl.isBlank()) {
                    "Bulut bağlantısı henüz ayarlı değil Halil; istersen sağ üstten PC / Yerel modunu açıp devam edelim."
                } else {
                    postJson(cloudApiUrl, prompt, "cloud-default")
                }
            }

            recentHistory.add(prompt to reply)
            if (recentHistory.size > 6) {
                recentHistory.removeAt(0)
            }
            reply
        }
    }

    private fun postJson(endpoint: String, prompt: String, model: String): String {
        val url = URL(endpoint)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        conn.connectTimeout = 5000
        conn.readTimeout = 30000
        conn.doOutput = true

        val historyArray = JSONArray()
        for ((userMsg, botMsg) in recentHistory) {
            historyArray.put(JSONObject().apply {
                put("user", userMsg)
                put("assistant", botMsg)
            })
        }

        val payload = JSONObject().apply {
            put("prompt", prompt)
            put("model", model)
            put("user", "Halil")
            put("persona", PERSONA_PROMPT)
            put("history", historyArray)
        }

        OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
        val responseText = conn.inputStream.bufferedReader().use { it.readText() }
        return JSONObject(responseText).optString("reply", responseText)
    }
}
