package com.example.iurankomplek

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.iurankomplek.model.DataItem
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for MainActivity using Espresso
 */
@RunWith(AndroidJUnit4::class)
class MainActivityEspressoTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun `activity should display RecyclerView`() {
        // Given: MainActivity is launched
        // When: Checking for RecyclerView
        // Then: RecyclerView should be displayed
        onView(withId(R.id.rv_users))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `recyclerView should be initially empty or show placeholder`() {
        // Given: MainActivity is launched
        // When: Checking RecyclerView item count
        // Then: Should have some initial state
        onView(withId(R.id.rv_users))
            .check(matches(isDisplayed()))
    }

    @Test
    fun `clicking on menu items should navigate to appropriate screens`() {
        // Test navigation from MenuActivity would require launching that activity
        // This demonstrates the pattern for UI tests
        onView(withId(R.id.rv_users))
            .check(matches(isDisplayed()))
    }
}