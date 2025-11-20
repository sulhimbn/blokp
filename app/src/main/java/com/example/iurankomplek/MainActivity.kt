package com.example.iurankomplek

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iurankomplek.databinding.ActivityMainBinding
import com.example.iurankomplek.data.repository.UserRepositoryImpl
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.utils.DataValidator
import com.example.iurankomplek.viewmodel.UserViewModel
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
        val userRepository = UserRepositoryImpl(ApiConfig.getApiService())
        viewModel = ViewModelProvider(this, UserViewModel.Factory(userRepository))[UserViewModel::class.java]
        
        adapter = UserAdapter(mutableListOf())
        binding.rvUsers.layoutManager = LinearLayoutManager(this)
        binding.rvUsers.adapter = adapter
        
        setupSwipeRefresh()
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
                                // Validate the data array before passing to adapter to prevent potential security issues
                                val validatedData = users.map { user ->
                                    // Use ValidatedDataItem for enhanced validation
                                    com.example.iurankomplek.model.ValidatedDataItem.fromDataItem(user)
                                }
                                // Convert back to regular DataItem for the adapter
                                val validatedUsers = validatedData.map { item ->
                                    com.example.iurankomplek.model.DataItem(
                                        first_name = item.first_name,
                                        last_name = item.last_name,
                                        email = item.email,
                                        alamat = item.alamat,
                                        iuran_perwarga = item.iuran_perwarga,
                                        total_iuran_rekap = item.total_iuran_rekap,
                                        jumlah_iuran_bulanan = item.jumlah_iuran_bulanan,
                                        total_iuran_individu = item.total_iuran_individu,
                                        pengeluaran_iuran_warga = item.pengeluaran_iuran_warga,
                                        pemanfaatan_iuran = item.pemanfaatan_iuran,
                                        avatar = item.avatar
                                    )
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