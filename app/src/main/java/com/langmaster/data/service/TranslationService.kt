package com.langmaster.data.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.common.model.DownloadConditions
import kotlinx.coroutines.tasks.await

interface TranslationService {
    suspend fun translateText(sourceLang: String, targetLang: String, text: String): String
}

class MlKitTranslateService : TranslationService {
    override suspend fun translateText(sourceLang: String, targetLang: String, text: String): String {
        return withContext(Dispatchers.IO) {
            val sourceIso = mapLangToIso(sourceLang)
            val targetIso = mapLangToIso(targetLang)

            val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceIso)
                .setTargetLanguage(targetIso)
                .build()

            val translator = Translation.getClient(options)
            val conditions = DownloadConditions.Builder().requireWifi().build()

            try {
                // Ensure the model is downloaded before translating
                val dlTask = kotlinx.coroutines.withTimeoutOrNull(10000) {
                    translator.downloadModelIfNeeded(conditions).await()
                }
                
                if (dlTask == null) {
                    android.util.Log.e("MlKit", "Translation model download timed out")
                }

                // Perform local translation purely offline
                val result = kotlinx.coroutines.withTimeoutOrNull(5000) {
                    translator.translate(text).await()
                }
                
                result ?: "[$targetLang] $text (Timeout)"
            } catch (e: Exception) {
                // Fallback to the original text if models failed to download or translate
                android.util.Log.e("MlKit", "Translation error", e)
                "[$targetLang] $text"
            } finally {
                translator.close()
            }
        }
    }

    private fun mapLangToIso(lang: String): String {
        return when (lang.lowercase()) {
            "english" -> TranslateLanguage.ENGLISH
            "hindi" -> TranslateLanguage.HINDI
            "gujarati" -> TranslateLanguage.GUJARATI
            "marathi" -> TranslateLanguage.MARATHI
            "tamil" -> TranslateLanguage.TAMIL
            else -> TranslateLanguage.ENGLISH
        }
    }
}
