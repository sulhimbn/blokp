package com.example.iurankomplek

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iurankomplek.databinding.ActivityMainBinding
import com.example.iurankomplek.data.repository.UserRepositoryFactory
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
        val userRepository = UserRepositoryFactory.getInstance()
        viewModel = ViewModelProvider(this, UserViewModel.Factory(userRepository))[UserViewModel::class.java]
        
        adapter = UserAdapter()
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