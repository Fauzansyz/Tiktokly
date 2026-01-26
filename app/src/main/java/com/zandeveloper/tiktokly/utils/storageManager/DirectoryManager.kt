package com.zandeveloper.tiktokly.utils.storageManager
import android.content.Context
import android.net.Uri
import android.content.Intent
import android.os.Environment
import androidx.documentfile.provider.DocumentFile


object DirectoryManager {

    private const val PREF_NAME = "storage_pref"
    private const val KEY_CUSTOM_DIR = "custom_dir_uri"

    // Default Download folder (SAF)
    private val DEFAULT_DOWNLOAD_URI: Uri =
        Uri.parse("content://com.android.externalstorage.documents/tree/primary:Download")

    // Simpan custom directory dari user
    fun saveCustomDir(context: Context, uri: Uri) {
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )

        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CUSTOM_DIR, uri.toString())
            .apply()
    }

    fun getCustomDir(context: Context): Uri? {
        val uriStr = context
            .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_CUSTOM_DIR, null)

        return uriStr?.let { Uri.parse(it) }
    }

    // Final directory (custom kalau ada, kalau tidak pakai Download)
    fun getFinalDir(context: Context): Uri {
        return getCustomDir(context) ?: DEFAULT_DOWNLOAD_URI
    }

    // Label folder untuk UI
    fun getDirectoryLabel(context: Context): String {
        val uri = getCustomDir(context) ?: return "Default (Download)"

        val doc = DocumentFile.fromTreeUri(context, uri)
        return doc?.name ?: "Custom Folder"
    }
}