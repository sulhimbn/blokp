package com.example.iurankomplek

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
import com.example.iurankomplek.viewmodel.AnnouncementViewModel
import kotlinx.coroutines.launch

class AnnouncementsFragment : Fragment() {

    private lateinit var adapter: AnnouncementAdapter
    private lateinit var binding: FragmentAnnouncementsBinding
    private lateinit var viewModel: AnnouncementViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAnnouncementsBinding.inflate(inflater, container, false)

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
                    is UiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is UiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        if (state.data.isEmpty()) {
                            Toast.makeText(context, getString(R.string.no_announcements_available), Toast.LENGTH_LONG).show()
                        } else {
                            adapter.submitList(state.data)
                        }
                    }
                    is UiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(context, getString(R.string.network_error, state.error), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}

    private fun loadAnnouncements() {
        // Show progress bar when starting the API call
        binding.progressBar.visibility = View.VISIBLE

         if (!NetworkUtils.isNetworkAvailable(requireContext())) {
             // Hide progress bar after failure
             binding.progressBar.visibility = View.GONE
             Toast.makeText(context, getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show()
             return
         }

         val apiService = ApiConfig.getApiService()

         viewLifecycleOwner.lifecycleScope.launch {
             try {
                 val response = apiService.getAnnouncements()
                 // Hide progress bar after response
                 binding.progressBar.visibility = View.GONE

                 if (response.isSuccessful) {
                     val announcements = response.body()
                     if (announcements != null) {
                         adapter.submitList(announcements)
                     } else {
                         Toast.makeText(context, getString(R.string.no_announcements_available), Toast.LENGTH_LONG).show()
                     }
                 } else {
                     Toast.makeText(context, getString(R.string.failed_to_load_announcements), Toast.LENGTH_LONG).show()
                 }
             } catch (e: Exception) {
                 // Hide progress bar after failure
                 binding.progressBar.visibility = View.GONE
                 Toast.makeText(context, getString(R.string.network_error, e.message), Toast.LENGTH_LONG).show()
             }
         }
    }
}