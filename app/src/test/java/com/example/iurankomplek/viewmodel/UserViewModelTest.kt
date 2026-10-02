package com.example.iurankomplek.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner.Silent::class)
class UserViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var userRepository: UserRepository

    private lateinit var eventBus: EventBus
    private lateinit var viewModel: UserViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val sampleItem = DataItem(
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

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        eventBus = EventBus()
        viewModel = UserViewModel(userRepository, eventBus)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `state starts as Loading before any load is requested`() {
        assertTrue(viewModel.usersState.value is UiState.Loading)
    }

    @Test
    fun `loadUsers transitions to Success with the repository payload`() = runTest(testDispatcher) {
        val expected = UserResponse(listOf(sampleItem))
        whenever(userRepository.getUsers()).thenReturn(Result.success(expected))

        viewModel.loadUsers()
        advanceUntilIdle()

        val state = viewModel.usersState.value
        assertTrue(state is UiState.Success)
        assertEquals(expected, (state as UiState.Success).data)
    }

    @Test
    fun `loadUsers transitions to Error carrying the repository message`() = runTest(testDispatcher) {
        whenever(userRepository.getUsers())
            .thenReturn(Result.failure(IOException("Network error occurred")))

        viewModel.loadUsers()
        advanceUntilIdle()

        val state = viewModel.usersState.value
        assertTrue(state is UiState.Error)
        assertEquals("Network error occurred", (state as UiState.Error).error)
    }

    @Test
    fun `loadUsers falls back to a generic message when the failure carries none`() = runTest(testDispatcher) {
        whenever(userRepository.getUsers()).thenReturn(Result.failure(IOException()))

        viewModel.loadUsers()
        advanceUntilIdle()

        assertEquals("Unknown error occurred", (viewModel.usersState.value as UiState.Error).error)
    }

    @Test
    fun `a second loadUsers call is ignored while the first is still in flight`() = runTest(testDispatcher) {
        whenever(userRepository.getUsers())
            .thenReturn(Result.success(UserResponse(listOf(sampleItem))))

        viewModel.loadUsers()
        viewModel.loadUsers()
        advanceUntilIdle()

        verify(userRepository, times(1)).getUsers()
    }

    @Test
    fun `loadUsers can be retried once the previous load settled`() = runTest(testDispatcher) {
        whenever(userRepository.getUsers())
            .thenReturn(Result.success(UserResponse(listOf(sampleItem))))

        viewModel.loadUsers()
        advanceUntilIdle()
        viewModel.loadUsers()
        advanceUntilIdle()

        verify(userRepository, times(2)).getUsers()
    }

    @Test
    fun `an empty roster is still reported as Success, not Error`() = runTest(testDispatcher) {
        val empty = UserResponse(emptyList())
        whenever(userRepository.getUsers()).thenReturn(Result.success(empty))

        viewModel.loadUsers()
        advanceUntilIdle()

        val state = viewModel.usersState.value
        assertTrue(state is UiState.Success)
        assertTrue((state as UiState.Success).data.data.isEmpty())
    }
}
