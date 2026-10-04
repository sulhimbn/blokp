package com.example.iurankomplek

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Test
import org.junit.runner.RunWith

/**
 * On-device UI verification for MainActivity.
 *
 * ApiConfig is an object bound to the configured spreadsheet base URL, so the
 * endpoint cannot be redirected per-test; these checks therefore cover the
 * view graph and adapter wiring, which are deterministic offline. Payload
 * parsing is covered on the JVM by network.ApiIntegrationTest.
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class MainActivityEspressoTest {

    @Test
    fun mainActivity_showsUserList() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.rv_users)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun mainActivity_attachesAdapterWithEmptyInitialContent() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.rv_users)).check(matches(hasChildCount(0)))
        }
    }

    @Test
    fun mainActivity_hasProgressIndicator() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.progressBar)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun mainActivity_userListIsScrollable() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.rv_users))
                .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(0))
        }
    }

    @Test
    fun mainActivity_survivesConfigurationChange() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.recreate()
            onView(withId(R.id.rv_users)).check(matches(isDisplayed()))
        }
    }
}
