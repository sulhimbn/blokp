package com.example.iurankomplek.presentation.ui.activity

import com.example.iurankomplek.core.base.BaseActivity
import com.example.iurankomplek.presentation.adapter.UserAdapter
import com.example.iurankomplek.R
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.iurankomplek.databinding.ActivityMainBinding
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.utils.InputSanitizer
import com.example.iurankomplek.presentation.viewmodel.UserViewModel
import android.content.res.Configuration
import kotlinx.coroutines.launch
import com.example.iurankomplek.presentation.ui.helper.RecyclerViewHelper
import com.example.iurankomplek.presentation.ui.helper.SwipeRefreshHelper
import com.example.iurankomplek.presentation.ui.helper.StateManager
import com.example.iurankomplek.di.DependencyContainer

class MainActivity : BaseActivity() {
    private lateinit var adapter: UserAdapter
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: UserViewModel
    private lateinit var stateManager: StateManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize ViewModel with use case from DI container
        viewModel = DependencyContainer.provideUserViewModel()
        
        // Initialize StateManager
        stateManager = StateManager.create(
            progressBar = binding.stateManagementInclude?.progressBar,
            emptyStateTextView = binding.stateManagementInclude?.emptyStateTextView,
            errorStateLayout = binding.stateManagementInclude?.errorStateLayout,
            errorStateTextView = binding.stateManagementInclude?.errorStateTextView,
            retryTextView = binding.stateManagementInclude?.retryTextView,
            recyclerView = binding.rvUsers,
            scope = lifecycleScope,
            context = this
        )
        
        // Initialize adapter
        adapter = UserAdapter()
        
        // Configure RecyclerView with helper (responsive layout, keyboard nav)
        RecyclerViewHelper.configureRecyclerView(
            recyclerView = binding.rvUsers,
            itemCount = 20,
            enableKeyboardNav = true,
            adapter = adapter,
            orientation = resources.configuration.orientation,
            screenWidthDp = resources.configuration.screenWidthDp
        )
        
        // Configure SwipeRefresh with helper
        SwipeRefreshHelper.configureSwipeRefresh(binding.swipeRefreshLayout) {
            viewModel.loadUsers()
        }
        
        // Observe state
        observeUserState()
        
        // Load initial data
        viewModel.loadUsers()
     }
     
     private fun observeUserState() {
         stateManager.observeState(viewModel.usersState, onSuccess = { data ->
             SwipeRefreshHelper.announceRefreshComplete(binding.swipeRefreshLayout, this)
             
             data.data.let { users ->
                 if (users.isNotEmpty()) {
                     // Validate required fields to prevent displaying invalid data
                     val validatedUsers = users.filter { user ->
                         user.email.isNotBlank() &&
                         (user.first_name.isNotBlank() || user.last_name.isNotBlank())
                     }
                     
                     if (validatedUsers.isNotEmpty()) {
                         stateManager.showSuccess()
                         adapter.submitList(validatedUsers)
                     } else {
                         stateManager.showEmpty()
                     }
                 } else {
                     stateManager.showEmpty()
                 }
             } ?: run {
                  stateManager.showError(
                      errorMessage = getString(R.string.invalid_response_format),
                      onRetry = { viewModel.loadUsers() }
                  )
              }
           })
       }
    }