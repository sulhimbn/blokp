package com.example.iurankomplek

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.utils.Constants
import com.example.iurankomplek.presentation.adapter.VendorAdapter
import com.example.iurankomplek.viewmodel.VendorViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VendorManagementActivity : AppCompatActivity() {
    
    private lateinit var vendorRecyclerView: RecyclerView
    private lateinit var vendorAdapter: VendorAdapter
    private val viewModel: VendorViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vendor_management)
        
        setupViews()
        observeVendors()
        viewModel.loadVendors()
    }
    
    private fun setupViews() {
        vendorRecyclerView = findViewById(R.id.vendorRecyclerView)
        vendorAdapter = VendorAdapter { vendor ->
            // Handle vendor click - could navigate to vendor details
            Toast.makeText(this, getString(R.string.vendor_selected, vendor.name), Constants.Toast.DURATION_SHORT).show()
        }
        
        vendorRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@VendorManagementActivity)
            adapter = vendorAdapter
        }
    }
    
    private fun observeVendors() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.vendorState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            // Show loading indicator
                        }
                        is UiState.Success -> {
                            vendorAdapter.submitList(state.data.data)
                        }
                        is UiState.Error -> {
                            Toast.makeText(this@VendorManagementActivity, getString(R.string.error_loading_data), Constants.Toast.DURATION_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}
