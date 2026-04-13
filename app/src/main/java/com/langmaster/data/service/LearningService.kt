package com.langmaster.data.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

data class LearningModuleDto(
    val id: String,
    val phase: Int,
    val title: String
)

interface LearningService {
    suspend fun fetchModules(language: String): List<LearningModuleDto>
}

class LocalLearningService : LearningService {
    override suspend fun fetchModules(language: String): List<LearningModuleDto> {
        return listOf(
            LearningModuleDto(id = "$language-m1", phase = 1, title = "$language Basics"),
            LearningModuleDto(id = "$language-m2", phase = 2, title = "$language Conversations"),
            LearningModuleDto(id = "$language-m3", phase = 3, title = "$language Advanced & Certification")
        )
    }
}

class BackendLearningService(private val baseUrl: String) : LearningService {
    override suspend fun fetchModules(language: String): List<LearningModuleDto> {
        return withContext(Dispatchers.IO) {
            val connection = (URL("$baseUrl/learn/modules/$language").openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout = 10_000
            }
            val body = BufferedReader(connection.inputStream.reader()).use { it.readText() }
            val json = JSONObject(body)
            val modules = json.optJSONArray("modules") ?: return@withContext emptyList()
            buildList {
                for (i in 0 until modules.length()) {
                    val node = modules.getJSONObject(i)
                    add(
                        LearningModuleDto(
                            id = node.optString("id"),
                            phase = node.optInt("phase"),
                            title = node.optString("title")
                        )
                    )
                }
            }
        }
    }
}
