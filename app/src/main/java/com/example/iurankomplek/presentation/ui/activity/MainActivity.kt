package com.example.iurankomplek.presentation.ui.activity

import com.example.iurankomplek.core.base.BaseActivity
import com.example.iurankomplek.presentation.adapter.UserAdapter
import com.example.iurankomplek.R
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iurankomplek.databinding.ActivityMainBinding
import com.example.iurankomplek.data.repository.UserRepositoryFactory
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.utils.InputSanitizer
import com.example.iurankomplek.presentation.viewmodel.UserViewModel
import android.content.res.Configuration
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.launch
import com.example.iurankomplek.presentation.ui.helper.RecyclerViewHelper
import com.example.iurankomplek.presentation.ui.helper.SwipeRefreshHelper
import com.example.iurankomplek.presentation.ui.helper.StateManager

class MainActivity : BaseActivity() {
    private lateinit var adapter: UserAdapter
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: UserViewModel
    private lateinit var stateManager: StateManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize ViewModel with use case
        val userRepository = UserRepositoryFactory.getInstance()
        val loadUsersUseCase = com.example.iurankomplek.domain.usecase.LoadUsersUseCase(userRepository)
        viewModel = ViewModelProvider(this, UserViewModel.Factory(loadUsersUseCase))[UserViewModel::class.java]
        
        // Initialize StateManager
        stateManager = StateManager.create(
            progressBar = binding.progressBar,
            emptyStateTextView = binding.emptyStateTextView,
            errorStateLayout = binding.errorStateLayout,
            errorStateTextView = binding.errorStateTextView,
            retryTextView = binding.retryTextView,
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
        
        val columnCount = when {
            isTablet && isLandscape -> 3
            isTablet -> 2
            isLandscape -> 2
            else -> 1
        }
        
        binding.rvUsers.layoutManager = when (columnCount) {
            1 -> LinearLayoutManager(this)
            else -> GridLayoutManager(this, columnCount)
        }
        
        binding.rvUsers.setHasFixedSize(true)
        binding.rvUsers.setItemViewCacheSize(20)
        binding.rvUsers.focusable = true
        binding.rvUsers.focusableInTouchMode = true
        binding.rvUsers.adapter = adapter
        setupRecyclerViewKeyboardNavigation()
        
        setupSwipeRefresh()
         observeUserState()
         viewModel.loadUsers()
     }
     
      private fun setupSwipeRefresh() {
          binding.swipeRefreshLayout.setOnRefreshListener {
              viewModel.loadUsers()
          }
      }

      private fun announceForAccessibility(text: String) {
          binding.swipeRefreshLayout.announceForAccessibility(text)
      }

       private fun setupRecyclerViewKeyboardNavigation() {
           binding.rvUsers.setOnKeyListener { _, keyCode, event ->
               if (event.action == android.view.KeyEvent.ACTION_DOWN) {
                   val layoutManager = binding.rvUsers.layoutManager
                   val spanCount = (layoutManager as? GridLayoutManager)?.spanCount ?: 1
                   
                   when (keyCode) {
                       android.view.KeyEvent.KEYCODE_DPAD_DOWN,
                       android.view.KeyEvent.KEYCODE_DPAD_UP,
                       android.view.KeyEvent.KEYCODE_DPAD_LEFT,
                       android.view.KeyEvent.KEYCODE_DPAD_RIGHT -> {
                           
                           when (layoutManager) {
                               is LinearLayoutManager -> {
                                   val firstVisible = layoutManager.findFirstVisibleItemPosition()
                                   val lastVisible = layoutManager.findLastVisibleItemPosition()
                                   
                                   when (keyCode) {
                                       android.view.KeyEvent.KEYCODE_DPAD_DOWN -> {
                                           if (lastVisible < adapter.itemCount - 1) {
                                               binding.rvUsers.smoothScrollToPosition(lastVisible + 1)
                                           }
                                       }
                                       android.view.KeyEvent.KEYCODE_DPAD_UP -> {
                                           if (firstVisible > 0) {
                                               binding.rvUsers.smoothScrollToPosition(firstVisible - 1)
                                           }
                                       }
                                       android.view.KeyEvent.KEYCODE_DPAD_LEFT,
                                       android.view.KeyEvent.KEYCODE_DPAD_RIGHT -> {
                                           // Horizontal navigation not applicable in single column
                                       }
                                   }
                               }
                               is GridLayoutManager -> {
                                   val firstVisible = layoutManager.findFirstVisibleItemPosition()
                                   val lastVisible = layoutManager.findLastVisibleItemPosition()
                                   
                                   when (keyCode) {
                                       android.view.KeyEvent.KEYCODE_DPAD_DOWN -> {
                                           val targetPosition = lastVisible + spanCount
                                           if (targetPosition < adapter.itemCount) {
                                               binding.rvUsers.smoothScrollToPosition(targetPosition)
                                           }
                                       }
                                       android.view.KeyEvent.KEYCODE_DPAD_UP -> {
                                           val targetPosition = firstVisible - spanCount
                                           if (targetPosition >= 0) {
                                               binding.rvUsers.smoothScrollToPosition(targetPosition)
                                           }
                                       }
                                       android.view.KeyEvent.KEYCODE_DPAD_RIGHT -> {
                                           if (lastVisible < adapter.itemCount - 1) {
                                               binding.rvUsers.smoothScrollToPosition(lastVisible + 1)
                                           }
                                       }
                                       android.view.KeyEvent.KEYCODE_DPAD_LEFT -> {
                                           if (firstVisible > 0) {
                                               binding.rvUsers.smoothScrollToPosition(firstVisible - 1)
                                           }
                                       }
                                   }
                               }
                           }
                       }
                   }
               }
               false
           }
       }
     

}