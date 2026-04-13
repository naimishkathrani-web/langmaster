package com.langmaster.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.langmaster.data.local.entity.ConversationMemberEntity
import com.langmaster.data.local.entity.TranslationEventEntity
import com.langmaster.data.local.entity.TranslationSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TranslationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMember(member: ConversationMemberEntity)

    @Query(
        "SELECT translation_enabled FROM conversation_members " +
            "WHERE conversation_id = :conversationId AND user_phone_e164 = :userPhone LIMIT 1"
    )
    suspend fun isTranslationEnabled(conversationId: String, userPhone: String): Boolean?

    @Query(
        "SELECT preferred_language FROM conversation_members " +
            "WHERE conversation_id = :conversationId AND user_phone_e164 = :userPhone LIMIT 1"
    )
    suspend fun preferredLanguage(conversationId: String, userPhone: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: TranslationEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSession(session: TranslationSessionEntity)

    @Query("SELECT * FROM translation_sessions WHERE user_id = :userId ORDER BY created_at DESC")
    fun observeSessions(userId: String): Flow<List<TranslationSessionEntity>>
}
