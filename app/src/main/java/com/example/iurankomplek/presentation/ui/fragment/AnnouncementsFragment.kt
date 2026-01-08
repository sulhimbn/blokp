package com.example.iurankomplek.presentation.ui.fragment

import com.example.iurankomplek.presentation.adapter.AnnouncementAdapter
import com.example.iurankomplek.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iurankomplek.databinding.FragmentAnnouncementsBinding
import com.example.iurankomplek.data.repository.AnnouncementRepositoryFactory
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.presentation.viewmodel.AnnouncementViewModel
import kotlinx.coroutines.launch

class AnnouncementsFragment : Fragment() {

    private var _binding: FragmentAnnouncementsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AnnouncementAdapter
    private lateinit var viewModel: AnnouncementViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnnouncementsBinding.inflate(inflater, container, false)

        adapter = AnnouncementAdapter()
        binding.rvAnnouncements.layoutManager = LinearLayoutManager(context)
        binding.rvAnnouncements.adapter = adapter

        initializeViewModel()
        observeAnnouncementsState()
        viewModel.loadAnnouncements()

        return binding.root
    }

    private fun initializeViewModel() {
        val announcementRepository = AnnouncementRepositoryFactory.getInstance()
        viewModel = ViewModelProvider(
            this,
            AnnouncementViewModel.Factory(announcementRepository)
        )[AnnouncementViewModel::class.java]
    }

    private fun observeAnnouncementsState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.announcementsState.collect { state ->
                when (state) {
                    is UiState.Idle -> {
                    }
                    is UiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is UiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        if (state.data.isEmpty()) {
                            Toast.makeText(requireContext(), getString(R.string.no_announcements_available), Toast.LENGTH_LONG).show()
                        } else {
                            adapter.submitList(state.data)
                        }
                    }
                    is UiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(requireContext(), getString(R.string.network_error, state.error), Toast.LENGTH_LONG).show()
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
