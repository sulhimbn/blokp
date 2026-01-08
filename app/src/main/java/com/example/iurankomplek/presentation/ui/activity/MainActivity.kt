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
        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            val gridLayoutManager = GridLayoutManager(this, 2)
            binding.rvUsers.layoutManager = gridLayoutManager
        } else {
            binding.rvUsers.layoutManager = LinearLayoutManager(this)
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
                  when (keyCode) {
                      android.view.KeyEvent.KEYCODE_DPAD_DOWN,
                      android.view.KeyEvent.KEYCODE_DPAD_UP,
                      android.view.KeyEvent.KEYCODE_DPAD_LEFT,
                      android.view.KeyEvent.KEYCODE_DPAD_RIGHT -> {
                          val layoutManager = binding.rvUsers.layoutManager
                          when (layoutManager) {
                              is LinearLayoutManager -> {
                                  val firstVisible = layoutManager.findFirstVisibleItemPosition()
                                  val lastVisible = layoutManager.findLastVisibleItemPosition()
                                  if (keyCode == android.view.KeyEvent.KEYCODE_DPAD_DOWN && lastVisible < adapter.itemCount - 1) {
                                      binding.rvUsers.smoothScrollToPosition(lastVisible + 1)
                                  } else if (keyCode == android.view.KeyEvent.KEYCODE_DPAD_UP && firstVisible > 0) {
                                      binding.rvUsers.smoothScrollToPosition(firstVisible - 1)
                                  }
                              }
                              is GridLayoutManager -> {
                                  val firstVisible = layoutManager.findFirstVisibleItemPosition()
                                  val lastVisible = layoutManager.findLastVisibleItemPosition()
                                  val spanCount = layoutManager.spanCount
                                  when (keyCode) {
                                      android.view.KeyEvent.KEYCODE_DPAD_DOWN -> {
                                          if (lastVisible < adapter.itemCount - 1) {
                                              binding.rvUsers.smoothScrollToPosition(lastVisible + spanCount)
                                          }
                                      }
                                      android.view.KeyEvent.KEYCODE_DPAD_UP -> {
                                          if (firstVisible >= spanCount) {
                                              binding.rvUsers.smoothScrollToPosition(firstVisible - spanCount)
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
                              announceForAccessibility(getString(R.string.swipe_refresh_complete))
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