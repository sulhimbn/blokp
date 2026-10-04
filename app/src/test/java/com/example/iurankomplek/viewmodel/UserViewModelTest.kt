package com.example.iurankomplek.viewmodel

import com.example.iurankomplek.TestFixtures
import com.example.iurankomplek.data.repository.UserRepository
import com.example.iurankomplek.event.AppEvent
import com.example.iurankomplek.event.EventBus
import com.example.iurankomplek.model.User
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.utils.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class UserViewModelTest {

    private lateinit var repository: UserRepository
    private lateinit var eventBus: EventBus
    private lateinit var viewModel: UserViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mock(UserRepository::class.java)
        eventBus = EventBus()
        viewModel = UserViewModel(repository, eventBus)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `starts in Loading before any load is requested`() = runTest(testDispatcher) {
        assertTrue(viewModel.usersState.value is UiState.Loading)
    }

    @Test
    fun `the very first loadUsers call reaches the repository`() = runTest(testDispatcher) {
        val payload = UserResponse(listOf(TestFixtures.dataItem()))
        `when`(repository.getUsers()).thenReturn(Result.success(payload))

        viewModel.loadUsers()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(UiState.Success(payload), viewModel.usersState.value)
        verify(repository, times(1)).getUsers()
    }

    @Test
    fun `a second loadUsers call while the first is in flight does not duplicate the request`() =
        runTest(testDispatcher) {
            `when`(repository.getUsers()).thenReturn(Result.success(UserResponse(emptyList())))

            viewModel.loadUsers()
            viewModel.loadUsers()
            testDispatcher.scheduler.advanceUntilIdle()

            verify(repository, times(1)).getUsers()
        }

    @Test
    fun `a load requested after the previous one settled runs again`() = runTest(testDispatcher) {
        val payload = UserResponse(listOf(TestFixtures.dataItem()))
        `when`(repository.getUsers()).thenReturn(Result.success(payload))

        viewModel.loadUsers()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.loadUsers()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(repository, times(2)).getUsers()
    }

    @Test
    fun `surfaces the failure message from the repository`() = runTest(testDispatcher) {
        `when`(repository.getUsers()).thenReturn(Result.failure(java.io.IOException("No internet connection")))

        viewModel.loadUsers()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.usersState.value
        assertTrue(state is UiState.Error)
        assertEquals("No internet connection", (state as UiState.Error).error)
    }

    @Test
    fun `substitutes a fallback message when the failure carries none`() = runTest(testDispatcher) {
        `when`(repository.getUsers()).thenReturn(Result.failure(java.io.IOException()))

        viewModel.loadUsers()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Unknown error occurred", (viewModel.usersState.value as UiState.Error).error)
    }

    @Test
    fun `a RefreshAllData event triggers a reload`() = runTest(testDispatcher) {
        val payload = UserResponse(listOf(TestFixtures.dataItem()))
        `when`(repository.getUsers()).thenReturn(Result.success(payload))
        viewModel.loadUsers()
        testDispatcher.scheduler.advanceUntilIdle()

        eventBus.publishBlocking(AppEvent.RefreshAllData)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(repository, times(2)).getUsers()
    }

    @Test
    fun `an unrelated event does not trigger a reload`() = runTest(testDispatcher) {
        `when`(repository.getUsers()).thenReturn(Result.success(UserResponse(emptyList())))
        viewModel.loadUsers()
        testDispatcher.scheduler.advanceUntilIdle()

        eventBus.publishBlocking(AppEvent.PaymentCompleted("pay-1", java.math.BigDecimal.TEN))
        testDispatcher.scheduler.advanceUntilIdle()

        verify(repository, times(1)).getUsers()
    }
}
