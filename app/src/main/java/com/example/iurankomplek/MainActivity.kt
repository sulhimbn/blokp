package com.example.iurankomplek
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.utils.DataValidator
import com.example.iurankomplek.utils.NetworkUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: UserAdapter
    private lateinit var rv_users: RecyclerView
    
    private var retryCount = 0
    private val maxRetries = 3
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rv_users = findViewById(R.id.rv_users)
        adapter = UserAdapter(mutableListOf())
        rv_users.layoutManager = LinearLayoutManager(this)
        rv_users.adapter = adapter
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
                           // Validate the data array before passing to adapter to prevent potential security issues
                           val validatedData = dataArray.map { user ->
                               // Ensure required fields are properly formatted to prevent injection attacks
                               user.copy(
                                   first_name = DataValidator.sanitizeName(user.first_name),
                                   last_name = DataValidator.sanitizeName(user.last_name),
                                   email = DataValidator.sanitizeEmail(user.email),
                                   alamat = DataValidator.sanitizeAddress(user.alamat),
                                   avatar = if (DataValidator.isValidUrl(user.avatar)) user.avatar else ""
                               )
                           }
                           
                           if (validatedData.isNotEmpty()) {
                               adapter.setUsers(validatedData)
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