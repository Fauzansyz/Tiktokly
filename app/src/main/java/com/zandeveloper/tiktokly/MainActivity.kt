package com.zandeveloper.tiktokly

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.zandeveloper.tiktokly.databinding.ActivityMainBinding
import android.content.Intent
import com.zandeveloper.tiktokly.about.AboutActivity
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
import com.zandeveloper.tiktokly.network.DataFetch
import java.io.IOException
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.zandeveloper.tiktokly.utils.ads.AdsApp
import com.google.android.material.appbar.MaterialToolbar
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
        ads.loadInterstitialAd()
        
        df = DataFetch()
        
        
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


binding.textInput.doOnTextChanged { text, _, _, _ ->
    binding.buttonDownload.visibility = if (text.isNullOrEmpty()) View.GONE else View.VISIBLE
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
    trace.start()

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
        val title = result?.get("title") ?: "-"
        val mp4 = result?.get("mp4") ?: "NaN"
        val mp3 = result?.get("mp3") ?: "NaN"
        val urlResult = result?.get("url") ?: "NaN"
        val thumbnail = result?.get("thumbnail") ?: "-"

        val downloadUrl = if (urlResult == "NaN") mp4 else urlResult

        binding.titleVideo.text = title.toString()
        binding.textInput.text = null

        ads.showInterstitial(this@MainActivity) {
        if(isValidUrl(mp3.toString())) {
           showVideoFormatDialog(this@MainActivity, formats){ selectedItems ->
           if(selectedItems == "mp3"){
           val filename = "tiktokly_${System.currentTimeMillis()}.mp3"
                dm.download(
            mp3.toString(), 
            filename, 
            
            onProgress = { p ->
        runOnUiThread {
           // binding.progressText.text = "Progress: $p%"
        }
    },

    onCompleted = { filePath ->
        runOnUiThread {
         alert.success()
        }
    },

    onError = {
        runOnUiThread {
                  alert.failed()
                     }
                 })
               } else {
                  val filename = "tiktokly_${System.currentTimeMillis()}.mp4"
        
     dm.download(
            downloadUrl.toString(), 
            filename, 
            
            onProgress = { p ->
        runOnUiThread {
           // binding.progressText.text = "Progress: $p%"
        }
    },

    onCompleted = { filePath ->
        runOnUiThread {
         alert.success()
        }
    },

    onError = {
        runOnUiThread {
                  alert.failed()
        }
    })
               }
           }
        } else {
        val filename = "tiktokly_${System.currentTimeMillis()}.mp4"
        
     dm.download(
            downloadUrl.toString(), 
            filename, 
            
            onProgress = { p ->
        runOnUiThread {
           // binding.progressText.text = "Progress: $p%"
        }
    },

    onCompleted = { filePath ->
        runOnUiThread {
         alert.success()
        }
    },

    onError = {
        runOnUiThread {
                  alert.failed()
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
            .load(thumbnail.toString())
            .centerCrop()
            .transform(RoundedCorners(20))
            .into(binding.itemThumbnail)
               }
         }
         trace.stop()

}


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}