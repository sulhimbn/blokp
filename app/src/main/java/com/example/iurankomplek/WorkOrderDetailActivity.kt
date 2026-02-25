package com.example.iurankomplek

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.iurankomplek.utils.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WorkOrderDetailActivity : AppCompatActivity() {
    
    private val viewModel: VendorViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work_order_detail)
        
        // Get work order ID from intent
        val workOrderId = intent.getStringExtra("WORK_ORDER_ID")
        
        if (workOrderId != null) {
            observeWorkOrderDetails()
            viewModel.loadWorkOrderDetail(workOrderId)
        } else {
            Toast.makeText(this, getString(R.string.work_order_id_not_provided), Constants.Toast.DURATION_SHORT).show()
            finish()
        }
    }
    
    private fun observeWorkOrderDetails() {
        viewModel.workOrderDetailState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> {
                    // Show loading indicator
                }
                is UiState.Success -> {
                    displayWorkOrderDetails(state.data.data)
                }
                is UiState.Error -> {
                    Toast.makeText(this, getString(R.string.error_loading_data), Constants.Toast.DURATION_SHORT).show()
                    finish()
                }
            }
        }
    }
    
    private fun displayWorkOrderDetails(workOrder: com.example.iurankomplek.model.WorkOrder) {
        findViewById<TextView>(R.id.workOrderTitle).text = workOrder.title
        findViewById<TextView>(R.id.workOrderDescription).text = workOrder.description
        findViewById<TextView>(R.id.workOrderCategory).text = workOrder.category
        findViewById<TextView>(R.id.workOrderStatus).text = workOrder.status
        findViewById<TextView>(R.id.workOrderPriority).text = workOrder.priority
        findViewById<TextView>(R.id.workOrderVendor).text = workOrder.vendorName ?: "Not assigned"
        findViewById<TextView>(R.id.workOrderEstimatedCost).text = "Rp ${workOrder.estimatedCost}"
        findViewById<TextView>(R.id.workOrderActualCost).text = "Rp ${workOrder.actualCost}"
        findViewById<TextView>(R.id.workOrderPropertyId).text = workOrder.propertyId
        findViewById<TextView>(R.id.workOrderCreatedAt).text = workOrder.createdAt
    }
}
