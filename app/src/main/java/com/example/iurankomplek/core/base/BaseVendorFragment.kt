package com.example.iurankomplek.core.base

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.presentation.adapter.VendorAdapter
import com.example.iurankomplek.presentation.viewmodel.VendorViewModel

abstract class BaseVendorFragment : BaseFragment<com.example.iurankomplek.utils.UiState<com.example.iurankomplek.model.VendorResponse>>() {

    protected abstract val vendorClickMessageRes: Int
    protected abstract val recyclerView: RecyclerView

    protected lateinit var vendorAdapter: VendorAdapter
    protected lateinit var vendorViewModel: VendorViewModel

    override fun createAdapter(): RecyclerView.Adapter<*> {
        vendorAdapter = VendorAdapter { vendor ->
            android.widget.Toast.makeText(
                requireContext(),
                getString(vendorClickMessageRes, vendor.name),
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
        return vendorAdapter
    }

    override fun observeViewModelState() {
        observeUiState(vendorViewModel.vendorState) { data ->
            vendorAdapter.submitList(data.data)
        }
    }

    override fun loadData() {
        vendorViewModel.loadVendors()
    }

    override fun initializeViewModel(viewModelProvider: ViewModelProvider) {
        val repository = com.example.iurankomplek.data.repository.VendorRepositoryFactory.getInstance()
        vendorViewModel = viewModelProvider.get(VendorViewModel::class.java)
    }
}
