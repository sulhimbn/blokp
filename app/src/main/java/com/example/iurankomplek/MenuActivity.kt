package com.example.iurankomplek

import android.content.Intent
import android.os.Bundle
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.iurankomplek.databinding.ActivityMenuBinding

class MenuActivity : BaseActivity() {
    private lateinit var binding: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupFullscreenMode()
        setupClickListeners()
    }

    private fun setupFullscreenMode() {
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.statusBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun setupClickListeners() {
        binding.cdMenu1.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        binding.cdMenu2.setOnClickListener {
            startActivity(Intent(this, LaporanActivity::class.java))
        }
        
        binding.cdMenu3.setOnClickListener {
            startActivity(Intent(this, CommunicationActivity::class.java))
        }
        
        binding.cdMenu4.setOnClickListener {
            startActivity(Intent(this, PaymentActivity::class.java))
        }
    }
}