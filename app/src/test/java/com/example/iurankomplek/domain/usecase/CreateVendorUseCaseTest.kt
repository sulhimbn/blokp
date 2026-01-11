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

class CreateVendorUseCaseTest {

    @Mock
    private lateinit var mockRepository: VendorRepository

    private lateinit var useCase: CreateVendorUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = CreateVendorUseCase(mockRepository)
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
            rating = 0.0,
            totalReviews = 0,
            contractStart = "2024-01-01",
            contractEnd = "2025-01-01",
            isActive = true
        )
        val expectedResponse = SingleVendorResponse(data = expectedVendor)
        doReturn(OperationResult.Success(expectedResponse)).`when`(mockRepository).createVendor(
            name = "ABC Plumbing",
            contactPerson = "John Doe",
            phoneNumber = "+1234567890",
            email = "john@abcplumbing.com",
            specialty = "plumbing",
            address = "123 Main St",
            licenseNumber = "LIC12345",
            insuranceInfo = "INS67890",
            contractStart = "2024-01-01",
            contractEnd = "2025-01-01"
        )

        val result = useCase(
            name = "ABC Plumbing",
            contactPerson = "John Doe",
            phoneNumber = "+1234567890",
            email = "john@abcplumbing.com",
            specialty = "plumbing",
            address = "123 Main St",
            licenseNumber = "LIC12345",
            insuranceInfo = "INS67890",
            contractStart = "2024-01-01",
            contractEnd = "2025-01-01"
        )

        assertTrue(result.isSuccess)
        assertEquals(Unit, result.getOrNull())
        verify(mockRepository).createVendor(
            name = "ABC Plumbing",
            contactPerson = "John Doe",
            phoneNumber = "+1234567890",
            email = "john@abcplumbing.com",
            specialty = "plumbing",
            address = "123 Main St",
            licenseNumber = "LIC12345",
            insuranceInfo = "INS67890",
            contractStart = "2024-01-01",
            contractEnd = "2025-01-01"
        )
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns error when repository throws exception`() = runTest {
        val exception = RuntimeException("Network error")
        doThrow(exception).`when`(mockRepository).createVendor(
            name = "ABC Plumbing",
            contactPerson = "John Doe",
            phoneNumber = "+1234567890",
            email = "john@abcplumbing.com",
            specialty = "plumbing",
            address = "123 Main St",
            licenseNumber = "LIC12345",
            insuranceInfo = "INS67890",
            contractStart = "2024-01-01",
            contractEnd = "2025-01-01"
        )

        val result = useCase(
            name = "ABC Plumbing",
            contactPerson = "John Doe",
            phoneNumber = "+1234567890",
            email = "john@abcplumbing.com",
            specialty = "plumbing",
            address = "123 Main St",
            licenseNumber = "LIC12345",
            insuranceInfo = "INS67890",
            contractStart = "2024-01-01",
            contractEnd = "2025-01-01"
        )

        assertTrue(result.isError)
        assertEquals(exception, (result as OperationResult.Error).exception)
        assertEquals("Network error", result.errorMessage)
        verify(mockRepository).createVendor(
            name = "ABC Plumbing",
            contactPerson = "John Doe",
            phoneNumber = "+1234567890",
            email = "john@abcplumbing.com",
            specialty = "plumbing",
            address = "123 Main St",
            licenseNumber = "LIC12345",
            insuranceInfo = "INS67890",
            contractStart = "2024-01-01",
            contractEnd = "2025-01-01"
        )
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns error when repository throws IOException`() = runTest {
        val exception = java.io.IOException("Connection timeout")
        doThrow(exception).`when`(mockRepository).createVendor(
            name = "XYZ Electrical",
            contactPerson = "Jane Smith",
            phoneNumber = "+0987654321",
            email = "jane@xyzelectrical.com",
            specialty = "electrical",
            address = "456 Oak Ave",
            licenseNumber = "LIC54321",
            insuranceInfo = "INS09876",
            contractStart = "2024-06-01",
            contractEnd = "2025-06-01"
        )

        val result = useCase(
            name = "XYZ Electrical",
            contactPerson = "Jane Smith",
            phoneNumber = "+0987654321",
            email = "jane@xyzelectrical.com",
            specialty = "electrical",
            address = "456 Oak Ave",
            licenseNumber = "LIC54321",
            insuranceInfo = "INS09876",
            contractStart = "2024-06-01",
            contractEnd = "2025-06-01"
        )

        assertTrue(result.isError)
        assertEquals(exception, (result as OperationResult.Error).exception)
        assertEquals("Connection timeout", result.errorMessage)
        verify(mockRepository).createVendor(
            name = "XYZ Electrical",
            contactPerson = "Jane Smith",
            phoneNumber = "+0987654321",
            email = "jane@xyzelectrical.com",
            specialty = "electrical",
            address = "456 Oak Ave",
            licenseNumber = "LIC54321",
            insuranceInfo = "INS09876",
            contractStart = "2024-06-01",
            contractEnd = "2025-06-01"
        )
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns error with default message when exception has no message`() = runTest {
        val exception = RuntimeException()
        doThrow(exception).`when`(mockRepository).createVendor(
            any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
        )

        val result = useCase(
            name = "Test Vendor",
            contactPerson = "Test Person",
            phoneNumber = "+1111111111",
            email = "test@test.com",
            specialty = "test",
            address = "Test Address",
            licenseNumber = "TEST",
            insuranceInfo = "TEST",
            contractStart = "2024-01-01",
            contractEnd = "2025-01-01"
        )

        assertTrue(result.isError)
        assertEquals("Failed to create vendor", result.errorMessage)
        verify(mockRepository).createVendor(
            any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
        )
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke creates vendor with plumbing specialty`() = runTest {
        val expectedResponse = SingleVendorResponse(
            data = Vendor(
                id = "vendor1",
                name = "ABC Plumbing",
                contactPerson = "John Doe",
                phoneNumber = "+1234567890",
                email = "john@abcplumbing.com",
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
        )
        doReturn(OperationResult.Success(expectedResponse)).`when`(mockRepository).createVendor(
            any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
        )

        val result = useCase(
            name = "ABC Plumbing",
            contactPerson = "John Doe",
            phoneNumber = "+1234567890",
            email = "john@abcplumbing.com",
            specialty = "plumbing",
            address = "123 Main St",
            licenseNumber = "LIC12345",
            insuranceInfo = "INS67890",
            contractStart = "2024-01-01",
            contractEnd = "2025-01-01"
        )

        assertTrue(result.isSuccess)
        verify(mockRepository).createVendor(
            name = "ABC Plumbing",
            contactPerson = "John Doe",
            phoneNumber = "+1234567890",
            email = "john@abcplumbing.com",
            specialty = "plumbing",
            address = "123 Main St",
            licenseNumber = "LIC12345",
            insuranceInfo = "INS67890",
            contractStart = "2024-01-01",
            contractEnd = "2025-01-01"
        )
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke creates vendor with electrical specialty`() = runTest {
        val expectedResponse = SingleVendorResponse(
            data = Vendor(
                id = "vendor2",
                name = "XYZ Electrical",
                contactPerson = "Jane Smith",
                phoneNumber = "+0987654321",
                email = "jane@xyzelectrical.com",
                specialty = "electrical",
                address = "456 Oak Ave",
                licenseNumber = "LIC54321",
                insuranceInfo = "INS09876",
                certifications = emptyList(),
                rating = 0.0,
                totalReviews = 0,
                contractStart = "2024-01-01",
                contractEnd = "2025-01-01",
                isActive = true
            )
        )
        doReturn(OperationResult.Success(expectedResponse)).`when`(mockRepository).createVendor(
            any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
        )

        val result = useCase(
            name = "XYZ Electrical",
            contactPerson = "Jane Smith",
            phoneNumber = "+0987654321",
            email = "jane@xyzelectrical.com",
            specialty = "electrical",
            address = "456 Oak Ave",
            licenseNumber = "LIC54321",
            insuranceInfo = "INS09876",
            contractStart = "2024-01-01",
            contractEnd = "2025-01-01"
        )

        assertTrue(result.isSuccess)
        verify(mockRepository).createVendor(
            name = "XYZ Electrical",
            contactPerson = "Jane Smith",
            phoneNumber = "+0987654321",
            email = "jane@xyzelectrical.com",
            specialty = "electrical",
            address = "456 Oak Ave",
            licenseNumber = "LIC54321",
            insuranceInfo = "INS09876",
            contractStart = "2024-01-01",
            contractEnd = "2025-01-01"
        )
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke creates vendor with landscaping specialty`() = runTest {
        val expectedResponse = SingleVendorResponse(
            data = Vendor(
                id = "vendor3",
                name = "Green Landscaping",
                contactPerson = "Bob Green",
                phoneNumber = "+5555555555",
                email = "bob@greenlandscaping.com",
                specialty = "landscaping",
                address = "789 Pine Rd",
                licenseNumber = "LIC99999",
                insuranceInfo = "INS88888",
                certifications = emptyList(),
                rating = 0.0,
                totalReviews = 0,
                contractStart = "2024-01-01",
                contractEnd = "2025-01-01",
                isActive = true
            )
        )
        doReturn(OperationResult.Success(expectedResponse)).`when`(mockRepository).createVendor(
            any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
        )

        val result = useCase(
            name = "Green Landscaping",
            contactPerson = "Bob Green",
            phoneNumber = "+5555555555",
            email = "bob@greenlandscaping.com",
            specialty = "landscaping",
            address = "789 Pine Rd",
            licenseNumber = "LIC99999",
            insuranceInfo = "INS88888",
            contractStart = "2024-01-01",
            contractEnd = "2025-01-01"
        )

        assertTrue(result.isSuccess)
        verify(mockRepository).createVendor(
            name = "Green Landscaping",
            contactPerson = "Bob Green",
            phoneNumber = "+5555555555",
            email = "bob@greenlandscaping.com",
            specialty = "landscaping",
            address = "789 Pine Rd",
            licenseNumber = "LIC99999",
            insuranceInfo = "INS88888",
            contractStart = "2024-01-01",
            contractEnd = "2025-01-01"
        )
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke creates vendor with all contract dates`() = runTest {
        val expectedResponse = SingleVendorResponse(
            data = Vendor(
                id = "vendor1",
                name = "Test Vendor",
                contactPerson = "Test Person",
                phoneNumber = "+1111111111",
                email = "test@test.com",
                specialty = "test",
                address = "Test Address",
                licenseNumber = "TEST",
                insuranceInfo = "TEST",
                certifications = emptyList(),
                rating = 0.0,
                totalReviews = 0,
                contractStart = "2024-01-01",
                contractEnd = "2025-01-01",
                isActive = true
            )
        )
        doReturn(OperationResult.Success(expectedResponse)).`when`(mockRepository).createVendor(
            any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
        )

        val result = useCase(
            name = "Test Vendor",
            contactPerson = "Test Person",
            phoneNumber = "+1111111111",
            email = "test@test.com",
            specialty = "test",
            address = "Test Address",
            licenseNumber = "TEST",
            insuranceInfo = "TEST",
            contractStart = "2024-01-01",
            contractEnd = "2025-01-01"
        )

        assertTrue(result.isSuccess)
        verify(mockRepository).createVendor(
            name = "Test Vendor",
            contactPerson = "Test Person",
            phoneNumber = "+1111111111",
            email = "test@test.com",
            specialty = "test",
            address = "Test Address",
            licenseNumber = "TEST",
            insuranceInfo = "TEST",
            contractStart = "2024-01-01",
            contractEnd = "2025-01-01"
        )
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke creates vendor with minimal required fields`() = runTest {
        val expectedResponse = SingleVendorResponse(
            data = Vendor(
                id = "vendor1",
                name = "Test",
                contactPerson = "Test",
                phoneNumber = "+1",
                email = "t@t.com",
                specialty = "test",
                address = "Test",
                licenseNumber = "T",
                insuranceInfo = "I",
                certifications = emptyList(),
                rating = 0.0,
                totalReviews = 0,
                contractStart = "2024-01-01",
                contractEnd = "2025-01-01",
                isActive = true
            )
        )
        doReturn(OperationResult.Success(expectedResponse)).`when`(mockRepository).createVendor(
            any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
        )

        val result = useCase(
            name = "Test",
            contactPerson = "Test",
            phoneNumber = "+1",
            email = "t@t.com",
            specialty = "test",
            address = "Test",
            licenseNumber = "T",
            insuranceInfo = "I",
            contractStart = "2024-01-01",
            contractEnd = "2025-01-01"
        )

        assertTrue(result.isSuccess)
        verify(mockRepository).createVendor(
            name = "Test",
            contactPerson = "Test",
            phoneNumber = "+1",
            email = "t@t.com",
            specialty = "test",
            address = "Test",
            licenseNumber = "T",
            insuranceInfo = "I",
            contractStart = "2024-01-01",
            contractEnd = "2025-01-01"
        )
        verifyNoMoreInteractions(mockRepository)
    }
}
