package com.example.iurankomplek.viewmodel

import com.example.iurankomplek.data.repository.UserRepository
import com.example.iurankomplek.event.EventBus
import com.example.iurankomplek.model.DataItem
import com.example.iurankomplek.model.User
import com.example.iurankomplek.model.UserResponse
import com.example.iurankomplek.utils.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class UserViewModelTest {

    private class FakeUserRepository : UserRepository {
        var getUsersCalls = 0
        var result: Result<UserResponse> = Result.success(UserResponse(data = emptyList()))

        override suspend fun getUsers(): Result<UserResponse> {
            getUsersCalls++
            return result
        }

        override suspend fun login(email: String, password: String): Result<User> =
            Result.success(User(id = "1", email = email, firstName = "Test", lastName = "User"))

        override suspend fun logout(): Result<Unit> = Result.success(Unit)

        override fun getCurrentUserFlow(): Flow<User?> = flowOf(null)
    }

    private lateinit var repository: FakeUserRepository
    private lateinit var viewModel: UserViewModel

    private val testDispatcher = StandardTestDispatcher()

    private val sampleUser = DataItem(
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
        repository = FakeUserRepository()
        viewModel = UserViewModel(repository, EventBus())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadUsersEmitsLoadingWhileRequestIsInFlight() = runTest {
        repository.result = Result.success(UserResponse(data = listOf(sampleUser)))

        viewModel.loadUsers()

        assertTrue(viewModel.usersState.value is UiState.Loading)
    }

    @Test
    fun loadUsersEmitsSuccessWhenRepositoryReturnsData() = runTest {
        val response = UserResponse(data = listOf(sampleUser))
        repository.result = Result.success(response)

        viewModel.loadUsers()
        advanceUntilIdle()

        val state = viewModel.usersState.value
        assertTrue(state is UiState.Success)
        assertEquals(response, (state as UiState.Success).data)
    }

    @Test
    fun loadUsersEmitsErrorCarryingTheRepositoryMessage() = runTest {
        val errorMessage = "Network error occurred"
        repository.result = Result.failure(IOException(errorMessage))

        viewModel.loadUsers()
        advanceUntilIdle()

        val state = viewModel.usersState.value
        assertTrue(state is UiState.Error)
        assertEquals(errorMessage, (state as UiState.Error).error)
    }

    @Test
    fun loadUsersDoesNotIssueDuplicateConcurrentRequests() = runTest {
        repository.result = Result.success(UserResponse(data = listOf(sampleUser)))

        viewModel.loadUsers()
        viewModel.loadUsers()
        advanceUntilIdle()

        assertEquals(1, repository.getUsersCalls)
    }

    @Test
    fun loadUsersSurfacesAnEmptyPayloadAsSuccess() = runTest {
        val response = UserResponse(data = emptyList())
        repository.result = Result.success(response)

        viewModel.loadUsers()
        advanceUntilIdle()

        val state = viewModel.usersState.value
        assertTrue(state is UiState.Success)
        assertEquals(response, (state as UiState.Success).data)
        assertTrue((state as UiState.Success).data.data.isEmpty())
    }

    @Test
    fun loadUsersCanBeRetriedAfterAFailure() = runTest {
        val errorMessage = "Network error occurred"
        val response = UserResponse(data = listOf(sampleUser))
        repository.result = Result.failure(IOException(errorMessage))

        viewModel.loadUsers()
        advanceUntilIdle()
        assertTrue(viewModel.usersState.value is UiState.Error)

        repository.result = Result.success(response)
        viewModel.loadUsers()
        advanceUntilIdle()

        val state = viewModel.usersState.value
        assertTrue(state is UiState.Success)
        assertEquals(response, (state as UiState.Success).data)
        assertEquals(2, repository.getUsersCalls)
    }
}
