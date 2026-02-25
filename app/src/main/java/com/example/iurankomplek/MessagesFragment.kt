package com.example.iurankomplek

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iurankomplek.databinding.FragmentMessagesBinding
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.viewmodel.MessagesViewModel

class MessagesFragment : Fragment() {

    private lateinit var adapter: MessageAdapter
    private lateinit var binding: FragmentMessagesBinding
    private lateinit var messagesViewModel: MessagesViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessagesBinding.inflate(inflater, container, false)

        adapter = MessageAdapter()
        binding.rvMessages.layoutManager = LinearLayoutManager(context)
        binding.rvMessages.adapter = adapter

        // Initialize ViewModel
        messagesViewModel = ViewModelProvider(
            this,
            MessagesViewModel.Factory(ApiConfig.getApiService())
        )[MessagesViewModel::class.java]

        // Observe state with viewLifecycleOwner
        observeMessagesState()

        // Using a default user ID for demo purposes
        messagesViewModel.loadMessages("default_user_id")

        return binding.root
    }

    private fun observeMessagesState() {
        messagesViewModel.messagesState.observe(viewLifecycleOwner) { state ->
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
