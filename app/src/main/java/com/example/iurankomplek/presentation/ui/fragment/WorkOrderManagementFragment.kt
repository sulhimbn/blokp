package com.example.iurankomplek.presentation.ui.fragment

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.R
import com.example.iurankomplek.core.base.BaseFragment
import com.example.iurankomplek.databinding.FragmentWorkOrderManagementBinding
import com.example.iurankomplek.presentation.adapter.WorkOrderAdapter
import com.example.iurankomplek.presentation.viewmodel.VendorViewModel
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.model.WorkOrderResponse

class WorkOrderManagementFragment : BaseFragment<WorkOrderResponse>() {

    private lateinit var binding: FragmentWorkOrderManagementBinding

    private lateinit var workOrderAdapter: WorkOrderAdapter
    private lateinit var vendorViewModel: VendorViewModel

    override val recyclerView: RecyclerView
        get() = binding.workOrderRecyclerView

    override val progressBar: View
        get() = binding.root.findViewById(com.example.iurankomplek.R.id.progressBar)

    override val emptyMessageStringRes: Int
        get() = R.string.toast_work_order_info

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: android.os.Bundle?
    ): View {
        binding = FragmentWorkOrderManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun createAdapter(): RecyclerView.Adapter<*> {
        workOrderAdapter = WorkOrderAdapter { workOrder ->
            android.widget.Toast.makeText(
                requireContext(),
                getString(R.string.toast_work_order_info, workOrder.title),
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
        return workOrderAdapter
    }

    override fun initializeViewModel(viewModelProvider: ViewModelProvider) {
        val factory = com.example.iurankomplek.di.DependencyContainer.provideVendorViewModel()
        vendorViewModel = ViewModelProvider(this, factory)[VendorViewModel::class.java]
    }

    override fun observeViewModelState() {
        observeUiState(vendorViewModel.workOrderState, { data ->
            workOrderAdapter.submitList(data.data)
        }, showErrorToast = false)
    }

    override fun loadData() {
        vendorViewModel.loadWorkOrders()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}
