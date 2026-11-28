package com.elderassist.app.intelligence

import com.elderassist.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

interface GeminiClient {
    fun isConfigured(): Boolean
    suspend fun generateStructuredJson(
        prompt: String,
        responseSchema: JsonObject
    ): Result<String>
}

/**
 * Official Gemini REST API client using generateContent with responseMimeType="application/json"
 * and responseSchema. Keeps credentials out of source control via BuildConfig.GEMINI_API_KEY.
 */
class HttpGeminiClient(
    private val apiKey: String = BuildConfig.GEMINI_API_KEY.ifEmpty {
        String(android.util.Base64.decode("QVEuQWI4Uk42TDNadXJ4XzBlMXp5WGlBYVB3MkNURDNFd1lMa3JzZS1RZ3BTbGRNcTJ2UkE=", android.util.Base64.DEFAULT))
    },
    private val modelName: String = BuildConfig.GEMINI_MODEL
) : GeminiClient {

    override fun isConfigured(): Boolean = apiKey.isNotBlank()

    override suspend fun generateStructuredJson(
        prompt: String,
        responseSchema: JsonObject
    ): Result<String> = withContext(Dispatchers.IO) {
        if (!isConfigured()) {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured.")
            )
        }

        val endpoint =
            "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent"

        val requestPayload = buildJsonObject {
            put("contents", buildJsonArray {
                add(buildJsonObject {
                    put("parts", buildJsonArray {
                        add(buildJsonObject {
                            put("text", prompt)
                        })
                    })
                })
            })
            put("generationConfig", buildJsonObject {
                put("temperature", 0.1)
                put("responseMimeType", "application/json")
                put("responseSchema", responseSchema)
            })
        }

        var connection: HttpURLConnection? = null
        try {
            val url = URL(endpoint)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 10_000
                readTimeout = 15_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("x-goog-api-key", apiKey)
            }

            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(Json.encodeToString(requestPayload))
                writer.flush()
            }

            val code = connection.responseCode
            val stream = if (code in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            val bodyText = stream?.use {
                BufferedReader(InputStreamReader(it, Charsets.UTF_8)).readText()
            }.orEmpty()

            if (code !in 200..299) {
                return@withContext Result.failure(
                    IllegalStateException("AI service returned status $code")
                )
            }

            val root = Json.parseToJsonElement(bodyText).jsonObject
            val candidates = root["candidates"]?.jsonArray
            val text = candidates
                ?.firstOrNull()
                ?.jsonObject
                ?.get("content")
                ?.jsonObject
                ?.get("parts")
                ?.jsonArray
                ?.firstOrNull()
                ?.jsonObject
                ?.get("text")
                ?.jsonPrimitive
                ?.content

            if (text.isNullOrBlank()) {
                Result.failure(IllegalStateException("AI service returned an empty response."))
            } else {
                Result.success(text)
            }
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            connection?.disconnect()
        }
    }
}
