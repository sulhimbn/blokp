package com.example.iurankomplek.presentation.ui.fragment

import com.example.iurankomplek.presentation.adapter.MessageAdapter
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
import com.example.iurankomplek.databinding.FragmentMessagesBinding
import com.example.iurankomplek.data.repository.MessageRepositoryFactory
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.viewmodel.MessageViewModel
import kotlinx.coroutines.launch

class MessagesFragment : Fragment() {

    private lateinit var adapter: MessageAdapter
    private lateinit var binding: FragmentMessagesBinding
    private lateinit var viewModel: MessageViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessagesBinding.inflate(inflater, container, false)

        adapter = MessageAdapter()
        binding.rvMessages.layoutManager = LinearLayoutManager(context)
        binding.rvMessages.adapter = adapter

        initializeViewModel()
        observeMessagesState()
        viewModel.loadMessages("default_user_id")

        return binding.root
    }

    private fun initializeViewModel() {
        val messageRepository = MessageRepositoryFactory.getInstance()
        viewModel = ViewModelProvider(
            this,
            MessageViewModel.Factory(messageRepository)
        )[MessageViewModel::class.java]
    }

    private fun observeMessagesState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.messagesState.collect { state ->
                when (state) {
                    is UiState.Idle -> {
                    }
                    is UiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is UiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        if (state.data.isEmpty()) {
                            Toast.makeText(context, getString(R.string.no_messages_available), Toast.LENGTH_LONG).show()
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
