package com.zandeveloper.tiktokly.utils.alerts
import com.zandeveloper.tiktokly.R
import www.sanju.motiontoast.MotionToast
import androidx.core.content.res.ResourcesCompat
import www.sanju.motiontoast.MotionToastStyle
import android.content.Context
import android.app.Activity

class Alerts private constructor(
    private val activity: Activity,
    private val title: String,
    private val message: String,
    private val type: Int
) {

    companion object {
        const val SUCCESS = 1
        const val ERROR = 2
        const val WARN = 3

        fun makeText(activity: Activity, title: String, message: String, type: Int): Alerts {
            return Alerts(activity, title, message, type)
        }
    }

    fun show() {
        // Contoh implementasi, bisa diganti Toast, Dialog, Snackbar, dsb
        when (type) {
            SUCCESS -> {
              MotionToast.createColorToast(
                activity,
                title,
                message,
                MotionToastStyle.SUCCESS,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(activity, R.font.helvetica_regular)
            )
            }
            
            ERROR -> {
              MotionToast.createColorToast(
                activity,
                title,
                message,
                MotionToastStyle.ERROR,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(activity, R.font.helvetica_regular)
             )
            }
            WARN -> {
              MotionToast.createColorToast(
            activity,
            title,
            message,
            MotionToastStyle.WARNING,
            MotionToast.GRAVITY_BOTTOM,
            MotionToast.LONG_DURATION,
            ResourcesCompat.getFont(activity, R.font.helvetica_regular)
        )
            }
        }
    }
}