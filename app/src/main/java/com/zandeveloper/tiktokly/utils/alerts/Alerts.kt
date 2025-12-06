package com.zandeveloper.tiktokly.utils.alerts
import com.zandeveloper.tiktokly.R
import www.sanju.motiontoast.MotionToast
import androidx.core.content.res.ResourcesCompat
import www.sanju.motiontoast.MotionToastStyle
import android.content.Context
import android.app.Activity

class Alerts(private var activity: Activity) {
   fun warn(Title: String, subText: String) {
   MotionToast.createColorToast(
            activity,
            Title,
            subText,
            MotionToastStyle.WARNING,
            MotionToast.GRAVITY_BOTTOM,
            MotionToast.LONG_DURATION,
            ResourcesCompat.getFont(activity, R.font.helvetica_regular)
        )
   }

  fun requiredInput() {
        MotionToast.createColorToast(
            activity,
            "Gagal mengunduh!!",
            "Silahkan masukan Url video yang valid",
            MotionToastStyle.ERROR,
            MotionToast.GRAVITY_BOTTOM,
            MotionToast.LONG_DURATION,
            ResourcesCompat.getFont(activity, R.font.helvetica_regular)
        )
}


  fun success(){
  
MotionToast.createColorToast(
                activity,
                "Berhasil mengunduh!!",
                "Video berhasil di unduh",
                MotionToastStyle.SUCCESS,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(activity, R.font.helvetica_regular)
            )
            
}

fun failed() {
        MotionToast.createColorToast(
                activity,
                "Gagal mengunduh!!",
                "Silahkan coba lagi beberapa saat",
                MotionToastStyle.ERROR,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(activity, R.font.helvetica_regular)
            )
    }

}
