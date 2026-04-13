package com.langmaster.data.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

interface AuthService {
    suspend fun registerPin(phone: String, pin: String): PinAuthResponse
    suspend fun loginWithPin(phone: String, pin: String): PinAuthResponse
}

class LocalDevAuthService : AuthService {
    private val pinMap = mutableMapOf<String, String>()

    override suspend fun registerPin(phone: String, pin: String): PinAuthResponse {
        pinMap[phone] = pin
        return PinAuthResponse(ok = true)
    }

    override suspend fun loginWithPin(phone: String, pin: String): PinAuthResponse {
        val isValid = pinMap[phone] == pin
        return PinAuthResponse(
            ok = isValid,
            token = if (isValid) "local-token-$phone" else null,
            error = if (isValid) null else "Invalid pin"
        )
    }
}

class BackendAuthService(
    private val baseUrl: String
) : AuthService {
    override suspend fun registerPin(phone: String, pin: String): PinAuthResponse {
        return withContext(Dispatchers.IO) {
            val response = postJson(
                "$baseUrl/auth/register-pin",
                JSONObject().put("phone", phone).put("pin", pin)
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
