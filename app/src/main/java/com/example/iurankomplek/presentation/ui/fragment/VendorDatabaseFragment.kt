package com.example.iurankomplek.presentation.ui.fragment

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.R
import com.example.iurankomplek.core.base.BaseFragment
import com.example.iurankomplek.databinding.FragmentVendorDatabaseBinding
import com.example.iurankomplek.di.DependencyContainer
import com.example.iurankomplek.presentation.adapter.VendorAdapter
import com.example.iurankomplek.presentation.viewmodel.VendorViewModel
import com.example.iurankomplek.utils.UiState

class VendorDatabaseFragment : BaseFragment<com.example.iurankomplek.model.VendorResponse>() {

    private lateinit var binding: FragmentVendorDatabaseBinding

    private lateinit var vendorAdapter: VendorAdapter
    private lateinit var vendorViewModel: VendorViewModel

    override val recyclerView: RecyclerView
        get() = binding.vendorRecyclerView

    override val progressBar: View
        get() = binding.root.findViewById(com.example.iurankomplek.R.id.progressBar)

    override val emptyMessageStringRes: Int
        get() = R.string.toast_vendor_info

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: android.os.Bundle?
    ): View {
        binding = FragmentVendorDatabaseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun createAdapter(): RecyclerView.Adapter<*> {
        vendorAdapter = VendorAdapter { vendor ->
            android.widget.Toast.makeText(
                requireContext(),
                getString(R.string.toast_vendor_info, vendor.name),
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
        return vendorAdapter
    }

    override fun observeViewModelState() {
        observeUiState(vendorViewModel.vendorState, { vendorResponse ->
            vendorAdapter.submitList(vendorResponse.data)
        }, showErrorToast = false)
    }

    override fun initializeViewModel(viewModelProvider: ViewModelProvider) {
        vendorViewModel = com.example.iurankomplek.di.DependencyContainer.provideVendorViewModel()
    }

    override fun loadData() {
        vendorViewModel.loadVendors()
    }
}
