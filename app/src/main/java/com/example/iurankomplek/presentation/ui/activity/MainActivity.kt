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
        
        // Initialize ViewModel with repository
        val userRepository = UserRepositoryFactory.getInstance()
        viewModel = ViewModelProvider(this, UserViewModel.Factory(userRepository))[UserViewModel::class.java]
        
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

                                    val validatedUsers = users.mapNotNull { user ->
                                        // Validate required fields to prevent displaying invalid data
                                        if (user.email.isNotBlank() &&
                                            (user.first_name.isNotBlank() || user.last_name.isNotBlank())) {
                                            com.example.iurankomplek.model.DataItem(
                                                first_name = user.first_name,
                                                last_name = user.last_name,
                                                email = user.email,
                                                alamat = user.alamat,
                                                iuran_perwarga = user.iuran_perwarga,
                                                total_iuran_rekap = user.total_iuran_rekap,
                                                jumlah_iuran_bulanan = user.jumlah_iuran_bulanan,
                                                total_iuran_individu = user.total_iuran_individu,
                                                pengeluaran_iuran_warga = user.pengeluaran_iuran_warga,
                                                pemanfaatan_iuran = user.pemanfaatan_iuran,
                                                avatar = user.avatar
                                            )
                                        } else null
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