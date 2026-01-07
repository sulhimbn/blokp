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
import kotlinx.coroutines.launch
import com.example.iurankomplek.databinding.FragmentVendorCommunicationBinding
import com.example.iurankomplek.data.repository.VendorRepositoryFactory
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.viewmodel.VendorViewModel

class VendorCommunicationFragment : Fragment() {

    private var _binding: FragmentVendorCommunicationBinding? = null
    private val binding get() = _binding!!
    private lateinit var vendorAdapter: VendorAdapter
    private lateinit var vendorViewModel: VendorViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVendorCommunicationBinding.inflate(inflater, container, false)
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
        observeVendors()
        vendorViewModel.loadVendors()
    }
    
    private fun setupViews() {
        vendorAdapter = VendorAdapter { vendor ->
            Toast.makeText(requireContext(), getString(R.string.toast_communicate_with_vendor, vendor.name), Toast.LENGTH_SHORT).show()
        }

        binding.vendorRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = vendorAdapter
        }
    }
    
    private fun observeVendors() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                vendorViewModel.vendorState.collect { state ->
                    when (state) {
                        is UiState.Idle -> {
                        }
                        is UiState.Loading -> {
                            // Show loading indicator
                        }
                        is UiState.Success -> {
                            vendorAdapter.submitList(state.data.data)
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
