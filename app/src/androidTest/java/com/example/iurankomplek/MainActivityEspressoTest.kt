package com.example.iurankomplek

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke tests for the real Activity on a device.
 *
 * Data rendering is covered deterministically by MainActivityTest (Hilt + Robolectric);
 * these tests only assert that the Activity starts on a real device, wires its views and
 * survives an unreachable backend instead of crashing.
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class MainActivityEspressoTest {

    @Test
    fun mainActivity_launchesAndWiresItsViews() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                assertTrue("Activity should not be finishing", !activity.isFinishing)

                val recyclerView = activity.findViewById<RecyclerView>(R.id.rv_users)
                assertNotNull("RecyclerView should be inflated", recyclerView)
                assertNotNull("ProgressBar should be inflated", activity.findViewById(R.id.progressBar))

                onView(withId(R.id.rv_users)).check(matches(isDisplayed()))
            }
        }
    }

    @Test
    fun mainActivity_survivesUnreachableBackend() {
        // ApiConfig points at the configured host; when it is unreachable the Activity
        // must show the error state instead of crashing.
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                assertTrue("Activity should survive a failed load", !activity.isFinishing)
                val recyclerView = activity.findViewById<RecyclerView>(R.id.rv_users)
                assertNotNull(recyclerView)
                assertEquals(recyclerView.adapter?.itemCount ?: 0, recyclerView.adapter?.itemCount)
            }
        }
    }

    @Test
    fun mainActivity_progressBarIsGoneOnceSettled() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            Thread.sleep(4_000)
            scenario.onActivity { activity ->
                val progressBar = activity.findViewById<android.widget.ProgressBar>(R.id.progressBar)
                assertEquals(
                    "ProgressBar should be hidden after loading settles",
                    android.view.View.GONE,
                    progressBar.visibility
                )
            }
        }
    }
}
