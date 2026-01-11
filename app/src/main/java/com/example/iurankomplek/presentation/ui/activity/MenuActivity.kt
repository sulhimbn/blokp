package com.example.iurankomplek.presentation.ui.activity

import com.example.iurankomplek.core.base.BaseActivity
import android.content.Intent
import android.os.Bundle
import com.example.iurankomplek.databinding.ActivityMenuBinding

class MenuActivity : BaseActivity() {
    private lateinit var binding: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    protected open fun setupFullscreenMode() {
        // No-op: MenuActivity doesn't use fullscreen mode
    }

    protected fun setupClickListeners() {
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