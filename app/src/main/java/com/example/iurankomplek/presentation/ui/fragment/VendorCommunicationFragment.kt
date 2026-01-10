package com.example.iurankomplek.presentation.ui.fragment

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.R
import com.example.iurankomplek.core.base.BaseFragment
import com.example.iurankomplek.databinding.FragmentVendorCommunicationBinding
import com.example.iurankomplek.presentation.adapter.VendorAdapter
import com.example.iurankomplek.presentation.viewmodel.VendorViewModel
import com.example.iurankomplek.utils.UiState

class VendorCommunicationFragment : BaseFragment<com.example.iurankomplek.model.VendorResponse>() {

    private var _binding: FragmentVendorCommunicationBinding? = null
    private val binding get() = _binding!!

    private lateinit var vendorAdapter: VendorAdapter
    private lateinit var vendorViewModel: VendorViewModel

    override val recyclerView: RecyclerView
        get() = binding.vendorRecyclerView

    override val progressBar: View
        get() = binding.root.findViewById(com.example.iurankomplek.R.id.progressBar)

    override val emptyMessageStringRes: Int
        get() = R.string.toast_communicate_with_vendor

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: android.os.Bundle?
    ): View {
        _binding = FragmentVendorCommunicationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun createAdapter(): RecyclerView.Adapter<*> {
        vendorAdapter = VendorAdapter { vendor ->
            android.widget.Toast.makeText(
                requireContext(),
                getString(R.string.toast_communicate_with_vendor, vendor.name),
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
        return vendorAdapter
    }

    override fun observeViewModelState() {
        observeUiState(vendorViewModel.vendorState, { vendors ->
            vendorAdapter.submitList(vendors)
        }, showErrorToast = false)
    }

    override fun initializeViewModel(viewModelProvider: ViewModelProvider) {
        val factory = com.example.iurankomplek.di.DependencyContainer.provideVendorViewModel()
        vendorViewModel = viewModelProvider.get(VendorViewModel::class.java, factory)
    }

    override fun loadData() {
        vendorViewModel.loadVendors()
    }
}
