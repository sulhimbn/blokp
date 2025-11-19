package com.example.iurankomplek
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iurankomplek.databinding.ActivityMainBinding
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.utils.NetworkUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: UserAdapter
    private lateinit var binding: ActivityMainBinding
    
    private var retryCount = 0
    private val maxRetries = 3
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adapter = UserAdapter(mutableListOf())
        binding.rvUsers.layoutManager = LinearLayoutManager(this)
        binding.rvUsers.adapter = adapter
        getUser()
    }
    private fun getUser(currentRetryCount: Int = 0) {
        // Check network connectivity before making API call
        if (!NetworkUtils.isNetworkAvailable(this)) {
            if (currentRetryCount == 0) {
                Toast.makeText(this, "No internet connection. Please check your network settings.", Toast.LENGTH_LONG).show()
            }
            return
        }
        
        val apiService = ApiConfig.getApiService()
        val client = apiService.getUsers()
         client.enqueue(object : Callback<UserResponse> {
             override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                  if (response.isSuccessful) {
                      val responseBody = response.body()
                      if (responseBody != null && responseBody.data != null) {
                          val dataArray = responseBody.data
                          if (dataArray.isNotEmpty()) {
                              adapter.setUsers(dataArray)
                          } else {
                              Toast.makeText(this@MainActivity, "No users available", Toast.LENGTH_LONG).show()
                          }
                      } else {
                          Toast.makeText(this@MainActivity, "Invalid response format", Toast.LENGTH_LONG).show()
                      }
                  } else {
                      if (currentRetryCount < maxRetries) {
                          Handler(Looper.getMainLooper()).postDelayed({
                              getUser(currentRetryCount + 1)
                          }, 1000L * (currentRetryCount + 1)) // Exponential backoff
                      } else {
                          Toast.makeText(this@MainActivity, "Failed to retrieve data after ${maxRetries + 1} attempts. Please check your connection and try again.", Toast.LENGTH_LONG).show()
                      }
                  }
             }
            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                if (currentRetryCount < maxRetries) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        getUser(currentRetryCount + 1)
                    }, 1000L * (currentRetryCount + 1)) // Exponential backoff
                } else {
                    Toast.makeText(this@MainActivity, "Network error: ${t.message}. Failed after ${maxRetries + 1} attempts. Please check your connection and try again.", Toast.LENGTH_LONG).show()
                    t.printStackTrace()
                }
            }
        })
    }
}