package com.zandeveloper.tiktokly.network.updateService

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.Gson
import java.io.File
import android.os.Build
import java.io.FileOutputStream
import android.provider.Settings
import com.zandeveloper.tiktokly.model.UpdateInfo


class UpdateServiceApp(private val context: Context) {

    private val client = OkHttpClient()

    fun checkUpdate() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = Request.Builder()
                    .url("https://api.github.com/repos/Fauzansyz/Tiktokly/releases/latest")
                    .header("Accept", "application/vnd.github+json")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@use
                    val body = response.body?.string() ?: return@use

                    val release = Gson().fromJson(body, UpdateInfo::class.java)
                    val latestVersion = release.tag_name
                    val currentVersion = context.packageManager
                        .getPackageInfo(context.packageName, 0).versionName

                    if (latestVersion != currentVersion) {
                        val apkAsset = release.assets.firstOrNull { it.name.endsWith(".apk") }
                        if (apkAsset != null) {
                            val file = File(context.getExternalFilesDir(null), apkAsset.name)
                            downloadApk(apkAsset.browser_download_url, file)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        if (!packageManager.canRequestPackageInstalls()) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }
    }
                withContext(Dispatchers.Main) {
                        installApk(file)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun downloadApk(url: String, file: File) = withContext(Dispatchers.IO) {
        client.newCall(Request.Builder().url(url).build()).execute().use { response ->
            val inputStream = response.body?.byteStream() ?: return@use
            FileOutputStream(file).use { fos -> inputStream.copyTo(fos) }
        }
    }

    private fun installApk(file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            context.packageName + ".provider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}