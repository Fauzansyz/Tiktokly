package com.zandeveloper.tiktokly.data.network

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
                    val result = jsonObj.get("result")
         
         if (result == null || result.isJsonNull) {
            Log.e("DataFetch", "RESULT NULL / INVALID")
          return@withContext mapOf(
        "platform" to platform,
        "result" to emptyMap<String, Any?>()
        )
   } else {
           val resultMap: Map<String, Any?> = when {
                        result.isJsonObject -> {
                            // TIKTOK/YT
                            result.asJsonObject.entrySet()
                                .associate { it.key to parseJson(it.value) }
                        }

                        result.isJsonArray -> {
                            // INSTAGRAM → ambil index 0
                            val array = result.asJsonArray
           if (array.size() == 0) {
             Log.e("DataFetch", "RESULT ARRAY EMPTY")
        emptyMap()
        } else {
          val firstObj = array[0].asJsonObject
    firstObj.entrySet()
        .associate { it.key to parseJson(it.value) }
          }
                        }

                        else -> emptyMap()
                    }

                    mapOf(
                        "platform" to platform,
                        "result" to resultMap
                    )
              }
              
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