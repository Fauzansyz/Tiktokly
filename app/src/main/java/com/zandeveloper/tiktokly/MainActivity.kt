package com.zandeveloper.tiktokly

import com.zandeveloper.tiktokly.BuildConfig
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.zandeveloper.tiktokly.databinding.ActivityMainBinding
import android.content.Intent
import com.zandeveloper.tiktokly.ui.about.AboutActivity
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import com.zandeveloper.tiktokly.utils.downloadManager
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zandeveloper.tiktokly.utils.alerts.Alerts
import com.zandeveloper.tiktokly.utils.stringValidator.StringValidator

import com.zandeveloper.tiktokly.utils.userHelp.UserHelpApp

import com.zandeveloper.tiktokly.data.network.updateService.UpdateServiceApp

import android.view.animation.DecelerateInterpolator

import com.google.gson.reflect.TypeToken
import com.zandeveloper.tiktokly.utils.uiHandler.UiHandler


class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    
    private val binding get() = _binding!!

    private lateinit var df: DataFetch
    private lateinit var dm: downloadManager
    private lateinit var alert: Alerts
    private lateinit var stringValidate: StringValidator
    private lateinit var uihandler: UiHandler
    
    private lateinit var ads: AdsApp
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        dm = downloadManager(this)
        
        ads = AdsApp(this)
        alert = Alerts(this@MainActivity)
        
        df = DataFetch(this)
        stringValidate = StringValidator()
        
        uihandler = UiHandler()
        
        ads.preload()
        
        // == update service ==
        
        val updateServices = UpdateServiceApp(this)
        updateServices.checkUpdate()
        
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
        R.id.action_about -> {
        val intent = Intent(this,AboutActivity::class.java)
         startActivity(intent)
            true
        }

        else -> false
    }
}


var trace = FirebasePerformance.getInstance().newTrace("Fetch_data")

        binding.buttonDownload.setOnClickListener {
    val inputUrl = binding.textInput.text.toString().trim()
    val formats = listOf("mp4", "mp3")
    if (inputUrl.isEmpty()) {
        alert.requiredInput()
        return@setOnClickListener
    }

    uihandler.showShimmer(binding.shimmerRoot, binding.contentContainer)
    
    lifecycleScope.launch {
        val apiUrl = "https://dl-server-core.vercel.app/download"
        val data = df.fetchDataVideo(apiUrl, inputUrl)

        if (data == null) {
            uihandler.hideShimmer(binding.shimmerRoot, binding.contentContainer)
            
           uiHandler.clearText(binding.titleVideo, binding.textInput)

            alert.failed()
            return@launch
        } else {
          handleDownload(data)
        }
}
private fun handleDownload(data: Map<String, Any?>) {
        val platform = data["platform"].toString()
        val result = data["result"] as? Map<*, *> ?: return

        val url = when(platform) {
            "TikTok" -> (result["video"] as? List<*>)?.firstOrNull()?.toString()
            "Instagram" -> result["url"]?.toString()
            else -> null
        } ?: return

        val filename = "${platform}_${System.currentTimeMillis()}.mp4"

        ads.showOrContinue(this) {
            dm.download(
                url, filename,
                onProgress = { /* update progress */ },
                onCompleted = {
                    alert.success()
                    uiHandler.clearThumbnail(binding.itemThumbnail)
                    uiHandler.clearText(binding.titleVideo, binding.textInput)
                },
                onError = {
                    alert.failed()
                    uiHandler.clearThumbnail(binding.itemThumbnail)
                    uiHandler.clearText(binding.titleVideo, binding.textInput)
                }
            )
        }

        // Update thumbnail
        val thumbnail = result["thumbnail"]?.toString()
        if(thumbnail != null) {
            uiHandler.showThumbnail(binding.itemThumbnail, thumbnail)
        }
    }
    
    }
    
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}