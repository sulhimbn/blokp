package com.example.iurankomplek

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iurankomplek.databinding.FragmentAnnouncementsBinding
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.viewmodel.AnnouncementsViewModel

class AnnouncementsFragment : Fragment() {

    private lateinit var adapter: AnnouncementAdapter
    private lateinit var binding: FragmentAnnouncementsBinding
    private lateinit var announcementsViewModel: AnnouncementsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAnnouncementsBinding.inflate(inflater, container, false)

        adapter = AnnouncementAdapter()
        binding.rvAnnouncements.layoutManager = LinearLayoutManager(context)
        binding.rvAnnouncements.adapter = adapter

        // Initialize ViewModel
        announcementsViewModel = ViewModelProvider(
            this,
            AnnouncementsViewModel.Factory(ApiConfig.getApiService())
        )[AnnouncementsViewModel::class.java]

        // Observe state with viewLifecycleOwner
        observeAnnouncementsState()

        // Load data
        announcementsViewModel.loadAnnouncements()

        return binding.root
    }

    private fun observeAnnouncementsState() {
        announcementsViewModel.announcementsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is UiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    adapter.submitList(state.data)
                }
                is UiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, state.error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
