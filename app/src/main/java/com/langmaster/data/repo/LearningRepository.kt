package com.langmaster.data.repo

import com.langmaster.data.local.AppDatabase
import com.langmaster.data.local.entity.LearnerProgressEntity
import com.langmaster.data.local.entity.LearningModuleEntity
import com.langmaster.data.local.entity.LearningTrackEntity
import com.langmaster.data.service.LearningModuleDto
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class LearningRepository(private val db: AppDatabase) {
    fun observeTracks(languageCode: String): Flow<List<LearningTrackEntity>> {
        return db.learningDao().observeTracks(languageCode)
    }

    fun observeModules(trackId: String): Flow<List<LearningModuleEntity>> {
        return db.learningDao().observeModules(trackId)
    }

    suspend fun seedTracks(languageCode: String) {
        val trackId = "track-$languageCode-beginner"
        db.learningDao().upsertTrack(
            LearningTrackEntity(
                id = trackId,
                languageCode = languageCode,
                level = "BEGINNER",
                title = "$languageCode Foundations",
                description = "Start speaking and understanding daily $languageCode quickly.",
                certificationHint = "Complete all modules and mock tests."
            )
        )

        val modules = listOf(
            LearningModuleEntity(
                id = "$trackId-m1",
                trackId = trackId,
                phaseOrder = 1,
                title = "Phase 1: Sounds & Greetings",
                goal = "Understand pronunciation and greeting patterns.",
                contentMarkdown = "Practice greetings and self-introduction."
            ),
            LearningModuleEntity(
                id = "$trackId-m2",
                trackId = trackId,
                phaseOrder = 2,
                title = "Phase 2: Everyday Conversations",
                goal = "Build sentence-level confidence.",
                contentMarkdown = "Role-play shopping, travel, and social chats."
            ),
            LearningModuleEntity(
                id = "$trackId-m3",
                trackId = trackId,
                phaseOrder = 3,
                title = "Phase 3: Certification Prep",
                goal = "Prepare for formal language assessments.",
                contentMarkdown = "Timed speaking and writing drills."
            )
        )

        modules.forEach { db.learningDao().upsertModule(it) }
    }

    suspend fun seedTracksFromService(languageCode: String, modules: List<LearningModuleDto>) {
        val trackId = "track-$languageCode-beginner"
        db.learningDao().upsertTrack(
            LearningTrackEntity(
                id = trackId,
                languageCode = languageCode,
                level = "BEGINNER",
                title = "$languageCode Foundations",
                description = "Personalized modules fetched from service.",
                certificationHint = "Follow all modules and complete practice tests."
            )
        )
        modules.forEach { module ->
            db.learningDao().upsertModule(
                LearningModuleEntity(
                    id = module.id,
                    trackId = trackId,
                    phaseOrder = module.phase,
                    title = module.title,
                    goal = "Complete phase ${module.phase} objectives.",
                    contentMarkdown = "Auto-synced module from learning service."
                )
            )
        }
    }

    suspend fun updateProgress(userId: String, moduleId: String, scorePercent: Int) {
        db.learningDao().upsertProgress(
            LearnerProgressEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                moduleId = moduleId,
                status = if (scorePercent >= 75) "COMPLETED" else "IN_PROGRESS",
                scorePercent = scorePercent,
                updatedAt = System.currentTimeMillis()
            )
        )
    }
}
