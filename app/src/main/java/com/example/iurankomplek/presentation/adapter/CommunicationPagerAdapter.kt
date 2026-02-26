package com.example.iurankomplek.presentation.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.iurankomplek.AnnouncementsFragment
import com.example.iurankomplek.CommunityFragment
import com.example.iurankomplek.MessagesFragment

class CommunicationPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

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
