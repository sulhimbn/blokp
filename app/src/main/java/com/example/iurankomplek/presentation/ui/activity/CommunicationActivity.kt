package com.example.iurankomplek.presentation.ui.activity

import com.example.iurankomplek.core.base.BaseActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.iurankomplek.databinding.ActivityCommunicationBinding
import com.example.iurankomplek.utils.NetworkUtils
import com.google.android.material.tabs.TabLayoutMediator
import androidx.viewpager2.widget.ViewPager2

class CommunicationActivity : BaseActivity() {

    private lateinit var binding: ActivityCommunicationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommunicationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = CommunicationPagerAdapter(this)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.tab_announcements)
                1 -> tab.text = getString(R.string.tab_messages)
                2 -> tab.text = getString(R.string.tab_community)
            }
        }.attach()
    }

    private class CommunicationPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> AnnouncementsFragment()
                1 -> MessagesFragment()
                2 -> CommunityFragment()
                else -> AnnouncementsFragment()
            }
        }
    }
}