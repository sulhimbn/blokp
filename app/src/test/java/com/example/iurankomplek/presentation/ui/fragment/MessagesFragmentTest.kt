package com.example.iurankomplek.presentation.ui.fragment

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import com.example.iurankomplek.R
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class MessagesFragmentTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testMessage = com.example.iurankomplek.data.dto.MessageDto(
        id = 1,
        senderId = 1,
        receiverId = 2,
        message = "Test message",
        timestamp = System.currentTimeMillis(),
        isRead = false
    )

    @Before
    fun setup() {
    }

    @After
    fun tearDown() {
    }

    @Test
    fun `onCreateView initializes RecyclerView with adapter`() {
        launchFragmentInContainer<MessagesFragment>()

        onView(ViewMatchers.withId(R.id.rvMessages))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun `onCreateView sets LinearLayoutManager on RecyclerView`() {
        launchFragmentInContainer<MessagesFragment>()

        onView(ViewMatchers.withId(R.id.rvMessages))
            .check { view, noViewFoundException ->
                Assert.assertNotNull("RecyclerView should be initialized", view)
                val recyclerView = view as RecyclerView
                Assert.assertTrue(
                    "RecyclerView should have LinearLayoutManager",
                    recyclerView.layoutManager is androidx.recyclerview.widget.LinearLayoutManager
                )
            }
    }

    @Test
    fun `onCreateView sets hasFixedSize to true on RecyclerView`() {
        launchFragmentInContainer<MessagesFragment>()

        onView(ViewMatchers.withId(R.id.rvMessages))
            .check { view, noViewFoundException ->
                val recyclerView = view as RecyclerView
                Assert.assertTrue(
                    "RecyclerView should have setHasFixedSize(true)",
                    recyclerView.hasFixedSize()
                )
            }
    }

    @Test
    fun `onCreateView sets ItemViewCacheSize to 20 on RecyclerView`() {
        launchFragmentInContainer<MessagesFragment>()

        onView(ViewMatchers.withId(R.id.rvMessages))
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
        val scenario = launchFragmentInContainer<MessagesFragment>()

        scenario.onFragment { fragment ->
            fragment.onDestroyView()
            val binding = fragment.javaClass.getDeclaredField("_binding").apply {
                isAccessible = true
            }
            Assert.assertNull("Binding should be null after onDestroyView", binding.get(fragment))
        }
    }
}
