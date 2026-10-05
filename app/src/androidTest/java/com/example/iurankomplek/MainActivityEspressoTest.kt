package com.example.iurankomplek

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end UI gate. Debug builds resolve the mock API through
 * BuildConfig.MOCK_API_URL (10.0.2.2:5000, the emulator's host-loopback alias),
 * so these run against the real Retrofit -> Gson -> repository path rather than
 * a stubbed ViewModel.
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class MainActivityEspressoTest {

    @Test
    fun mainActivity_shouldInflateRecyclerviewAndProgressBar() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            // The progress bar is hidden once data arrives, so assert it is part
            // of the hierarchy rather than visible; MainActivityTest covers its
            // visibility during the initial Loading state.
            onView(withId(R.id.rv_users)).check(matches(isDisplayed()))
            scenario.onActivity { activity ->
                assertNotNull(
                    "Progress bar should be inflated",
                    activity.findViewById<android.view.View>(R.id.progressBar)
                )
            }
        }
    }

    @Test
    fun mainActivity_shouldBindAnAdapterToTheResidentsList() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val recyclerView = activity.findViewById<RecyclerView>(R.id.rv_users)
                assertNotNull("RecyclerView should exist", recyclerView)
                assertNotNull("Adapter should be attached", recyclerView.adapter)
            }
        }
    }

    @Test
    fun mainActivity_shouldLoadResidentsFromTheApi() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            // The repository call is asynchronous, so poll rather than assert once.
            var count = 0
            val deadline = System.currentTimeMillis() + 15_000
            while (count != 2 && System.currentTimeMillis() < deadline) {
                scenario.onActivity { activity ->
                    count = activity.findViewById<RecyclerView>(R.id.rv_users).adapter?.itemCount ?: 0
                }
                if (count != 2) Thread.sleep(200)
            }
            assertEquals(
                "Mock API serves two residents; they must all be bound",
                2,
                count
            )
        }
    }

    @Test
    fun mainActivity_shouldScrollTheResidentsList() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.rv_users)).perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(0))
        }
    }
}