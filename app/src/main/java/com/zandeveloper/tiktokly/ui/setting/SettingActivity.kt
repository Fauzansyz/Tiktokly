package com.zandeveloper.tiktokly.ui.setting

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import com.zandeveloper.tiktokly.ui.about.AboutActivity
import com.zandeveloper.tiktokly.databinding.ActivitySettingBinding

class SettingActivity : AppCompatActivity() {

private var _binding: ActivitySettingBinding? = null
private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       
        _binding = ActivitySettingBinding.inflate(layoutInflater)
        
        setSupportActionBar(binding.topAppBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // tombol back aktif
        
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

        
        binding.topAppBar.setNavigationOnClickListener {
            finish() // kembali ke activity sebelumnya
        }
        
        setContentView(binding.root)
    }
}
