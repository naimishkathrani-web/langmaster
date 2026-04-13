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

class LocalTranslationService : TranslationService {
    override suspend fun translateText(sourceLang: String, targetLang: String, text: String): String {
        return "[$targetLang] $text"
    }
}

class BackendTranslationService(private val baseUrl: String) : TranslationService {
    override suspend fun translateText(sourceLang: String, targetLang: String, text: String): String {
        return withContext(Dispatchers.IO) {
            val payload = JSONObject()
                .put("sourceLang", sourceLang)
                .put("targetLang", targetLang)
                .put("text", text)
            val response = postJson("$baseUrl/translate/text", payload)
            response.optString("translatedText").ifBlank { "[$targetLang] $text" }
        }
    }

    private fun postJson(url: String, payload: JSONObject): JSONObject {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            connectTimeout = 10_000
            readTimeout = 10_000
            doOutput = true
        }

        OutputStreamWriter(connection.outputStream).use { writer ->
            writer.write(payload.toString())
            writer.flush()
        }

        val code = connection.responseCode
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
        val body = BufferedReader(stream.reader()).use { it.readText() }
        return runCatching { JSONObject(body) }.getOrElse { JSONObject() }
    }
}
