package com.example.iurankomplek

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iurankomplek.databinding.ActivityMainBinding
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.utils.DataValidator
import retrofit2.Response

class MainActivity : BaseActivity() {
    private lateinit var adapter: UserAdapter
    private lateinit var binding: ActivityMainBinding
    private lateinit var progressBar: ProgressBar
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Find the progress bar from the layout
        progressBar = findViewById(R.id.progressBar)
        
        adapter = UserAdapter(mutableListOf())
        binding.rvUsers.layoutManager = LinearLayoutManager(this)
        binding.rvUsers.adapter = adapter
        
        // Load users with retry logic from BaseActivity and progress indicator
        getUser()
    }
    
    private fun getUser() {
        executeWithRetry(
            operation = { ApiConfig.getApiService().getUsers() },
            onSuccess = { response ->
                // Hide progress bar after successful response
                progressBar.visibility = View.GONE
                
                response.data?.let { users ->
                    if (users.isNotEmpty()) {
                        // Validate the data array before passing to adapter to prevent potential security issues
                        val validatedData = users.map { user ->
                            // Ensure required fields are properly formatted to prevent injection attacks
                            user.copy(
                                first_name = DataValidator.sanitizeName(user.first_name),
                                last_name = DataValidator.sanitizeName(user.last_name),
                                email = DataValidator.sanitizeEmail(user.email),
                                alamat = DataValidator.sanitizeAddress(user.alamat),
                                avatar = if (DataValidator.isValidUrl(user.avatar)) user.avatar else ""
                            )
                        }
                        adapter.setUsers(validatedData)
                    } else {
                        Toast.makeText(this, "No users available", Toast.LENGTH_LONG).show()
                    }
                } ?: run {
                    Toast.makeText(this, "Invalid response format", Toast.LENGTH_LONG).show()
                }
            },
            onError = { error ->
                // Hide progress bar after final failure
                progressBar.visibility = View.GONE
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            }
        )
        
        // Show progress bar when starting the API call
        progressBar.visibility = View.VISIBLE
    }
}