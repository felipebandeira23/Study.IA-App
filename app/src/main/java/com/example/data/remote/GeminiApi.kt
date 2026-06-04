package com.example.data.remote

import com.example.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

// ── Request models ──────────────────────────────────────────────────────────

data class GeminiRequest(
    val contents: List<Content>,
    val tools: List<Tool>? = null,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

data class Content(
    val role: String? = null,
    val parts: List<Part>
)

data class Part(val text: String)

data class GenerationConfig(
    val responseMimeType: String? = null,
    val temperature: Float? = null
)

/** Enables Google Search Grounding — send as tools = [Tool(google_search = GoogleSearch())] */
data class Tool(val google_search: GoogleSearch? = null)

/** Empty object that activates Search Grounding in the Gemini API */
class GoogleSearch

// ── Response models ──────────────────────────────────────────────────────────

data class GeminiResponse(val candidates: List<Candidate>? = null)
data class Candidate(val content: ResponseContent? = null)
data class ResponseContent(val parts: List<ResponsePart>? = null)
data class ResponsePart(val text: String? = null)

// ── Retrofit service ──────────────────────────────────────────────────────────

interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

// ── Client singleton ──────────────────────────────────────────────────────────

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    /** Overridden at runtime by SettingsScreen when user saves their API key. */
    var runtimeApiKey: String = ""

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(90, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    private fun effectiveApiKey(): String {
        if (runtimeApiKey.isNotBlank()) return runtimeApiKey
        return try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
    }

    fun isApiKeyAvailable(): Boolean {
        val key = effectiveApiKey()
        return key.isNotEmpty() && key != "MY_GEMINI_API_KEY" && !key.contains("PLACEHOLDER")
    }

    /**
     * Standard text generation (no web search).
     * Uses gemini-2.0-flash for stable, fast inference.
     */
    suspend fun fetchContent(
        prompt: String,
        systemInstruction: String? = null,
        jsonOutput: Boolean = false
    ): String {
        if (!isApiKeyAvailable()) throw IllegalStateException("API Key não configurada")
        val request = GeminiRequest(
            contents = listOf(Content(role = "user", parts = listOf(Part(prompt)))),
            generationConfig = if (jsonOutput) GenerationConfig(responseMimeType = "application/json") else null,
            systemInstruction = systemInstruction?.let { Content(parts = listOf(Part(it))) }
        )
        val response = service.generateContent("gemini-2.0-flash", effectiveApiKey(), request)
        return response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw Exception("Gemini retornou resposta vazia")
    }

    /**
     * Text generation with Google Search Grounding — the model searches the web in real time.
     * Uses gemini-2.0-flash which natively supports search grounding.
     */
    suspend fun fetchWithSearch(
        prompt: String,
        systemInstruction: String? = null
    ): String {
        if (!isApiKeyAvailable()) throw IllegalStateException("API Key não configurada")
        val request = GeminiRequest(
            contents = listOf(Content(role = "user", parts = listOf(Part(prompt)))),
            tools = listOf(Tool(google_search = GoogleSearch())),
            systemInstruction = systemInstruction?.let { Content(parts = listOf(Part(it))) }
        )
        val response = service.generateContent("gemini-2.0-flash", effectiveApiKey(), request)
        return response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw Exception("Gemini retornou resposta vazia")
    }
}
