package com.langmaster.data.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

interface TranslationService {
    suspend fun translateText(sourceLang: String, targetLang: String, text: String): String
}

class GoogleTranslateService : TranslationService {
    override suspend fun translateText(sourceLang: String, targetLang: String, text: String): String {
        return withContext(Dispatchers.IO) {
            val sourceIso = mapLangToIso(sourceLang)
            val targetIso = mapLangToIso(targetLang)
            
            // translate.googleapis.com requires url encoded text
            val encodedText = java.net.URLEncoder.encode(text, "UTF-8")
            val urlStr = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=$sourceIso&tl=$targetIso&dt=t&q=$encodedText"

            runCatching {
                val connection = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("User-Agent", "Mozilla/5.0")
                    connectTimeout = 10_000
                    readTimeout = 10_000
                }
                val body = BufferedReader(connection.inputStream.reader()).use { it.readText() }
                // Response is a weird nested JSON array: [[[ "translated text", "original text", ... ]], ...]
                val jsonArray = org.json.JSONArray(body)
                val sections = jsonArray.getJSONArray(0)
                val sb = java.lang.StringBuilder()
                for (i in 0 until sections.length()) {
                    sb.append(sections.getJSONArray(i).getString(0))
                }
                sb.toString()
            }.getOrElse { 
                "[$targetLang] $text" // Fallback on complete failure
            }
        }
    }

    private fun mapLangToIso(lang: String): String {
        return when (lang.lowercase()) {
            "english" -> "en"
            "hindi" -> "hi"
            "gujarati" -> "gu"
            "marathi" -> "mr"
            "tamil" -> "ta"
            else -> "en"
        }
    }
}
