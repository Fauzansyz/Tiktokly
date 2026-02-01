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
import android.content.ClipboardManager
import androidx.activity.result.contract.ActivityResultContracts
import com.zandeveloper.tiktokly.utils.urlValidator.UrlValidator
import androidx.core.view.doOnLayout
import androidx.core.widget.doOnTextChanged
import com.zandeveloper.tiktokly.MainActivity

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    
    private val binding get() = _binding!!

    private lateinit var df: DataFetch
    private lateinit var dm: SafDownloader
    private lateinit var urlValidator: UrlValidator
    private lateinit var stringValidator: StringValidator
    private val folderPickerLauncher =
    registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
        if (uri != null) {
        contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            DirectoryManager.saveCustomDir(this, uri)
            isPickingFolder = false
        }
    }
    private lateinit var uihandler: UiHandler
    
    private lateinit var ads: AdsApp
    private var isPickingFolder = false
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
        stringValidator = StringValidator()
        urlValidator = UrlValidator()
        
        uihandler = UiHandler(
           binding.textInput,
           binding.titleVideo,
           binding.itemThumbnail
    )
        
        ads.preload()

        val settingButtonAnim = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade)
        
        settingButtonAnim.startOffset = 1500
binding.actionSetting.startAnimation(settingButtonAnim)

        val pasteButtonAnim = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade)

        pasteButtonAnim.startOffset = 1700
        
        binding.buttonPaste.startAnimation(pasteButtonAnim)

       
        
        val prefs = getSharedPreferences("tutorial", MODE_PRIVATE)
        val firstRun = prefs.getBoolean("firstRun", true)
        val tutorialDone = DirectoryManager.isTutorialFinish(this)
        val folderUri = DirectoryManager.getCustomDir(this)
        
if (firstRun) {
   val help = UserHelpApp.Builder()
            .setActivity(this)
            .setBinding(binding)
            .setOnFinishListener {
                prefs.edit().putBoolean("firstRun", false).apply()
                openFolderPicker()
            }
            .build()

        binding.root.doOnLayout { help.startHelp() }

} else if (folderUri == null) {
   openFolderPicker()
}

binding.actionSetting.setOnClickListener {
  val intent = Intent(this,SettingActivity::class.java)
         startActivity(intent)
}

binding.buttonPaste.setOnClickListener {
  val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
  var text = ""
  
  val item = clipboard?.primaryClip?.getItemAt(0)
  text = item?.text.toString()
  
  binding.textInput.setText(text)
  
}

binding.textInput.doOnTextChanged { text, start, before, count  ->
    val inputUrl = text.toString().trim()
    var urls = urlValidator.extractUrlsFromString(inputUrl)
    
    if (binding.textInput.hasFocus()) {
    binding.inputTextContainer.setErrorEnabled(false)
}
      if (urls == "") {
      val messageError = getString(R.string.input_required_msg)
      binding.inputTextContainer.setError(messageError)
      
      binding.inputTextContainer.setErrorIconDrawable(R.drawable.ic_error)
      
     }
     uihandler.showShimmer(binding.shimmerRoot, binding.contentContainer)
    
    lifecycleScope.launch {
        val apiUrl = "https://dl-server-core.vercel.app/download"
        val data = df.fetchDataVideo(apiUrl, urls.toString())

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
        
        if(result != null){
        uihandler.hideShimmer(binding.shimmerRoot, binding.contentContainer)
        
        binding.buttonDownload.visibility = View.VISIBLE
         
        uihandler.hideShimmer(binding.shimmerRoot, binding.contentContainer)
        
        uihandler.showThumbnail(binding.itemThumbnail,thumbnail.toString())
        binding.titleVideo.text = title.toString()
        
        binding.buttonDownload.setOnClickListener {
        
         ads.showOrContinue(this@MainActivity) {
         
         binding.textInput.text = null
        
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
      uihandler.clearAllText()
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
   
            }
        }
      }
   }
}
   
private fun openFolderPicker() {
    if (isPickingFolder) return
    isPickingFolder = true
    folderPickerLauncher.launch(null)
}
    
    
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}