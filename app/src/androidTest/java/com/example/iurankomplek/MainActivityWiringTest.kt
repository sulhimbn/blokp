package com.example.iurankomplek

import android.view.View
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.iurankomplek.presentation.adapter.UserAdapter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Activity-level wiring for MainActivity. MainActivity is @AndroidEntryPoint and obtains its
 * ViewModel through the `by viewModels()` delegate, so it can only be exercised on a device or
 * emulator with the real Hilt graph - it cannot be unit tested on the JVM.
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class MainActivityWiringTest {

    @Test
    fun mainActivity_launchesAndBindsItsViews() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val recyclerView = activity.findViewById<RecyclerView>(R.id.rv_users)
                val progressBar = activity.findViewById<ProgressBar>(R.id.progressBar)

                assertNotNull("RecyclerView should be inflated", recyclerView)
                assertNotNull("ProgressBar should be inflated", progressBar)
                assertNotNull("RecyclerView should have a UserAdapter", recyclerView.adapter)
                assertTrue(
                    "RecyclerView adapter should be a UserAdapter",
                    recyclerView.adapter is UserAdapter
                )
                assertTrue("Activity should not be finishing", !activity.isFinishing)
            }
        }
    }

    @Test
    fun mainActivity_hidesProgressBarOnceDataIsBound() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val progressBar = activity.findViewById<ProgressBar>(R.id.progressBar)
                assertNotNull(progressBar)
                assertEquals(
                    "ProgressBar is GONE unless the state is Loading",
                    View.GONE,
                    progressBar.visibility
                )
            }
            onView(withId(R.id.rv_users)).check(matches(isDisplayed()))
        }
    }
}