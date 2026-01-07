package com.example.iurankomplek

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iurankomplek.databinding.FragmentMessagesBinding
import com.example.iurankomplek.model.Message
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.utils.NetworkUtils
import kotlinx.coroutines.launch

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

        loadMessages("default_user_id")

        return binding.root
    }

    private fun loadMessages(userId: String) {
        binding.progressBar.visibility = View.VISIBLE

         if (!NetworkUtils.isNetworkAvailable(requireContext())) {
             binding.progressBar.visibility = View.GONE
             Toast.makeText(context, getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show()
             return
         }

         val apiService = ApiConfig.getApiService()

         viewLifecycleOwner.lifecycleScope.launch {
             try {
                 val response = apiService.getMessages(userId)
                 binding.progressBar.visibility = View.GONE

                 if (response.isSuccessful) {
                     val messages = response.body()
                     if (messages != null) {
                         adapter.submitList(messages)
                     } else {
                         Toast.makeText(context, getString(R.string.no_messages_available), Toast.LENGTH_LONG).show()
                     }
                 } else {
                     Toast.makeText(context, getString(R.string.failed_to_load_messages), Toast.LENGTH_LONG).show()
                 }
             } catch (e: Exception) {
                 binding.progressBar.visibility = View.GONE
                 Toast.makeText(context, getString(R.string.network_error, e.message), Toast.LENGTH_LONG).show()
             }
         }
    }
}
