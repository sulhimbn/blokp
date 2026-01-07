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
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.data.repository.VendorRepositoryFactory
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.viewmodel.VendorViewModel

class VendorDatabaseFragment : Fragment() {
    
    private lateinit var vendorRecyclerView: RecyclerView
    private lateinit var vendorAdapter: VendorAdapter
    private lateinit var vendorViewModel: VendorViewModel
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_vendor_database, container, false)
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
        observeVendors()
        vendorViewModel.loadVendors()
    }
    
    private fun setupViews() {
        vendorRecyclerView = view?.findViewById(R.id.vendorRecyclerView)!!
        vendorAdapter = VendorAdapter { vendor ->
            // Handle vendor click - could navigate to vendor details
            Toast.makeText(context, "Vendor: ${vendor.name}", Toast.LENGTH_SHORT).show()
        }
        
        vendorRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = vendorAdapter
        }
    }
    
    private fun observeVendors() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                vendorViewModel.vendorState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            // Show loading indicator
                        }
                        is UiState.Success -> {
                            vendorAdapter.submitList(state.data.data)
                        }
                        is UiState.Error -> {
                            Toast.makeText(context, "Error: ${state.error}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }