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

import com.zandeveloper.tiktokly.utils.userHelp.UserHelpApp

import com.zandeveloper.tiktokly.data.network.updateService.UpdateServiceApp

import android.view.animation.DecelerateInterpolator

import com.google.gson.reflect.TypeToken


class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    
    private val binding get() = _binding!!

    private lateinit var df: DataFetch
    private lateinit var dm: downloadManager
    private lateinit var alert: Alerts
    
    private lateinit var ads: AdsApp
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        dm = downloadManager(this)
        
        ads = AdsApp(this)
        alert = Alerts(this@MainActivity)
        
        df = DataFetch(this)
        
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

fun isValidUrl(url: String?): Boolean {
    if (url.isNullOrBlank()) return false
    return url.startsWith("http://") || url.startsWith("https://")
}


fun showVideoFormatDialog(
    context: Context,
    availableFormats: List<String>,
    onFormatSelected: (String) -> Unit
) {
    val formatsArray = availableFormats.toTypedArray()
    
    MaterialAlertDialogBuilder(context)
        .setTitle("Pilih format video")
        .setSingleChoiceItems(formatsArray, -1) { dialog, which ->
            // Pilihan langsung diambil
            onFormatSelected(formatsArray[which])
            dialog.dismiss()
        }
        .setNegativeButton("Batal") { dialog, _ ->
            dialog.dismiss()
        }
        .show()
}


var trace = FirebasePerformance.getInstance().newTrace("Fetch_data")

        binding.buttonDownload.setOnClickListener {
    val inputUrl = binding.textInput.text.toString().trim()
    val formats = listOf("mp4", "mp3")
    if (inputUrl.isEmpty()) {
        alert.requiredInput()
        return@setOnClickListener
    }

    binding.shimmerRoot.startShimmer()
    binding.shimmerRoot.visibility = View.VISIBLE
    binding.contentContainer.visibility = View.GONE
    
    lifecycleScope.launch {
        val apiUrl = "https://dl-server-core.vercel.app/download"
        val data = df.fetchDataVideo(apiUrl, inputUrl)

        if (data == null) {
            binding.shimmerRoot.stopShimmer()
            binding.shimmerRoot.visibility = View.GONE
            binding.contentContainer.visibility = View.VISIBLE

            alert.failed()
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

    val filename = "tiktokly_${System.currentTimeMillis()}.mp4"

dm.download(
            mp4.toString(), 
            filename, 
            
            onProgress = { p ->
        runOnUiThread {
           // binding.progressText.text = "Progress: $p%"
        }
    },

    onCompleted = { filePath ->
        runOnUiThread {
         alert.success()
         clearUI()
         binding.textInput.text?.clear()
        }
    },

    onError = {
        runOnUiThread {
          alert.failed()
          clearUI()
          binding.textInput.text?.clear()
        }
    })
}
        
        // ============== 
        
        // Youtube feature in maintance mode
        
        if(platform.toString() == "YouTube") {
                
      alert.warn("Pegunduhan tidak bisa dilanjutkan!!", "Ada sedikit masalah untuk pengunduhan video Youtube,silahkan tunggu update selanjutnya")
    
     }
     
     if(platform.toString() == "Instagram") {
     val videoUrl = result?.get("url") ?:"NaN"
     val filename = "insta_${System.currentTimeMillis()}.mp4"
        
     dm.download(
            videoUrl.toString(), 
            filename, 
            
            onProgress = { p ->
        runOnUiThread {
           // binding.progressText.text = "Progress: $p%"
        }
    },

    onCompleted = { filePath ->
        runOnUiThread {
         alert.success()
         clearUI()
         binding.textInput.text?.clear()
        }
    },

    onError = {
        runOnUiThread {
          alert.failed()
          clearUI()
          binding.textInput.text?.clear()
        }
    })
     }
 
            
                binding.shimmerRoot.stopShimmer()
                binding.shimmerRoot.visibility = View.GONE
                binding.contentContainer.visibility = View.VISIBLE
        }
        
        binding.shimmerRoot.stopShimmer()
                binding.shimmerRoot.visibility = View.GONE
                binding.contentContainer.visibility = View.VISIBLE

        Glide.with(this@MainActivity)
    .load(thumbnail)
    .thumbnail(0.25f) // load versi kecil dulu (kerasa banget di HP kentang)
    .override(600, 600) // batasi ukuran biar gak rakus RAM
    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
    .transform(
        CenterCrop(),
        RoundedCorners(20)
    )
    .into(binding.itemThumbnail)
    
            }
         }
}

private fun clearUI() {
    // clear text
    binding.contentContainer.visibility = View.GONE
    binding.titleVideo.text = ""
    binding.debugText.text = ""

    // clear image (Glide)
    Glide.with(this).clear(binding.itemThumbnail)
    binding.itemThumbnail.setImageDrawable(null)

    // reset progress kalau ada
    // binding.progressBar.progress = 0
}

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}