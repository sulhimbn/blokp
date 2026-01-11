package com.example.iurankomplek.domain.usecase

import com.example.iurankomplek.data.repository.VendorRepository
import com.example.iurankomplek.model.SingleVendorResponse
import com.example.iurankomplek.model.Vendor
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

class LoadVendorDetailUseCaseTest {

    @Mock
    private lateinit var mockRepository: VendorRepository

    private lateinit var useCase: LoadVendorDetailUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = LoadVendorDetailUseCase(mockRepository)
    }

    @Test
    fun `invoke returns success when repository succeeds`() = runTest {
        val expectedVendor = Vendor(
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
            totalReviews = 100,
            contractStart = "2024-01-01",
            contractEnd = "2025-01-01",
            isActive = true
        )
        val expectedResponse = SingleVendorResponse(data = expectedVendor)
        doReturn(expectedResponse).`when`(mockRepository).getVendor("vendor1")

        val result = useCase("vendor1")

        assertTrue(result.isSuccess)
        val vendor = result.getOrNull()
        assertNotNull(vendor)
        assertEquals(expectedVendor, vendor?.data)
        assertEquals("vendor1", vendor?.data?.id)
        assertEquals("ABC Plumbing", vendor?.data?.name)
        verify(mockRepository).getVendor("vendor1")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns error when repository throws IOException`() = runTest {
        val exception = java.io.IOException("Network error")
        doThrow(exception).`when`(mockRepository).getVendor("vendor1")

        val result = useCase("vendor1")

        assertTrue(result.isError)
        val error = result.errorOrNull()
        assertNotNull(error)
        assertEquals(exception, error?.exception)
        assertEquals("Network error", error?.message)
        verify(mockRepository).getVendor("vendor1")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns error with default message when repository throws exception without message`() = runTest {
        val exception = RuntimeException()
        doThrow(exception).`when`(mockRepository).getVendor("vendor1")

        val result = useCase("vendor1")

        assertTrue(result.isError)
        val error = result.errorOrNull()
        assertNotNull(error)
        assertEquals(exception, error?.exception)
        assertEquals("Failed to load vendor details", error?.message)
        verify(mockRepository).getVendor("vendor1")
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success with all fields preserved`() = runTest {
        val expectedVendor = Vendor(
            id = "vendor2",
            name = "XYZ Electrical",
            contactPerson = "Jane Smith",
            phoneNumber = "+9876543210",
            email = "jane@xyzelectrical.com",
            specialty = "electrical",
            address = "456 Oak Ave",
            licenseNumber = "LIC54321",
            insuranceInfo = "INS09876",
            certifications = listOf("Electrician Cert", "Safety Cert", "Master License"),
            rating = 4.8,
            totalReviews = 250,
            contractStart = "2023-06-01",
            contractEnd = "2025-06-01",
            isActive = true
        )
        val expectedResponse = SingleVendorResponse(data = expectedVendor)
        doReturn(expectedResponse).`when`(mockRepository).getVendor("vendor2")

        val result = useCase("vendor2")

        assertTrue(result.isSuccess)
        val vendor = result.getOrNull()
        assertNotNull(vendor)
        assertEquals("vendor2", vendor?.data?.id)
        assertEquals("XYZ Electrical", vendor?.data?.name)
        assertEquals("Jane Smith", vendor?.data?.contactPerson)
        assertEquals("+9876543210", vendor?.data?.phoneNumber)
        assertEquals("jane@xyzelectrical.com", vendor?.data?.email)
        assertEquals("electrical", vendor?.data?.specialty)
        assertEquals("456 Oak Ave", vendor?.data?.address)
        assertEquals("LIC54321", vendor?.data?.licenseNumber)
        assertEquals("INS09876", vendor?.data?.insuranceInfo)
        assertEquals(listOf("Electrician Cert", "Safety Cert", "Master License"), vendor?.data?.certifications)
        assertEquals(4.8, vendor?.data?.rating, 0.001)
        assertEquals(250, vendor?.data?.totalReviews)
        assertEquals("2023-06-01", vendor?.data?.contractStart)
        assertEquals("2025-06-01", vendor?.data?.contractEnd)
        assertTrue(vendor?.data?.isActive == true)
    }

    @Test
    fun `invoke returns success with different specialties`() = runTest {
        val specialties = listOf("plumbing", "electrical", "landscaping", "hvac", "general")

        specialties.forEach { specialty ->
            val expectedVendor = Vendor(
                id = "vendor_$specialty",
                name = "$specialty Company",
                contactPerson = "Contact",
                phoneNumber = "+1234567890",
                email = "contact@example.com",
                specialty = specialty,
                address = "123 Main St",
                licenseNumber = "LIC12345",
                insuranceInfo = "INS67890",
                certifications = emptyList(),
                rating = 0.0,
                totalReviews = 0,
                contractStart = "2024-01-01",
                contractEnd = "2025-01-01",
                isActive = true
            )
            val expectedResponse = SingleVendorResponse(data = expectedVendor)
            doReturn(expectedResponse).`when`(mockRepository).getVendor("vendor_$specialty")

            val result = useCase("vendor_$specialty")

            assertTrue(result.isSuccess)
            assertEquals(specialty, result.getOrNull()?.data?.specialty)
        }
    }

    @Test
    fun `invoke returns success with inactive vendor`() = runTest {
        val expectedVendor = Vendor(
            id = "vendor3",
            name = "Old Service Co",
            contactPerson = "Old Contact",
            phoneNumber = "+1111111111",
            email = "old@example.com",
            specialty = "plumbing",
            address = "789 Old Rd",
            licenseNumber = "LIC00000",
            insuranceInfo = "INS00000",
            certifications = emptyList(),
            rating = 3.5,
            totalReviews = 50,
            contractStart = "2022-01-01",
            contractEnd = "2023-01-01",
            isActive = false
        )
        val expectedResponse = SingleVendorResponse(data = expectedVendor)
        doReturn(expectedResponse).`when`(mockRepository).getVendor("vendor3")

        val result = useCase("vendor3")

        assertTrue(result.isSuccess)
        assertEquals(false, result.getOrNull()?.data?.isActive)
    }

    @Test
    fun `invoke returns success with numeric ID`() = runTest {
        val expectedVendor = Vendor(
            id = "12345",
            name = "Numeric ID Vendor",
            contactPerson = "Contact",
            phoneNumber = "+1234567890",
            email = "contact@example.com",
            specialty = "plumbing",
            address = "123 Main St",
            licenseNumber = "LIC12345",
            insuranceInfo = "INS67890",
            certifications = emptyList(),
            rating = 0.0,
            totalReviews = 0,
            contractStart = "2024-01-01",
            contractEnd = "2025-01-01",
            isActive = true
        )
        val expectedResponse = SingleVendorResponse(data = expectedVendor)
        doReturn(expectedResponse).`when`(mockRepository).getVendor("12345")

        val result = useCase("12345")

        assertTrue(result.isSuccess)
        assertEquals("12345", result.getOrNull()?.data?.id)
    }

    @Test
    fun `invoke returns success with alphanumeric ID`() = runTest {
        val expectedVendor = Vendor(
            id = "vendor_abc123_xyz",
            name = "Alphanumeric ID Vendor",
            contactPerson = "Contact",
            phoneNumber = "+1234567890",
            email = "contact@example.com",
            specialty = "plumbing",
            address = "123 Main St",
            licenseNumber = "LIC12345",
            insuranceInfo = "INS67890",
            certifications = emptyList(),
            rating = 0.0,
            totalReviews = 0,
            contractStart = "2024-01-01",
            contractEnd = "2025-01-01",
            isActive = true
        )
        val expectedResponse = SingleVendorResponse(data = expectedVendor)
        doReturn(expectedResponse).`when`(mockRepository).getVendor("vendor_abc123_xyz")

        val result = useCase("vendor_abc123_xyz")

        assertTrue(result.isSuccess)
        assertEquals("vendor_abc123_xyz", result.getOrNull()?.data?.id)
    }

    @Test
    fun `invoke passes ID correctly to repository`() = runTest {
        val testId = "test_vendor_id_12345"
        val expectedVendor = Vendor(
            id = testId,
            name = "Test Vendor",
            contactPerson = "Test Contact",
            phoneNumber = "+1234567890",
            email = "test@example.com",
            specialty = "plumbing",
            address = "123 Test St",
            licenseNumber = "LIC12345",
            insuranceInfo = "INS67890",
            certifications = emptyList(),
            rating = 0.0,
            totalReviews = 0,
            contractStart = "2024-01-01",
            contractEnd = "2025-01-01",
            isActive = true
        )
        val expectedResponse = SingleVendorResponse(data = expectedVendor)
        doReturn(expectedResponse).`when`(mockRepository).getVendor(testId)

        val result = useCase(testId)

        assertTrue(result.isSuccess)
        verify(mockRepository).getVendor(testId)
        verifyNoMoreInteractions(mockRepository)
    }
}
