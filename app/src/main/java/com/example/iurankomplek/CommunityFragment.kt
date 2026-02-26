package com.example.iurankomplek

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iurankomplek.databinding.FragmentCommunityBinding
import com.example.iurankomplek.model.CommunityPost
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.utils.Constants

class CommunityFragment : BaseFragment() {

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
        // Show progress bar when starting the API call
        binding.progressBar.visibility = View.VISIBLE

        val apiService = ApiConfig.getApiService()
        val call = apiService.getCommunityPosts()

        executeWithRetry(
            operation = { call },
            onSuccess = { posts ->
                if (!isAdded) return@executeWithRetry
                binding.progressBar.visibility = View.GONE
                
                if (posts.isNotEmpty()) {
                    adapter.submitList(posts)
                } else {
                    Toast.makeText(requireContext(), getString(R.string.no_community_posts_available), Constants.Toast.DURATION_LONG).show()
                }
            },
            onError = { errorMessage ->
                if (!isAdded) return@executeWithRetry
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), errorMessage, Constants.Toast.DURATION_LONG).show()
            }
        )
    }
}
