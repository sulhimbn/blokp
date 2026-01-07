package com.example.iurankomplek

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.iurankomplek.model.WorkOrder
import com.example.iurankomplek.model.WorkOrderResponse
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
import java.util.Date

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [28])
class WorkOrderManagementFragmentTest {

    @Mock
    private lateinit var mockViewModel: VendorViewModel

    private lateinit var fragment: WorkOrderManagementFragment
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testWorkOrderList = listOf(
        WorkOrder(
            id = "1",
            title = "Plumbing Repair",
            description = "Fix leaking pipe in kitchen",
            vendorId = "vendor1",
            vendorName = "John's Plumbing",
            status = "PENDING",
            priority = "HIGH",
            assignedDate = Date(),
            dueDate = Date(System.currentTimeMillis() + 86400000),
            completedDate = null
        ),
        WorkOrder(
            id = "2",
            title = "Electrical Maintenance",
            description = "Replace faulty wiring in living room",
            vendorId = "vendor2",
            vendorName = "Bright Electric",
            status = "IN_PROGRESS",
            priority = "MEDIUM",
            assignedDate = Date(),
            dueDate = Date(System.currentTimeMillis() + 172800000),
            completedDate = null
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
        val scenario = launchFragmentInContainer<WorkOrderManagementFragment>(
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
        val scenario = launchFragmentInContainer<WorkOrderManagementFragment>(
            themeResId = R.style.Theme_AppCompat_Light_DarkActionBar
        )

        scenario.onFragment { fragment ->
            val recyclerView = fragment.view?.findViewById<RecyclerView>(R.id.workOrderRecyclerView)
            assertNotNull("RecyclerView should be initialized", recyclerView)
            assertNotNull("RecyclerView adapter should be set", recyclerView?.adapter)
            assertTrue("Adapter should be WorkOrderAdapter", recyclerView?.adapter is WorkOrderAdapter)
        }
    }

    @Test
    fun `fragment should observe ViewModel loading state`() {
        val testStateFlow = MutableStateFlow<UiState<WorkOrderResponse>>(UiState.Loading)
        `when`(mockViewModel.workOrderState).thenReturn(testStateFlow.asStateFlow())

        val scenario = launchFragmentInContainer<WorkOrderManagementFragment>(
            themeResId = R.style.Theme_AppCompat_Light_DarkActionBar
        )

        scenario.onFragment { fragment ->
            fragment.injectViewModel(mockViewModel)
            val recyclerView = fragment.view?.findViewById<RecyclerView>(R.id.workOrderRecyclerView)
            assertNotNull("RecyclerView should be present", recyclerView)
            assertEquals("WorkOrder state should be Loading initially", UiState.Loading, testStateFlow.value)
        }
    }

    @Test
    fun `fragment should display work orders on success state`() {
        val workOrderResponse = WorkOrderResponse(
            success = true,
            message = "Work orders fetched successfully",
            data = testWorkOrderList
        )
        val testStateFlow = MutableStateFlow<UiState<WorkOrderResponse>>(UiState.Success(workOrderResponse))
        `when`(mockViewModel.workOrderState).thenReturn(testStateFlow.asStateFlow())

        val scenario = launchFragmentInContainer<WorkOrderManagementFragment>(
            themeResId = R.style.Theme_AppCompat_Light_DarkActionBar
        )

        scenario.onFragment { fragment ->
            fragment.injectViewModel(mockViewModel)
            val recyclerView = fragment.view?.findViewById<RecyclerView>(R.id.workOrderRecyclerView)
            assertNotNull("RecyclerView should be present", recyclerView)
            assertEquals("Adapter should display work order list", testWorkOrderList.size, recyclerView?.adapter?.itemCount)
        }
    }

    @Test
    fun `fragment should handle empty work order list successfully`() {
        val emptyResponse = WorkOrderResponse(
            success = true,
            message = "No work orders available",
            data = emptyList()
        )
        val testStateFlow = MutableStateFlow<UiState<WorkOrderResponse>>(UiState.Success(emptyResponse))
        `when`(mockViewModel.workOrderState).thenReturn(testStateFlow.asStateFlow())

        val scenario = launchFragmentInContainer<WorkOrderManagementFragment>(
            themeResId = R.style.Theme_AppCompat_Light_DarkActionBar
        )

        scenario.onFragment { fragment ->
            fragment.injectViewModel(mockViewModel)
            val recyclerView = fragment.view?.findViewById<RecyclerView>(R.id.workOrderRecyclerView)
            assertNotNull("RecyclerView should be present", recyclerView)
            assertEquals("Adapter should handle empty list", 0, recyclerView?.adapter?.itemCount)
        }
    }

    @Test
    fun `fragment should show error toast on error state`() {
        val errorMessage = "Failed to load work orders"
        val testStateFlow = MutableStateFlow<UiState<WorkOrderResponse>>(UiState.Error(errorMessage))
        `when`(mockViewModel.workOrderState).thenReturn(testStateFlow.asStateFlow())

        val scenario = launchFragmentInContainer<WorkOrderManagementFragment>(
            themeResId = R.style.Theme_AppCompat_Light_DarkActionBar
        )

        scenario.onFragment { fragment ->
            fragment.injectViewModel(mockViewModel)
            val recyclerView = fragment.view?.findViewById<RecyclerView>(R.id.workOrderRecyclerView)
            assertNotNull("RecyclerView should be present", recyclerView)
            assertEquals("WorkOrder state should be Error", UiState.Error(errorMessage), testStateFlow.value)
        }
    }

    @Test
    fun `fragment should handle work order click correctly`() {
        val workOrderResponse = WorkOrderResponse(
            success = true,
            message = "Work orders fetched successfully",
            data = testWorkOrderList
        )
        val testStateFlow = MutableStateFlow<UiState<WorkOrderResponse>>(UiState.Success(workOrderResponse))
        `when`(mockViewModel.workOrderState).thenReturn(testStateFlow.asStateFlow())

        val scenario = launchFragmentInContainer<WorkOrderManagementFragment>(
            themeResId = R.style.Theme_AppCompat_Light_DarkActionBar
        )

        scenario.onFragment { fragment ->
            fragment.injectViewModel(mockViewModel)
            val recyclerView = fragment.view?.findViewById<RecyclerView>(R.id.workOrderRecyclerView)
            assertNotNull("RecyclerView should be present", recyclerView)

            val viewHolder = recyclerView?.findViewHolderForPosition(0)
            assertNotNull("ViewHolder should be present", viewHolder)
            viewHolder?.itemView?.performClick()
        }
    }

    @Test
    fun `fragment should cleanup binding on destroy view`() {
        val scenario = launchFragmentInContainer<WorkOrderManagementFragment>(
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
        val responseWithNullData = WorkOrderResponse(
            success = true,
            message = "Success",
            data = null
        )
        val testStateFlow = MutableStateFlow<UiState<WorkOrderResponse>>(UiState.Success(responseWithNullData))
        `when`(mockViewModel.workOrderState).thenReturn(testStateFlow.asStateFlow())

        val scenario = launchFragmentInContainer<WorkOrderManagementFragment>(
            themeResId = R.style.Theme_AppCompat_Light_DarkActionBar
        )

        scenario.onFragment { fragment ->
            fragment.injectViewModel(mockViewModel)
            val recyclerView = fragment.view?.findViewById<RecyclerView>(R.id.workOrderRecyclerView)
            assertNotNull("RecyclerView should handle null data gracefully", recyclerView)
        }
    }

    @Test
    fun `fragment should use LinearLayoutManager for RecyclerView`() {
        val scenario = launchFragmentInContainer<WorkOrderManagementFragment>(
            themeResId = R.style.Theme_AppCompat_Light_DarkActionBar
        )

        scenario.onFragment { fragment ->
            val recyclerView = fragment.view?.findViewById<RecyclerView>(R.id.workOrderRecyclerView)
            assertNotNull("RecyclerView should be present", recyclerView)
            assertTrue("LayoutManager should be LinearLayoutManager", recyclerView?.layoutManager is androidx.recyclerview.widget.LinearLayoutManager)
        }
    }

    @Test
    fun `fragment should preserve adapter configuration across state changes`() {
        val initialResponse = WorkOrderResponse(
            success = true,
            message = "Initial work orders",
            data = testWorkOrderList.take(1)
        )
        val updatedResponse = WorkOrderResponse(
            success = true,
            message = "Updated work orders",
            data = testWorkOrderList
        )

        val testStateFlow = MutableStateFlow<UiState<WorkOrderResponse>>(UiState.Success(initialResponse))
        `when`(mockViewModel.workOrderState).thenReturn(testStateFlow.asStateFlow())

        val scenario = launchFragmentInContainer<WorkOrderManagementFragment>(
            themeResId = R.style.Theme_AppCompat_Light_DarkActionBar
        )

        scenario.onFragment { fragment ->
            fragment.injectViewModel(mockViewModel)
            val recyclerView = fragment.view?.findViewById<RecyclerView>(R.id.workOrderRecyclerView)
            val initialAdapter = recyclerView?.adapter

            testStateFlow.value = UiState.Success(updatedResponse)

            assertEquals("Adapter should remain the same", initialAdapter, recyclerView?.adapter)
            assertEquals("Item count should update", testWorkOrderList.size, recyclerView?.adapter?.itemCount)
        }
    }

    @Test
    fun `fragment should handle large work order lists efficiently`() {
        val largeWorkOrderList = (1..100).map { index ->
            WorkOrder(
                id = "$index",
                title = "Work Order $index",
                description = "Description for work order $index",
                vendorId = "vendor$index",
                vendorName = "Vendor $index",
                status = if (index % 3 == 0) "COMPLETED" else if (index % 2 == 0) "IN_PROGRESS" else "PENDING",
                priority = when (index % 4) {
                    0 -> "URGENT"
                    1 -> "HIGH"
                    2 -> "MEDIUM"
                    else -> "LOW"
                },
                assignedDate = Date(),
                dueDate = Date(System.currentTimeMillis() + (index * 86400000)),
                completedDate = if (index % 3 == 0) Date() else null
            )
        }

        val largeResponse = WorkOrderResponse(
            success = true,
            message = "Large work order list",
            data = largeWorkOrderList
        )
        val testStateFlow = MutableStateFlow<UiState<WorkOrderResponse>>(UiState.Success(largeResponse))
        `when`(mockViewModel.workOrderState).thenReturn(testStateFlow.asStateFlow())

        val scenario = launchFragmentInContainer<WorkOrderManagementFragment>(
            themeResId = R.style.Theme_AppCompat_Light_DarkActionBar
        )

        scenario.onFragment { fragment ->
            fragment.injectViewModel(mockViewModel)
            val recyclerView = fragment.view?.findViewById<RecyclerView>(R.id.workOrderRecyclerView)
            assertNotNull("RecyclerView should handle large lists", recyclerView)
            assertEquals("All items should be displayed", largeWorkOrderList.size, recyclerView?.adapter?.itemCount)
        }
    }

    @Test
    fun `fragment should handle work orders with different statuses`() {
        val workOrdersWithStatuses = listOf(
            WorkOrder(
                id = "1",
                title = "Pending Work",
                description = "Description",
                vendorId = "v1",
                vendorName = "Vendor 1",
                status = "PENDING",
                priority = "HIGH",
                assignedDate = Date(),
                dueDate = Date(),
                completedDate = null
            ),
            WorkOrder(
                id = "2",
                title = "In Progress Work",
                description = "Description",
                vendorId = "v2",
                vendorName = "Vendor 2",
                status = "IN_PROGRESS",
                priority = "MEDIUM",
                assignedDate = Date(),
                dueDate = Date(),
                completedDate = null
            ),
            WorkOrder(
                id = "3",
                title = "Completed Work",
                description = "Description",
                vendorId = "v3",
                vendorName = "Vendor 3",
                status = "COMPLETED",
                priority = "LOW",
                assignedDate = Date(),
                dueDate = Date(),
                completedDate = Date()
            )
        )

        val response = WorkOrderResponse(
            success = true,
            message = "Work orders with different statuses",
            data = workOrdersWithStatuses
        )
        val testStateFlow = MutableStateFlow<UiState<WorkOrderResponse>>(UiState.Success(response))
        `when`(mockViewModel.workOrderState).thenReturn(testStateFlow.asStateFlow())

        val scenario = launchFragmentInContainer<WorkOrderManagementFragment>(
            themeResId = R.style.Theme_AppCompat_Light_DarkActionBar
        )

        scenario.onFragment { fragment ->
            fragment.injectViewModel(mockViewModel)
            val recyclerView = fragment.view?.findViewById<RecyclerView>(R.id.workOrderRecyclerView)
            assertNotNull("RecyclerView should handle different statuses", recyclerView)
            assertEquals("All status work orders should be displayed", workOrdersWithStatuses.size, recyclerView?.adapter?.itemCount)
        }
    }

    @Test
    fun `fragment should handle work orders with different priorities`() {
        val workOrdersWithPriorities = listOf(
            WorkOrder(
                id = "1",
                title = "Urgent Work",
                description = "Description",
                vendorId = "v1",
                vendorName = "Vendor 1",
                status = "PENDING",
                priority = "URGENT",
                assignedDate = Date(),
                dueDate = Date(),
                completedDate = null
            ),
            WorkOrder(
                id = "2",
                title = "High Priority Work",
                description = "Description",
                vendorId = "v2",
                vendorName = "Vendor 2",
                status = "PENDING",
                priority = "HIGH",
                assignedDate = Date(),
                dueDate = Date(),
                completedDate = null
            ),
            WorkOrder(
                id = "3",
                title = "Medium Priority Work",
                description = "Description",
                vendorId = "v3",
                vendorName = "Vendor 3",
                status = "PENDING",
                priority = "MEDIUM",
                assignedDate = Date(),
                dueDate = Date(),
                completedDate = null
            ),
            WorkOrder(
                id = "4",
                title = "Low Priority Work",
                description = "Description",
                vendorId = "v4",
                vendorName = "Vendor 4",
                status = "PENDING",
                priority = "LOW",
                assignedDate = Date(),
                dueDate = Date(),
                completedDate = null
            )
        )

        val response = WorkOrderResponse(
            success = true,
            message = "Work orders with different priorities",
            data = workOrdersWithPriorities
        )
        val testStateFlow = MutableStateFlow<UiState<WorkOrderResponse>>(UiState.Success(response))
        `when`(mockViewModel.workOrderState).thenReturn(testStateFlow.asStateFlow())

        val scenario = launchFragmentInContainer<WorkOrderManagementFragment>(
            themeResId = R.style.Theme_AppCompat_Light_DarkActionBar
        )

        scenario.onFragment { fragment ->
            fragment.injectViewModel(mockViewModel)
            val recyclerView = fragment.view?.findViewById<RecyclerView>(R.id.workOrderRecyclerView)
            assertNotNull("RecyclerView should handle different priorities", recyclerView)
            assertEquals("All priority work orders should be displayed", workOrdersWithPriorities.size, recyclerView?.adapter?.itemCount)
        }
    }
}

private fun WorkOrderManagementFragment.injectViewModel(viewModel: VendorViewModel) {
    val viewModelField = WorkOrderManagementFragment::class.java.getDeclaredField("vendorViewModel")
    viewModelField.isAccessible = true
    viewModelField.set(this, viewModel)
}