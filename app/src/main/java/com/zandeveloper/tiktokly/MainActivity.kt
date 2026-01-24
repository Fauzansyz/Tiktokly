package com.zandeveloper.tiktokly

import com.zandeveloper.tiktokly.BuildConfig
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.zandeveloper.tiktokly.databinding.ActivityMainBinding
import android.content.Intent
import com.zandeveloper.tiktokly.ui.about.AboutActivity
import com.zandeveloper.tiktokly.ui.setting.SettingActivity
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import android.net.Uri
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import com.zandeveloper.tiktokly.utils.storageManager.SafDownloader
import android.content.Context
import com.zandeveloper.tiktokly.data.network.DataFetch
import java.io.IOException
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.zandeveloper.tiktokly.utils.ads.AdsApp
import com.google.android.material.appbar.MaterialToolbar
import android.view.LayoutInflater
import android.view.View
import androidx.core.widget.doOnTextChanged
import android.graphics.drawable.Drawable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import www.sanju.motiontoast.MotionToast
import androidx.core.content.res.ResourcesCompat
import www.sanju.motiontoast.MotionToastStyle
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import com.zandeveloper.tiktokly.utils.alerts.Alerts
import com.zandeveloper.tiktokly.utils.stringValidator.StringValidator

import com.zandeveloper.tiktokly.utils.userHelp.UserHelpApp

import com.zandeveloper.tiktokly.data.network.updateService.UpdateServiceApp
import android.view.animation.DecelerateInterpolator
import com.google.gson.reflect.TypeToken
import com.zandeveloper.tiktokly.utils.uiHandler.UiHandler
import com.zandeveloper.tiktokly.utils.storageManager.DirectoryManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zandeveloper.tiktokly.databinding.DialogDownloadProgressBinding
import androidx.appcompat.app.AlertDialog
import android.view.animation.AnimationUtils
import com.zandeveloper.tiktokly.utils.startDownload.StartDownload

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    
    private val binding get() = _binding!!

    private lateinit var df: DataFetch
    private lateinit var dm: SafDownloader
    private lateinit var stringValidate: StringValidator
    private lateinit var uihandler: UiHandler
    
    private lateinit var ads: AdsApp
    private lateinit var savedUri: Uri
    
    private var downloadDialog: AlertDialog? = null
private var dialogBinding: DialogDownloadProgressBinding? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        dm = SafDownloader(this)
        
        ads = AdsApp(this)
        
        df = DataFetch(this)
        stringValidate = StringValidator()
        
        uihandler = UiHandler(
           binding.textInput,
           binding.titleVideo,
           binding.itemThumbnail
    )
        
        ads.preload()

        val downloadButtonAnimSlide = downloadButtonAnimSlide.loadAnimation(this, R.anim.slide_up_fade)
        downloadButtonAnimSlide.startOffset = 1500
binding.buttonDownload.startAnimation(downloadButtonAnimSlide)

val pasteButtonAnimSlide = pasteButtonAnimSlide.loadAnimation(this, R.anim.slide_up_fade)
pasteButtonAnimSlide.startOffset = 1700
binding.buttonPaste.startAnimation(pasteButtonAnimSlide)

       
        
        val prefs = getSharedPreferences("tutorial", MODE_PRIVATE)
        val firstRun = prefs.getBoolean("firstRun", true)

        if (firstRun) {
          val help = UserHelpApp.Builder()
             .setActivity(this)
             .setBinding(binding)
             .build()
    
       binding.root.post {
          help.startHelp()
            }
            prefs.edit().putBoolean("firstRun", false).apply()
        }
        
        binding.topAppBar.setOnMenuItemClickListener { item ->
    when (item.itemId) {
        R.id.action_setting -> {
        val intent = Intent(this,SettingActivity::class.java)
         startActivity(intent)
            true
        }

        else -> false
    }
}


        binding.buttonDownload.setOnClickListener {
    val inputUrl = binding.textInput.text.toString().trim()
    if (inputUrl.isEmpty()) {
        Alerts.makeText(this@MainActivity, getString(R.string.failed_alert_title), getString(R.string.input_required_msg), Alerts.ERROR).show()
        return@setOnClickListener
    }

    uihandler.showShimmer(binding.shimmerRoot, binding.contentContainer)
    
    lifecycleScope.launch {
        val apiUrl = "https://dl-server-core.vercel.app/download"
        val data = df.fetchDataVideo(apiUrl, inputUrl)

        if (data == null) {
            uihandler.hideShimmer(binding.shimmerRoot, binding.contentContainer)

            Alerts.makeText(this@MainActivity, getString(R.string.failed_alert_title), getString(R.string.failed_download_msg),Alerts.ERROR).show()
            return@launch
        }
        
        val result = data["result"] as? Map<*, *>
        val platform = data?.get("platform")
        val title = result?.get("title") ?: "-"
        val urlResult = result?.get("url") ?: "NaN"
        val thumbnail = result?.get("thumbnail") ?: "-"

        binding.titleVideo.text = title.toString()
        binding.textInput.text = null
        
          ads.showOrContinue(this@MainActivity) {
        
   if (platform == "TikTok") {
    val videoList = result?.get("video") as? List<*>
    val mp4 = videoList?.firstOrNull()?.toString() ?: "NaN"
    lifecycleScope.launch {
        
      val filename = "tiktokly_${System.currentTimeMillis()}.mp4"
       
       val begin = StartDownload(this@MainActivity,dm,uiHandler = uihandler)
      
      begin.startDownload(mp4.toString(), filename)
      uihandler.clearThumbnail(binding.itemThumbnail)
    
    }

}
        if(platform.toString() == "YouTube") {
                
      Alerts.makeText(this@MainActivity,getString(R.string.alertDownloader), getString(R.string.alertDownloaderMessage), Alerts.ERROR).show()
      uihandler.clearText(binding.textInput, binding.titleVideo)
      uihandler.clearThumbnail(binding.itemThumbnail)
      
     }
     
     if(platform.toString() == "Instagram") {
     val videoUrl = result?.get("url") ?:"NaN"
     
     lifecycleScope.launch {
        
     val filename = "insta_${System.currentTimeMillis()}.mp4"
     
     val begin = StartDownload(this@MainActivity,dm,uiHandler = uihandler)
      
      begin.startDownload(videoUrl.toString(), filename)
      uihandler.clearThumbnail(binding.itemThumbnail)
        
  }
}
 
            
        uihandler.hideShimmer(binding.shimmerRoot, binding.contentContainer)
        }
        
        uihandler.hideShimmer(binding.shimmerRoot, binding.contentContainer)
        
        uihandler.showThumbnail(binding.itemThumbnail,thumbnail.toString())
   
            }
    
       }
       
   }
    
    
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}