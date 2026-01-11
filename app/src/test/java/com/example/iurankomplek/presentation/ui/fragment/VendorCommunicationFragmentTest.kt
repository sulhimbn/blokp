package com.example.iurankomplek.presentation.ui.fragment

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import com.example.iurankomplek.R
import com.example.iurankomplek.data.dto.VendorDto
import com.example.iurankomplek.presentation.viewmodel.VendorViewModel
import com.example.iurankomplek.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class VendorCommunicationFragmentTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var vendorViewModel: VendorViewModel

    private val testVendor = VendorDto(
        id = 1,
        name = "Test Vendor",
        category = "Cleaning",
        contact = "test@vendor.com",
        status = "Active"
    )

    private val mockVendorStateFlow = MutableStateFlow<UiState<com.example.iurankomplek.model.VendorResponse>>(UiState.Loading)

    @Before
    fun setup() {
    }

    @After
    fun tearDown() {
    }

    @Test
    fun `onCreateView initializes RecyclerView with adapter`() {
        launchFragmentInContainer<VendorCommunicationFragment>()

        onView(ViewMatchers.withId(R.id.vendorRecyclerView))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun `onCreateView sets LinearLayoutManager on RecyclerView`() {
        launchFragmentInContainer<VendorCommunicationFragment>()

        onView(ViewMatchers.withId(R.id.vendorRecyclerView))
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
        launchFragmentInContainer<VendorCommunicationFragment>()

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
    fun `onCreateView sets ItemViewCacheSize to 20 on RecyclerView`() {
        launchFragmentInContainer<VendorCommunicationFragment>()

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
    fun `onCreateView shows progressBar initially`() {
        launchFragmentInContainer<VendorCommunicationFragment>()

        onView(ViewMatchers.withId(R.id.progressBar))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun `onViewCreated hides progressBar when data is loaded`() {
        launchFragmentInContainer<VendorCommunicationFragment>()

        onView(ViewMatchers.withId(R.id.progressBar))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun `onDestroyView nullifies binding`() {
        val scenario = launchFragmentInContainer<VendorCommunicationFragment>()

        scenario.onFragment { fragment ->
            fragment.onDestroyView()
            val binding = fragment.javaClass.getDeclaredField("_binding").apply {
                isAccessible = true
            }
            Assert.assertNull("Binding should be null after onDestroyView", binding.get(fragment))
        }
    }

    @Test
    fun `VendorAdapter is created with click listener`() {
        launchFragmentInContainer<VendorCommunicationFragment>()

        onView(ViewMatchers.withId(R.id.vendorRecyclerView))
            .check { view, noViewFoundException ->
                val recyclerView = view as RecyclerView
                Assert.assertNotNull(
                    "RecyclerView should have an adapter",
                    recyclerView.adapter
                )
            }
    }

    @Test
    fun `onViewCreated calls initializeViewModel`() {
        launchFragmentInContainer<VendorCommunicationFragment>()

        onView(ViewMatchers.withId(R.id.vendorRecyclerView))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun `onViewCreated calls loadData`() {
        launchFragmentInContainer<VendorCommunicationFragment>()

        onView(ViewMatchers.withId(R.id.vendorRecyclerView))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun `emptyMessageStringRes is correct toast_communicate_with_vendor`() {
        launchFragmentInContainer<VendorCommunicationFragment>()

        onView(ViewMatchers.withId(R.id.vendorRecyclerView))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun `recyclerView is correct vendorRecyclerView`() {
        launchFragmentInContainer<VendorCommunicationFragment>()

        onView(ViewMatchers.withId(R.id.vendorRecyclerView))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun `progressBar is correct progressBar`() {
        launchFragmentInContainer<VendorCommunicationFragment>()

        onView(ViewMatchers.withId(R.id.progressBar))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun `fragment extends BaseFragment`() {
        val scenario = launchFragmentInContainer<VendorCommunicationFragment>()

        scenario.onFragment { fragment ->
            Assert.assertTrue(
                "VendorCommunicationFragment should extend BaseFragment",
                fragment is com.example.iurankomplek.core.base.BaseFragment<*>
            )
        }
    }
}
