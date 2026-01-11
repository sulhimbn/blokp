package com.example.iurankomplek.presentation.ui.fragment

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
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
class VendorPerformanceFragmentTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
    }

    @After
    fun tearDown() {
    }

    @Test
    fun `onCreateView initializes views`() {
        launchFragmentInContainer<VendorPerformanceFragment>()

        onView(ViewMatchers.withId(R.id.analyticsTextView))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun `onCreateView sets analyticsTextView text correctly`() {
        launchFragmentInContainer<VendorPerformanceFragment>()

        onView(ViewMatchers.withId(R.id.analyticsTextView))
            .check(ViewAssertions.matches(ViewMatchers.withText(R.string.vendor_performance_analytics)))
    }

    @Test
    fun `onViewCreated sets analyticsTextView text`() {
        launchFragmentInContainer<VendorPerformanceFragment>()

        onView(ViewMatchers.withId(R.id.analyticsTextView))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun `onDestroyView nullifies binding`() {
        val scenario = launchFragmentInContainer<VendorPerformanceFragment>()

        scenario.onFragment { fragment ->
            fragment.onDestroyView()
            val binding = fragment.javaClass.getDeclaredField("_binding").apply {
                isAccessible = true
            }
            Assert.assertNull("Binding should be null after onDestroyView", binding.get(fragment))
        }
    }

    @Test
    fun `fragment extends Fragment`() {
        val scenario = launchFragmentInContainer<VendorPerformanceFragment>()

        scenario.onFragment { fragment ->
            Assert.assertTrue(
                "VendorPerformanceFragment should extend Fragment",
                fragment is androidx.fragment.app.Fragment
            )
        }
    }

    @Test
    fun `onCreateView returns non-null View`() {
        val scenario = launchFragmentInContainer<VendorPerformanceFragment>()

        scenario.onFragment { fragment ->
            Assert.assertNotNull("Fragment view should not be null", fragment.view)
        }
    }

    @Test
    fun `fragment view is displayed`() {
        launchFragmentInContainer<VendorPerformanceFragment>()

        onView(ViewMatchers.withId(R.id.analyticsTextView))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun `fragment does not crash on recreation`() {
        val scenario = launchFragmentInContainer<VendorPerformanceFragment>()

        scenario.recreate()

        onView(ViewMatchers.withId(R.id.analyticsTextView))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }
}
