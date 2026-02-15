package com.example.iurankomplek.data.api.models

import com.example.iurankomplek.model.WorkOrder

// Response models for work order-related API calls
data class WorkOrderResponse(
    val data: List<WorkOrder>
)

data class SingleWorkOrderResponse(
    val data: WorkOrder
)
