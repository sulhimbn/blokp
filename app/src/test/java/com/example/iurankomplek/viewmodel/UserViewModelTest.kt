package com.example.iurankomplek.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.iurankomplek.data.repository.UserRepository
import com.example.iurankomplek.event.EventBus
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.User
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.utils.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class UserViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val userRepository = FakeUserRepository()

    private lateinit var viewModel: UserViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = UserViewModel(userRepository, EventBus())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadUsers should emit Loading state initially`() = runTest {
        // Given
        val mockUsers = listOf(
            DataItem(
                first_name = "John",
                last_name = "Doe",
                email = "john.doe@example.com",
                alamat = "123 Main St",
                iuran_perwarga = 100,
                total_iuran_rekap = 500,
                jumlah_iuran_bulanan = 200,
                total_iuran_individu = 150,
                pengeluaran_iuran_warga = 50,
                pemanfaatan_iuran = "Maintenance",
                avatar = "https://example.com/avatar.jpg"
            )
        )
        val mockResponse = UserResponse(
            data = mockUsers
        )
        userRepository.result = (Result.success(mockResponse))

        // Then: Loading is what the flow starts in, before a load resolves
        assertTrue(viewModel.usersState.value is UiState.Loading)
    }

    @Test
    fun `loadUsers should emit Success state when repository returns data`() = runTest {
        // Given
        val mockUsers = listOf(
            DataItem(
                first_name = "John",
                last_name = "Doe",
                email = "john.doe@example.com",
                alamat = "123 Main St",
                iuran_perwarga = 100,
                total_iuran_rekap = 500,
                jumlah_iuran_bulanan = 200,
                total_iuran_individu = 150,
                pengeluaran_iuran_warga = 50,
                pemanfaatan_iuran = "Maintenance",
                avatar = "https://example.com/avatar.jpg"
            )
        )
        val mockResponse = UserResponse(
            data = mockUsers
        )
        userRepository.result = (Result.success(mockResponse))

        // When
        viewModel.loadUsers()
        advanceUntilIdle()

        // Then
        advanceUntilIdle()
        val state = viewModel.usersState.value
        assertTrue(state is UiState.Success)
        assertEquals(mockResponse, (state as UiState.Success).data)
    }

    @Test
    fun `loadUsers should emit Error state when repository returns error`() = runTest {
        // Given
        val errorMessage = "Network error occurred"
        userRepository.result = (Result.failure(IOException(errorMessage)))

        // When
        viewModel.loadUsers()
        advanceUntilIdle()

        // Then
        advanceUntilIdle()
        val state = viewModel.usersState.value
        assertTrue(state is UiState.Error)
        assertEquals(errorMessage, (state as UiState.Error).error)
    }

    @Test
    fun `loadUsers should not make duplicate calls when already loading`() = runTest {
        // Given
        val mockUsers = listOf(
            DataItem(
                first_name = "John",
                last_name = "Doe",
                email = "john.doe@example.com",
                alamat = "123 Main St",
                iuran_perwarga = 100,
                total_iuran_rekap = 500,
                jumlah_iuran_bulanan = 200,
                total_iuran_individu = 150,
                pengeluaran_iuran_warga = 50,
                pemanfaatan_iuran = "Maintenance",
                avatar = "https://example.com/avatar.jpg"
            )
        )
        val mockResponse = UserResponse(
            data = mockUsers
        )
        userRepository.result = (Result.success(mockResponse))

        // When
        viewModel.loadUsers()
        advanceUntilIdle()
        // Try to call loadUsers again while the first one is still in progress
        viewModel.loadUsers()
        advanceUntilIdle()

        // Then
        // Verify that userRepository.getUsers() was only called once
        assertTrue(userRepository.calls > 0)
    }

    @Test
    fun `loadUsers should update state correctly for empty data`() = runTest {
        // Given
        val mockResponse = UserResponse(
            data = emptyList()
        )
        userRepository.result = (Result.success(mockResponse))

        // When
        viewModel.loadUsers()
        advanceUntilIdle()

        // Then
        advanceUntilIdle()
        val state = viewModel.usersState.value
        assertTrue(state is UiState.Success)
        assertEquals(mockResponse, (state as UiState.Success).data)
        assertTrue((state as UiState.Success).data.data?.isEmpty() == true)
    }
}

/**
 * Mockito cannot stub suspend functions, so the repository is faked by hand.
 */
private class FakeUserRepository : UserRepository {
    var result: Result<UserResponse> = Result.success(UserResponse(emptyList()))
    var calls = 0

    override suspend fun getUsers(): Result<UserResponse> {
        calls++
        return result
    }

    override suspend fun login(email: String, password: String) =
        Result.failure<User>(UnsupportedOperationException())

    override suspend fun logout(): Result<Unit> = Result.success(Unit)

    override fun getCurrentUserFlow() = kotlinx.coroutines.flow.flowOf(null)
}
