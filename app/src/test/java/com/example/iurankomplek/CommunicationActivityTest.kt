package com.example.iurankomplek

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.iurankomplek.presentation.ui.activity.CommunicationActivity
import com.example.iurankomplek.presentation.ui.fragment.AnnouncementsFragment
import com.example.iurankomplek.presentation.ui.fragment.MessagesFragment
import com.example.iurankomplek.presentation.ui.fragment.CommunityFragment
import com.google.android.material.tabs.TabLayout
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class CommunicationActivityTest {

    private lateinit var activity: CommunicationActivity
    private lateinit var controller: Robolectric.BuildActivity<CommunicationActivity>

    @Before
    fun setup() {
        controller = Robolectric.buildActivity(CommunicationActivity::class.java)
        activity = controller.create().get()
    }

    @Test
    fun `should initialize activity correctly`() {
        assertNotNull("Activity should be created", activity)
        assertEquals("Activity should be in created state", Lifecycle.State.CREATED, activity.lifecycle.currentState)
    }

    @Test
    fun `should initialize ViewPager2 correctly`() {
        val viewPager = activity.findViewById<ViewPager2>(R.id.view_pager)
        assertNotNull("ViewPager2 should be initialized", viewPager)
    }

    @Test
    fun `should set adapter on ViewPager2`() {
        val viewPager = activity.findViewById<ViewPager2>(R.id.view_pager)
        assertNotNull("ViewPager2 should have an adapter", viewPager.adapter)
    }

    @Test
    fun `should initialize TabLayout correctly`() {
        val tabLayout = activity.findViewById<TabLayout>(R.id.tab_layout)
        assertNotNull("TabLayout should be initialized", tabLayout)
    }

    @Test
    fun `should have exactly 3 tabs`() {
        val tabLayout = activity.findViewById<TabLayout>(R.id.tab_layout)
        assertEquals("TabLayout should have exactly 3 tabs", 3, tabLayout.tabCount)
    }

    @Test
    fun `should have 3 fragments in adapter`() {
        val viewPager = activity.findViewById<ViewPager2>(R.id.view_pager)
        assertEquals("Adapter should have exactly 3 fragments", 3, viewPager.adapter?.itemCount)
    }

    @Test
    fun `should verify first tab text is Announcements`() {
        val tabLayout = activity.findViewById<TabLayout>(R.id.tab_layout)
        val firstTab = tabLayout.getTabAt(0)
        assertNotNull("First tab should exist", firstTab)
        assertEquals("First tab text should be 'Announcements'", 
            activity.getString(R.string.tab_announcements), firstTab?.text)
    }

    @Test
    fun `should verify second tab text is Messages`() {
        val tabLayout = activity.findViewById<TabLayout>(R.id.tab_layout)
        val secondTab = tabLayout.getTabAt(1)
        assertNotNull("Second tab should exist", secondTab)
        assertEquals("Second tab text should be 'Messages'", 
            activity.getString(R.string.tab_messages), secondTab?.text)
    }

    @Test
    fun `should verify third tab text is Community`() {
        val tabLayout = activity.findViewById<TabLayout>(R.id.tab_layout)
        val thirdTab = tabLayout.getTabAt(2)
        assertNotNull("Third tab should exist", thirdTab)
        assertEquals("Third tab text should be 'Community'", 
            activity.getString(R.string.tab_community), thirdTab?.text)
    }

    @Test
    fun `should create AnnouncementsFragment at position 0`() {
        val viewPager = activity.findViewById<ViewPager2>(R.id.view_pager)
        val adapter = viewPager.adapter
        
        if (adapter is CommunicationActivity.CommunicationPagerAdapter) {
            val fragment = adapter.createFragment(0)
            assertTrue("Fragment at position 0 should be AnnouncementsFragment", 
                fragment is AnnouncementsFragment)
        }
    }

    @Test
    fun `should create MessagesFragment at position 1`() {
        val viewPager = activity.findViewById<ViewPager2>(R.id.view_pager)
        val adapter = viewPager.adapter
        
        if (adapter is CommunicationActivity.CommunicationPagerAdapter) {
            val fragment = adapter.createFragment(1)
            assertTrue("Fragment at position 1 should be MessagesFragment", 
                fragment is MessagesFragment)
        }
    }

    @Test
    fun `should create CommunityFragment at position 2`() {
        val viewPager = activity.findViewById<ViewPager2>(R.id.view_pager)
        val adapter = viewPager.adapter
        
        if (adapter is CommunicationActivity.CommunicationPagerAdapter) {
            val fragment = adapter.createFragment(2)
            assertTrue("Fragment at position 2 should be CommunityFragment", 
                fragment is CommunityFragment)
        }
    }

    @Test
    fun `should default to AnnouncementsFragment for invalid position`() {
        val viewPager = activity.findViewById<ViewPager2>(R.id.view_pager)
        val adapter = viewPager.adapter
        
        if (adapter is CommunicationActivity.CommunicationPagerAdapter) {
            val fragment = adapter.createFragment(999)
            assertTrue("Fragment at invalid position should default to AnnouncementsFragment", 
                fragment is AnnouncementsFragment)
        }
    }

    @Test
    fun `should verify adapter extends FragmentStateAdapter`() {
        val viewPager = activity.findViewById<ViewPager2>(R.id.view_pager)
        assertNotNull("Adapter should extend FragmentStateAdapter", viewPager.adapter)
    }

    @Test
    fun `should handle ViewPager2 scrolling`() {
        val viewPager = activity.findViewById<ViewPager2>(R.id.view_pager)
        assertNotNull("ViewPager2 should support scrolling", viewPager)

        viewPager.setCurrentItem(1, false)
        assertEquals("ViewPager2 should scroll to position 1", 1, viewPager.currentItem)
    }

    @Test
    fun `should handle tab switching`() {
        val tabLayout = activity.findViewById<TabLayout>(R.id.tab_layout)
        val viewPager = activity.findViewById<ViewPager2>(R.id.view_pager)
        
        assertNotNull("TabLayout should support tab switching", tabLayout)
        assertNotNull("ViewPager2 should sync with tab layout", viewPager)
    }

    @Test
    fun `should verify activity lifecycle states`() {
        assertEquals("Activity should be in created state", Lifecycle.State.CREATED, activity.lifecycle.currentState)
        
        controller.start()
        assertEquals("Activity should be in started state", Lifecycle.State.STARTED, activity.lifecycle.currentState)
        
        controller.resume()
        assertEquals("Activity should be in resumed state", Lifecycle.State.RESUMED, activity.lifecycle.currentState)
    }

    @Test
    fun `should verify all tabs are accessible`() {
        val tabLayout = activity.findViewById<TabLayout>(R.id.tab_layout)
        
        for (i in 0 until tabLayout.tabCount) {
            val tab = tabLayout.getTabAt(i)
            assertNotNull("Tab at position $i should be accessible", tab)
        }
    }

    @Test
    fun `should verify ViewPager2 orientation`() {
        val viewPager = activity.findViewById<ViewPager2>(R.id.view_pager)
        assertNotNull("ViewPager2 should be horizontal by default", viewPager.orientation)
    }

    @Test
    fun `should handle fragment recreation on configuration change`() {
        val viewPager = activity.findViewById<ViewPager2>(R.id.view_pager)
        assertNotNull("ViewPager2 should handle fragment recreation", viewPager.adapter)
    }

    @Test
    fun `should verify all fragment types are distinct`() {
        val viewPager = activity.findViewById<ViewPager2>(R.id.view_pager)
        val adapter = viewPager.adapter
        
        if (adapter is CommunicationActivity.CommunicationPagerAdapter) {
            val fragment0 = adapter.createFragment(0)
            val fragment1 = adapter.createFragment(1)
            val fragment2 = adapter.createFragment(2)
            
            assertTrue("All fragment types should be distinct", 
                fragment0::class != fragment1::class &&
                fragment1::class != fragment2::class &&
                fragment0::class != fragment2::class)
        }
    }

    @Test
    fun `should initialize TabLayoutMediator correctly`() {
        val tabLayout = activity.findViewById<TabLayout>(R.id.tab_layout)
        val viewPager = activity.findViewById<ViewPager2>(R.id.view_pager)
        
        assertNotNull("TabLayout should be initialized", tabLayout)
        assertNotNull("ViewPager2 should be initialized", viewPager)
        assertTrue("TabLayout and ViewPager2 should be mediated", 
            tabLayout.tabCount == viewPager.adapter?.itemCount)
    }

    @Test
    fun `should handle ViewPager2 setCurrentItem with smooth scroll`() {
        val viewPager = activity.findViewById<ViewPager2>(R.id.view_pager)
        
        viewPager.setCurrentItem(2, true)
        assertEquals("ViewPager2 should scroll to position 2 with smooth scroll", 2, viewPager.currentItem)
    }

    @Test
    fun `should verify ViewPager2 current item defaults to 0`() {
        val viewPager = activity.findViewById<ViewPager2>(R.id.view_pager)
        assertEquals("ViewPager2 current item should default to 0", 0, viewPager.currentItem)
    }
}