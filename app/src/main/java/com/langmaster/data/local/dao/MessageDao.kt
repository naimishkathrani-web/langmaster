package com.langmaster.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.langmaster.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query(
        "SELECT * FROM messages " +
            "WHERE conversation_id = :conversationId AND deleted_for_me = 0 " +
            "ORDER BY created_at ASC"
    )
    fun observeMessages(conversationId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(message: MessageEntity)

    @Query(
        "UPDATE messages SET deleted_for_everyone_at = :deletedAt " +
            "WHERE id = :messageId AND created_at >= :createdAfter"
    )
    suspend fun deleteForEveryoneIfInWindow(messageId: String, deletedAt: Long, createdAfter: Long)

    @Query("UPDATE messages SET deleted_for_me = 1 WHERE id = :messageId")
    suspend fun deleteForMe(messageId: String)
}
