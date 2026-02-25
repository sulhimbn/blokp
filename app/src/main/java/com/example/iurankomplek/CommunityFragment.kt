package com.example.iurankomplek

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iurankomplek.databinding.FragmentCommunityBinding
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.viewmodel.CommunityViewModel

class CommunityFragment : Fragment() {

    private lateinit var adapter: CommunityPostAdapter
    private lateinit var binding: FragmentCommunityBinding
    private lateinit var communityViewModel: CommunityViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCommunityBinding.inflate(inflater, container, false)

        adapter = CommunityPostAdapter()
        binding.rvCommunity.layoutManager = LinearLayoutManager(context)
        binding.rvCommunity.adapter = adapter

        // Initialize ViewModel
        communityViewModel = ViewModelProvider(
            this,
            CommunityViewModel.Factory(ApiConfig.getApiService())
        )[CommunityViewModel::class.java]

        // Observe state with viewLifecycleOwner
        observeCommunityState()

        // Load data
        communityViewModel.loadCommunityPosts()

        return binding.root
    }

    private fun observeCommunityState() {
        communityViewModel.communityState.observe(viewLifecycleOwner) { state ->
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
