package com.zandeveloper.tiktokly.ui.setting

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.zandeveloper.tiktokly.databinding.ActivitySettingBinding

class SettingActivity : AppCompatActivity() {

private var _binding: ActivitySettingBinding? = null
private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       
        _binding = ActivitySettingBinding.inflate(layoutInflater)
        
        setContentView(binding.root)
    }
}
