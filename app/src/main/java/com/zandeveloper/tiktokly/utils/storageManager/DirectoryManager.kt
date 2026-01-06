package com.zandeveloper.tiktokly.utils.storageManager
import android.content.Context
import android.net.Uri
import android.content.Intent
import android.os.Environment
import androidx.documentfile.provider.DocumentFile


object DirectoryManager {

    private const val PREF_NAME = "storage_pref"
    private const val KEY_CUSTOM_DIR = "custom_dir_uri"

fun saveCustomDir(context: Context, uri: Uri) {
    context.contentResolver.takePersistableUriPermission(
        uri,
        Intent.FLAG_GRANT_READ_URI_PERMISSION or
        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    )

    context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        .edit()
        .putString(KEY_CUSTOM_DIR, uri.toString())
        .apply()
}

    fun getCustomDir(context: Context): Uri? {
    val uriStr = context
        .getSharedPreferences("settings", Context.MODE_PRIVATE)
        .getString(KEY_CUSTOM_DIR, null)

    return uriStr?.let { Uri.parse(it) }
}

fun getDefaultDir(): Uri {
    val file = Environment
        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

    return Uri.fromFile(file)
}

fun getDownloadFolder(context: Context): Uri {
    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    val uriString = prefs.getString("download_dir", null)

    return if (uriString != null) {
        Uri.parse(uriString)
    } else {
        Uri.parse(
            "content://com.android.externalstorage.documents/tree/primary:Download"
        )
    }
}

fun resolveDownloadDir(context: Context): Uri {
    return getCustomDir(context) ?: getDownloadFolder(context)
}

    fun getDirectoryLabel(context: Context): String {
        val uri = getCustomDir(context) ?: return "Default (Download/)"
        val doc = DocumentFile.fromTreeUri(context, uri)
        return doc?.name ?: "Custom Folder"
    }
}