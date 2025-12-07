package com.zandeveloper.tiktokly.network

import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.Gson
import android.widget.Toast
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import android.content.Context
import kotlinx.coroutines.withContext
import android.util.Log

class DataFetch(private var context: Context) {

 suspend fun fetchDataVideo(apiUrl: String, postUrl: String): Map<String, Any?>? {
 
    return withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val json = """{"url":"$postUrl"}"""
            val body = json.toRequestBody("application/json".toMediaTypeOrNull())

            val request = Request.Builder()
    .url(apiUrl)
    .post(body)
    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36")
    .build()
    
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val responseBody = response.body?.string() ?: return@withContext null
                
                // Parse manual pakai JsonElement
                Log.d("DataFetch", "RAW DATA: $responseBody")
                
                val jsonElement = Gson().fromJson(responseBody, com.google.gson.JsonElement::class.java)
                val jsonObj = jsonElement.asJsonObject

                val platform = jsonObj["platform"]?.asString ?: "Unknown"
                val result = jsonObj["result"]

val resultMap: Map<String, Any?> = if (result != null) {
                    if (result.isJsonArray) {
                        // Instagram-style
                        val first = result.asJsonArray.get(0).asJsonObject
                        first.entrySet().associate { it.key to it.value.toString().trim('"') }
                    } else if (result.isJsonObject) {
                        // YouTube-style
                        
                        result.asJsonObject.entrySet().associate { it.key to it.value.asString }
                    } else {
                        emptyMap()
                    }
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


}
