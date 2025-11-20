package com.example.iurankomplek

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.model.CommunityPost
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.utils.NetworkUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CommunityFragment : Fragment() {

    private lateinit var adapter: CommunityPostAdapter
    private lateinit var rv_community: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_community, container, false)

        rv_community = view.findViewById(R.id.rv_community)
        adapter = CommunityPostAdapter()
        rv_community.layoutManager = LinearLayoutManager(context)
        rv_community.adapter = adapter

        loadCommunityPosts()

        return view
    }

    private fun loadCommunityPosts() {
        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            Toast.makeText(context, "No internet connection", Toast.LENGTH_LONG).show()
            return
        }

        val apiService = ApiConfig.getApiService()
        val call = apiService.getCommunityPosts()

        call.enqueue(object : Callback<List<CommunityPost>> {
            override fun onResponse(call: Call<List<CommunityPost>>, response: Response<List<CommunityPost>>) {
                if (response.isSuccessful) {
                    val posts = response.body()
                    if (posts != null) {
                        adapter.submitList(posts)
                    } else {
                        Toast.makeText(context, "No community posts available", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(context, "Failed to load community posts", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<List<CommunityPost>>, t: retrofit2.Call<List<CommunityPost>>) {
                Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}