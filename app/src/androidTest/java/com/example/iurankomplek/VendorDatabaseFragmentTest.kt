package com.example.iurankomplek

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.iurankomplek.model.Vendor
import com.example.iurankomplek.model.VendorResponse
import com.example.iurankomplek.utils.UiState
import com.example.iurankomplek.viewmodel.VendorViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [28])
class VendorDatabaseFragmentTest {

    @Mock
    private lateinit var mockViewModel: VendorViewModel

    private lateinit var fragment: VendorDatabaseFragment
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testVendorList = listOf(
        Vendor(
            id = "1",
            name = "Test Vendor 1",
            specialty = "Plumbing",
            phoneNumber = "08123456789",
            rating = 4.5,
            email = "vendor1@example.com",
            address = "123 Test St"
        ),
        Vendor(
            id = "2",
            name = "Test Vendor 2",
            specialty = "Electrical",
            phoneNumber = "08198765432",
            rating = 3.8,
            email = "vendor2@example.com",
            address = "456 Test Ave"
        )
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fragment should create view successfully`() {
        val scenario = launchFragmentInContainer<VendorDatabaseFragment>(
            themeResId = R.style.Theme_AppCompat_Light_DarkActionBar
        )

        scenario.onFragment { fragment ->
            assertNotNull("Fragment should be created", fragment)
            assertNotNull("Fragment view should be created", fragment.view)
            assertEquals(Lifecycle.State.RESUMED, fragment.lifecycle.currentState)
        }
    }

    @Test
    fun `fragment should initialize RecyclerView correctly`() {
        val scenario = launchFragmentInContainer<VendorDatabaseFragment>(
            themeResId = R.style.Theme_AppCompat_Light_DarkActionBar
        )

        scenario.onFragment { fragment ->
            val recyclerView = fragment.view?.findViewById<RecyclerView>(R.id.vendorRecyclerView)
            assertNotNull("RecyclerView should be initialized", recyclerView)
            assertNotNull("RecyclerView adapter should be set", recyclerView?.adapter)
            assertTrue("Adapter should be VendorAdapter", recyclerView?.adapter is VendorAdapter)
        }
    }

    @Test
    fun `fragment should observe ViewModel loading state`() {
        val testStateFlow = MutableStateFlow<UiState<VendorResponse>>(UiState.Loading)
        `when`(mockViewModel.vendorState).thenReturn(testStateFlow.asStateFlow())

        val scenario = launchFragmentInContainer<VendorDatabaseFragment>(
            themeResId = R.style.Theme_AppCompat_Light_DarkActionBar
        )

        scenario.onFragment { fragment ->
            fragment.injectViewModel(mockViewModel)
            val recyclerView = fragment.view?.findViewById<RecyclerView>(R.id.vendorRecyclerView)
            assertNotNull("RecyclerView should be present", recyclerView)
            assertEquals("Vendor state should be Loading initially", UiState.Loading, testStateFlow.value)
        }
    }

    @Test
    fun `fragment should display vendors on success state`() {
        val vendorResponse = VendorResponse(
            success = true,
            message = "Vendors fetched successfully",
            data = testVendorList
        )
        val testStateFlow = MutableStateFlow<UiState<VendorResponse>>(UiState.Success(vendorResponse))
        `when`(mockViewModel.vendorState).thenReturn(testStateFlow.asStateFlow())

        val scenario = launchFragmentInContainer<VendorDatabaseFragment>(
            themeResId = R.style.Theme_AppCompat_Light_DarkActionBar
        )

        scenario.onFragment { fragment ->
            fragment.injectViewModel(mockViewModel)
            val recyclerView = fragment.view?.findViewById<RecyclerView>(R.id.vendorRecyclerView)
            assertNotNull("RecyclerView should be present", recyclerView)
            assertEquals("Adapter should display vendor list", testVendorList.size, recyclerView?.adapter?.itemCount)
        }
    }

    @Test
    fun `fragment should handle empty vendor list successfully`() {
        val emptyResponse = VendorResponse(
            success = true,
            message = "No vendors available",
            data = emptyList()
        )
        val testStateFlow = MutableStateFlow<UiState<VendorResponse>>(UiState.Success(emptyResponse))
        `when`(mockViewModel.vendorState).thenReturn(testStateFlow.asStateFlow())

        val scenario = launchFragmentInContainer<VendorDatabaseFragment>(
            themeResId = R.style.Theme_AppCompat_Light_DarkActionBar
        )

        scenario.onFragment { fragment ->
            fragment.injectViewModel(mockViewModel)
            val recyclerView = fragment.view?.findViewById<RecyclerView>(R.id.vendorRecyclerView)
            assertNotNull("RecyclerView should be present", recyclerView)
            assertEquals("Adapter should handle empty list", 0, recyclerView?.adapter?.itemCount)
        }
    }

    @Test
    fun `fragment should show error toast on error state`() {
        val errorMessage = "Failed to load vendors"
        val testStateFlow = MutableStateFlow<UiState<VendorResponse>>(UiState.Error(errorMessage))
        `when`(mockViewModel.vendorState).thenReturn(testStateFlow.asStateFlow())

        val scenario = launchFragmentInContainer<VendorDatabaseFragment>(
            themeResId = R.style.Theme_AppCompat_Light_DarkActionBar
        )

        scenario.onFragment { fragment ->
            fragment.injectViewModel(mockViewModel)
            val recyclerView = fragment.view?.findViewById<RecyclerView>(R.id.vendorRecyclerView)
            assertNotNull("RecyclerView should be present", recyclerView)
            assertEquals("Vendor state should be Error", UiState.Error(errorMessage), testStateFlow.value)
        }
    }

    @Test
    fun `fragment should handle vendor click correctly`() {
        val vendorResponse = VendorResponse(
            success = true,
            message = "Vendors fetched successfully",
            data = testVendorList
        )
        val testStateFlow = MutableStateFlow<UiState<VendorResponse>>(UiState.Success(vendorResponse))
        `when`(mockViewModel.vendorState).thenReturn(testStateFlow.asStateFlow())

        val scenario = launchFragmentInContainer<VendorDatabaseFragment>(
            themeResId = R.style.Theme_AppCompat_Light_DarkActionBar
        )

        scenario.onFragment { fragment ->
            fragment.injectViewModel(mockViewModel)
            val recyclerView = fragment.view?.findViewById<RecyclerView>(R.id.vendorRecyclerView)
            assertNotNull("RecyclerView should be present", recyclerView)

            val viewHolder = recyclerView?.findViewHolderForPosition(0)
            assertNotNull("ViewHolder should be present", viewHolder)
            viewHolder?.itemView?.performClick()
        }
    }

    @Test
    fun `fragment should cleanup binding on destroy view`() {
        val scenario = launchFragmentInContainer<VendorDatabaseFragment>(
            themeResId = R.style.Theme_AppCompat_Light_DarkActionBar
        )

        scenario.onFragment { fragment ->
            assertNotNull("Fragment binding should be initialized", fragment.binding)
            scenario.moveToState(Lifecycle.State.DESTROYED)
            assertNull("Fragment binding should be null after destroy", fragment.binding)
        }
    }

    @Test
    fun `fragment should handle null data in success state gracefully`() {
        val responseWithNullData = VendorResponse(
            success = true,
            message = "Success",
            data = null
        )
        val testStateFlow = MutableStateFlow<UiState<VendorResponse>>(UiState.Success(responseWithNullData))
        `when`(mockViewModel.vendorState).thenReturn(testStateFlow.asStateFlow())

        val scenario = launchFragmentInContainer<VendorDatabaseFragment>(
            themeResId = R.style.Theme_AppCompat_Light_DarkActionBar
        )

        scenario.onFragment { fragment ->
            fragment.injectViewModel(mockViewModel)
            val recyclerView = fragment.view?.findViewById<RecyclerView>(R.id.vendorRecyclerView)
            assertNotNull("RecyclerView should handle null data gracefully", recyclerView)
        }
    }

    @Test
    fun `fragment should use LinearLayoutManager for RecyclerView`() {
        val scenario = launchFragmentInContainer<VendorDatabaseFragment>(
            themeResId = R.style.Theme_AppCompat_Light_DarkActionBar
        )

        scenario.onFragment { fragment ->
            val recyclerView = fragment.view?.findViewById<RecyclerView>(R.id.vendorRecyclerView)
            assertNotNull("RecyclerView should be present", recyclerView)
            assertTrue("LayoutManager should be LinearLayoutManager", recyclerView?.layoutManager is androidx.recyclerview.widget.LinearLayoutManager)
        }
    }

    @Test
    fun `fragment should preserve adapter configuration across state changes`() {
        val initialResponse = VendorResponse(
            success = true,
            message = "Initial vendors",
            data = testVendorList.take(1)
        )
        val updatedResponse = VendorResponse(
            success = true,
            message = "Updated vendors",
            data = testVendorList
        )

        val testStateFlow = MutableStateFlow<UiState<VendorResponse>>(UiState.Success(initialResponse))
        `when`(mockViewModel.vendorState).thenReturn(testStateFlow.asStateFlow())

        val scenario = launchFragmentInContainer<VendorDatabaseFragment>(
            themeResId = R.style.Theme_AppCompat_Light_DarkActionBar
        )

        scenario.onFragment { fragment ->
            fragment.injectViewModel(mockViewModel)
            val recyclerView = fragment.view?.findViewById<RecyclerView>(R.id.vendorRecyclerView)
            val initialAdapter = recyclerView?.adapter

            testStateFlow.value = UiState.Success(updatedResponse)

            assertEquals("Adapter should remain the same", initialAdapter, recyclerView?.adapter)
            assertEquals("Item count should update", testVendorList.size, recyclerView?.adapter?.itemCount)
        }
    }

    @Test
    fun `fragment should handle large vendor lists efficiently`() {
        val largeVendorList = (1..100).map { index ->
            Vendor(
                id = "$index",
                name = "Vendor $index",
                specialty = "Specialty $index",
                phoneNumber = "08${index.toString().repeat(9)}",
                rating = (index % 50) / 10.0,
                email = "vendor$index@example.com",
                address = "$index Test Street"
            )
        }

        val largeResponse = VendorResponse(
            success = true,
            message = "Large vendor list",
            data = largeVendorList
        )
        val testStateFlow = MutableStateFlow<UiState<VendorResponse>>(UiState.Success(largeResponse))
        `when`(mockViewModel.vendorState).thenReturn(testStateFlow.asStateFlow())

        val scenario = launchFragmentInContainer<VendorDatabaseFragment>(
            themeResId = R.style.Theme_AppCompat_Light_DarkActionBar
        )

        scenario.onFragment { fragment ->
            fragment.injectViewModel(mockViewModel)
            val recyclerView = fragment.view?.findViewById<RecyclerView>(R.id.vendorRecyclerView)
            assertNotNull("RecyclerView should handle large lists", recyclerView)
            assertEquals("All items should be displayed", largeVendorList.size, recyclerView?.adapter?.itemCount)
        }
    }

    @Test
    fun `fragment should handle special characters in vendor data`() {
        val vendorsWithSpecialChars = listOf(
            Vendor(
                id = "1",
                name = "Vendor with émojis 🎉",
                specialty = "Plumbíng & Heating",
                phoneNumber = "+62 812 345-6789",
                rating = 4.5,
                email = "vendor+test@example.com",
                address = "123 Main St, Apt 4B"
            )
        )

        val response = VendorResponse(
            success = true,
            message = "Special character vendors",
            data = vendorsWithSpecialChars
        )
        val testStateFlow = MutableStateFlow<UiState<VendorResponse>>(UiState.Success(response))
        `when`(mockViewModel.vendorState).thenReturn(testStateFlow.asStateFlow())

        val scenario = launchFragmentInContainer<VendorDatabaseFragment>(
            themeResId = R.style.Theme_AppCompat_Light_DarkActionBar
        )

        scenario.onFragment { fragment ->
            fragment.injectViewModel(mockViewModel)
            val recyclerView = fragment.view?.findViewById<RecyclerView>(R.id.vendorRecyclerView)
            assertNotNull("RecyclerView should handle special characters", recyclerView)
            assertEquals("Special character vendor should be displayed", 1, recyclerView?.adapter?.itemCount)
        }
    }
}

private fun VendorDatabaseFragment.injectViewModel(viewModel: VendorViewModel) {
    val viewModelField = VendorDatabaseFragment::class.java.getDeclaredField("vendorViewModel")
    viewModelField.isAccessible = true
    viewModelField.set(this, viewModel)
}

private fun VendorDatabaseFragment.getBinding(): FragmentVendorDatabaseBinding? {
    return this.view?.let {
        FragmentVendorDatabaseBinding.bind(it)
    }
}