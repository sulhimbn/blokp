package com.example.iurankomplek

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.iurankomplek.presentation.ui.fragment.VendorDatabaseFragment
import com.example.iurankomplek.presentation.adapter.VendorAdapter
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VendorDatabaseFragmentTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
    }

    @After
    fun tearDown() {
    }

    @Test
    fun `fragment creates view successfully`() {
        launchFragmentInContainer<VendorDatabaseFragment>(
            themeResId = R.style.Theme_AppCompat_Light_DarkActionBar
        )

        onView(ViewMatchers.withId(R.id.vendorRecyclerView))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun `fragment initializes RecyclerView with adapter`() {
        launchFragmentInContainer<VendorDatabaseFragment>(
            themeResId = R.style.Theme_AppCompat_Light_DarkActionBar
        )

        onView(ViewMatchers.withId(R.id.vendorRecyclerView))
            .check { view, noViewFoundException ->
                Assert.assertNotNull("RecyclerView should be initialized", view)
                val recyclerView = view as RecyclerView
                Assert.assertNotNull("RecyclerView adapter should be set", recyclerView.adapter)
                Assert.assertTrue("Adapter should be VendorAdapter", recyclerView.adapter is VendorAdapter)
            }
    }

    @Test
    fun `fragment sets LinearLayoutManager on RecyclerView`() {
        launchFragmentInContainer<VendorDatabaseFragment>(
            themeResId = R.style.Theme_AppCompat_Light_DarkActionBar
        )

        onView(ViewMatchers.withId(R.id.vendorRecyclerView))
            .check { view, noViewFoundException ->
                val recyclerView = view as RecyclerView
                Assert.assertTrue(
                    "RecyclerView should have LinearLayoutManager",
                    recyclerView.layoutManager is androidx.recyclerview.widget.LinearLayoutManager
                )
            }
    }

    @Test
    fun `fragment sets hasFixedSize to true on RecyclerView`() {
        launchFragmentInContainer<VendorDatabaseFragment>(
            themeResId = R.style.Theme_AppCompat_Light_DarkActionBar
        )

        onView(ViewMatchers.withId(R.id.vendorRecyclerView))
            .check { view, noViewFoundException ->
                val recyclerView = view as RecyclerView
                Assert.assertTrue(
                    "RecyclerView should have setHasFixedSize(true)",
                    recyclerView.hasFixedSize()
                )
            }
    }

    @Test
    fun `fragment sets ItemViewCacheSize to 20 on RecyclerView`() {
        launchFragmentInContainer<VendorDatabaseFragment>(
            themeResId = R.style.Theme_AppCompat_Light_DarkActionBar
        )

        onView(ViewMatchers.withId(R.id.vendorRecyclerView))
            .check { view, noViewFoundException ->
                val recyclerView = view as RecyclerView
                Assert.assertEquals(
                    "RecyclerView should have setItemViewCacheSize(20)",
                    20,
                    recyclerView.itemViewCacheSize
                )
            }
    }

    @Test
    fun `onDestroyView nullifies binding`() {
        val scenario = launchFragmentInContainer<VendorDatabaseFragment>(
            themeResId = R.style.Theme_AppCompat_Light_DarkActionBar
        )

        scenario.onFragment { fragment ->
            fragment.onDestroyView()
            val binding = fragment.javaClass.getDeclaredField("_binding").apply {
                isAccessible = true
            }
            Assert.assertNull("Binding should be null after onDestroyView", binding.get(fragment))
        }
    }
}
