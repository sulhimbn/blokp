package com.example.iurankomplek.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.iurankomplek.data.repository.UserRepository
import com.example.iurankomplek.event.EventBus
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.utils.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
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
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking
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

    private fun dataItem(email: String = "john.doe@example.com") = DataItem(
        first_name = "John",
        last_name = "Doe",
        email = email,
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
        `when`(eventBus.events).thenReturn(MutableSharedFlow())
        viewModel = UserViewModel(userRepository, eventBus)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadUsers should emit Loading state while the request is in flight`() = runTest {
        `when`(userRepository.getUsers())
            .thenReturn(Result.success(UserResponse(data = listOf(dataItem()))))

        viewModel.loadUsers()

        assertTrue(viewModel.usersState.value is UiState.Loading)
    }

    @Test
    fun `loadUsers should emit Success state when repository returns data`() = runTest {
        val response = UserResponse(data = listOf(dataItem()))
        `when`(userRepository.getUsers()).thenReturn(Result.success(response))

        viewModel.loadUsers()
        advanceUntilIdle()

        val state = viewModel.usersState.value
        assertTrue("expected Success but was $state", state is UiState.Success)
        assertEquals(response, (state as UiState.Success).data)
    }

    @Test
    fun `loadUsers should emit Error state when repository returns error`() = runTest {
        `when`(userRepository.getUsers())
            .thenReturn(Result.failure(IOException("Network error occurred")))

        viewModel.loadUsers()
        advanceUntilIdle()

        val state = viewModel.usersState.value
        assertTrue("expected Error but was $state", state is UiState.Error)
        assertEquals("Network error occurred", (state as UiState.Error).error)
    }

    @Test
    fun `loadUsers should not make duplicate calls while a load is in flight`() = runTest {
        `when`(userRepository.getUsers())
            .thenReturn(Result.success(UserResponse(data = listOf(dataItem()))))

        viewModel.loadUsers()
        viewModel.loadUsers()
        advanceUntilIdle()

        verifyBlocking(userRepository, times(1)) { getUsers() }
    }

    @Test
    fun `a later load runs again after the previous one finished`() = runTest {
        `when`(userRepository.getUsers())
            .thenReturn(Result.success(UserResponse(data = emptyList())))

        viewModel.loadUsers()
        advanceUntilIdle()
        viewModel.loadUsers()
        advanceUntilIdle()

        verifyBlocking(userRepository, times(2)) { getUsers() }
    }

    @Test
    fun `loadUsers should surface an empty result as Success`() = runTest {
        val response = UserResponse(data = emptyList())
        `when`(userRepository.getUsers()).thenReturn(Result.success(response))

        viewModel.loadUsers()
        advanceUntilIdle()

        val state = viewModel.usersState.value
        assertTrue(state is UiState.Success)
        assertTrue((state as UiState.Success).data.data.isEmpty())
    }
}