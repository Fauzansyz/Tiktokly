package com.zandeveloper.tiktokly.utils.startDownload

import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zandeveloper.tiktokly.databinding.DialogDownloadProgressBinding
import com.zandeveloper.tiktokly.utils.storageManager.DirectoryManager
import com.zandeveloper.tiktokly.utils.storageManager.SafDownloader
import com.zandeveloper.tiktokly.utils.alerts.Alerts
import com.zandeveloper.tiktokly.R
import android.app.Activity
import com.zandeveloper.tiktokly.utils.uiHandler.UiHandler

class StartDownload(
    private val activity: Activity,
    private val dm: SafDownloader,
    private val uiHandler: UiHandler
) {

    private var downloadDialog: AlertDialog? = null
    private var dialogBinding: DialogDownloadProgressBinding? = null

    fun startDownload(
        url: String,
        filename: String
    ) {

        val savedUri = DirectoryManager.resolveDownloadDir(activity)

        if (savedUri == null) {
            Alerts.makeText(
                activity,
                "Pegunduhan tidak bisa dilanjutkan!!",
                "Silahkan atur lokasi penyimpanan download",
                Alerts.WARN
            ).show()
            return
        }

        dialogBinding = DialogDownloadProgressBinding.inflate(
            android.view.LayoutInflater.from(activity)
        )

        downloadDialog = MaterialAlertDialogBuilder(activity)
            .setTitle("Downloading")
            .setView(dialogBinding!!.root)
            .setCancelable(false)
            .setNegativeButton("Cancel") { d, _ ->
                dm.cancel()
                d.dismiss()
            }
            .show()

        dm.download(
            url = url,
            folderUri = savedUri,
            fileName = filename,

            onProgress = { progress ->
                dialogBinding?.progressBar?.setProgressCompat(progress, true)
                dialogBinding?.progressText?.text = "$progress%"
            },

            onCompleted = { uri ->
                downloadDialog?.dismiss()
                Alerts.makeText(
                    activity,
                    activity.getString(R.string.success_video_downloading),
                    activity.getString(R.string.success_download_msg),
                    Alerts.SUCCESS
                ).show()

                uiHandler.clearAllText()
            },

            onError = {
                downloadDialog?.dismiss()
                Alerts.makeText(
                    activity,
                    activity.getString(R.string.failed_alert_title),
                    activity.getString(R.string.failed_download_msg),
                    Alerts.ERROR
                ).show()

                uiHandler.clearAllText()
            }
        )
    }
}