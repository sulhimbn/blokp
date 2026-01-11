package com.example.iurankomplek.domain.usecase

import com.example.iurankomplek.data.repository.VendorRepository
import com.example.iurankomplek.model.Vendor
import com.example.iurankomplek.model.VendorResponse
import com.example.iurankomplek.utils.OperationResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import java.util.Date

class LoadVendorsUseCaseTest {

    @Mock
    private lateinit var mockRepository: VendorRepository

    private lateinit var useCase: LoadVendorsUseCase

    private val testVendor = Vendor(
        id = "vendor1",
        name = "ABC Plumbing",
        contactPerson = "John Doe",
        phoneNumber = "+1234567890",
        email = "john@abcplumbing.com",
        specialty = "plumbing",
        address = "123 Main St",
        licenseNumber = "LIC12345",
        insuranceInfo = "INS67890",
        certifications = listOf("Cert1", "Cert2"),
        rating = 4.5,
        totalReviews = 10,
        contractStart = "2024-01-01",
        contractEnd = "2025-01-01",
        isActive = true
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = LoadVendorsUseCase(mockRepository)
    }

    @Test
    fun `invoke returns success when repository succeeds`() = runTest {
        val expectedResponse = VendorResponse(data = listOf(testVendor))
        doReturn(OperationResult.Success(expectedResponse)).`when`(mockRepository).getVendors()

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(expectedResponse, result.getOrNull())
        verify(mockRepository).getVendors()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with empty list`() = runTest {
        val expectedResponse = VendorResponse(data = emptyList())
        doReturn(OperationResult.Success(expectedResponse)).`when`(mockRepository).getVendors()

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(expectedResponse, result.getOrNull())
        assertEquals(0, result.getOrNull()?.data?.size)
        verify(mockRepository).getVendors()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with multiple vendors`() = runTest {
        val vendor2 = testVendor.copy(
            id = "vendor2",
            name = "XYZ Electrical",
            specialty = "electrical",
            rating = 4.0
        )
        val vendor3 = testVendor.copy(
            id = "vendor3",
            name = "Landscaping Pro",
            specialty = "landscaping",
            rating = 5.0
        )
        val expectedResponse = VendorResponse(data = listOf(testVendor, vendor2, vendor3))
        doReturn(OperationResult.Success(expectedResponse)).`when`(mockRepository).getVendors()

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull()?.data?.size)
        verify(mockRepository).getVendors()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with active vendors only`() = runTest {
        val activeVendor = testVendor.copy(
            id = "vendor2",
            name = "Active Plumbing",
            isActive = true
        )
        val inactiveVendor = testVendor.copy(
            id = "vendor3",
            name = "Inactive Plumbing",
            isActive = false
        )
        val expectedResponse = VendorResponse(data = listOf(activeVendor, inactiveVendor))
        doReturn(OperationResult.Success(expectedResponse)).`when`(mockRepository).getVendors()

        val result = useCase()

        assertTrue(result.isSuccess)
        val vendors = result.getOrNull()?.data
        assertEquals(2, vendors?.size)
        assertTrue(vendors?.first { it.id == "vendor2" }?.isActive == true)
        assertTrue(vendors?.first { it.id == "vendor3" }?.isActive == false)
        verify(mockRepository).getVendors()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns error when repository throws exception`() = runTest {
        val exception = RuntimeException("Network error")
        doThrow(exception).`when`(mockRepository).getVendors()

        val result = useCase()

        assertTrue(result.isError)
        assertEquals(exception, (result as OperationResult.Error).exception)
        assertEquals("Network error", result.errorMessage)
        verify(mockRepository).getVendors()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns error when repository throws IOException`() = runTest {
        val exception = java.io.IOException("Connection timeout")
        doThrow(exception).`when`(mockRepository).getVendors()

        val result = useCase()

        assertTrue(result.isError)
        assertEquals(exception, (result as OperationResult.Error).exception)
        assertEquals("Connection timeout", result.errorMessage)
        verify(mockRepository).getVendors()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns error with default message when exception has no message`() = runTest {
        val exception = RuntimeException()
        doThrow(exception).`when`(mockRepository).getVendors()

        val result = useCase()

        assertTrue(result.isError)
        assertEquals("Failed to load vendors", result.errorMessage)
        verify(mockRepository).getVendors()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke preserves all vendor fields`() = runTest {
        val expectedResponse = VendorResponse(data = listOf(testVendor))
        doReturn(OperationResult.Success(expectedResponse)).`when`(mockRepository).getVendors()

        val result = useCase()
        val vendor = result.getOrNull()?.data?.first()

        assertEquals("vendor1", vendor?.id)
        assertEquals("ABC Plumbing", vendor?.name)
        assertEquals("John Doe", vendor?.contactPerson)
        assertEquals("+1234567890", vendor?.phoneNumber)
        assertEquals("john@abcplumbing.com", vendor?.email)
        assertEquals("plumbing", vendor?.specialty)
        assertEquals("123 Main St", vendor?.address)
        assertEquals("LIC12345", vendor?.licenseNumber)
        assertEquals("INS67890", vendor?.insuranceInfo)
        assertEquals(listOf("Cert1", "Cert2"), vendor?.certifications)
        assertEquals(4.5, vendor?.rating)
        assertEquals(10, vendor?.totalReviews)
        assertEquals("2024-01-01", vendor?.contractStart)
        assertEquals("2025-01-01", vendor?.contractEnd)
        assertTrue(vendor?.isActive == true)
        verify(mockRepository).getVendors()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke handles vendors with different specialties`() = runTest {
        val plumbingVendor = testVendor.copy(id = "v1", specialty = "plumbing")
        val electricalVendor = testVendor.copy(id = "v2", specialty = "electrical")
        val landscapingVendor = testVendor.copy(id = "v3", specialty = "landscaping")
        val hvacVendor = testVendor.copy(id = "v4", specialty = "hvac")
        val expectedResponse = VendorResponse(data = listOf(plumbingVendor, electricalVendor, landscapingVendor, hvacVendor))
        doReturn(OperationResult.Success(expectedResponse)).`when`(mockRepository).getVendors()

        val result = useCase()
        val vendors = result.getOrNull()?.data

        assertEquals(4, vendors?.size)
        assertEquals("plumbing", vendors?.get(0)?.specialty)
        assertEquals("electrical", vendors?.get(1)?.specialty)
        assertEquals("landscaping", vendors?.get(2)?.specialty)
        assertEquals("hvac", vendors?.get(3)?.specialty)
        verify(mockRepository).getVendors()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke handles vendors with different ratings`() = runTest {
        val fiveStarVendor = testVendor.copy(id = "v1", name = "5 Star", rating = 5.0, totalReviews = 50)
        val fourStarVendor = testVendor.copy(id = "v2", name = "4 Star", rating = 4.0, totalReviews = 30)
        val threeStarVendor = testVendor.copy(id = "v3", name = "3 Star", rating = 3.0, totalReviews = 20)
        val expectedResponse = VendorResponse(data = listOf(fiveStarVendor, fourStarVendor, threeStarVendor))
        doReturn(OperationResult.Success(expectedResponse)).`when`(mockRepository).getVendors()

        val result = useCase()
        val vendors = result.getOrNull()?.data

        assertEquals(3, vendors?.size)
        assertEquals(5.0, vendors?.get(0)?.rating)
        assertEquals(50, vendors?.get(0)?.totalReviews)
        assertEquals(4.0, vendors?.get(1)?.rating)
        assertEquals(30, vendors?.get(1)?.totalReviews)
        assertEquals(3.0, vendors?.get(2)?.rating)
        assertEquals(20, vendors?.get(2)?.totalReviews)
        verify(mockRepository).getVendors()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke handles vendors with empty certifications`() = runTest {
        val noCertVendor = testVendor.copy(id = "vendor2", certifications = emptyList())
        val expectedResponse = VendorResponse(data = listOf(testVendor, noCertVendor))
        doReturn(OperationResult.Success(expectedResponse)).`when`(mockRepository).getVendors()

        val result = useCase()
        val vendors = result.getOrNull()?.data

        assertEquals(2, vendors?.size)
        assertEquals(2, vendors?.get(0)?.certifications?.size)
        assertEquals(0, vendors?.get(1)?.certifications?.size)
        verify(mockRepository).getVendors()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke handles large vendor list efficiently`() = runTest {
        val largeVendorList = (1..100).map { i ->
            testVendor.copy(
                id = "vendor$i",
                name = "Vendor $i",
                rating = (i % 5).toDouble()
            )
        }
        val expectedResponse = VendorResponse(data = largeVendorList)
        doReturn(OperationResult.Success(expectedResponse)).`when`(mockRepository).getVendors()

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(100, result.getOrNull()?.data?.size)
        verify(mockRepository).getVendors()
        verifyNoMoreInteractions(mockRepository)
    }
}
