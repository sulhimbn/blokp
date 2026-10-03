package com.example.iurankomplek.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iurankomplek.data.repository.VendorRepository
import com.example.iurankomplek.data.api.models.VendorResponse
import com.example.iurankomplek.data.api.models.SingleVendorResponse
import com.example.iurankomplek.data.api.models.WorkOrderResponse
import com.example.iurankomplek.data.api.models.SingleWorkOrderResponse
import com.example.iurankomplek.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VendorViewModel @Inject constructor(
    private val vendorRepository: VendorRepository
) : ViewModel() {
    
    private val _vendorState = MutableStateFlow<UiState<VendorResponse>>(UiState.Loading)
    val vendorState: StateFlow<UiState<VendorResponse>> = _vendorState
    
    private val _workOrderState = MutableStateFlow<UiState<WorkOrderResponse>>(UiState.Loading)
    val workOrderState: StateFlow<UiState<WorkOrderResponse>> = _workOrderState
    
    private val _vendorDetailState = MutableStateFlow<UiState<SingleVendorResponse>>(UiState.Loading)
    val vendorDetailState: StateFlow<UiState<SingleVendorResponse>> = _vendorDetailState
    
    private val _workOrderDetailState = MutableStateFlow<UiState<SingleWorkOrderResponse>>(UiState.Loading)
    val workOrderDetailState: StateFlow<UiState<SingleWorkOrderResponse>> = _workOrderDetailState
    
    /**
     * Guards against overlapping requests. They cannot be derived from the state flows,
     * because those start out as Loading and would then block the very first load.
     */
    private var vendorLoadInFlight = false
    private var workOrderLoadInFlight = false

    fun loadVendors() {
        if (vendorLoadInFlight) return
        vendorLoadInFlight = true
        
        viewModelScope.launch {
            _vendorState.value = UiState.Loading
            vendorRepository.getVendors()
                .onSuccess { response ->
                    _vendorState.value = UiState.Success(response)
                }
                .onFailure { exception ->
                    _vendorState.value = UiState.Error(exception.message ?: "Unknown error occurred")
                }
            vendorLoadInFlight = false
        }
    }
    
    fun loadWorkOrders() {
        if (workOrderLoadInFlight) return
        workOrderLoadInFlight = true
        
        viewModelScope.launch {
            _workOrderState.value = UiState.Loading
            vendorRepository.getWorkOrders()
                .onSuccess { response ->
                    _workOrderState.value = UiState.Success(response)
                }
                .onFailure { exception ->
                    _workOrderState.value = UiState.Error(exception.message ?: "Unknown error occurred")
                }
            workOrderLoadInFlight = false
        }
    }
    
    fun loadVendorDetail(id: String) {
        viewModelScope.launch {
            _vendorDetailState.value = UiState.Loading
            vendorRepository.getVendor(id)
                .onSuccess { response ->
                    _vendorDetailState.value = UiState.Success(response)
                }
                .onFailure { exception ->
                    _vendorDetailState.value = UiState.Error(exception.message ?: "Unknown error occurred")
                }
        }
    }
    
    fun loadWorkOrderDetail(id: String) {
        viewModelScope.launch {
            _workOrderDetailState.value = UiState.Loading
            vendorRepository.getWorkOrder(id)
                .onSuccess { response ->
                    _workOrderDetailState.value = UiState.Success(response)
                }
                .onFailure { exception ->
                    _workOrderDetailState.value = UiState.Error(exception.message ?: "Unknown error occurred")
                }
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
                .onSuccess { response ->
                    // Optionally refresh vendor list after creation
                    loadVendors()
                }
                .onFailure { exception ->
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
                .onSuccess { response ->
                    // Optionally refresh work order list after creation
                    loadWorkOrders()
                }
                .onFailure { exception ->
                    // Handle error - could emit to a separate error state flow
                }
        }
    }
}
