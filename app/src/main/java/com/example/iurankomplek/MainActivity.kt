package com.example.iurankomplek

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iurankomplek.databinding.ActivityMainBinding
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    private lateinit var adapter: UserAdapter
    private lateinit var binding: ActivityMainBinding
    private val viewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = UserAdapter(mutableListOf())
        binding.rvUsers.layoutManager = LinearLayoutManager(this)
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
                                    if (user.email.isNotBlank() &&
                                        (user.first_name.isNotBlank() || user.last_name.isNotBlank())) {
                                        user
                                    } else null
                                }
                                adapter.setUsers(validatedUsers)
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
}
