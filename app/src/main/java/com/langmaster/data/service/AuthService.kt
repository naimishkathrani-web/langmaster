package com.langmaster.data.service

import com.langmaster.data.local.AppDatabase
import com.langmaster.data.local.entity.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

interface AuthService {
    suspend fun register(
        phone: String,
        email: String,
        pin: String,
        nativeLanguage: String,
        otherLanguages: List<String>
    ): PinAuthResponse
    suspend fun loginWithPin(phone: String, pin: String): PinAuthResponse
}

class LocalDevAuthService(private val db: AppDatabase) : AuthService {

    override suspend fun register(
        phone: String,
        email: String,
        pin: String,
        nativeLanguage: String,
        otherLanguages: List<String>
    ): PinAuthResponse {
        val user = UserEntity(
            id = UUID.randomUUID().toString(),
            phoneE164 = phone,
            displayName = phone,
            pin = pin,
            googleAccountEmail = email,
            nativeLanguage = nativeLanguage,
            otherLanguages = otherLanguages.joinToString(","),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        db.userDao().upsert(user)
        return PinAuthResponse(ok = true)
    }

    override suspend fun loginWithPin(phone: String, pin: String): PinAuthResponse {
        seedTestAccounts()
        val user = db.userDao().getUserByPhone(phone)
        val isValid = user != null && user.pin == pin
        return PinAuthResponse(
            ok = isValid,
            token = if (isValid) "local-token-$phone" else null,
            error = if (isValid) null else if (user == null) "User not found" else "Invalid pin"
        )
    }

    private suspend fun seedTestAccounts() {
        val testAccounts = listOf(
            "7016899689" to "Naimish Kathrani",
            "9999999999" to "Ravi Kumar",
            "8888888888" to "Priya Sharma",
            "7777777777" to "Amit Patel",
            "6666666666" to "Sneha Gupta"
        )
        for ((phone, name) in testAccounts) {
            if (db.userDao().getUserByPhone(phone) == null) {
                db.userDao().upsert(UserEntity(
                    id = UUID.randomUUID().toString(),
                    phoneE164 = phone,
                    displayName = name,
                    pin = "2541",
                    googleAccountEmail = "",
                    nativeLanguage = "English",
                    otherLanguages = "Hindi",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                ))
            }
        }
    }
}

class BackendAuthService(
    private val baseUrl: String
) : AuthService {
    override suspend fun register(
        phone: String,
        email: String,
        pin: String,
        nativeLanguage: String,
        otherLanguages: List<String>
    ): PinAuthResponse {
        return withContext(Dispatchers.IO) {
            val response = postJson(
                "$baseUrl/auth/register",
                JSONObject()
                    .put("phone", phone)
                    .put("email", email)
                    .put("pin", pin)
                    .put("nativeLanguage", nativeLanguage)
                    .put("otherLanguages", otherLanguages.joinToString(","))
            )
            PinAuthResponse(
                ok = response.optBoolean("ok", false),
                error = response.optString("error").ifBlank { null }
            )
        }
    }

    override suspend fun loginWithPin(phone: String, pin: String): PinAuthResponse {
        return withContext(Dispatchers.IO) {
            val response = postJson(
                "$baseUrl/auth/login-pin",
                JSONObject()
                    .put("phone", phone)
                    .put("pin", pin)
            )
            PinAuthResponse(
                ok = response.optBoolean("ok", false),
                token = response.optString("token").ifBlank { null },
                error = response.optString("error").ifBlank { null }
            )
        }
    }

    private fun postJson(url: String, payload: JSONObject): JSONObject {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            connectTimeout = 10_000
            readTimeout = 10_000
            doOutput = true
        }

        OutputStreamWriter(connection.outputStream).use { writer ->
            writer.write(payload.toString())
            writer.flush()
        }

        val code = connection.responseCode
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
        val body = BufferedReader(stream.reader()).use { it.readText() }
        return runCatching { JSONObject(body) }.getOrElse { JSONObject().put("ok", false) }
    }
}
