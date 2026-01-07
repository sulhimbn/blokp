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
        binding.progressBar.visibility = View.VISIBLE

        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            binding.progressBar.visibility = View.GONE
            Toast.makeText(context, "No internet connection", Toast.LENGTH_LONG).show()
            return
        }

        val apiService = ApiConfig.getApiService()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = apiService.getCommunityPosts()
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
                binding.progressBar.visibility = View.GONE
                Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
