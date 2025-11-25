package com.zandeveloper.tiktokly.utils

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import kotlinx.coroutines.*
import kotlin.coroutines.resume

class downloadManager(private val context: Context) {

    private val dm by lazy {
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }

    fun download(
        url: String,
        fileName: String,
        onProgress: (progress: Int) -> Unit,
        onCompleted: (filePath: String) -> Unit,
        onError: () -> Unit
    ) {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(fileName)
            .setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                fileName
            )
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadId = dm.enqueue(request)

        // PROGRESS LOOP
        GlobalScope.launch(Dispatchers.IO) {
            var downloading = true
            while (downloading) {

                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor: Cursor = dm.query(query)

                if (cursor.moveToFirst()) {
                    val status =
                        cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                    val total =
                        cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    val downloaded =
                        cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))

                    if (status == DownloadManager.STATUS_FAILED) {
                        onError()
                        downloading = false
                        cursor.close()
                        break
                    }

                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        val fileUri = cursor.getString(
                            cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI)
                        )
                        onCompleted(fileUri)
                        downloading = false
                        cursor.close()
                        break
                    }

                    if (total > 0) {
                        val progress = ((downloaded * 100L) / total).toInt()
                        onProgress(progress)
                    }

                }
                cursor.close()
                delay(300) // check progress tiap 0.3s
            }
        }
    }
}