package com.langmaster.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "phone_e164") val phoneE164: String,
    @ColumnInfo(name = "display_name") val displayName: String,
    @ColumnInfo(name = "google_account_email") val googleAccountEmail: String? = null,
    @ColumnInfo(name = "avatar_uri") val avatarUri: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long
)

@Entity(
    tableName = "contacts",
    indices = [Index(value = ["phone_e164"], unique = true)]
)
data class ContactEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "phone_e164") val phoneE164: String,
    @ColumnInfo(name = "display_name") val displayName: String,
    @ColumnInfo(name = "avatar_uri") val avatarUri: String? = null,
    @ColumnInfo(name = "is_app_user") val isAppUser: Boolean,
    @ColumnInfo(name = "last_synced_at") val lastSyncedAt: Long
)

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val type: String,
    val title: String? = null,
    @ColumnInfo(name = "avatar_uri") val avatarUri: String? = null,
    @ColumnInfo(name = "created_by") val createdBy: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "last_message_id") val lastMessageId: String? = null
)

@Entity(
    tableName = "conversation_members",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversation_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["conversation_id", "user_phone_e164"], unique = true)]
)
data class ConversationMemberEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "conversation_id") val conversationId: String,
    @ColumnInfo(name = "user_phone_e164") val userPhoneE164: String,
    val role: String,
    @ColumnInfo(name = "translation_enabled") val translationEnabled: Boolean,
    @ColumnInfo(name = "preferred_language") val preferredLanguage: String,
    @ColumnInfo(name = "joined_at") val joinedAt: Long,
    @ColumnInfo(name = "left_at") val leftAt: Long? = null
)

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversation_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("conversation_id"), Index("created_at")]
)
data class MessageEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "conversation_id") val conversationId: String,
    @ColumnInfo(name = "sender_phone_e164") val senderPhoneE164: String,
    @ColumnInfo(name = "message_type") val messageType: String,
    val body: String? = null,
    @ColumnInfo(name = "reply_to_message_id") val replyToMessageId: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "edited_at") val editedAt: Long? = null,
    @ColumnInfo(name = "deleted_for_everyone_at") val deletedForEveryoneAt: Long? = null,
    @ColumnInfo(name = "deleted_for_me") val deletedForMe: Boolean = false
)

@Entity(
    tableName = "message_receipts",
    foreignKeys = [
        ForeignKey(entity = MessageEntity::class, parentColumns = ["id"], childColumns = ["message_id"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["message_id", "recipient_phone_e164"], unique = true)]
)
data class MessageReceiptEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "message_id") val messageId: String,
    @ColumnInfo(name = "recipient_phone_e164") val recipientPhoneE164: String,
    val status: String,
    @ColumnInfo(name = "updated_at") val updatedAt: Long
)

@Entity(
    tableName = "media_assets",
    foreignKeys = [
        ForeignKey(entity = MessageEntity::class, parentColumns = ["id"], childColumns = ["message_id"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("message_id"), Index("size_bytes")]
)
data class MediaAssetEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "message_id") val messageId: String,
    @ColumnInfo(name = "media_kind") val mediaKind: String,
    @ColumnInfo(name = "encrypted_file_path") val encryptedFilePath: String,
    @ColumnInfo(name = "mime_type") val mimeType: String,
    @ColumnInfo(name = "duration_ms") val durationMs: Long? = null,
    @ColumnInfo(name = "size_bytes") val sizeBytes: Long,
    @ColumnInfo(name = "checksum_sha256") val checksumSha256: String,
    @ColumnInfo(name = "created_at") val createdAt: Long
)

@Entity(tableName = "call_sessions", indices = [Index("conversation_id")])
data class CallSessionEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "conversation_id") val conversationId: String,
    @ColumnInfo(name = "call_type") val callType: String,
    @ColumnInfo(name = "started_at") val startedAt: Long,
    @ColumnInfo(name = "ended_at") val endedAt: Long? = null,
    val status: String,
    @ColumnInfo(name = "initiator_phone_e164") val initiatorPhoneE164: String
)

@Entity(tableName = "translation_events", indices = [Index("source_message_id"), Index("target_phone_e164")])
data class TranslationEventEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "source_message_id") val sourceMessageId: String,
    @ColumnInfo(name = "target_phone_e164") val targetPhoneE164: String,
    @ColumnInfo(name = "source_lang") val sourceLang: String,
    @ColumnInfo(name = "target_lang") val targetLang: String,
    @ColumnInfo(name = "was_ai_used") val wasAiUsed: Boolean,
    @ColumnInfo(name = "model_tier_used") val modelTierUsed: String,
    @ColumnInfo(name = "latency_ms") val latencyMs: Long,
    @ColumnInfo(name = "created_at") val createdAt: Long
)

@Entity(tableName = "translation_sessions", indices = [Index("user_id"), Index("created_at")])
data class TranslationSessionEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "input_mode") val inputMode: String,
    @ColumnInfo(name = "source_lang") val sourceLang: String,
    @ColumnInfo(name = "target_lang") val targetLang: String,
    @ColumnInfo(name = "input_text") val inputText: String? = null,
    @ColumnInfo(name = "output_text") val outputText: String? = null,
    @ColumnInfo(name = "input_audio_path") val inputAudioPath: String? = null,
    @ColumnInfo(name = "output_audio_path") val outputAudioPath: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long
)

@Entity(tableName = "learning_tracks", indices = [Index("language_code")])
data class LearningTrackEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "language_code") val languageCode: String,
    val level: String,
    val title: String,
    val description: String,
    @ColumnInfo(name = "certification_hint") val certificationHint: String? = null
)

@Entity(
    tableName = "learning_modules",
    foreignKeys = [
        ForeignKey(entity = LearningTrackEntity::class, parentColumns = ["id"], childColumns = ["track_id"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("track_id"), Index(value = ["track_id", "phase_order"], unique = true)]
)
data class LearningModuleEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "track_id") val trackId: String,
    @ColumnInfo(name = "phase_order") val phaseOrder: Int,
    val title: String,
    val goal: String,
    @ColumnInfo(name = "content_markdown") val contentMarkdown: String
)

@Entity(
    tableName = "learner_progress",
    foreignKeys = [
        ForeignKey(entity = LearningModuleEntity::class, parentColumns = ["id"], childColumns = ["module_id"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["user_id", "module_id"], unique = true)]
)
data class LearnerProgressEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "module_id") val moduleId: String,
    @ColumnInfo(name = "status") val status: String,
    @ColumnInfo(name = "score_percent") val scorePercent: Int = 0,
    @ColumnInfo(name = "updated_at") val updatedAt: Long
)

@Entity(tableName = "device_capability")
data class DeviceCapabilityEntity(
    @PrimaryKey val id: String = "local_device",
    @ColumnInfo(name = "device_ram_mb") val deviceRamMb: Long,
    @ColumnInfo(name = "tier_allowed") val tierAllowed: String,
    @ColumnInfo(name = "last_probe_at") val lastProbeAt: Long,
    val reason: String? = null
)

@Entity(tableName = "retention_policies", indices = [Index("conversation_id", unique = true)])
data class RetentionPolicyEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "conversation_id") val conversationId: String,
    @ColumnInfo(name = "active_limit") val activeLimit: Int = 5,
    @ColumnInfo(name = "archive_limit") val archiveLimit: Int = 20,
    @ColumnInfo(name = "large_file_threshold_bytes") val largeFileThresholdBytes: Long = 10 * 1024 * 1024,
    @ColumnInfo(name = "updated_at") val updatedAt: Long
)

@Entity(tableName = "backup_state")
data class BackupStateEntity(
    @PrimaryKey val id: String = "backup_state",
    @ColumnInfo(name = "last_backup_at") val lastBackupAt: Long? = null,
    @ColumnInfo(name = "last_restore_at") val lastRestoreAt: Long? = null,
    @ColumnInfo(name = "backup_provider") val backupProvider: String = "GOOGLE",
    @ColumnInfo(name = "backup_encrypted") val backupEncrypted: Boolean = true,
    @ColumnInfo(name = "backup_version") val backupVersion: Int = 1
)
