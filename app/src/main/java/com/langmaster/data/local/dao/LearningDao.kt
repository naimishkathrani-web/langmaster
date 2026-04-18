package com.langmaster.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.langmaster.data.local.entity.LearnerProgressEntity
import com.langmaster.data.local.entity.LearningModuleEntity
import com.langmaster.data.local.entity.LearningTrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LearningDao {
    @Query("SELECT * FROM learning_tracks WHERE language_code = :languageCode ORDER BY level ASC")
    fun observeTracks(languageCode: String): Flow<List<LearningTrackEntity>>

    @Query("SELECT * FROM learning_modules WHERE track_id = :trackId ORDER BY phase_order ASC")
    fun observeModules(trackId: String): Flow<List<LearningModuleEntity>>

    @Query("SELECT COUNT(*) FROM learning_tracks")
    suspend fun getTrackCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTrack(track: LearningTrackEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertModule(module: LearningModuleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: LearnerProgressEntity)
}
