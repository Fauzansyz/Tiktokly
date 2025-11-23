package com.zandeveloper.tiktokly

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.zandeveloper.tiktokly.databinding.ActivityMainBinding
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
import com.zandeveloper.tiktokly.network.DataFetch
import java.io.IOException
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.zandeveloper.tiktokly.utils.ads.AdsApp
import com.google.android.material.appbar.MaterialToolbar
import android.view.View
import android.content.Intent
import androidx.core.widget.doOnTextChanged
import android.graphics.drawable.Drawable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import www.sanju.motiontoast.MotionToast
import androidx.core.content.res.ResourcesCompat
import www.sanju.motiontoast.MotionToastStyle


class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var df: DataFetch
    private lateinit var dm: downloadManager
    
    private lateinit var ads: AdsApp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        dm = downloadManager(this@MainActivity)
        
        ads = AdsApp(this)
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

binding.textInput.doOnTextChanged { text, _, _, _ ->
    binding.buttonDownload.visibility = if (text.isNullOrEmpty()) View.GONE else View.VISIBLE
}

        binding.buttonDownload.setOnClickListener {

    val inputUrl = binding.textInput.text.toString().trim()

    when {
        inputUrl.isEmpty() -> {
        MotionToast.createColorToast(
                    this@MainActivity,
                    "Gagal mengunduh!!",
                    "Silahkan masukan Url video yang valid",
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(this@MainActivity, R.font.helvetica_regular)
                )
        }

        else -> {
        
        binding.shimmerRoot.startShimmer()
    binding.shimmerRoot.visibility = View.VISIBLE
    binding.contentContainer.visibility = View.GONE

            
            val filename = "tiktokly_${System.currentTimeMillis()}.mp4"

            lifecycleScope.launch {
                val apiUrl = "https://dl-server-core.vercel.app/download"
                val data = df.fetchDataVideo(apiUrl, inputUrl)

                Toast.makeText(this@MainActivity, "error: $data", Toast.LENGTH_SHORT).show()

                when (data) {

                    null -> {
                    
                    binding.shimmerRoot.stopShimmer()
            binding.shimmerRoot.visibility = View.GONE
            binding.contentContainer.visibility = View.VISIBLE
                        
                        MotionToast.createColorToast(
                    this@MainActivity,
                    "Gagal mengunduh!!",
                    "Silahkan Coba lagi beberapa saat",
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(this@MainActivity, R.font.helvetica_regular)
                )
      }

                    else -> {
                        val result = data["result"] as? Map<*, *>

                        val title = result?.get("title") ?: "-"
                        val mp4 = result?.get("mp4") ?: "NaN"
                        val urlResult = result?.get("url") ?: "NaN"
                        val thumbnail = result?.get("thumbnail") ?: "-"

                        val downloadUrl = if (urlResult == "NaN") mp4 else urlResult

                        binding.titleVideo.text = title.toString()
                        binding.textInput.text = null
                

                        ads.showInterstitial(this@MainActivity) {
                            dm.download(downloadUrl.toString(), filename)
                
                        }
                        
                        dm.download(downloadUrl.toString(), filename)
                        
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
            }
        }
    }
  }

}


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}