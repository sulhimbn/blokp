package com.example.iurankomplek

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iurankomplek.databinding.FragmentWorkOrderManagementBinding
import com.example.iurankomplek.data.repository.VendorRepositoryFactory
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.viewmodel.VendorViewModel

class WorkOrderManagementFragment : Fragment() {

    private var _binding: FragmentWorkOrderManagementBinding? = null
    private val binding get() = _binding!!
    private lateinit var workOrderAdapter: WorkOrderAdapter
    private lateinit var vendorViewModel: VendorViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkOrderManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize ViewModel
        val repository = VendorRepositoryFactory.getInstance()
        vendorViewModel = ViewModelProvider(
            this, 
            VendorViewModel.Factory(repository)
        )[VendorViewModel::class.java]
        
        setupViews()
        observeWorkOrders()
        vendorViewModel.loadWorkOrders()
    }
    
    private fun setupViews() {
        workOrderAdapter = WorkOrderAdapter { workOrder ->
            Toast.makeText(requireContext(), getString(R.string.toast_work_order_info, workOrder.title), Toast.LENGTH_SHORT).show()
        }

        binding.workOrderRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = workOrderAdapter
        }
    }
    
    private fun observeWorkOrders() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                vendorViewModel.workOrderState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            // Show loading indicator
                        }
                        is UiState.Success -> {
                            workOrderAdapter.submitList(state.data.data)
                        }
                        is UiState.Error -> {
                            Toast.makeText(requireContext(), getString(R.string.toast_error, state.error), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
