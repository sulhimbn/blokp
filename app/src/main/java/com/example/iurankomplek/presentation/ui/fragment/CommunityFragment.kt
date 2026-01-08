package com.example.iurankomplek.presentation.ui.fragment

import com.example.iurankomplek.presentation.adapter.CommunityPostAdapter
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
import com.example.iurankomplek.databinding.FragmentCommunityBinding
import com.example.iurankomplek.data.repository.CommunityPostRepositoryFactory
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.presentation.viewmodel.CommunityPostViewModel
import kotlinx.coroutines.launch

class CommunityFragment : Fragment() {

    private lateinit var adapter: CommunityPostAdapter
    private lateinit var binding: FragmentCommunityBinding
    private lateinit var viewModel: CommunityPostViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCommunityBinding.inflate(inflater, container, false)

        adapter = CommunityPostAdapter()
        binding.rvCommunity.layoutManager = LinearLayoutManager(context)
        binding.rvCommunity.adapter = adapter

        initializeViewModel()
        observePostsState()
        viewModel.loadPosts()

        return binding.root
    }

    private fun initializeViewModel() {
        val communityPostRepository = CommunityPostRepositoryFactory.getInstance()
        viewModel = ViewModelProvider(
            this,
            CommunityPostViewModel.Factory(communityPostRepository)
        )[CommunityPostViewModel::class.java]
    }

    private fun observePostsState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.postsState.collect { state ->
                when (state) {
                    is UiState.Idle -> {
                    }
                    is UiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is UiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        if (state.data.isEmpty()) {
                            Toast.makeText(context, getString(R.string.no_community_posts_available), Toast.LENGTH_LONG).show()
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
