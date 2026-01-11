package com.example.iurankomplek.presentation.ui.fragment

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.R
import com.example.iurankomplek.core.base.BaseFragment
import com.example.iurankomplek.di.DependencyContainer
import com.example.iurankomplek.databinding.FragmentAnnouncementsBinding
import com.example.iurankomplek.presentation.adapter.AnnouncementAdapter
import com.example.iurankomplek.presentation.viewmodel.AnnouncementViewModel
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.model.Announcement

class AnnouncementsFragment : BaseFragment<List<Announcement>>() {

    private lateinit var binding: FragmentAnnouncementsBinding

    private lateinit var adapter: AnnouncementAdapter
    private lateinit var viewModel: AnnouncementViewModel

    override val recyclerView: RecyclerView
        get() = binding.rvAnnouncements

    override val progressBar: View
        get() = binding.progressBar

    override val emptyMessageStringRes: Int
        get() = R.string.no_announcements_available

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: android.os.Bundle?
    ): View {
        binding = FragmentAnnouncementsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun createAdapter(): RecyclerView.Adapter<*> = AnnouncementAdapter().also { adapter = it }

    override fun initializeViewModel(viewModelProvider: ViewModelProvider) {
        val factory = com.example.iurankomplek.di.DependencyContainer.provideAnnouncementViewModel()
        viewModel = ViewModelProvider(this, factory)[AnnouncementViewModel::class.java]
    }

    override fun observeViewModelState() {
        observeUiState(viewModel.announcementsState, { data ->
            adapter.submitList(data)
        }, showErrorToast = false)
    }

    override fun loadData() {
        viewModel.loadAnnouncements()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}
