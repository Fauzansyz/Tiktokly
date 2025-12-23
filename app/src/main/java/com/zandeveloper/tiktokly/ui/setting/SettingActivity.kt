package com.zandeveloper.tiktokly.ui.setting

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import com.zandeveloper.tiktokly.R
import com.zandeveloper.tiktokly.ui.about.AboutActivity
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import com.zandeveloper.tiktokly.databinding.ActivitySettingBinding
import com.zandeveloper.tiktokly.utils.storageManager.DirectoryManager

class SettingActivity : AppCompatActivity() {

private var _binding: ActivitySettingBinding? = null
private val binding get() = _binding!!
private val folderPicker =
    registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let {
            // kasih izin permanen
            contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )

            DirectoryManager.saveDirectory(this, it)
            binding.textCurrentDirectory.text =
                DirectoryManager.getDirectoryLabel(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       
        _binding = ActivitySettingBinding.inflate(layoutInflater)
        
        binding.buttonChangeDirectory.setOnClickListener {
          folderPicker.launch(null)
        }
        
        binding.textCurrentDirectory.text = DirectoryManager.getDirectoryLabel(this)
        
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
