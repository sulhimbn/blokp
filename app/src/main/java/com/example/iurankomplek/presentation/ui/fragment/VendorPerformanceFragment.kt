package com.example.iurankomplek.presentation.ui.fragment

import com.example.iurankomplek.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.iurankomplek.databinding.FragmentVendorPerformanceBinding

class VendorPerformanceFragment : Fragment() {

    private var _binding: FragmentVendorPerformanceBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVendorPerformanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.analyticsTextView.text = getString(R.string.vendor_performance_analytics)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}