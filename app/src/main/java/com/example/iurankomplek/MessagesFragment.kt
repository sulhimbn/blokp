package com.example.iurankomplek
import com.example.iurankomplek.utils.Constants

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iurankomplek.databinding.FragmentMessagesBinding
import com.example.iurankomplek.model.Message
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.utils.NetworkUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MessagesFragment : Fragment() {

    private lateinit var adapter: MessageAdapter
    private lateinit var binding: FragmentMessagesBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessagesBinding.inflate(inflater, container, false)

        adapter = MessageAdapter()
        binding.rvMessages.layoutManager = LinearLayoutManager(context)
        binding.rvMessages.adapter = adapter

        // Using a default user ID for demo purposes
        loadMessages("default_user_id")

        return binding.root
    }

    private fun loadMessages(userId: String) {
        // Show progress bar when starting the API call
        binding.progressBar.visibility = View.VISIBLE

        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            // Hide progress bar after failure
            binding.progressBar.visibility = View.GONE
            Toast.makeText(requireContext(), getString(R.string.no_internet_connection), Constants.Toast.DURATION_LONG).show()
            return
        }

        val apiService = ApiConfig.getApiService()
        val call = apiService.getMessages(userId)

        call.enqueue(object : Callback<List<Message>> {
            override fun onResponse(call: Call<List<Message>>, response: Response<List<Message>>) {
                // Hide progress bar after response - check if fragment is still attached
                if (!isAdded) return
                binding.progressBar.visibility = View.GONE
                
                if (response.isSuccessful) {
                    val messages = response.body()
                    if (messages != null) {
                        adapter.submitList(messages)
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.no_messages_available), Constants.Toast.DURATION_LONG).show()
                    }
                } else {
                    Toast.makeText(requireContext(), getString(R.string.failed_to_load_messages), Constants.Toast.DURATION_LONG).show()
                }
            }

            override fun onFailure(call: Call<List<Message>>, t: retrofit2.Call<List<Message>>) {
                // Hide progress bar after failure - check if fragment is still attached
                if (!isAdded) return
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), getString(R.string.network_error_messages, t.message), Constants.Toast.DURATION_LONG).show()
            }
        })
    }
}
