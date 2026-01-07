package com.example.iurankomplek

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.data.repository.VendorRepositoryFactory
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.viewmodel.VendorViewModel

class VendorManagementActivity : AppCompatActivity() {
    
    private lateinit var vendorRecyclerView: RecyclerView
    private lateinit var vendorAdapter: VendorAdapter
    private lateinit var vendorViewModel: VendorViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vendor_management)
        
        // Initialize ViewModel
        val repository = VendorRepositoryFactory.getInstance()
        vendorViewModel = ViewModelProvider(this, VendorViewModel.Factory(repository))[VendorViewModel::class.java]
        
        setupViews()
        observeVendors()
        vendorViewModel.loadVendors()
    }
    
    private fun setupViews() {
        vendorRecyclerView = findViewById(R.id.vendorRecyclerView)
        vendorAdapter = VendorAdapter { vendor ->
            // Handle vendor click - could navigate to vendor details
            Toast.makeText(this, "Vendor: ${vendor.name}", Toast.LENGTH_SHORT).show()
        }
        
        vendorRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@VendorManagementActivity)
            adapter = vendorAdapter
        }
    }
    
    private fun observeVendors() {
        lifecycleScope.launch {
            vendorViewModel.vendorState.collect { state ->
                when (state) {
                    is UiState.Loading -> {
                        // Show loading indicator
                    }
                    is UiState.Success -> {
                        vendorAdapter.submitList(state.data.data)
                    }
                    is UiState.Error -> {
                        Toast.makeText(this@VendorManagementActivity, "Error: ${state.error}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}