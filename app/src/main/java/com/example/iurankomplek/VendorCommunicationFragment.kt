package com.example.iurankomplek
import com.example.iurankomplek.utils.Constants

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
class VendorCommunicationFragment : Fragment() {
    
    private lateinit var vendorRecyclerView: RecyclerView
    private lateinit var vendorAdapter: VendorAdapter
    private val viewModel: VendorViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_vendor_communication, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews(view)
        observeVendors()
        viewModel.loadVendors()
    }

    private fun setupViews(view: View) {
        vendorRecyclerView = view.findViewById(R.id.vendorRecyclerView)
        vendorAdapter = VendorAdapter { vendor ->
            // Handle vendor click - could initiate communication
            Toast.makeText(requireContext(), getString(R.string.communicate_with_vendor, vendor.name), Constants.Toast.DURATION_SHORT).show()
        }
        
        vendorRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = vendorAdapter
        }
    }
    
    private fun observeVendors() {
        viewModel.vendorState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    // Show loading indicator
                }
                is UiState.Success -> {
                    vendorAdapter.submitList(state.data.data)
                }
                is UiState.Error -> {
                    Toast.makeText(requireContext(), getString(R.string.error_loading_data), Constants.Toast.DURATION_SHORT).show()
                }
            }
        }
    }
}
