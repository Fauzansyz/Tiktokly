package com.zandeveloper.tiktokly.utils.storageManager
import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile


object DirectoryManager {

    private const val PREF_NAME = "storage_pref"
    private const val KEY_DIR_URI = "dir_uri"

    fun saveDirectory(context: Context, uri: Uri) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_DIR_URI, uri.toString())
            .apply()
    }

    fun getDirectoryUri(context: Context): Uri? {
        val uriStr = context
            .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_DIR_URI, null)

        return uriStr?.let { Uri.parse(it) }
    }

    fun getDirectoryLabel(context: Context): String {
        val uri = getDirectoryUri(context) ?: return "Default (Download/)"
        val doc = DocumentFile.fromTreeUri(context, uri)
        return doc?.name ?: "Custom Folder"
    }
}