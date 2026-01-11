package com.example.iurankomplek.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.iurankomplek.data.api.models.UserResponse
import com.example.iurankomplek.domain.usecase.LoadUsersUseCase
import com.example.iurankomplek.utils.OperationResult
import com.example.iurankomplek.utils.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class UserViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    @Mock
    private lateinit var mockLoadUsersUseCase: LoadUsersUseCase

    private lateinit var viewModel: UserViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = UserViewModel(mockLoadUsersUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadUsers should set Loading then Success states`() = runTest {
        val userResponse = UserResponse(
            data = listOf(
                com.example.iurankomplek.data.api.models.UserResponse.DataItem(
                    id = 1,
                    firstName = "John",
                    lastName = "Doe",
                    email = "john@example.com",
                    phoneNumber = "1234567890",
                    address = "123 Main St",
                    createdAt = Date(),
                    updatedAt = Date()
                )
            )
        )
        whenever(mockLoadUsersUseCase()).thenReturn(OperationResult.Success(userResponse))

        val states = mutableListOf<UiState<UserResponse>>()
        val job = launch {
            viewModel.usersState.collect { states.add(it) }
        }

        viewModel.loadUsers()
        advanceUntilIdle()

        assertTrue("Should have Loading state", states.any { it is UiState.Loading })
        assertTrue("Should have Success state", states.any { it is UiState.Success })
        val successState = states.filterIsInstance<UiState.Success<UserResponse>>().last()
        assertEquals(userResponse, successState.data)
        verify(mockLoadUsersUseCase)()
        job.cancel()
    }

    @Test
    fun `loadUsers should set Error state when use case returns error`() = runTest {
        val errorMessage = "Network error"
        whenever(mockLoadUsersUseCase()).thenReturn(
            OperationResult.Error(Exception(errorMessage), errorMessage)
        )

        val states = mutableListOf<UiState<UserResponse>>()
        val job = launch {
            viewModel.usersState.collect { states.add(it) }
        }

        viewModel.loadUsers()
        advanceUntilIdle()

        assertTrue("Should have Loading state", states.any { it is UiState.Loading })
        assertTrue("Should have Error state", states.any { it is UiState.Error })
        val errorState = states.filterIsInstance<UiState.Error<UserResponse>>().last()
        assertEquals(errorMessage, errorState.message)
        verify(mockLoadUsersUseCase)()
        job.cancel()
    }

    @Test
    fun `loadUsers should handle use case throwing exception`() = runTest {
        whenever(mockLoadUsersUseCase()).thenThrow(RuntimeException("Unexpected error"))

        val states = mutableListOf<UiState<UserResponse>>()
        val job = launch {
            viewModel.usersState.collect { states.add(it) }
        }

        viewModel.loadUsers()
        advanceUntilIdle()

        assertTrue("Should have Loading state", states.any { it is UiState.Loading })
        assertTrue("Should have Error state", states.any { it is UiState.Error })
        val errorState = states.filterIsInstance<UiState.Error<UserResponse>>().last()
        assertTrue("Error message should contain error", errorState.message?.contains("error") == true)
        verify(mockLoadUsersUseCase)()
        job.cancel()
    }

    @Test
    fun `loadUsers should set Error state for Loading result`() = runTest {
        whenever(mockLoadUsersUseCase()).thenReturn(OperationResult.Loading)

        val states = mutableListOf<UiState<UserResponse>>()
        val job = launch {
            viewModel.usersState.collect { states.add(it) }
        }

        viewModel.loadUsers()
        advanceUntilIdle()

        assertTrue("Should have Loading state", states.any { it is UiState.Loading })
        job.cancel()
    }

    @Test
    fun `loadUsers should set Error state for Empty result`() = runTest {
        whenever(mockLoadUsersUseCase()).thenReturn(OperationResult.Empty)

        val states = mutableListOf<UiState<UserResponse>>()
        val job = launch {
            viewModel.usersState.collect { states.add(it) }
        }

        viewModel.loadUsers()
        advanceUntilIdle()

        assertTrue("Should have Loading state", states.any { it is UiState.Loading })
        assertTrue("Should have Error state", states.any { it is UiState.Error })
        val errorState = states.filterIsInstance<UiState.Error<UserResponse>>().last()
        assertEquals("No data available", errorState.message)
        job.cancel()
    }

    @Test
    fun `loadUsers should handle empty user list`() = runTest {
        val userResponse = UserResponse(data = emptyList())
        whenever(mockLoadUsersUseCase()).thenReturn(OperationResult.Success(userResponse))

        val states = mutableListOf<UiState<UserResponse>>()
        val job = launch {
            viewModel.usersState.collect { states.add(it) }
        }

        viewModel.loadUsers()
        advanceUntilIdle()

        val successState = states.filterIsInstance<UiState.Success<UserResponse>>().last()
        assertTrue("Success state should have data", successState.data.data.isEmpty())
        job.cancel()
    }

    @Test
    fun `loadUsers should handle multiple users`() = runTest {
        val userResponse = UserResponse(
            data = listOf(
                com.example.iurankomplek.data.api.models.UserResponse.DataItem(
                    id = 1,
                    firstName = "John",
                    lastName = "Doe",
                    email = "john@example.com",
                    phoneNumber = "1234567890",
                    address = "123 Main St",
                    createdAt = Date(),
                    updatedAt = Date()
                ),
                com.example.iurankomplek.data.api.models.UserResponse.DataItem(
                    id = 2,
                    firstName = "Jane",
                    lastName = "Smith",
                    email = "jane@example.com",
                    phoneNumber = "0987654321",
                    address = "456 Oak Ave",
                    createdAt = Date(),
                    updatedAt = Date()
                )
            )
        )
        whenever(mockLoadUsersUseCase()).thenReturn(OperationResult.Success(userResponse))

        val states = mutableListOf<UiState<UserResponse>>()
        val job = launch {
            viewModel.usersState.collect { states.add(it) }
        }

        viewModel.loadUsers()
        advanceUntilIdle()

        val successState = states.filterIsInstance<UiState.Success<UserResponse>>().last()
        assertEquals(2, successState.data.data.size)
        assertEquals("John", successState.data.data[0].firstName)
        assertEquals("Jane", successState.data.data[1].firstName)
        job.cancel()
    }

    @Test
    fun `loadUsers should be idempotent`() = runTest {
        val userResponse = UserResponse(
            data = listOf(
                com.example.iurankomplek.data.api.models.UserResponse.DataItem(
                    id = 1,
                    firstName = "John",
                    lastName = "Doe",
                    email = "john@example.com",
                    phoneNumber = "1234567890",
                    address = "123 Main St",
                    createdAt = Date(),
                    updatedAt = Date()
                )
            )
        )
        whenever(mockLoadUsersUseCase()).thenReturn(OperationResult.Success(userResponse))

        viewModel.loadUsers()
        advanceUntilIdle()

        val firstSuccess = viewModel.usersState.value
        assertTrue("First load should be Success", firstSuccess is UiState.Success)

        viewModel.loadUsers()
        advanceUntilIdle()

        val secondSuccess = viewModel.usersState.value
        assertTrue("Second load should be Success", secondSuccess is UiState.Success)
        assertEquals("Both loads should return same data", 
            (firstSuccess as UiState.Success).data,
            (secondSuccess as UiState.Success).data
        )
        verify(mockLoadUsersUseCase, times(2))()
    }

    @Test
    fun `usersState should emit states in correct order`() = runTest {
        val userResponse = UserResponse(
            data = listOf(
                com.example.iurankomplek.data.api.models.UserResponse.DataItem(
                    id = 1,
                    firstName = "John",
                    lastName = "Doe",
                    email = "john@example.com",
                    phoneNumber = "1234567890",
                    address = "123 Main St",
                    createdAt = Date(),
                    updatedAt = Date()
                )
            )
        )
        whenever(mockLoadUsersUseCase()).thenReturn(OperationResult.Success(userResponse))

        val states = mutableListOf<UiState<UserResponse>>()
        val job = launch {
            viewModel.usersState.collect { states.add(it) }
        }

        viewModel.loadUsers()
        advanceUntilIdle()

        assertTrue("First state should be Loading", states[0] is UiState.Loading)
        assertTrue("Second state should be Success", states[1] is UiState.Success)
        assertEquals("Should have exactly 2 states", 2, states.size)
        job.cancel()
    }

    @Test
    fun `multiple load calls should not cause duplicate Loading states`() = runTest {
        val userResponse = UserResponse(
            data = listOf(
                com.example.iurankomplek.data.api.models.UserResponse.DataItem(
                    id = 1,
                    firstName = "John",
                    lastName = "Doe",
                    email = "john@example.com",
                    phoneNumber = "1234567890",
                    address = "123 Main St",
                    createdAt = Date(),
                    updatedAt = Date()
                )
            )
        )
        whenever(mockLoadUsersUseCase()).thenReturn(OperationResult.Success(userResponse))

        val states = mutableListOf<UiState<UserResponse>>()
        val job = launch {
            viewModel.usersState.collect { states.add(it) }
        }

        viewModel.loadUsers()
        viewModel.loadUsers()
        advanceUntilIdle()

        val loadingCount = states.count { it is UiState.Loading }
        assertEquals("Should have exactly 1 Loading state", 1, loadingCount)
        job.cancel()
    }

    @Test
    fun `Factory should create UserViewModel with correct dependencies`() {
        val loadUsersUseCase = LoadUsersUseCase(mock())
        val factory = UserViewModel.Factory(loadUsersUseCase)

        val viewModel = factory.create(UserViewModel::class.java)

        assertNotNull("ViewModel should be created", viewModel)
        assertTrue("ViewModel should be UserViewModel", viewModel is UserViewModel)
    }

    @Test
    fun `Factory should throw exception for unknown ViewModel class`() {
        val loadUsersUseCase = LoadUsersUseCase(mock())
        val factory = UserViewModel.Factory(loadUsersUseCase)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            factory.create(FinancialViewModel::class.java)
        }

        assertTrue("Exception message should contain unknown", exception.message?.contains("Unknown") == true)
    }
}
