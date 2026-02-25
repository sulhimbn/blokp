package com.example.iurankomplek

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.viewmodel.VendorViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WorkOrderManagementFragment : Fragment() {
    
    private lateinit var workOrderRecyclerView: RecyclerView
    private lateinit var workOrderAdapter: WorkOrderAdapter
    private val viewModel: VendorViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_work_order_management, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews(view)
        observeWorkOrders()
        viewModel.loadWorkOrders()
    }

    private fun setupViews(view: View) {
        workOrderRecyclerView = view.findViewById(R.id.workOrderRecyclerView)
        workOrderAdapter = WorkOrderAdapter { workOrder ->
            // Handle work order click - could navigate to work order details
            Toast.makeText(requireContext(), getString(R.string.work_order_selected, workOrder.title), Toast.LENGTH_SHORT).show()
        }
        
        workOrderRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = workOrderAdapter
        }
    }
    
    private fun observeWorkOrders() {
        viewModel.workOrderState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    // Show loading indicator
                }
                is UiState.Success -> {
                    workOrderAdapter.submitList(state.data.data)
                }
                is UiState.Error -> {
                    Toast.makeText(requireContext(), getString(R.string.error_loading_data), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
