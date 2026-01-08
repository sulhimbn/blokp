package com.example.iurankomplek.presentation.ui.fragment

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.R
import com.example.iurankomplek.core.base.BaseVendorFragment
import com.example.iurankomplek.databinding.FragmentVendorCommunicationBinding

class VendorCommunicationFragment : BaseVendorFragment() {

    private var _binding: FragmentVendorCommunicationBinding? = null
    private val binding get() = _binding!!

    override val recyclerView: RecyclerView
        get() = binding.vendorRecyclerView

    override val progressBar: View
        get() = binding.root.findViewById(com.example.iurankomplek.R.id.progressBar)

    override val emptyMessageStringRes: Int
        get() = R.string.toast_communicate_with_vendor

    override val vendorClickMessageRes: Int
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
}
