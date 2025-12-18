package com.zandeveloper.tiktokly.ui.about

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.zandeveloper.tiktokly.databinding.ActivityAboutBinding
import android.content.pm.PackageManager
import android.content.Context

 class AboutActivity : AppCompatActivity() {

    private var _binding: ActivityAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currentVersion = try {
            packageManager.getPackageInfo(packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            "Unknown"
        }

        binding.appVersion.text = currentVersion
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}