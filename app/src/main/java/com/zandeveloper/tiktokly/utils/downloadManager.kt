package com.zandeveloper.tiktokly.utils

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import android.net.Uri

class DownloadManagerApp(private val context: Context) {

    private val dm by lazy {
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }

    fun download(
        url: String,
        folderUri: Uri,
        fileName: String,
        onProgress: (Int) -> Unit,
        onCompleted: (Uri) -> Unit,
        onError: () -> Unit
    ) {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(fileName)
            .setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )
            .setDestinationUri(
                Uri.withAppendedPath(folderUri, fileName)
            )
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadId = dm.enqueue(request)

        CoroutineScope(Dispatchers.IO).launch {
            var downloading = true
            while (downloading) {
                val cursor = dm.query(
                    DownloadManager.Query().setFilterById(downloadId)
                )

                if (cursor.moveToFirst()) {
                    when (
                        cursor.getInt(
                            cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)
                        )
                    ) {
                        DownloadManager.STATUS_FAILED -> {
                            onError()
                            downloading = false
                        }

                        DownloadManager.STATUS_SUCCESSFUL -> {
                            val uri = Uri.parse(
                                cursor.getString(
                                    cursor.getColumnIndexOrThrow(
                                        DownloadManager.COLUMN_LOCAL_URI
                                    )
                                )
                            )
                            onCompleted(uri)
                            downloading = false
                        }
                    }

                    val total =
                        cursor.getLong(cursor.getColumnIndexOrThrow(
                            DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    val downloaded =
                        cursor.getLong(cursor.getColumnIndexOrThrow(
                            DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))

                    if (total > 0) {
                        onProgress(((downloaded * 100) / total).toInt())
                    }
                }
                cursor.close()
                delay(300)
            }
        }
    }
}