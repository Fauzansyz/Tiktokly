package com.zandeveloper.tiktokly.utils

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import kotlinx.coroutines.launch
import okhttp3.Request

class downloadManager(private val context: Context) {

  fun download(
        url: String,
        folderUri: Uri,
        fileName: String,
        mimeType: String = "video/mp4",
        onProgress: (Int) -> Unit,
        onCompleted: (Uri) -> Unit,
        onError: (Exception) -> Unit
    ) {
    
    CoroutineScope(Dispatchers.IO).launch {

        try {
        
        val resolvedFolderUri = folderUri
    ?: throw IllegalStateException("Folder URI belum dipilih")
            // 1️⃣ buat file di SAF
            val fileUri = DocumentsContract.createDocument(
                context.contentResolver,
                resolvedFolderUri,
                mimeType,
                fileName
            ) ?: throw Exception("Gagal membuat file")

            val request = Request.Builder().url(url).build()
            val response = OkHttpClient().newCall(request).execute()

            if (!response.isSuccessful) {
                throw Exception("Download gagal")
            }

            val body = response.body ?: throw Exception("Body kosong")
            val total = body.contentLength()

            val input = body.byteStream()
            val output = context.contentResolver.openOutputStream(fileUri)
                ?: throw Exception("OutputStream null")

            val buffer = ByteArray(8 * 1024)
            var downloaded = 0L
            var read: Int

            while (input.read(buffer).also { read = it } != -1) {
                output.write(buffer, 0, read)
                downloaded += read

                if (total > 0) {
                    val progress = ((downloaded * 100) / total).toInt()
                    onProgress(progress)
                }
            }

            output.flush()
            output.close()
            input.close()

            onCompleted(fileUri)

        } catch (e: Exception) {
            onError(e)
        }
      }
    }
}