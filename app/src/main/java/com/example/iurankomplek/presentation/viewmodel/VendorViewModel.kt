package com.example.iurankomplek.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.iurankomplek.core.base.BaseViewModel
import com.example.iurankomplek.domain.usecase.LoadVendorsUseCase
import com.example.iurankomplek.domain.usecase.LoadWorkOrdersUseCase
import com.example.iurankomplek.domain.usecase.LoadVendorDetailUseCase
import com.example.iurankomplek.domain.usecase.LoadWorkOrderDetailUseCase
import com.example.iurankomplek.domain.usecase.CreateVendorUseCase
import com.example.iurankomplek.domain.usecase.CreateWorkOrderUseCase
import com.example.iurankomplek.model.VendorResponse
import com.example.iurankomplek.model.SingleVendorResponse
import com.example.iurankomplek.model.WorkOrderResponse
import com.example.iurankomplek.model.SingleWorkOrderResponse
import com.example.iurankomplek.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VendorViewModel(
    private val loadVendorsUseCase: LoadVendorsUseCase,
    private val loadWorkOrdersUseCase: LoadWorkOrdersUseCase,
    private val loadVendorDetailUseCase: LoadVendorDetailUseCase,
    private val loadWorkOrderDetailUseCase: LoadWorkOrderDetailUseCase,
    private val createVendorUseCase: CreateVendorUseCase,
    private val createWorkOrderUseCase: CreateWorkOrderUseCase
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
            loadVendorsUseCase()
        }
    }

    fun loadWorkOrders() {
        executeWithLoadingStateForResult(_workOrderState) {
            loadWorkOrdersUseCase()
        }
    }

    fun loadVendorDetail(id: String) {
        executeWithLoadingStateForResult(_vendorDetailState, preventDuplicate = false) {
            loadVendorDetailUseCase(id)
        }
    }

    fun loadWorkOrderDetail(id: String) {
        executeWithLoadingStateForResult(_workOrderDetailState, preventDuplicate = false) {
            loadWorkOrderDetailUseCase(id)
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
            when (val result = createVendorUseCase(
                name, contactPerson, phoneNumber, email, specialty, address,
                licenseNumber, insuranceInfo, contractStart, contractEnd
            )) {
                is com.example.iurankomplek.utils.OperationResult.Success -> {
                    loadVendors()
                }
                is com.example.iurankomplek.utils.OperationResult.Error -> {
                }
                else -> {}
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
            when (val result = createWorkOrderUseCase(
                title, description, category, priority, propertyId, reporterId, estimatedCost
            )) {
                is com.example.iurankomplek.utils.OperationResult.Success -> {
                    loadWorkOrders()
                }
                is com.example.iurankomplek.utils.OperationResult.Error -> {
                }
                else -> {}
            }
        }
    }

    class Factory(
        private val loadVendorsUseCase: LoadVendorsUseCase,
        private val loadWorkOrdersUseCase: LoadWorkOrdersUseCase,
        private val loadVendorDetailUseCase: LoadVendorDetailUseCase,
        private val loadWorkOrderDetailUseCase: LoadWorkOrderDetailUseCase,
        private val createVendorUseCase: CreateVendorUseCase,
        private val createWorkOrderUseCase: CreateWorkOrderUseCase
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(VendorViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return VendorViewModel(
                    loadVendorsUseCase, loadWorkOrdersUseCase, loadVendorDetailUseCase,
                    loadWorkOrderDetailUseCase, createVendorUseCase, createWorkOrderUseCase
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
