package com.langmaster.data.repo

import com.langmaster.data.local.AppDatabase
import com.langmaster.data.local.entity.ConversationEntity
import com.langmaster.data.local.entity.ConversationMemberEntity
import com.langmaster.data.local.entity.MessageEntity
import com.langmaster.data.local.entity.TranslationEventEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ChatRepository(private val db: AppDatabase) {
    fun observeConversations() = db.conversationDao().observeConversations()

    fun observeMessages(conversationId: String): Flow<List<MessageEntity>> {
        return db.messageDao().observeMessages(conversationId)
    }

    suspend fun getMessagesSync(conversationId: String): List<MessageEntity> {
        return db.messageDao().getMessagesSync(conversationId)
    }

    suspend fun seedSimulatedData(localUserPhone: String) {
        if (db.conversationDao().getConversationCount() > 0) return

        val now = System.currentTimeMillis()
        val c1 = "conv-sim-1"
        val c2 = "conv-sim-2"

        // 1. Conversation with Ravi
        db.conversationDao().upsert(ConversationEntity(id = c1, type = "DIRECT", title = "Ravi Kumar", createdBy = localUserPhone, createdAt = now - 500000, updatedAt = now, lastMessageId = "msg2"))
        db.translationDao().upsertMember(ConversationMemberEntity(id = "$c1-$localUserPhone", conversationId = c1, userPhoneE164 = localUserPhone, role = "MEMBER", translationEnabled = false, preferredLanguage = "English", joinedAt = now))
        db.translationDao().upsertMember(ConversationMemberEntity(id = "$c1-9999999999", conversationId = c1, userPhoneE164 = "9999999999", role = "MEMBER", translationEnabled = false, preferredLanguage = "Hindi", joinedAt = now))
        
        db.messageDao().upsert(MessageEntity(id = "msg1", conversationId = c1, senderPhoneE164 = localUserPhone, messageType = "TEXT", body = "Hey Ravi, are you coming to the meeting?", createdAt = now - 400000))
        db.messageDao().upsert(MessageEntity(id = "msg2", conversationId = c1, senderPhoneE164 = "9999999999", messageType = "TEXT", body = "Haan, main raste mein hoon.", createdAt = now - 300000)) // "Yes, I am on the way"

        // 2. Conversation with Priya
        db.conversationDao().upsert(ConversationEntity(id = c2, type = "DIRECT", title = "Priya Sharma", createdBy = "8888888888", createdAt = now - 200000, updatedAt = now, lastMessageId = "msg4"))
        db.translationDao().upsertMember(ConversationMemberEntity(id = "$c2-$localUserPhone", conversationId = c2, userPhoneE164 = localUserPhone, role = "MEMBER", translationEnabled = true, preferredLanguage = "English", joinedAt = now))
        db.translationDao().upsertMember(ConversationMemberEntity(id = "$c2-8888888888", conversationId = c2, userPhoneE164 = "8888888888", role = "MEMBER", translationEnabled = false, preferredLanguage = "Gujarati", joinedAt = now))
        
        db.messageDao().upsert(MessageEntity(id = "msg3", conversationId = c2, senderPhoneE164 = "8888888888", messageType = "TEXT", body = "Tame kyam chho?", createdAt = now - 150000)) // "How are you?"
        db.messageDao().upsert(MessageEntity(id = "msg4", conversationId = c2, senderPhoneE164 = localUserPhone, messageType = "TEXT", body = "I'm doing well, thanks! Getting ready for work.", createdAt = now - 100000))

        // 3. Seed AI Agent history
        db.translationDao().upsertSession(
            com.langmaster.data.local.entity.TranslationSessionEntity(
                id = UUID.randomUUID().toString(),
                userId = localUserPhone,
                inputMode = "TEXT",
                sourceLang = "English",
                targetLang = "Marathi",
                inputText = "Where is the nearest train station?",
                outputText = "सर्वात जवळचे रेल्वे स्टेशन कुठे आहे?",
                createdAt = now - 600000
            )
        )
        db.translationDao().upsertSession(
            com.langmaster.data.local.entity.TranslationSessionEntity(
                id = UUID.randomUUID().toString(),
                userId = localUserPhone,
                inputMode = "VOICE",
                sourceLang = "English",
                targetLang = "Hindi",
                inputText = "I need help finding an apartment.",
                outputText = "मुझे अपार्टमेंट ढूंढने में मदद चाहिए।",
                createdAt = now - 800000
            )
        )
    }

    suspend fun upsertMemberPreference(
        conversationId: String,
        phone: String,
        translationEnabled: Boolean,
        preferredLanguage: String
    ) {
        db.translationDao().upsertMember(
            ConversationMemberEntity(
                id = "$conversationId-$phone",
                conversationId = conversationId,
                userPhoneE164 = phone,
                role = "MEMBER",
                translationEnabled = translationEnabled,
                preferredLanguage = preferredLanguage,
                joinedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun sendTextMessage(
        conversationId: String,
        senderPhone: String,
        text: String,
        language: String,
        replyToMessageId: String? = null
    ) {
        val now = System.currentTimeMillis()
        db.messageDao().upsert(
            MessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = conversationId,
                senderPhoneE164 = senderPhone,
                messageType = "TEXT",
                body = "[$language] $text",
                replyToMessageId = replyToMessageId,
                createdAt = now
            )
        )
    }

    suspend fun deleteForEveryoneWith24hWindow(messageId: String, messageCreatedAt: Long) {
        val twentyFourHoursMs = 24L * 60 * 60 * 1000
        val createdAfter = System.currentTimeMillis() - twentyFourHoursMs
        if (messageCreatedAt >= createdAfter) {
            db.messageDao().deleteForEveryoneIfInWindow(
                messageId = messageId,
                deletedAt = System.currentTimeMillis(),
                createdAfter = createdAfter
            )
        }
    }

    suspend fun deleteForMe(messageId: String) {
        db.messageDao().deleteForMe(messageId)
    }

    suspend fun forwardMessage(
        sourceMessage: MessageEntity,
        targetConversationId: String,
        senderPhone: String
    ) {
        db.messageDao().upsert(
            sourceMessage.copy(
                id = UUID.randomUUID().toString(),
                conversationId = targetConversationId,
                senderPhoneE164 = senderPhone,
                replyToMessageId = null,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun maybeRecordTranslationEvent(
        conversationId: String,
        listenerPhone: String,
        sourceMessageId: String,
        sourceLanguage: String,
        latencyMs: Long,
        modelTier: String
    ): Boolean {
        val enabled = db.translationDao().isTranslationEnabled(conversationId, listenerPhone) ?: false
        val preferred = db.translationDao().preferredLanguage(conversationId, listenerPhone)
        val useAi = TranslationPolicyEvaluator.shouldUseAi(enabled, preferred, sourceLanguage)

        if (useAi) {
            db.translationDao().insertEvent(
                TranslationEventEntity(
                    id = UUID.randomUUID().toString(),
                    sourceMessageId = sourceMessageId,
                    targetPhoneE164 = listenerPhone,
                    sourceLang = sourceLanguage,
                    targetLang = preferred ?: sourceLanguage,
                    wasAiUsed = true,
                    modelTierUsed = modelTier,
                    latencyMs = latencyMs,
                    createdAt = System.currentTimeMillis()
                )
            )
        }

        return useAi
    }
}
