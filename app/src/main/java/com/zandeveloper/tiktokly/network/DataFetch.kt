package com.zandeveloper.tiktokly.network

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonElement
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DataFetch(private val context: Context) {

    suspend fun fetchDataVideo(apiUrl: String, postUrl: String): Map<String, Any?>? {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val json = """{"url":"$postUrl"}"""
                val body = json.toRequestBody("application/json".toMediaTypeOrNull())

                val request = Request.Builder()
                    .url(apiUrl)
                    .post(body)
                    .header(
                        "User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36"
                    )
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext null
                    val responseBody = response.body?.string() ?: return@withContext null

                    Log.d("DataFetch", "RAW DATA: $responseBody")

                    val jsonElement = Gson().fromJson(responseBody, JsonElement::class.java)
                    val jsonObj = jsonElement.asJsonObject

                    val platform = jsonObj["platform"]?.asString ?: "Unknown"
                    val result = jsonObj["result"]

                    val resultMap: Map<String, Any?> = if (result != null && result.isJsonObject) {
                        result.asJsonObject.entrySet().associate { it.key to parseJsonElement(it.value) }
                    } else emptyMap()

                    mapOf(
                        "platform" to platform,
                        "result" to resultMap
                    )
                }

            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun parseJsonElement(elem: JsonElement): Any? {
        return when {
            elem.isJsonPrimitive -> elem.asString
            elem.isJsonArray -> elem.asJsonArray.map { parseJsonElement(it) }
            elem.isJsonObject -> elem.asJsonObject.entrySet().associate { it.key to parseJsonElement(it.value) }
            else -> null
        }
    }
}