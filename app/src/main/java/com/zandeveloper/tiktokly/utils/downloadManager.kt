package com.zandeveloper.tiktokly.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import kotlinx.coroutines.*
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL

class downloadManager(
    private val context: Context,
    private val scope: CoroutineScope
) {

    fun download(
        url: String,
        folderUri: Uri?,
        fileName: String,
        onProgress: (Int) -> Unit,
        onCompleted: (Uri) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        scope.launch(Dispatchers.IO) {
            try {
            val resolvedFolderUri = folderUri
    ?: throw IllegalStateException("Folder URI belum dipilih")
                // 1️⃣ buat file di SAF
                val fileUri = DocumentsContract.createDocument(
                    context.contentResolver,
                    resolvedFolderUri,
                    "video/mp4", // ganti sesuai tipe
                    fileName
                ) ?: throw Exception("Gagal create file")

                // 2️⃣ buka koneksi
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connect()

                val totalSize = connection.contentLength

                val input = BufferedInputStream(connection.inputStream)
                val output = context.contentResolver.openOutputStream(fileUri)
                    ?: throw Exception("OutputStream null")

                val buffer = ByteArray(8 * 1024)
                var downloaded = 0
                var read: Int

                // 3️⃣ streaming + progress
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                    downloaded += read

                    if (totalSize > 0) {
                        val progress = (downloaded * 100) / totalSize
                        withContext(Dispatchers.Main) {
                            onProgress(progress)
                        }
                    }
                }

                output.flush()
                output.close()
                input.close()
                connection.disconnect()

                withContext(Dispatchers.Main) {
                    onCompleted(fileUri)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }
}