package com.example.iurankomplek.presentation.ui.fragment

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.R
import com.example.iurankomplek.core.base.BaseFragment
import com.example.iurankomplek.di.DependencyContainer
import com.example.iurankomplek.databinding.FragmentCommunityBinding
import com.example.iurankomplek.presentation.adapter.CommunityPostAdapter
import com.example.iurankomplek.presentation.viewmodel.CommunityPostViewModel
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.model.CommunityPost

class CommunityFragment : BaseFragment<List<CommunityPost>>() {

    private lateinit var binding: FragmentCommunityBinding

    private lateinit var adapter: CommunityPostAdapter
    private lateinit var viewModel: CommunityPostViewModel

    override val recyclerView: RecyclerView
        get() = binding.rvCommunity

    override val progressBar: View
        get() = binding.progressBar

    override val emptyMessageStringRes: Int
        get() = R.string.no_community_posts_available

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: android.os.Bundle?
    ): View {
        binding = FragmentCommunityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun createAdapter(): RecyclerView.Adapter<*> = CommunityPostAdapter().also { adapter = it }
 
    override fun initializeViewModel(viewModelProvider: ViewModelProvider) {
        viewModel = DependencyContainer.provideCommunityPostViewModel()
    }

    override fun observeViewModelState() {
        observeUiState(viewModel.postsState, { data ->
            adapter.submitList(data)
        }, showErrorToast = false)
    }

    override fun loadData() {
        viewModel.loadPosts()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}
