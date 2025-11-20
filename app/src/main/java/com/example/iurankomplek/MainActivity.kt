package com.example.iurankomplek

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.lifecycleScope
import com.example.iurankomplek.databinding.ActivityMainBinding
import com.example.iurankomplek.data.repository.UserRepositoryImpl
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.utils.DataValidator
import com.example.iurankomplek.utils.NetworkUtils
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.viewmodel.UserViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: UserAdapter
    private lateinit var binding: ActivityMainBinding
    private lateinit var userViewModel: UserViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize repository and view model
        val apiService = ApiConfig.getApiService()
        val userRepository = UserRepositoryImpl(apiService)
        userViewModel = UserViewModel(userRepository)
        
        adapter = UserAdapter(mutableListOf())
        binding.rvUsers.layoutManager = LinearLayoutManager(this)
        binding.rvUsers.adapter = adapter
        
        // Observe ViewModel state
        observeUserState()
        
        // Load users
        userViewModel.loadUsers()
    }
    
    private fun observeUserState() {
        lifecycleScope.launch {
            userViewModel.usersState.collect { state ->
                when (state) {
                    is UiState.Loading -> {
                        // Show loading indicator if needed
                    }
                    is UiState.Success -> {
                        val responseBody = state.data
                        if (responseBody.data != null) {
                            val dataArray = responseBody.data
                            if (dataArray.isNotEmpty()) {
                                // Validate data before setting to adapter
                                val validatedData = dataArray.map { user ->
                                    // Basic validation to prevent null or invalid data
                                    if (user.email.isBlank() || !user.email.contains("@")) {
                                        Toast.makeText(this@MainActivity, "Invalid user data detected", Toast.LENGTH_LONG).show()
                                    }
                                    user
                                }
                                adapter.setUsers(validatedData)
                            } else {
                                Toast.makeText(this@MainActivity, "No users available", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(this@MainActivity, "Invalid response format", Toast.LENGTH_LONG).show()
                        }
                    }
                    is UiState.Error -> {
                        Toast.makeText(this@MainActivity, "Error: ${state.error}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}