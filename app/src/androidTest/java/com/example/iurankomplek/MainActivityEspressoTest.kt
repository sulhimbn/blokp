package com.example.iurankomplek

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class MainActivityEspressoTest {

    private var scenario: ActivityScenario<MainActivity>? = null

    private fun launch(): ActivityScenario<MainActivity> =
        ActivityScenario.launch(MainActivity::class.java).also { scenario = it }

    @After
    fun tearDown() {
        scenario?.close()
        scenario = null
    }

    @Test
    fun mainActivity_showsTheResidentListOnLaunch() {
        launch()

        onView(withId(R.id.rv_users)).check(matches(isDisplayed()))
    }

    @Test
    fun mainActivity_showsTheLoadingIndicatorOnLaunch() {
        launch()

        onView(withId(R.id.progressBar)).check(matches(isDisplayed()))
    }

    @Test
    fun mainActivity_showsPullToRefresh() {
        launch()

        onView(withId(R.id.swipeRefreshLayout)).check(matches(isDisplayed()))
    }

    @Test
    fun mainActivity_residentListScrollsWithoutCrashing() {
        launch()

        onView(withId(R.id.rv_users)).perform(swipeUp())
    }

    @Test
    fun mainActivity_exposesARecyclerViewForTheResidentList() {
        launch()

        scenario?.onActivity { activity ->
            val list = activity.findViewById<RecyclerView>(R.id.rv_users)
            checkNotNull(list)
            checkNotNull(list.adapter) { "the resident list must have an adapter" }
            check(list.layoutManager != null) { "the resident list must have a layout manager" }
        }
    }
}
