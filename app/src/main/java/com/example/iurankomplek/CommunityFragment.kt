package com.example.iurankomplek

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iurankomplek.databinding.FragmentCommunityBinding
import com.example.iurankomplek.model.CommunityPost
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.utils.NetworkUtils
import com.example.iurankomplek.utils.Constants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
        // Show progress bar when starting the API call
        binding.progressBar.visibility = View.VISIBLE

        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            // Hide progress bar after failure
            binding.progressBar.visibility = View.GONE
            Toast.makeText(requireContext(), getString(R.string.no_internet_connection), Constants.Toast.DURATION_LONG).show()
            return
        }

        val apiService = ApiConfig.getApiService()
        val call = apiService.getCommunityPosts()

        call.enqueue(object : Callback<List<CommunityPost>> {
            override fun onResponse(call: Call<List<CommunityPost>>, response: Response<List<CommunityPost>>) {
                // Hide progress bar after response - check if fragment is still attached
                if (!isAdded) return
                binding.progressBar.visibility = View.GONE
                
                if (response.isSuccessful) {
                    val posts = response.body()
                    if (posts != null) {
                        adapter.submitList(posts)
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.no_community_posts_available), Constants.Toast.DURATION_LONG).show()
                    }
                } else {
                    Toast.makeText(requireContext(), getString(R.string.failed_to_load_community_posts), Constants.Toast.DURATION_LONG).show()
                }
            }

            override fun onFailure(call: Call<List<CommunityPost>>, t: retrofit2.Call<List<CommunityPost>>) {
                // Hide progress bar after failure - check if fragment is still attached
                if (!isAdded) return
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), getString(R.string.network_error_community_posts, t.message), Constants.Toast.DURATION_LONG).show()
            }
        })
    }
}
