package com.example.iurankomplek.presentation.ui.activity

import com.example.iurankomplek.core.base.BaseActivity
import com.example.iurankomplek.R
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.iurankomplek.databinding.ActivityWorkOrderDetailBinding
import com.example.iurankomplek.data.repository.VendorRepositoryFactory
import com.example.iurankomplek.model.WorkOrder
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.presentation.viewmodel.VendorViewModel
import com.example.iurankomplek.utils.InputSanitizer
import kotlinx.coroutines.launch

class WorkOrderDetailActivity : BaseActivity() {

    private lateinit var binding: ActivityWorkOrderDetailBinding
    private lateinit var vendorViewModel: VendorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val rawWorkOrderId = intent.getStringExtra("WORK_ORDER_ID")
        val workOrderId = if (!rawWorkOrderId.isNullOrBlank() && 
            InputSanitizer.isValidAlphanumericId(rawWorkOrderId)) {
            rawWorkOrderId.trim()
        } else {
            null
        }

        if (workOrderId != null) {
            val repository = VendorRepositoryFactory.getInstance()
            vendorViewModel = ViewModelProvider(this, VendorViewModel.Factory(repository))[VendorViewModel::class.java]

            observeWorkOrderDetails()
            vendorViewModel.loadWorkOrderDetail(workOrderId)
        } else {
            Toast.makeText(this, getString(R.string.work_order_id_not_provided), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun observeWorkOrderDetails() {
        lifecycleScope.launch {
            vendorViewModel.workOrderDetailState.collect { state ->
                when (state) {
                    is UiState.Idle -> {
                    }
                    is UiState.Loading -> {
                    }
                    is UiState.Success -> {
                        displayWorkOrderDetails(state.data.data)
                    }
                    is UiState.Error -> {
                        Toast.makeText(this@WorkOrderDetailActivity, getString(R.string.error_with_message, state.error), Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }
    }

    private fun displayWorkOrderDetails(workOrder: WorkOrder) {
        binding.workOrderTitle.text = workOrder.title
        binding.workOrderDescription.text = workOrder.description
        binding.workOrderCategory.text = workOrder.category
        binding.workOrderStatus.text = workOrder.status
        binding.workOrderPriority.text = workOrder.priority
        binding.workOrderVendor.text = workOrder.vendorName ?: getString(R.string.work_order_not_assigned)
        binding.workOrderEstimatedCost.text = "Rp ${workOrder.estimatedCost}"
        binding.workOrderActualCost.text = "Rp ${workOrder.actualCost}"
        binding.workOrderPropertyId.text = workOrder.propertyId
        binding.workOrderCreatedAt.text = workOrder.createdAt
    }
}