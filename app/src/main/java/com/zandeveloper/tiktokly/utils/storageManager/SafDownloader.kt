package com.zandeveloper.tiktokly.utils.storageManager

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class SafDownloader(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {

    fun download(
        url: String,
        folderUri: Uri,
        fileName: String,
        onProgress: (Int) -> Unit,
        onCompleted: (Uri) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        scope.launch {

            try {
                // 1️⃣ Folder SAF
                val folder = DocumentFile.fromTreeUri(context, folderUri)
                    ?: throw IllegalStateException("Folder SAF tidak valid")

                // 2️⃣ Buat file tujuan
                val file = folder.createFile(
                    "application/octet-stream",
                    fileName
                ) ?: throw IOException("Gagal membuat file")

                // 3️⃣ Request download
                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    throw IOException("HTTP error ${response.code}")
                }

                val body = response.body ?: throw IOException("Response body null")
                val total = body.contentLength()

                // 4️⃣ Stream ke SAF
                val input = body.byteStream()
                val output = context.contentResolver.openOutputStream(file.uri)
                    ?: throw IOException("Gagal membuka output stream")

                var downloaded = 0L
                val buffer = ByteArray(8 * 1024)
                var read: Int

                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                    downloaded += read

                    if (total > 0) {
                        val progress = ((downloaded * 100) / total).toInt()
                        withContext(Dispatchers.Main) {
                            onProgress(progress)
                        }
                    }
                }

                output.flush()
                input.close()
                output.close()

                withContext(Dispatchers.Main) {
                    onCompleted(file.uri)
                }

            } catch (e: Throwable) {
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }

    fun cancel() {
        scope.cancel()
    }
}