package com.example.iurankomplek.presentation.ui.activity

import com.example.iurankomplek.core.base.BaseActivity
import com.example.iurankomplek.presentation.adapter.VendorAdapter
import com.example.iurankomplek.R
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import com.example.iurankomplek.databinding.ActivityVendorManagementBinding
import com.example.iurankomplek.data.repository.VendorRepositoryFactory
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.viewmodel.VendorViewModel

class VendorManagementActivity : BaseActivity() {

    private lateinit var binding: ActivityVendorManagementBinding
    private lateinit var vendorAdapter: VendorAdapter
    private lateinit var vendorViewModel: VendorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVendorManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize ViewModel
        val repository = VendorRepositoryFactory.getInstance()
        vendorViewModel = ViewModelProvider(this, VendorViewModel.Factory(repository))[VendorViewModel::class.java]
        
        setupViews()
        observeVendors()
        vendorViewModel.loadVendors()
    }
    
    private fun setupViews() {
        vendorAdapter = VendorAdapter { vendor ->
            Toast.makeText(this, getString(R.string.toast_vendor_info, vendor.name), Toast.LENGTH_SHORT).show()
        }

        binding.vendorRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@VendorManagementActivity)
            adapter = vendorAdapter
        }
    }
    
    private fun observeVendors() {
        lifecycleScope.launch {
            vendorViewModel.vendorState.collect { state ->
                when (state) {
                    is UiState.Idle -> {
                    }
                    is UiState.Loading -> {
                        // Show loading indicator
                    }
                    is UiState.Success -> {
                        vendorAdapter.submitList(state.data.data)
                    }
                    is UiState.Error -> {
                        Toast.makeText(this@VendorManagementActivity, getString(R.string.toast_error, state.error), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}