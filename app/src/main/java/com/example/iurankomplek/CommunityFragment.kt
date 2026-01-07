package com.example.iurankomplek

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iurankomplek.databinding.FragmentCommunityBinding
import com.example.iurankomplek.model.CommunityPost
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.utils.NetworkUtils
import kotlinx.coroutines.launch

class CommunityFragment : Fragment() {

    private lateinit var adapter: CommunityPostAdapter
    private lateinit var binding: FragmentCommunityBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCommunityBinding.inflate(inflater, container, false)

        adapter = CommunityPostAdapter()
        binding.rvCommunity.layoutManager = LinearLayoutManager(context)
        binding.rvCommunity.adapter = adapter

        loadCommunityPosts()

        return binding.root
    }

    private fun loadCommunityPosts() {
        // Show progress bar when starting API call
        binding.progressBar.visibility = View.VISIBLE

        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            // Hide progress bar after failure
            binding.progressBar.visibility = View.GONE
            Toast.makeText(context, "No internet connection", Toast.LENGTH_LONG).show()
            return
        }

        val apiService = ApiConfig.getApiService()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = apiService.getCommunityPosts()
                // Hide progress bar after response
                binding.progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val posts = response.body()
                    if (posts != null) {
                        adapter.submitList(posts)
                    } else {
                        Toast.makeText(context, "No community posts available", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(context, "Failed to load community posts", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                // Hide progress bar after failure
                binding.progressBar.visibility = View.GONE
                Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

    private fun loadCommunityPosts() {
        // Show progress bar when starting the API call
        binding.progressBar.visibility = View.VISIBLE

        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            // Hide progress bar after failure
            binding.progressBar.visibility = View.GONE
            Toast.makeText(context, "No internet connection", Toast.LENGTH_LONG).show()
            return
        }

        val apiService = ApiConfig.getApiService()
        val call = apiService.getCommunityPosts()

        call.enqueue(object : Callback<List<CommunityPost>> {
            override fun onResponse(call: Call<List<CommunityPost>>, response: Response<List<CommunityPost>>) {
                // Hide progress bar after response
                binding.progressBar.visibility = View.GONE
                
                if (response.isSuccessful) {
                    val posts = response.body()
                    if (posts != null) {
                        adapter.submitList(posts)
                    } else {
                        Toast.makeText(context, "No community posts available", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(context, "Failed to load community posts", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<List<CommunityPost>>, t: retrofit2.Call<List<CommunityPost>>) {
                // Hide progress bar after failure
                binding.progressBar.visibility = View.GONE
                Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}