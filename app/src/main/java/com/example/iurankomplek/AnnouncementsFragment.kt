package com.example.iurankomplek

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iurankomplek.databinding.FragmentAnnouncementsBinding
import com.example.iurankomplek.model.Announcement
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.utils.NetworkUtils
import kotlinx.coroutines.launch

class AnnouncementsFragment : Fragment() {

    private lateinit var adapter: AnnouncementAdapter
    private lateinit var binding: FragmentAnnouncementsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAnnouncementsBinding.inflate(inflater, container, false)

        adapter = AnnouncementAdapter()
        binding.rvAnnouncements.layoutManager = LinearLayoutManager(context)
        binding.rvAnnouncements.adapter = adapter

        loadAnnouncements()

        return binding.root
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