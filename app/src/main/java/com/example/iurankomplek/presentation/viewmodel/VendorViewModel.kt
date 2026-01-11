package com.example.iurankomplek.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.iurankomplek.core.base.BaseViewModel
import com.example.iurankomplek.data.repository.VendorRepository
import com.example.iurankomplek.model.VendorResponse
import com.example.iurankomplek.model.SingleVendorResponse
import com.example.iurankomplek.model.WorkOrderResponse
import com.example.iurankomplek.model.SingleWorkOrderResponse
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.utils.onSuccess
import com.example.iurankomplek.utils.onError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VendorViewModel(
    private val vendorRepository: VendorRepository
) : BaseViewModel() {
    
    private val _vendorState = createMutableStateFlow<VendorResponse>(UiState.Loading)
    val vendorState: StateFlow<UiState<VendorResponse>> = _vendorState
    
    private val _workOrderState = createMutableStateFlow<WorkOrderResponse>(UiState.Loading)
    val workOrderState: StateFlow<UiState<WorkOrderResponse>> = _workOrderState
    
    private val _vendorDetailState = createMutableStateFlow<SingleVendorResponse>(UiState.Loading)
    val vendorDetailState: StateFlow<UiState<SingleVendorResponse>> = _vendorDetailState
    
    private val _workOrderDetailState = createMutableStateFlow<SingleWorkOrderResponse>(UiState.Loading)
    val workOrderDetailState: StateFlow<UiState<SingleWorkOrderResponse>> = _workOrderDetailState
    
    fun loadVendors() {
        executeWithLoadingStateForResult(_vendorState) {
            vendorRepository.getVendors()
        }
    }
    
    fun loadWorkOrders() {
        executeWithLoadingStateForResult(_workOrderState) {
            vendorRepository.getWorkOrders()
        }
    }
    
    fun loadVendorDetail(id: String) {
        executeWithLoadingStateForResult(_vendorDetailState, preventDuplicate = false) {
            vendorRepository.getVendor(id)
        }
    }
    
    fun loadWorkOrderDetail(id: String) {
        executeWithLoadingStateForResult(_workOrderDetailState, preventDuplicate = false) {
            vendorRepository.getWorkOrder(id)
        }
    }
    
    fun createVendor(
        name: String,
        contactPerson: String,
        phoneNumber: String,
        email: String,
        specialty: String,
        address: String,
        licenseNumber: String,
        insuranceInfo: String,
        contractStart: String,
        contractEnd: String
    ) {
        viewModelScope.launch {
            vendorRepository.createVendor(
                name, contactPerson, phoneNumber, email, specialty, address,
                licenseNumber, insuranceInfo, contractStart, contractEnd
            )
                .onSuccess { _ ->
                    // Optionally refresh vendor list after creation
                    loadVendors()
                }
                .onError { _ ->
                    // Handle error - could emit to a separate error state flow
                }
        }
    }
    
    fun createWorkOrder(
        title: String,
        description: String,
        category: String,
        priority: String,
        propertyId: String,
        reporterId: String,
        estimatedCost: Double
    ) {
        viewModelScope.launch {
            vendorRepository.createWorkOrder(
                title, description, category, priority, propertyId, reporterId, estimatedCost
            )
                .onSuccess { _ ->
                    // Optionally refresh work order list after creation
                    loadWorkOrders()
                }
                .onError { _ ->
                    // Handle error - could emit to a separate error state flow
                }
        }
    }
    
    class Factory(private val vendorRepository: VendorRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(VendorViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return VendorViewModel(vendorRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}