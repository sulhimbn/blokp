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
import com.example.iurankomplek.utils.NetworkUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : BaseActivity() {
    private lateinit var adapter: UserAdapter
    private lateinit var rv_users: RecyclerView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rv_users = findViewById(R.id.rv_users)
        adapter = UserAdapter(mutableListOf())
        rv_users.layoutManager = LinearLayoutManager(this)
        rv_users.adapter = adapter
        getUser()
    }
    
    private fun getUser() {
        executeWithRetry(
            operation = { ApiConfig.getApiService().getUsers() },
            onSuccess = { response ->
                response.data?.let { users ->
                    if (users.isNotEmpty()) {
                        adapter.setUsers(users)
                    } else {
                        Toast.makeText(this, "No users available", Toast.LENGTH_LONG).show()
                    }
                } ?: run {
                    Toast.makeText(this, "Invalid response format", Toast.LENGTH_LONG).show()
                }
            },
            onError = { error ->
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            }
        )
    }
}