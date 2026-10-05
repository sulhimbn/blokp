package com.example.iurankomplek.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.iurankomplek.TestFixtures
import com.example.iurankomplek.data.repository.UserRepository
import com.example.iurankomplek.event.EventBus
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
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class UserViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var userRepository: UserRepository

    private lateinit var viewModel: UserViewModel
    private lateinit var eventBus: EventBus

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        MockitoAnnotations.openMocks(this)
        eventBus = EventBus()
        viewModel = UserViewModel(userRepository, eventBus)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() {
        assertTrue(viewModel.usersState.value is UiState.Loading)
    }

    @Test
    fun `the first load must not be swallowed by the duplicate-load guard`() = runTest {
        val response = TestFixtures.userResponse(TestFixtures.dataItem())
        Mockito.`when`(userRepository.getUsers()).thenReturn(Result.success(response))

        viewModel.loadUsers()
        advanceUntilIdle()

        Mockito.verify(userRepository, Mockito.times(1)).getUsers()
        assertTrue(viewModel.usersState.value is UiState.Success)
    }

    @Test
    fun `loadUsers should emit Success state when repository returns data`() = runTest {
        val response = TestFixtures.userResponse(TestFixtures.dataItem())
        Mockito.`when`(userRepository.getUsers()).thenReturn(Result.success(response))

        viewModel.loadUsers()
        advanceUntilIdle()

        val state = viewModel.usersState.value
        assertTrue(state is UiState.Success)
        assertEquals(response, (state as UiState.Success).data)
        assertEquals("john.doe@example.com", state.data.data[0].email)
    }

    @Test
    fun `loadUsers should emit Error state when repository fails`() = runTest {
        Mockito.`when`(userRepository.getUsers())
            .thenReturn(Result.failure(IOException("Network error occurred")))

        viewModel.loadUsers()
        advanceUntilIdle()

        val state = viewModel.usersState.value
        assertTrue(state is UiState.Error)
        assertEquals("Network error occurred", (state as UiState.Error).error)
    }

    @Test
    fun `loadUsers should surface a fallback message when the exception has none`() = runTest {
        Mockito.`when`(userRepository.getUsers()).thenReturn(Result.failure(IOException()))

        viewModel.loadUsers()
        advanceUntilIdle()

        val state = viewModel.usersState.value
        assertTrue(state is UiState.Error)
        assertEquals("Unknown error occurred", (state as UiState.Error).error)
    }

    @Test
    fun `loadUsers should succeed for an empty data set`() = runTest {
        val response = TestFixtures.userResponse()
        Mockito.`when`(userRepository.getUsers()).thenReturn(Result.success(response))

        viewModel.loadUsers()
        advanceUntilIdle()

        val state = viewModel.usersState.value
        assertTrue(state is UiState.Success)
        assertTrue((state as UiState.Success).data.data.isEmpty())
    }

    @Test
    fun `concurrent loadUsers calls hit the repository only once`() = runTest {
        Mockito.`when`(userRepository.getUsers())
            .thenReturn(Result.success(TestFixtures.userResponse(TestFixtures.dataItem())))

        viewModel.loadUsers()
        viewModel.loadUsers()
        viewModel.loadUsers()
        advanceUntilIdle()

        Mockito.verify(userRepository, Mockito.times(1)).getUsers()
    }

    @Test
    fun `a reload after a finished load reaches the repository again`() = runTest {
        Mockito.`when`(userRepository.getUsers())
            .thenReturn(Result.success(TestFixtures.userResponse(TestFixtures.dataItem())))

        viewModel.loadUsers()
        advanceUntilIdle()
        viewModel.loadUsers()
        advanceUntilIdle()

        Mockito.verify(userRepository, Mockito.times(2)).getUsers()
    }
}