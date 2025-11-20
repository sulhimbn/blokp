package com.example.iurankomplek
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.utils.NetworkUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: UserAdapter
    private lateinit var rv_users: RecyclerView
    private lateinit var progressBar: ProgressBar
    
    private var retryCount = 0
    private val maxRetries = 3
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rv_users = findViewById(R.id.rv_users)
        progressBar = findViewById(R.id.progressBar)
        adapter = UserAdapter(mutableListOf())
        rv_users.layoutManager = LinearLayoutManager(this)
        rv_users.adapter = adapter
        getUser()
    }
    private fun getUser(currentRetryCount: Int = 0) {
        // Show progress bar when starting the API call (only on the first attempt)
        if (currentRetryCount == 0) {
            progressBar.visibility = android.view.View.VISIBLE
        }
        
        // Check network connectivity before making API call
        if (!NetworkUtils.isNetworkAvailable(this)) {
            if (currentRetryCount == 0) {
                Toast.makeText(this, "No internet connection. Please check your network settings.", Toast.LENGTH_LONG).show()
                progressBar.visibility = android.view.View.GONE
            }
            return
        }
        
        val apiService = ApiConfig.getApiService()
         val client = apiService.getUsers()
         client.enqueue(object : Callback<UserResponse> {
             override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                 if (response.isSuccessful) {
                     val dataArray = response.body()?.data

                     if (dataArray != null) {
                         adapter.setUsers(dataArray)
                     } else {
                         Toast.makeText(this@MainActivity, "No data available", Toast.LENGTH_LONG).show()
                     }
                 } else {
                     if (currentRetryCount < maxRetries) {
                         Handler(Looper.getMainLooper()).postDelayed({
                             getUser(currentRetryCount + 1)
                         }, 1000L * (currentRetryCount + 1)) // Exponential backoff
                         return // Don't hide progress bar if retrying
                     } else {
                         Toast.makeText(this@MainActivity, "Failed to retrieve data after ${maxRetries + 1} attempts. Please check your connection and try again.", Toast.LENGTH_LONG).show()
                     }
                 }
                 // Hide progress bar after successful response or final failure
                 progressBar.visibility = android.view.View.GONE
             }
             override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                if (currentRetryCount < maxRetries) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        getUser(currentRetryCount + 1)
                    }, 1000L * (currentRetryCount + 1)) // Exponential backoff
                    return // Don't hide progress bar if retrying
                } else {
                    Toast.makeText(this@MainActivity, "Network error: ${t.message}. Failed after ${maxRetries + 1} attempts. Please check your connection and try again.", Toast.LENGTH_LONG).show()
                    t.printStackTrace()
                }
                // Hide progress bar after final failure
                progressBar.visibility = android.view.View.GONE
            }
        })
    }
}