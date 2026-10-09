package com.example.data.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object GeminiApiClient {

    private const val PRIMARY_MODEL = "gemini-3.8-flash"
    private const val FALLBACK_MODEL = "gemini-3.5-flash"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generateContent(
        prompt: String,
        systemInstruction: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("Gemini API Key is not configured. Please add your GEMINI_API_KEY in the Secrets panel in AI Studio.")
            )
        }

        // Try PRIMARY_MODEL first, fallback to FALLBACK_MODEL if needed
        val firstAttempt = executeRequest(apiKey, PRIMARY_MODEL, prompt, systemInstruction)
        if (firstAttempt.isSuccess) {
            return@withContext firstAttempt
        }

        // If primary model failed with 404 or model not found, try fallback
        val errorMsg = firstAttempt.exceptionOrNull()?.message.orEmpty()
        if (errorMsg.contains("404") || errorMsg.contains("not found") || errorMsg.contains("NotFound")) {
            return@withContext executeRequest(apiKey, FALLBACK_MODEL, prompt, systemInstruction)
        }

        return@withContext firstAttempt
    }

    private fun executeRequest(
        apiKey: String,
        model: String,
        prompt: String,
        systemInstruction: String?
    ): Result<String> {
        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

            val jsonBody = JSONObject().apply {
                val contentsArray = JSONArray()
                val contentObj = JSONObject().apply {
                    val partsArray = JSONArray()
                    partsArray.put(JSONObject().apply { put("text", prompt) })
                    put("parts", partsArray)
                }
                contentsArray.put(contentObj)
                put("contents", contentsArray)

                if (!systemInstruction.isNullOrBlank()) {
                    val sysInstructionObj = JSONObject().apply {
                        val parts = JSONArray()
                        parts.put(JSONObject().apply { put("text", systemInstruction) })
                        put("parts", parts)
                    }
                    put("systemInstruction", sysInstructionObj)
                }

                val genConfig = JSONObject().apply {
                    put("temperature", 0.3)
                    put("topP", 0.95)
                }
                put("generationConfig", genConfig)
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    val parsedError = try {
                        val errObj = JSONObject(bodyStr).optJSONObject("error")
                        errObj?.optString("message") ?: "HTTP ${response.code}: ${response.message}"
                    } catch (e: Exception) {
                        "HTTP ${response.code}: $bodyStr"
                    }
                    return Result.failure(IOException(parsedError))
                }

                val rootJson = JSONObject(bodyStr)
                val candidates = rootJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val text = parts.getJSONObject(0).optString("text")
                        return Result.success(text)
                    }
                }
                return Result.failure(IOException("Empty response from AI model."))
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
}
