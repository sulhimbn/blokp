package com.example.iurankomplek.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.iurankomplek.data.repository.UserRepository
import com.example.iurankomplek.event.EventBus
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.utils.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
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

    @Mock
    private lateinit var userRepository: UserRepository


    private val eventBus = EventBus()

    private lateinit var viewModel: UserViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = UserViewModel(userRepository, eventBus)
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
        Mockito.`when`(userRepository.getUsers()).thenReturn(Result.success(mockResponse))

        // When
        viewModel.loadUsers()

        // Then
        val loadingState = viewModel.usersState.value
        assertTrue(loadingState is UiState.Loading)
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
        Mockito.`when`(userRepository.getUsers()).thenReturn(Result.success(mockResponse))

        // When
        viewModel.loadUsers()

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
        Mockito.`when`(userRepository.getUsers()).thenReturn(Result.failure(IOException(errorMessage)))

        // When
        viewModel.loadUsers()

        // Then
        advanceUntilIdle()
        val state = viewModel.usersState.value
        assertTrue(state is UiState.Error)
        assertEquals(errorMessage, (state as UiState.Error).error)
    }

    @Test
    fun `loadUsers should not make duplicate calls when already loading`() = runTest {
        val slowRepository = SlowUserRepository()
        val slowViewModel = UserViewModel(slowRepository, eventBus)

        slowViewModel.loadUsers()
        testDispatcher.scheduler.advanceTimeBy(100)
        slowViewModel.loadUsers()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, slowRepository.calls)

        slowViewModel.loadUsers()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, slowRepository.calls)
    }
    @Test
    fun `loadUsers should update state correctly for empty data`() = runTest {
        // Given
        val mockResponse = UserResponse(
            data = emptyList()
        )
        Mockito.`when`(userRepository.getUsers()).thenReturn(Result.success(mockResponse))

        // When
        viewModel.loadUsers()

        // Then
        advanceUntilIdle()
        val state = viewModel.usersState.value
        assertTrue(state is UiState.Success)
        assertEquals(mockResponse, (state as UiState.Success).data)
        assertTrue((state as UiState.Success).data.data?.isEmpty() == true)
    }
}
private class SlowUserRepository : UserRepository {
    var calls = 0
        private set

    override suspend fun getUsers(): Result<UserResponse> {
        calls++
        delay(1_000)
        return Result.success(UserResponse(data = emptyList()))
    }

    override suspend fun login(email: String, password: String): Result<com.example.iurankomplek.model.User> =
        Result.failure(UnsupportedOperationException())

    override suspend fun logout(): Result<Unit> = Result.failure(UnsupportedOperationException())

    override fun getCurrentUserFlow(): kotlinx.coroutines.flow.Flow<com.example.iurankomplek.model.User?> =
        kotlinx.coroutines.flow.flowOf(null)
}
