package com.example.iurankomplek.domain.usecase

import com.example.iurankomplek.data.api.models.UserResponse
import com.example.iurankomplek.data.repository.UserRepository
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

class LoadUsersUseCaseTest {

    @Mock
    private lateinit var mockRepository: UserRepository

    private lateinit var useCase: LoadUsersUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = LoadUsersUseCase(mockRepository)
    }

    @Test
    fun `invoke returns success when repository succeeds`() = runTest {
        val expectedResponse = UserResponse(
            status = "success",
            message = "Users retrieved successfully",
            data = emptyList()
        )

        doReturn(Result.success(expectedResponse)).`when`(mockRepository).getUsers()

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(expectedResponse, result.getOrNull())
        verify(mockRepository).getUsers()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns failure when repository fails`() = runTest {
        val exception = RuntimeException("Network error")
        doReturn(Result.failure<UserResponse>(exception)).`when`(mockRepository).getUsers()

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(mockRepository).getUsers()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns success when repository throws exception`() = runTest {
        val expectedResponse = UserResponse(
            status = "success",
            message = "Users retrieved successfully",
            data = emptyList()
        )

        doReturn(expectedResponse).`when`(mockRepository).getUsers()

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(expectedResponse, result.getOrNull())
        verify(mockRepository).getUsers()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke with forceRefresh true calls repository`() = runTest {
        val expectedResponse = UserResponse(
            status = "success",
            message = "Users retrieved successfully",
            data = emptyList()
        )

        doReturn(Result.success(expectedResponse)).`when`(mockRepository).getUsers()

        val result = useCase(forceRefresh = true)

        assertTrue(result.isSuccess)
        assertEquals(expectedResponse, result.getOrNull())
        verify(mockRepository).getUsers()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke with forceRefresh false calls repository`() = runTest {
        val expectedResponse = UserResponse(
            status = "success",
            message = "Users retrieved successfully",
            data = emptyList()
        )

        doReturn(Result.success(expectedResponse)).`when`(mockRepository).getUsers()

        val result = useCase(forceRefresh = false)

        assertTrue(result.isSuccess)
        assertEquals(expectedResponse, result.getOrNull())
        verify(mockRepository).getUsers()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns failure when repository throws exception`() = runTest {
        val exception = RuntimeException("Network error")
        doThrow(exception).`when`(mockRepository).getUsers()

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(mockRepository).getUsers()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke with forceRefresh true handles repository exception`() = runTest {
        val exception = RuntimeException("Network error")
        doThrow(exception).`when`(mockRepository).getUsers()

        val result = useCase(forceRefresh = true)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(mockRepository).getUsers()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke with forceRefresh false handles repository exception`() = runTest {
        val exception = RuntimeException("Network error")
        doThrow(exception).`when`(mockRepository).getUsers()

        val result = useCase(forceRefresh = false)

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(mockRepository).getUsers()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns successful response with user data`() = runTest {
        val expectedResponse = UserResponse(
            status = "success",
            message = "Users retrieved successfully",
            data = emptyList()
        )

        doReturn(Result.success(expectedResponse)).`when`(mockRepository).getUsers()

        val result = useCase()

        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertNotNull(response)
        assertEquals("success", response?.status)
        assertEquals("Users retrieved successfully", response?.message)
        verify(mockRepository).getUsers()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke propagates error details from repository failure`() = runTest {
        val errorMessage = "Failed to fetch users: Timeout"
        val exception = RuntimeException(errorMessage)
        doReturn(Result.failure<UserResponse>(exception)).`when`(mockRepository).getUsers()

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals(errorMessage, result.exceptionOrNull()?.message)
        verify(mockRepository).getUsers()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns failure for null response from repository`() = runTest {
        val exception = NullPointerException("Response is null")
        doThrow(exception).`when`(mockRepository).getUsers()

        val result = useCase()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NullPointerException)
        verify(mockRepository).getUsers()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke handles IOException from repository`() = runTest {
        val exception = java.io.IOException("No network connection")
        doThrow(exception).`when`(mockRepository).getUsers()

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(mockRepository).getUsers()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke handles IllegalStateException from repository`() = runTest {
        val exception = IllegalStateException("Repository not initialized")
        doThrow(exception).`when`(mockRepository).getUsers()

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(mockRepository).getUsers()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns Result type for successful operation`() = runTest {
        val expectedResponse = UserResponse(
            status = "success",
            message = "Success",
            data = emptyList()
        )

        doReturn(Result.success(expectedResponse)).`when`(mockRepository).getUsers()

        val result = useCase()

        assertTrue(result.isSuccess)
        assertFalse(result.isFailure)
        assertNotNull(result.getOrNull())
        verify(mockRepository).getUsers()
        verifyNoMoreInteractions(mockRepository)
    }

    @Test
    fun `invoke returns Result type for failed operation`() = runTest {
        val exception = RuntimeException("Error")
        doReturn(Result.failure<UserResponse>(exception)).`when`(mockRepository).getUsers()

        val result = useCase()

        assertTrue(result.isFailure)
        assertFalse(result.isSuccess)
        assertNull(result.getOrNull())
        assertNotNull(result.exceptionOrNull())
        verify(mockRepository).getUsers()
        verifyNoMoreInteractions(mockRepository)
    }
}
