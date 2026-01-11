package com.example.iurankomplek.presentation.ui.fragment

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.R
import com.example.iurankomplek.core.base.BaseFragment
import com.example.iurankomplek.di.DependencyContainer
import com.example.iurankomplek.databinding.FragmentMessagesBinding
import com.example.iurankomplek.model.Message
import com.example.iurankomplek.presentation.adapter.MessageAdapter
import com.example.iurankomplek.presentation.viewmodel.MessageViewModel
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.utils.Constants

class MessagesFragment : BaseFragment<List<Message>>() {

    private lateinit var binding: FragmentMessagesBinding

    private lateinit var adapter: MessageAdapter
    private lateinit var viewModel: MessageViewModel

    override val recyclerView: RecyclerView
        get() = binding.rvMessages

    override val progressBar: View
        get() = binding.progressBar

    override val emptyMessageStringRes: Int
        get() = R.string.no_messages_available

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: android.os.Bundle?
    ): View {
        binding = FragmentMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun createAdapter(): RecyclerView.Adapter<*> = MessageAdapter().also { adapter = it }
 
    override fun initializeViewModel(viewModelProvider: ViewModelProvider) {
        val factory = DependencyContainer.provideMessageViewModel()
        viewModel = ViewModelProvider(this, factory)[MessageViewModel::class.java]
    }

    override fun observeViewModelState() {
        observeUiState(viewModel.messagesState, { data ->
            adapter.submitList(data)
        }, showErrorToast = false)
    }

    override fun loadData() {
        viewModel.loadMessages(Constants.Api.DEFAULT_USER_ID)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}
