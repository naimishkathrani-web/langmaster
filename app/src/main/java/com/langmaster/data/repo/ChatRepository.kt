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

    suspend fun seedDefaultConversation(
        conversationId: String,
        localUserPhone: String,
        contactPhone: String
    ) {
        val now = System.currentTimeMillis()
        db.conversationDao().upsert(
            ConversationEntity(
                id = conversationId,
                type = "DIRECT",
                title = "Ravi",
                createdBy = localUserPhone,
                createdAt = now,
                updatedAt = now
            )
        )
        db.translationDao().upsertMember(
            ConversationMemberEntity(
                id = "$conversationId-$localUserPhone",
                conversationId = conversationId,
                userPhoneE164 = localUserPhone,
                role = "MEMBER",
                translationEnabled = false,
                preferredLanguage = "English",
                joinedAt = now
            )
        )
        db.translationDao().upsertMember(
            ConversationMemberEntity(
                id = "$conversationId-$contactPhone",
                conversationId = conversationId,
                userPhoneE164 = contactPhone,
                role = "MEMBER",
                translationEnabled = false,
                preferredLanguage = "Hindi",
                joinedAt = now
            )
        )
    }

    suspend fun seedGroupConversation(
        conversationId: String,
        localUserPhone: String,
        memberPhones: List<String>
    ) {
        val now = System.currentTimeMillis()
        db.conversationDao().upsert(
            ConversationEntity(
                id = conversationId,
                type = "GROUP",
                title = "LangMaster Squad",
                createdBy = localUserPhone,
                createdAt = now,
                updatedAt = now
            )
        )
        val allMembers = listOf(localUserPhone) + memberPhones
        allMembers.forEach { phone ->
            db.translationDao().upsertMember(
                ConversationMemberEntity(
                    id = "$conversationId-$phone",
                    conversationId = conversationId,
                    userPhoneE164 = phone,
                    role = if (phone == localUserPhone) "ADMIN" else "MEMBER",
                    translationEnabled = false,
                    preferredLanguage = "English",
                    joinedAt = now
                )
            )
        }
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
