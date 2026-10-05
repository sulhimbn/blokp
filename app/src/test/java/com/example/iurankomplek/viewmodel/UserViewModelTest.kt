package com.example.iurankomplek.viewmodel

import com.example.iurankomplek.data.repository.UserRepository
import com.example.iurankomplek.event.EventBus
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.utils.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class UserViewModelTest {

    private val userRepository = Mockito.mock(UserRepository::class.java)
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

    private fun sampleUser() = DataItem(
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

    @Test
    fun `initial state is Loading before any load`() {
        assertTrue(viewModel.usersState.value is UiState.Loading)
    }

    @Test
    fun `loadUsers should emit Success state when repository returns data`() = runTest(testDispatcher) {
        val mockResponse = UserResponse(data = listOf(sampleUser()))
        Mockito.`when`(userRepository.getUsers()).thenReturn(Result.success(mockResponse))

        viewModel.loadUsers()
        advanceUntilIdle()

        val state = viewModel.usersState.value
        assertTrue(state is UiState.Success)
        assertEquals(mockResponse, (state as UiState.Success).data)
    }

    @Test
    fun `loadUsers should emit Error state when repository returns error`() = runTest(testDispatcher) {
        Mockito.`when`(userRepository.getUsers())
            .thenReturn(Result.failure(IOException("Network error occurred")))

        viewModel.loadUsers()
        advanceUntilIdle()

        val state = viewModel.usersState.value
        assertTrue(state is UiState.Error)
        assertEquals("Network error occurred", (state as UiState.Error).error)
    }

    @Test
    fun `first loadUsers call is not skipped by the initial Loading state`() =
        runTest(testDispatcher) {
            val mockResponse = UserResponse(data = listOf(sampleUser()))
            Mockito.`when`(userRepository.getUsers()).thenReturn(Result.success(mockResponse))

            viewModel.loadUsers()
            advanceUntilIdle()

            Mockito.verify(userRepository, Mockito.times(1)).getUsers()
            assertEquals(mockResponse, (viewModel.usersState.value as UiState.Success).data)
        }

    @Test
    fun `loadUsers should not start a second request while one is in flight`() =
        runTest(testDispatcher) {
            Mockito.`when`(userRepository.getUsers())
                .thenReturn(Result.success(UserResponse(data = emptyList())))

            viewModel.loadUsers()
            viewModel.loadUsers()
            advanceUntilIdle()

            Mockito.verify(userRepository, Mockito.times(1)).getUsers()
        }

    @Test
    fun `loadUsers should update state correctly for empty data`() = runTest(testDispatcher) {
        val mockResponse = UserResponse(data = emptyList())
        Mockito.`when`(userRepository.getUsers()).thenReturn(Result.success(mockResponse))

        viewModel.loadUsers()
        advanceUntilIdle()

        val state = viewModel.usersState.value
        assertTrue(state is UiState.Success)
        assertTrue((state as UiState.Success).data.data.isEmpty())
    }

    @Test
    fun `loadUsers should fall back to a generic message when failure carries none`() =
        runTest(testDispatcher) {
            Mockito.`when`(userRepository.getUsers()).thenReturn(Result.failure(Exception()))

            viewModel.loadUsers()
            advanceUntilIdle()

            val state = viewModel.usersState.value
            assertTrue(state is UiState.Error)
            assertEquals("Unknown error occurred", (state as UiState.Error).error)
        }
}