package com.example.iurankomplek.presentation.ui.activity
 
import com.example.iurankomplek.core.base.BaseActivity
import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.core.app.ActivityOptionsCompat
import com.example.iurankomplek.databinding.ActivityMenuBinding
import android.transition.Fade
import android.transition.TransitionSet
 
class MenuActivity : BaseActivity() {
    private lateinit var binding: ActivityMenuBinding
 
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupEnterTransition()
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
 
        setupClickListeners()
    }
 
    private fun setupEnterTransition() {
        val fade = Fade()
        fade.duration = 300
        window.enterTransition = fade
    }
 
    private fun startActivityWithTransition(intent: Intent) {
        val options = ActivityOptionsCompat.makeCustomAnimation(
            this,
            android.R.anim.fade_in,
            android.R.anim.fade_out
        )
        startActivity(intent, options.toBundle())
    }
 
    private fun setupClickListeners() {
        binding.cdMenu1.setOnClickListener {
            startActivityWithTransition(Intent(this, MainActivity::class.java))
        }
 
        binding.cdMenu2.setOnClickListener {
            startActivityWithTransition(Intent(this, LaporanActivity::class.java))
        }
        
        binding.cdMenu3.setOnClickListener {
            startActivityWithTransition(Intent(this, CommunicationActivity::class.java))
        }
        
        binding.cdMenu4.setOnClickListener {
            startActivityWithTransition(Intent(this, PaymentActivity::class.java))
        }
    }
}