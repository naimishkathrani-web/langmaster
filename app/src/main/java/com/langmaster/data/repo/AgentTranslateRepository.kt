package com.langmaster.data.repo

import com.langmaster.data.local.AppDatabase
import com.langmaster.data.local.entity.TranslationSessionEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class AgentTranslateRepository(private val db: AppDatabase) {
    fun observeSessions(userId: String): Flow<List<TranslationSessionEntity>> {
        return db.translationDao().observeSessions(userId)
    }

    suspend fun saveTextSession(
        userId: String,
        sourceLanguage: String,
        targetLanguage: String,
        inputText: String,
        outputText: String
    ) {
        db.translationDao().upsertSession(
            TranslationSessionEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                inputMode = "TEXT",
                sourceLang = sourceLanguage,
                targetLang = targetLanguage,
                inputText = inputText,
                outputText = outputText,
                createdAt = System.currentTimeMillis()
            )
        )
    }
}
