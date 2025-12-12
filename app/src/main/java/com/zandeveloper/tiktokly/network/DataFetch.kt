package com.zandeveloper.tiktokly.network

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonElement
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import com.zandeveloper.tiktokly.model.UpdateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DataFetch(private var context: Context) {

suspend fun checkUpdate(): UpdateInfo? = withContext(Dispatchers.IO) {
    try {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://api.github.com/repos/Fauzansyz/Tiktokly/releases/latest")
            .header("Accept", "application/vnd.github+json")
            .build()

        client.newCall(request).execute().use { res ->
            if (!res.isSuccessful) return@withContext null
            val body = res.body?.string() ?: return@withContext null

            return@withContext Gson().fromJson(body, UpdateInfo::class.java)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

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
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
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

                    val resultMap: Map<String, Any?> = when {
                        result.isJsonObject -> {
                            // TIKTOK/YT
                            result.asJsonObject.entrySet()
                                .associate { it.key to parseJson(it.value) }
                        }

                        result.isJsonArray -> {
                            // INSTAGRAM → ambil index 0
                            val firstObj = result.asJsonArray.get(0).asJsonObject
                            firstObj.entrySet()
                                .associate { it.key to parseJson(it.value) }
                        }

                        else -> emptyMap()
                    }

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


    private fun parseJson(elem: JsonElement): Any? {
        return when {
            elem.isJsonPrimitive -> elem.asString
            elem.isJsonArray -> elem.asJsonArray.map { parseJson(it) }
            elem.isJsonObject -> elem.asJsonObject.entrySet()
                .associate { it.key to parseJson(it.value) }
            else -> null
        }
    }
}