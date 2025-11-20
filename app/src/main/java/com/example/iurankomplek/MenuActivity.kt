package com.example.iurankomplek

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.iurankomplek.databinding.ActivityMenuBinding

class MenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupFullscreenMode()
        setupClickListeners()
    }

    private fun setupFullscreenMode() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
    }

    private fun setupClickListeners() {
        binding.cdMenu1.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        binding.cdMenu2.setOnClickListener {
            startActivity(Intent(this, LaporanActivity::class.java))
        }
    }
}