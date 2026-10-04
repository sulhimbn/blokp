package com.example.iurankomplek.data.repository

import com.example.iurankomplek.TestFixtures
import com.example.iurankomplek.data.api.models.SingleVendorResponse
import com.example.iurankomplek.data.api.models.SingleWorkOrderResponse
import com.example.iurankomplek.data.api.models.VendorResponse
import com.example.iurankomplek.data.api.models.WorkOrderResponse
import com.example.iurankomplek.network.ApiService
import com.example.iurankomplek.utils.CacheManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class VendorRepositoryImplTest {

    private lateinit var apiService: ApiService
    private lateinit var repository: VendorRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        apiService = mock(ApiService::class.java)
        CacheManager.getInstance().clearSync()
        repository = VendorRepositoryImpl(apiService)
    }

    @After
    fun tearDown() {
        CacheManager.getInstance().clearSync()
        Dispatchers.resetMain()
    }

    private fun <T> errorResponse(code: Int): Response<T> =
        Response.error(code, "{}".toResponseBody("application/json".toMediaType()))

    @Test
    fun `getVendors returns the vendor list from the API`() = runTest(testDispatcher) {
        val payload = VendorResponse(listOf(TestFixtures.vendor()))
        `when`(apiService.getVendors()).thenReturn(Response.success(payload))

        val result = repository.getVendors()

        assertTrue(result.isSuccess)
        assertEquals(listOf("CV Sinar Jaya"), result.getOrNull()?.data?.map { it.name })
    }

    @Test
    fun `getVendors returns an empty list without failing`() = runTest(testDispatcher) {
        `when`(apiService.getVendors()).thenReturn(Response.success(VendorResponse(emptyList())))

        val result = repository.getVendors()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.data.isEmpty())
    }

    @Test
    fun `getVendors fails with HttpException on a 404`() = runTest(testDispatcher) {
        `when`(apiService.getVendors()).thenReturn(errorResponse<VendorResponse>(404))

        val result = repository.getVendors()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is HttpException)
        assertEquals(404, (result.exceptionOrNull() as HttpException).code())
    }

    @Test
    fun `getVendors propagates a network IOException`() = runTest(testDispatcher) {
        `when`(apiService.getVendors()).thenAnswer { throw IOException("socket closed") }

        val result = repository.getVendors()

        assertTrue(result.isFailure)
        assertEquals("socket closed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getVendors is served from cache on the second call`() = runTest(testDispatcher) {
        `when`(apiService.getVendors()).thenReturn(Response.success(VendorResponse(listOf(TestFixtures.vendor()))))

        repository.getVendors()
        repository.getVendors()

        verify(apiService, times(1)).getVendors()
    }

    @Test
    fun `getVendor caches each vendor under its own key`() = runTest(testDispatcher) {
        val a = SingleVendorResponse(TestFixtures.vendor(id = "v-1"))
        val b = SingleVendorResponse(TestFixtures.vendor(id = "v-2"))
        `when`(apiService.getVendor("v-1")).thenReturn(Response.success(a))
        `when`(apiService.getVendor("v-2")).thenReturn(Response.success(b))

        assertEquals(a, repository.getVendor("v-1").getOrNull())
        assertEquals(b, repository.getVendor("v-2").getOrNull())

        repository.getVendor("v-1")
        verify(apiService, times(1)).getVendor("v-1")
        verify(apiService, times(1)).getVendor("v-2")
    }

    @Test
    fun `getWorkOrders returns work orders from the API`() = runTest(testDispatcher) {
        val payload = WorkOrderResponse(listOf(TestFixtures.workOrder()))
        `when`(apiService.getWorkOrders()).thenReturn(Response.success(payload))

        val result = repository.getWorkOrders()

        assertTrue(result.isSuccess)
        assertEquals(listOf("Pipa bocor"), result.getOrNull()?.data?.map { it.title })
    }

    @Test
    fun `getWorkOrder returns a single work order`() = runTest(testDispatcher) {
        val payload = SingleWorkOrderResponse(TestFixtures.workOrder(id = "wo-9"))
        `when`(apiService.getWorkOrder("wo-9")).thenReturn(Response.success(payload))

        val result = repository.getWorkOrder("wo-9")

        assertEquals(payload, result.getOrNull())
    }

    @Test
    fun `createVendor forwards every field to the API and invalidates the vendor cache`() =
        runTest(testDispatcher) {
            val created = SingleVendorResponse(TestFixtures.vendor(id = "v-new"))
            `when`(apiService.getVendors()).thenReturn(Response.success(VendorResponse(listOf(TestFixtures.vendor()))))
            repository.getVendors()
            `when`(
                apiService.createVendor(
                    "CV Sinar Jaya", "Budi", "0812000000", "budi@sinarjaya.test",
                    "plumbing", "Jl. Industri 5", "LIC-001", "AS-001", "2026-01-01", "2026-12-31"
                )
            ).thenReturn(Response.success(created))

            val result = repository.createVendor(
                "CV Sinar Jaya", "Budi", "0812000000", "budi@sinarjaya.test",
                "plumbing", "Jl. Industri 5", "LIC-001", "AS-001", "2026-01-01", "2026-12-31"
            )

            assertEquals(created, result.getOrNull())
            assertTrue(!CacheManager.getInstance().contains("vendor_list"))
        }

    @Test
    fun `createWorkOrder forwards every field to the API and invalidates the work order cache`() =
        runTest(testDispatcher) {
            val created = SingleWorkOrderResponse(TestFixtures.workOrder(id = "wo-new"))
            `when`(apiService.getWorkOrders()).thenReturn(Response.success(WorkOrderResponse(listOf(TestFixtures.workOrder()))))
            repository.getWorkOrders()
            `when`(
                apiService.createWorkOrder("Pipa bocor", "Bocor di lantai 2", "plumbing", "high", "prop-1", "user-1", 500_000.0)
            ).thenReturn(Response.success(created))

            val result = repository.createWorkOrder(
                "Pipa bocor", "Bocor di lantai 2", "plumbing", "high", "prop-1", "user-1", 500_000.0
            )

            assertEquals(created, result.getOrNull())
            assertTrue(!CacheManager.getInstance().contains("work_order_list"))
        }

    @Test
    fun `assignVendorToWorkOrder passes a null scheduled date through`() = runTest(testDispatcher) {
        val assigned = SingleWorkOrderResponse(TestFixtures.workOrder(vendorId = "v-1", scheduledDate = null))
        `when`(apiService.assignVendorToWorkOrder("wo-1", "v-1", null)).thenReturn(Response.success(assigned))

        val result = repository.assignVendorToWorkOrder("wo-1", "v-1", null)

        assertEquals(assigned, result.getOrNull())
        verify(apiService).assignVendorToWorkOrder("wo-1", "v-1", null)
    }

    @Test
    fun `updateWorkOrderStatus fails with HttpException on a 400`() = runTest(testDispatcher) {
        `when`(apiService.updateWorkOrderStatus("wo-1", "completed", "done"))
            .thenReturn(errorResponse<SingleWorkOrderResponse>(400))

        val result = repository.updateWorkOrderStatus("wo-1", "completed", "done")

        assertTrue(result.isFailure)
        assertEquals(400, (result.exceptionOrNull() as HttpException).code())
    }

    @Test
    fun `a null body on a 200 is reported as a failure`() = runTest(testDispatcher) {
        `when`(apiService.getVendors()).thenReturn(Response.success(null))

        val result = repository.getVendors()

        assertTrue(result.isFailure)
        assertEquals("Response body is null", result.exceptionOrNull()?.message)
    }
}
