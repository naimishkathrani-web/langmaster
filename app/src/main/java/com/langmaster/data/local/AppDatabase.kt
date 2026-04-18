package com.langmaster.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.langmaster.data.local.dao.ConversationDao
import com.langmaster.data.local.dao.LearningDao
import com.langmaster.data.local.dao.MessageDao
import com.langmaster.data.local.dao.TranslationDao
import com.langmaster.data.local.dao.UserDao
import com.langmaster.data.local.entity.BackupStateEntity
import com.langmaster.data.local.entity.CallSessionEntity
import com.langmaster.data.local.entity.ContactEntity
import com.langmaster.data.local.entity.ConversationEntity
import com.langmaster.data.local.entity.ConversationMemberEntity
import com.langmaster.data.local.entity.DeviceCapabilityEntity
import com.langmaster.data.local.entity.LearnerProgressEntity
import com.langmaster.data.local.entity.LearningModuleEntity
import com.langmaster.data.local.entity.LearningTrackEntity
import com.langmaster.data.local.entity.MediaAssetEntity
import com.langmaster.data.local.entity.MessageEntity
import com.langmaster.data.local.entity.MessageReceiptEntity
import com.langmaster.data.local.entity.RetentionPolicyEntity
import com.langmaster.data.local.entity.TranslationEventEntity
import com.langmaster.data.local.entity.TranslationSessionEntity
import com.langmaster.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        ContactEntity::class,
        ConversationEntity::class,
        ConversationMemberEntity::class,
        MessageEntity::class,
        MessageReceiptEntity::class,
        MediaAssetEntity::class,
        CallSessionEntity::class,
        TranslationEventEntity::class,
        TranslationSessionEntity::class,
        LearningTrackEntity::class,
        LearningModuleEntity::class,
        LearnerProgressEntity::class,
        DeviceCapabilityEntity::class,
        RetentionPolicyEntity::class,
        BackupStateEntity::class
    ],
    version = 9,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun translationDao(): TranslationDao
    abstract fun learningDao(): LearningDao
}
