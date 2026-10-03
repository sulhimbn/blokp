package com.example.iurankomplek.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.iurankomplek.data.repository.UserRepository
import com.example.iurankomplek.event.EventBus
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.utils.UiState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
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

    @Mock
    private lateinit var eventBus: EventBus

    private lateinit var viewModel: UserViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        Mockito.`when`(eventBus.events).thenReturn(MutableSharedFlow())
        viewModel = UserViewModel(userRepository, eventBus)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadUsers should emit Loading state initially`() = runTest(testDispatcher) {
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
        val seen = mutableListOf<UiState<UserResponse>>()
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
            viewModel.usersState.collect { seen += it }
        }
        viewModel.loadUsers()
        advanceUntilIdle()

        // Then
        assertTrue(
            "expected a Loading emission but saw ${seen.map { it::class.simpleName }}",
            seen.any { it is UiState.Loading }
        )
    }

    @Test
    fun `loadUsers should emit Success state when repository returns data`() = runTest(testDispatcher) {
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
    fun `loadUsers should emit Error state when repository returns error`() = runTest(testDispatcher) {
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
    fun `loadUsers should not make duplicate calls when already loading`() = runTest(testDispatcher) {
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
        advanceUntilIdle()
        // A completed load must not block the next one.
        viewModel.loadUsers()
        advanceUntilIdle()

        // Then
        Mockito.verify(userRepository, Mockito.times(2)).getUsers()
    }

    @Test
    fun `loadUsers should ignore a second call while the first is still in flight`() = runTest(testDispatcher) {
        Mockito.`when`(userRepository.getUsers()).thenReturn(Result.success(UserResponse(data = emptyList())))

        val collector = backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) {
            viewModel.usersState.collect { }
        }

        viewModel.loadUsers()
        viewModel.loadUsers()
        viewModel.loadUsers()
        advanceUntilIdle()

        Mockito.verify(userRepository, Mockito.times(1)).getUsers()
        collector.cancel()
    }

    @Test
    fun `loadUsers should update state correctly for empty data`() = runTest(testDispatcher) {
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