package com.example.iurankomplek

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.model.Message
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.utils.NetworkUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MessagesFragment : Fragment() {

    private lateinit var adapter: MessageAdapter
    private lateinit var rv_messages: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_messages, container, false)

        rv_messages = view.findViewById(R.id.rv_messages)
        adapter = MessageAdapter()
        rv_messages.layoutManager = LinearLayoutManager(context)
        rv_messages.adapter = adapter

        // Using a default user ID for demo purposes
        loadMessages("default_user_id")

        return view
    }

    private fun loadMessages(userId: String) {
        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            Toast.makeText(context, "No internet connection", Toast.LENGTH_LONG).show()
            return
        }

        val apiService = ApiConfig.getApiService()
        val call = apiService.getMessages(userId)

        call.enqueue(object : Callback<List<Message>> {
            override fun onResponse(call: Call<List<Message>>, response: Response<List<Message>>) {
                if (response.isSuccessful) {
                    val messages = response.body()
                    if (messages != null) {
                        adapter.submitList(messages)
                    } else {
                        Toast.makeText(context, "No messages available", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(context, "Failed to load messages", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<List<Message>>, t: retrofit2.Call<List<Message>>) {
                Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}