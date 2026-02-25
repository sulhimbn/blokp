package com.example.iurankomplek

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.viewmodel.VendorViewModel
import dagger.hilt.android.AndroidEntryPoint

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
            Toast.makeText(this, getString(R.string.vendor_selected, vendor.name), Toast.LENGTH_SHORT).show()
        }
        
        vendorRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@VendorManagementActivity)
            adapter = vendorAdapter
        }
    }
    
    private fun observeVendors() {
        viewModel.vendorState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> {
                    // Show loading indicator
                }
                is UiState.Success -> {
                    vendorAdapter.submitList(state.data.data)
                }
                is UiState.Error -> {
                    Toast.makeText(this, getString(R.string.error_loading_data), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
