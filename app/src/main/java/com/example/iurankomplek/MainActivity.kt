package com.example.iurankomplek

import android.os.Bundle
<<<<<<< HEAD
import android.view.View
import android.widget.ProgressBar
=======
>>>>>>> origin/main
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iurankomplek.databinding.ActivityMainBinding
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.utils.DataValidator
<<<<<<< HEAD
import retrofit2.Response
=======
>>>>>>> origin/main

class MainActivity : BaseActivity() {
    private lateinit var adapter: UserAdapter
    private lateinit var binding: ActivityMainBinding
<<<<<<< HEAD
    private lateinit var progressBar: ProgressBar
=======
>>>>>>> origin/main
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
<<<<<<< HEAD
        // Find the progress bar from the layout
        progressBar = findViewById(R.id.progressBar)
        
=======
>>>>>>> origin/main
        adapter = UserAdapter(mutableListOf())
        binding.rvUsers.layoutManager = LinearLayoutManager(this)
        binding.rvUsers.adapter = adapter
        
<<<<<<< HEAD
        // Load users with retry logic from BaseActivity and progress indicator
=======
        // Load users with retry logic from BaseActivity
>>>>>>> origin/main
        getUser()
    }
    
    private fun getUser() {
        executeWithRetry(
            operation = { ApiConfig.getApiService().getUsers() },
            onSuccess = { response ->
<<<<<<< HEAD
                // Hide progress bar after successful response
                progressBar.visibility = View.GONE
                
=======
>>>>>>> origin/main
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
<<<<<<< HEAD
                // Hide progress bar after final failure
                progressBar.visibility = View.GONE
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            }
        )
        
        // Show progress bar when starting the API call
        progressBar.visibility = View.VISIBLE
=======
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            }
        )
>>>>>>> origin/main
    }
}