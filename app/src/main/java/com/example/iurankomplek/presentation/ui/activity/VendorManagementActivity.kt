package com.example.iurankomplek.presentation.ui.activity

import com.example.iurankomplek.core.base.BaseActivity
import com.example.iurankomplek.presentation.adapter.VendorAdapter
import com.example.iurankomplek.R
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.iurankomplek.presentation.ui.helper.StateManager
import com.example.iurankomplek.presentation.ui.helper.RecyclerViewHelper
import kotlinx.coroutines.launch
import com.example.iurankomplek.databinding.ActivityVendorManagementBinding
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.presentation.viewmodel.VendorViewModel
import com.example.iurankomplek.di.DependencyContainer

class VendorManagementActivity : BaseActivity() {

    private lateinit var binding: ActivityVendorManagementBinding
    private lateinit var vendorAdapter: VendorAdapter
    private lateinit var vendorViewModel: VendorViewModel
    private lateinit var stateManager: StateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVendorManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ViewModel
        vendorViewModel = DependencyContainer.provideVendorViewModel()

        // Initialize StateManager
        stateManager = StateManager.create(
            progressBar = binding.loadingProgressBar,
            emptyStateTextView = binding.emptyStateTextView,
            errorStateLayout = binding.errorStateLayout,
            errorStateTextView = binding.errorStateTextView,
            retryTextView = binding.retryTextView,
            recyclerView = binding.vendorRecyclerView,
            scope = lifecycleScope,
            context = this
        )

        setupViews()
        observeVendors()
        vendorViewModel.loadVendors()
    }

    private fun setupViews() {
        vendorAdapter = VendorAdapter { vendor ->
            Toast.makeText(this, getString(R.string.toast_vendor_info, vendor.name), Toast.LENGTH_SHORT).show()
        }

        RecyclerViewHelper.configureRecyclerView(
            recyclerView = binding.vendorRecyclerView,
            itemCount = 20,
            enableKeyboardNav = true,
            adapter = vendorAdapter,
            orientation = resources.configuration.orientation,
            screenWidthDp = resources.configuration.screenWidthDp
        )
    }

    private fun observeVendors() {
        stateManager.observeState(vendorViewModel.vendorState, onSuccess = { data ->
            data.data.let { vendors ->
                if (vendors.isNotEmpty()) {
                    vendorAdapter.submitList(vendors)
                } else {
                    stateManager.showEmpty()
                }
            }
        }, onError = { error ->
            Toast.makeText(this@VendorManagementActivity, getString(R.string.toast_error, error), Toast.LENGTH_SHORT).show()
        })
    }
}