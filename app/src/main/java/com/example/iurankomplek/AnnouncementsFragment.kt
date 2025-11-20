package com.example.iurankomplek

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.iurankomplek.model.Announcement
import com.example.iurankomplek.network.ApiConfig
import com.example.iurankomplek.utils.NetworkUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AnnouncementsFragment : Fragment() {

    private lateinit var adapter: AnnouncementAdapter
    private lateinit var rv_announcements: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_announcements, container, false)

        rv_announcements = view.findViewById(R.id.rv_announcements)
        adapter = AnnouncementAdapter()
        rv_announcements.layoutManager = LinearLayoutManager(context)
        rv_announcements.adapter = adapter

        loadAnnouncements()

        return view
    }

    private fun loadAnnouncements() {
        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            Toast.makeText(context, "No internet connection", Toast.LENGTH_LONG).show()
            return
        }

        val apiService = ApiConfig.getApiService()
        val call = apiService.getAnnouncements()

        call.enqueue(object : Callback<List<Announcement>> {
            override fun onResponse(call: Call<List<Announcement>>, response: Response<List<Announcement>>) {
                if (response.isSuccessful) {
                    val announcements = response.body()
                    if (announcements != null) {
                        adapter.submitList(announcements)
                    } else {
                        Toast.makeText(context, "No announcements available", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(context, "Failed to load announcements", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<List<Announcement>>, t: retrofit2.Call<List<Announcement>>) {
                Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}