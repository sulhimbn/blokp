package com.example.iurankomplek

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iurankomplek.databinding.ActivityMainBinding
import com.example.iurankomplek.ui.component.UserSearchFilterViewModel
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    private lateinit var adapter: UserAdapter
    private lateinit var binding: ActivityMainBinding
    private val viewModel: UserViewModel by viewModels()
    private val searchFilterViewModel: UserSearchFilterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = UserAdapter(mutableListOf())
        binding.rvUsers.layoutManager = LinearLayoutManager(this)
        binding.rvUsers.adapter = adapter

        setupSearchFilter()
        setupSwipeRefresh()
        observeUserState()
        observeFilteredUsers()
        viewModel.loadUsers()
    }

    private fun setupSearchFilter() {
        binding.searchFilterView.apply {
            setOnSearchQueryChanged { query ->
                searchFilterViewModel.setSearchQuery(query)
            }
            setOnSortOptionChanged { option ->
                searchFilterViewModel.setSortOption(option)
            }
            setOnClearFilters {
                searchFilterViewModel.clearFilters()
            }
            observeViewModel(this@MainActivity, searchFilterViewModel)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadUsers()
        }
    }

    private fun observeUserState() {
        lifecycleScope.launch {
            viewModel.usersState.collectLatest { state ->
                when (state) {
                    is UiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.swipeRefreshLayout.isRefreshing = true
                    }
                    is UiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.swipeRefreshLayout.isRefreshing = false
                        state.data.data?.let { users ->
                            if (users.isNotEmpty()) {
                                val validatedUsers = users.mapNotNull { user ->
                                    if (!user.email.isNullOrBlank() &&
                                        (!user.first_name.isNullOrBlank() || !user.last_name.isNullOrBlank())) {
                                        user
                                    } else null
                                }
                                searchFilterViewModel.setItems(validatedUsers)
                            } else {
                                Toast.makeText(this@MainActivity, getString(R.string.no_users_available), Toast.LENGTH_LONG).show()
                            }
                        } ?: run {
                            Toast.makeText(this@MainActivity, getString(R.string.invalid_response_format), Toast.LENGTH_LONG).show()
                        }
                    }
                    is UiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.swipeRefreshLayout.isRefreshing = false
                        Toast.makeText(this@MainActivity, state.error, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun observeFilteredUsers() {
        lifecycleScope.launch {
            searchFilterViewModel.filteredItems.collectLatest { users ->
                adapter.setUsers(users)
                if (users.isEmpty() && searchFilterViewModel.isFilterActive.value) {
                    Toast.makeText(this@MainActivity, getString(R.string.no_results_found), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
