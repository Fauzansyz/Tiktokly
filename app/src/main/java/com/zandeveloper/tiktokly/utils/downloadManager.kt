package com.zandeveloper.tiktokly.utils

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class downloadManager(private val context: Context) {

    suspend fun download(
        url: String,
        folderUri: Uri,
        fileName: String,
        onProgress: (Int) -> Unit,
        onCompleted: (String) -> Unit,
        onError: () -> Unit
    ) = withContext(Dispatchers.IO) {

        try {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) throw IOException("Download failed")

            val body = response.body ?: throw IOException("Empty body")
            val total = body.contentLength()

            val folder = DocumentFile.fromTreeUri(context, folderUri)
                ?: throw IOException("Invalid folder")

            val file = folder.createFile(
                "video/mp4",
                fileName
            ) ?: throw IOException("Failed create file")

            val outputStream =
                context.contentResolver.openOutputStream(file.uri)
                    ?: throw IOException("OutputStream null")

            body.byteStream().use { input ->
                outputStream.use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead: Int
                    var downloaded = 0L

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloaded += bytesRead

                        if (total > 0) {
                            val progress = ((downloaded * 100) / total).toInt()
                            withContext(Dispatchers.Main) {
                                onProgress(progress)
                            }
                        }
                    }
                }
            }

            withContext(Dispatchers.Main) {
                onCompleted(file.uri.toString())
            }

        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                onError()
            }
        }
    }
}