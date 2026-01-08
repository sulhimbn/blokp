package com.example.iurankomplek.presentation.ui.activity

import com.example.iurankomplek.core.base.BaseActivity
import com.example.iurankomplek.presentation.adapter.UserAdapter
import com.example.iurankomplek.R
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iurankomplek.databinding.ActivityMainBinding
import com.example.iurankomplek.data.repository.UserRepositoryFactory
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.utils.InputSanitizer
import com.example.iurankomplek.presentation.viewmodel.UserViewModel
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {
    private lateinit var adapter: UserAdapter
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: UserViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize ViewModel with use case
        val userRepository = UserRepositoryFactory.getInstance()
        val loadUsersUseCase = com.example.iurankomplek.domain.usecase.LoadUsersUseCase(userRepository)
        viewModel = ViewModelProvider(this, UserViewModel.Factory(loadUsersUseCase))[UserViewModel::class.java]
        
        adapter = UserAdapter()
        binding.rvUsers.layoutManager = LinearLayoutManager(this)
        binding.rvUsers.setHasFixedSize(true)
        binding.rvUsers.setItemViewCacheSize(20)
        binding.rvUsers.adapter = adapter
        
        setupSwipeRefresh()
         observeUserState()
         viewModel.loadUsers()
     }
     
     private fun setupSwipeRefresh() {
         binding.swipeRefreshLayout.setOnRefreshListener {
             viewModel.loadUsers()
         }
     }
     
      private fun observeUserState() {
         lifecycleScope.launch {
             viewModel.usersState.collect { state ->
                 when (state) {
                     is UiState.Idle -> {
                     }
                      is UiState.Loading -> {
                          binding.progressBar.visibility = View.VISIBLE
                          binding.emptyStateTextView.visibility = View.GONE
                          binding.errorStateLayout.visibility = View.GONE
                          binding.swipeRefreshLayout.isRefreshing = true
                      }
                        is UiState.Success -> {
                             binding.progressBar.visibility = View.GONE
                             binding.swipeRefreshLayout.isRefreshing = false
                             state.data.data.let { users ->
                                if (users.isNotEmpty()) {
                                    binding.rvUsers.visibility = View.VISIBLE
                                    binding.emptyStateTextView.visibility = View.GONE
                                    binding.errorStateLayout.visibility = View.GONE

                                    val validatedUsers = users.filter { user ->
                                        // Validate required fields to prevent displaying invalid data (NO object allocation)
                                        user.email.isNotBlank() &&
                                        (user.first_name.isNotBlank() || user.last_name.isNotBlank())
                                    }
                                     adapter.submitList(validatedUsers)
                                } else {
                                    binding.rvUsers.visibility = View.GONE
                                    binding.progressBar.visibility = View.GONE
                                    binding.emptyStateTextView.visibility = View.VISIBLE
                                    binding.errorStateLayout.visibility = View.GONE
                                }
                            } ?: run {
                                binding.rvUsers.visibility = View.GONE
                                binding.progressBar.visibility = View.GONE
                                binding.emptyStateTextView.visibility = View.GONE
                                binding.errorStateLayout.visibility = View.VISIBLE
                                binding.errorStateTextView.text = getString(R.string.invalid_response_format)
                                binding.retryTextView.setOnClickListener { viewModel.loadUsers() }
                            }
                       }
                       is UiState.Error -> {
                           binding.rvUsers.visibility = View.GONE
                           binding.progressBar.visibility = View.GONE
                           binding.emptyStateTextView.visibility = View.GONE
                           binding.errorStateLayout.visibility = View.VISIBLE
                           binding.errorStateTextView.text = state.error
                           binding.swipeRefreshLayout.isRefreshing = false
                           binding.retryTextView.setOnClickListener { viewModel.loadUsers() }
                       }
                  }
             }
         }
     }
}